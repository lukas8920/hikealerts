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
#todo replace 2 by 200
batchSize = 50
id_list = []
#todo remove totalCount
totalCount = 1

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
            statement = f"EXEC dbo.InsertRawEvents '{item['id']}', 'US', '{title}', '{item['parkCode']}', '{description}', '{item['url']}', 1"

            # Create callable statement and execute it
            exec_statement = con.prepareCall(statement)
            exec_statement.execute()
            exec_statement.close()  # Close the statement after execution

    # increment offset
    offset = offset + batchSize
    if offset > int(totalCount):
        break

# Convert the list to a string with values separated by commas
id_string = ', '.join(f"'{id}'" for id in id_list)

# remove entries not in id list
statement = f"DELETE FROM dbo.raw_events WHERE event_id not in ({id_string}) AND country = 'US'"
exec_statement = con.prepareCall(statement)
exec_statement.execute()
exec_statement.close()
# delete also from final event list
statement = f"DELETE FROM dbo.events WHERE event_id not in ({id_string}) AND country = 'US'"
exec_statement = con.prepareCall(statement)
exec_statement.execute()
exec_statement.close()

# Close the connection
con.close()

# METADATA ********************

# META {
# META   "language": "python",
# META   "language_group": "synapse_pyspark"
# META }
