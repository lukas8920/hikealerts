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

import http.client
import json
from pyspark.sql import SparkSession
from azure.storage.queue import QueueClient

jdbc_url = "jdbc:sqlserver://hiking-sql-server.database.windows.net:1433;database=hiking-sql-db;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"
sql_username = notebookutils.credentials.getSecret('https://lk-keyvault-93.vault.azure.net/', 'sql-server-username')
sql_password = notebookutils.credentials.getSecret('https://lk-keyvault-93.vault.azure.net/', 'sql-server-password')

# fetch total event count
conn = http.client.HTTPSConnection("developer.nps.gov")
us_nps_api_key = notebookutils.credentials.getSecret('https://lk-keyvault-93.vault.azure.net/', 'us-nps-api-key')
# Define headers
headers = {
    "X-Api-Key": us_nps_api_key
}

conn.request("GET", "/api/v1/alerts?limit=1&start=0", headers=headers)

# Get the response
response = conn.getresponse()
data = response.read()

#load total count
totalCount = json.loads(data).get("total")

offset = 0
batchSize = 200
id_list = []

# Fetch the driver manager from your Spark context
driver_manager = spark._sc._gateway.jvm.java.sql.DriverManager

# Create a connection object using a JDBC URL, SQL username & password
con = driver_manager.getConnection(jdbc_url, sql_username, sql_password)

# insert/update events
while True:
    # fetch 200 datasets
    url = "/api/v1/alerts?limit=" + str(batchSize) + "&start=" + str(offset)
    conn.request("GET", url, headers=headers)

    # Get the response
    response = conn.getresponse()
    data = response.read()

    # generate list of ids
    data = json.loads(data)

    # Iterate through each JSON object and add the 'id' property to the list
    data = data.get("data")
    for item in data:
        if 'id' in item:
            id_list.append(item['id'])

            title = item['title'].replace("'", r"''")
            description = item['description'].replace("'", r"''")
            statement = f"EXEC dbo.InsertRawEvents '{item['id']}', 'US', '{title}', '{item['parkCode']}', '{description}', '{item['url']}', 1, {None}, {None}, {None}"

            # Create callable statement and execute it
            exec_statement = con.prepareCall(statement)
            exec_statement.execute()
            exec_statement.close()  # Close the statement after execution

    # increment offset
    offset = offset + batchSize
    if offset > int(totalCount):
        break

id_list = list(map(str, id_list))
# Convert to json
content = {
    "country": "US",
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
