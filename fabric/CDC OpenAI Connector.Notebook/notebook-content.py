# Fabric notebook source

# METADATA ********************

# META {
# META   "kernel_info": {
# META     "name": "synapse_pyspark"
# META   },
# META   "dependencies": {
# META     "environment": {
# META       "environmentId": "3dd1081d-e4e7-407a-837b-c0c239236fcd",
# META       "workspaceId": "00000000-0000-0000-0000-000000000000"
# META     }
# META   }
# META }

# CELL ********************

import json
from pyspark.sql import SparkSession
import datetime
import http.client
from azure.storage.queue import QueueClient
import time


def create_list(resultset):
    # Example for extracting the data from ResultSet
    datetime_col_index = 4
    metadata = resultset.getMetaData()
    num_cols = metadata.getColumnCount()
    # List to store rows
    rows = []
    # Fetch rows from the result set
    while resultset.next():
        row = [
            resultset.getObject(i + 1)
            for i in range(num_cols)
            if (i + 1) != datetime_col_index
        ]

        # Retrieve the datetime column separately using getTimestamp()
        timestamp_value = resultset.getTimestamp(datetime_col_index)
        if timestamp_value:
            # Convert Java Timestamp to Python datetime
            datetime_value = timestamp_value.toInstant().toEpochMilli() / 1000.0
            datetime_python = datetime.datetime.fromtimestamp(datetime_value)

            # Append the datetime value to the row
            row.append(datetime_python)

        rows.append(row)
    return rows


def send_openai_request(message_dict):
    # Convert the dictionary to a JSON string
    json_string = json.dumps(message_dict)

    # Define headers
    headers = {
        "api-key": openai_api_key,
        "Content-Type": "application/json"
    }

    url = "/openai/deployments/gpt-4/chat/completions?api-version=2024-02-15-preview"

    conn.request("POST", url, headers=headers, body=json_string)

    # Get the response
    response = conn.getresponse()
    data = json.loads(response.read())
    return data


def create_title(description):
    message_dict = {
        "model": "gpt-4",
        "messages": [
            {
                "role": "system",
                "content": "Create a title summarizing the message with maximum 15 words. Return only the title with no other info."
            },
            {
                "role": "user",
                "content": description
            }
        ]
    }
    return send_openai_request(message_dict)


def standardize_event(json_string):
    message_dict = {
        "model": "gpt-4",
        # "model": "gpt-35-turbo",
        "messages": [
            {
                "role": "system",
                "content": "For each alert identify most likely matching trails with problems based on country, area and trail information provided with the alert. \n Do not output and ignore alerts where: \n - no trail name identification is possible and the problem does not seem to affect trails in the entire park or region. \n - no trail name, no park name or no region can be identified. \n - there seems to be no problem with trail conditions. \n For the remaining alerts, output the results as json list. Each json item consists of: Event id, country as two-letter code, park_name, region, trail_name, from_date and to_date \n Each item must have either a trail name and a park name and/or a region. Trail names might be in the alert description, but extract solely the trail name. The description should not be in the response. \n Each item might have a from and to date in format dd/mm/YYYY. If no year mentioned, enter a placeholder YYYY. \n One alert can have zero, one or multiple json items - each trail should go in a separate json item.  \n Replace any known abbreviations and correct known misspellings. \n Respond only with the data without any additional comments."
            },
            {
                "role": "user",
                "content": json_string
            }
        ]
    }
    return send_openai_request(message_dict)


def update_title(event_id, country, title):
    if not title is None:
        title = title.replace("'", r"''")

    statement = f"EXEC dbo.UpdateRawEventTitle '{event_id}', '{country}', '{title}'"

    # Create callable statement and execute it
    exec_statement = con.prepareCall(statement)
    exec_statement.execute()
    exec_statement.close()


jdbc_url = "jdbc:sqlserver://hiking-sql-server.database.windows.net:1433;database=hiking-sql-db;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"
sql_username = notebookutils.credentials.getSecret('https://lk-keyvault-93.vault.azure.net/', 'sql-server-username')
sql_password = notebookutils.credentials.getSecret('https://lk-keyvault-93.vault.azure.net/', 'sql-server-password')

# Fetch the driver manager from your Spark context
driver_manager = spark._sc._gateway.jvm.java.sql.DriverManager

# Create a connection object using a JDBC URL, SQL username & password
con = driver_manager.getConnection(jdbc_url, sql_username, sql_password)


# gpt-4 proxy
conn = http.client.HTTPSConnection("eventopenai.openai.azure.com")
openai_api_key = notebookutils.credentials.getSecret('https://lk-keyvault-93.vault.azure.net/', 'openai-api-key')


query = f"EXEC dbo.FetchRawEventChanges"

# Create callable statement and execute it
statement = con.createStatement()
resultset = statement.executeQuery(query)
resultlist = create_list(resultset)
statement.close()  # Close the statement after execution

notebook_failed = False
if len(resultlist) > 0:
    for i in range(0, len(resultlist), 10):
        # buffer requests to open ai due to quota limit
        time.sleep(20)

        sublist = resultlist[i:i + 10]

        # use openai to generate a title for the event
        try:
            for row in sublist:
                title = row[2]
                if title == "None" or title is None or len(title) < 2:
                    row[2] = create_title(row[4])['choices'][0]['message']['content']
                    print("Created title: " + row[2])
                    update_title(row[0], row[1], row[2])
                    time.sleep(20)
        except Exception as e:
            print(e)
            notebook_failed = True
            continue

        # Create the desired dictionary structure
        output_dict = {
            "country": sublist[0][1] if sublist else None,
            "alerts": [
                {
                    "event_id": item[0],
                    "title": item[2],
                    "parkCode": item[3],
                    "description": item[4]
                } for item in sublist
            ]
        }

        # Convert the dictionary to a JSON string
        json_string = json.dumps(output_dict)
        print(json_string)

        # Request standardized event via openai
        data = standardize_event(json_string)

        try:
            # Extract content
            content = data['choices'][0]['message']['content']

            # send via azure queue to spring boot backend
            print("Send to event queue")
            print(content)
            connect_str = notebookutils.credentials.getSecret('https://lk-keyvault-93.vault.azure.net/', 'queue-connection-string')
            queue_client = QueueClient.from_connection_string(connect_str, "openai-events")
            queue_client.send_message(content)
        except:
            print(data)
            notebook_failed = True

if notebook_failed:
    raise RuntimeError("Notebook execution failed due to one or more errors.")

# METADATA ********************

# META {
# META   "language": "python",
# META   "language_group": "synapse_pyspark"
# META }
