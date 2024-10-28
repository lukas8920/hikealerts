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
import pandas as pd
from datetime import datetime
import http.client
from azure.storage.queue import QueueClient


def convert_timedate(date_str):
    # Parse the date string into a datetime object
    dt = datetime.strptime(date_str, "%a, %d %b %Y %H:%M:%S %Z")
    formatted_datetime = dt.strftime('%Y-%m-%d %H:%M:%S')
    return formatted_datetime


# Welcome to your new notebook
# Type here in the cell editor to add code!

jdbc_url = "jdbc:sqlserver://hiking-sql-server.database.windows.net:1433;database=hiking-sql-db;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"
sql_username = notebookutils.credentials.getSecret('https://lk-keyvault-93.vault.azure.net/', 'sql-server-username')
sql_password = notebookutils.credentials.getSecret('https://lk-keyvault-93.vault.azure.net/', 'sql-server-password')

# Define the API endpoint
conn = http.client.HTTPSConnection("api.doc.govt.nz")

# Define headers
nz_doc_api_key = notebookutils.credentials.getSecret('https://lk-keyvault-93.vault.azure.net/', 'nz-doc-api-key')
headers = {
    "x-api-key": nz_doc_api_key,
    "accept": "application/json"
}

# Make the API call
url = "/v2/alerts"
# todo set header
conn.request("GET", url, headers=headers)

# Get the response
response = conn.getresponse()
raw_json = response.read()

data = json.loads(raw_json)

id_list = []
attributes_list = []
for item in data:
    id_list.append(item['id'])
    row = [
            item["id"],
            item["summary"],
            item["description"],
            item["startDate"],
            item["endDate"],
            item["regions"][0]["name"] if item["regions"] else None
        ]
    attributes_list.append(row)

# Create a DataFrame from the list of attributes
df = pd.DataFrame(attributes_list)
df = df.rename(columns= {0: "id", 1: "title", 2: "description", 3: "start_date", 4: "end_date", 5: "region"})

df['start_date'] = df['start_date'].apply(lambda x: convert_timedate(x) if not x == "" else None)
df['end_date'] = df['end_date'].map(lambda x: convert_timedate(x) if not x == "" else None)

df = spark.createDataFrame(df)

# Fetch the driver manager from your Spark context
driver_manager = spark._sc._gateway.jvm.java.sql.DriverManager

# Create a connection object using a JDBC URL, SQL username & password
con = driver_manager.getConnection(jdbc_url, sql_username, sql_password)

for row in df.collect():
    if not row.title is None:
        title = row.title.replace("'", r"''")
    if not row.description is None:
        description = row.description.replace("'", r"''")
    if not row.region is None:
        region = row.region.replace("'", r"''")

    statement = f"EXEC dbo.InsertRawEvents '{row.id}', 'NZ', '{title}', {None}, '{description}', {None}, 2, '{region}', '{row.start_date}', '{row.end_date}'"

    # Create callable statement and execute it
    exec_statement = con.prepareCall(statement)
    exec_statement.execute()
    exec_statement.close()

id_list = list(map(str, id_list))
# Convert to json
content = {
    "country": "NZ",
    "ids": id_list
}
content = json.dumps(content)


# Post to deleted-events queue
connect_str = notebookutils.credentials.getSecret('https://lk-keyvault-93.vault.azure.net/', 'queue-connection-string')
queue_client = QueueClient.from_connection_string(connect_str, "deleted-events")
queue_client.send_message(content)

con.close()

# METADATA ********************

# META {
# META   "language": "python",
# META   "language_group": "synapse_pyspark"
# META }
