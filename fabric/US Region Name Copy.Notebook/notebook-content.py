# Fabric notebook source

# METADATA ********************

# META {
# META   "kernel_info": {
# META     "name": "synapse_pyspark"
# META   },
# META   "dependencies": {}
# META }

# CELL ********************

import json
import http.client

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

conn.request("GET", "/api/v1/parks?limit=500", headers=headers)

# Fetch the driver manager from your Spark context
driver_manager = spark._sc._gateway.jvm.java.sql.DriverManager

# Create a connection object using a JDBC URL, SQL username & password
con = driver_manager.getConnection(jdbc_url, sql_username, sql_password)

# Get the response
response = conn.getresponse()
data = response.read()

json_data = json.loads(data)

# Extract the park name and park code
parks = json_data.get("data", [])
for park in parks:
    park_name = park.get("fullName").replace("'", r"''")
    park_code = park.get("parkCode")

    statement = f"EXEC dbo.InsertRegionName '{park_code}', '{park_name}'"

    # Create callable statement and execute it
    exec_statement = con.prepareCall(statement)
    exec_statement.execute()
    exec_statement.close()  # Close the statement after execution

# METADATA ********************

# META {
# META   "language": "python",
# META   "language_group": "synapse_pyspark"
# META }
