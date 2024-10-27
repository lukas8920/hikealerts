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
import pandas as pd
import json
from shapely.geometry import LineString

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
url = "/v1/tracks"
# todo set header
conn.request("GET", url, headers=headers)

# Get the response
response = conn.getresponse()
data = response.read()

data = json.loads(data)

attributes_list = []
for item in data:
    row = [
            item["assetId"],
            item["name"],
            item["region"][0] if item["region"] else None,
            (lambda x: LineString(x[0]).wkt if len(x) > 0 else LineString(x).wkt)(item["line"])
        ]
    attributes_list.append(row)

# Create a DataFrame from the list of attributes
df = pd.DataFrame(attributes_list)
df = df.rename(columns= {0: "id", 1: "trailname", 2: "unitname", 3: "coordinates"})
df = spark.createDataFrame(df)

# Fetch the driver manager from your Spark context
driver_manager = spark._sc._gateway.jvm.java.sql.DriverManager

# Create a connection object using a JDBC URL, SQL username & password
con = driver_manager.getConnection(jdbc_url, sql_username, sql_password)

for row in df.collect():
    if not row.trailname is None:
        trailname = row.trailname.replace("'", r"''")
    if not row.unitname is None:
        unitname = row.unitname.replace("'", r"''")
    try:
        statement = f"EXEC dbo.InsertGeodataTrails '{row.id}', 'NZ', '{trailname}', {None}, {None}, '{unitname}', '{None}', 'Department of Conservation', '{row.coordinates}'"
        
        # Create callable statement and execute it
        exec_statement = con.prepareCall(statement)
        exec_statement.execute()
        exec_statement.close()  # Close the statement after execution
    except Exception as e:
        print(row.id)
        print(row.trailname)
        print(row.unitname)
        print("Error while parsing row")
        print(e)

con.close()

# METADATA ********************

# META {
# META   "language": "python",
# META   "language_group": "synapse_pyspark"
# META }
