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

jdbc_url = "jdbc:sqlserver://hiking-sql-server.database.windows.net:1433;database=hiking-sql-db;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"
sql_username = notebookutils.credentials.getSecret('https://lk-keyvault-93.vault.azure.net/', 'sql-server-username')
sql_password = notebookutils.credentials.getSecret('https://lk-keyvault-93.vault.azure.net/', 'sql-server-password')

# Define the API endpoint
conn = http.client.HTTPSConnection("mapservices.nps.gov")

# Make the API call
url = "/arcgis/rest/services/NationalDatasets/NPS_Public_Trails/FeatureServer/0/query?where=1%3D1&outFields=*&outSR=4326&f=json&resultOffset=" + str(offset) + "&resultRecordCount=" + str(batchSize);
conn.request("GET", url)  # Adjust the path as needed

# Get the response
response = conn.getresponse()
data = response.read()

data = json.loads(data)

# Extract the features from the ESRI JSON
features = data.get('features', [])

# Create a list to hold the attributes
attributes_list = []

for feature in features:
    # Extract attributes and geometry
    attributes = feature.get('attributes', {})
    geometry = feature.get('geometry', {})

    # Add geometry as a separate column if needed
    attributes['geometry'] = geometry  # Add geometry to attributes if needed

    attributes_list.append(attributes)

# Create a DataFrame from the list of attributes
df = pd.DataFrame(attributes_list)

df['geometry'] = df['geometry'].apply(lambda x: x['paths'])
df['wkt'] = df['geometry'].apply(lambda x: LineString(x[0]).wkt if len(x) > 0 else LineString(x).wkt)

# Display the DataFrame
filtered_df = df[['OBJECTID', 'TRLNAME', 'MAPLABEL', 'UNITCODE', 'UNITNAME', 'REGIONCODE', 'MAINTAINER', 'wkt']]
filtered_df = filtered_df.rename(columns={'OBJECTID': 'id', 'TRLNAME': 'trailname', 'MAPLABEL': 'maplabel', 'UNITCODE': 'unitcode', 'UNITNAME': 'unitname', 'REGIONCODE': 'regioncode', 'MAINTAINER': 'maintainer', 'wkt': 'coordinates'})

df = spark.createDataFrame(filtered_df)

# Fetch the driver manager from your Spark context
driver_manager = spark._sc._gateway.jvm.java.sql.DriverManager

# Create a connection object using a JDBC URL, SQL username & password
con = driver_manager.getConnection(jdbc_url, sql_username, sql_password)

# Iterate over each row in the DataFrame and execute the statement
for row in df.collect():  # collect() brings all rows to the driver
    try:
        statement = f"EXEC dbo.InsertGeodataTrails {row.id}, 'US', '{row.trailname}', '{row.maplabel}', '{row.unitcode}', '{row.unitname}', '{row.regioncode}', '{row.maintainer}', '{row.coordinates}'"

        # Create callable statement and execute it
        exec_statement = con.prepareCall(statement)
        exec_statement.execute()
        exec_statement.close()  # Close the statement after execution
    except Exception as e:
        print("Error while parsing row")
    

# Close the connection
con.close()


# METADATA ********************

# META {
# META   "language": "python",
# META   "language_group": "synapse_pyspark"
# META }
