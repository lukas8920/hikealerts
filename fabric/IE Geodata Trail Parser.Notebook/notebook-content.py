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
import uuid

import pandas as pd
from shapely.geometry import LineString


jdbc_url = "jdbc:sqlserver://hiking-sql-server.database.windows.net:1433;database=hiking-sql-db;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"
sql_username = notebookutils.credentials.getSecret('https://lk-keyvault-93.vault.azure.net/', 'sql-server-username')
sql_password = notebookutils.credentials.getSecret('https://lk-keyvault-93.vault.azure.net/', 'sql-server-password')

# Fetch the driver manager from your Spark context
driver_manager = spark._sc._gateway.jvm.java.sql.DriverManager

# Create a connection object using a JDBC URL, SQL username & password
con = driver_manager.getConnection(jdbc_url, sql_username, sql_password)

# Define the API endpoint
conn = http.client.HTTPSConnection("services-eu1.arcgis.com")

offset = 0
limit = 100

while True:
    # Make the API call
    conn.request("GET", "/CltcWyRoZmdwaB7T/ArcGIS/rest/services/GetIrelandActiveTrailRoutes/FeatureServer/0/query?where=Activity='Walking'&outFields=*&f=geojson&resultOffset=" + str(offset) + "&resultRecordCount=" + str(limit))  # Adjust the path as needed

    # Get the response
    response = conn.getresponse()
    data = response.read()

    data = json.loads(data)

    # Extract the features from the ESRI JSON
    features = data.get('features', [])

    if(len(features) > 0):
        # Create a list to hold the attributes
        attributes_list = []

        for feature in features:
            geometry = feature.get('geometry', {})
            for p in geometry['coordinates']:
                if p and len(p) > 0 and isinstance(p[0], list):
                    attributes = {}
                    attributes['linestring'] = LineString(p).wkt
                    properties = feature.get('properties', {})
                    attributes['name'] = properties['Name']
                    attributes['maintainer'] = properties['ManagementOrganisation']
                    attributes['unitname'] = properties['County']
                    attributes['id'] = str(uuid.uuid4())

                    attributes_list.append(attributes)

        if len(attributes_list) > 0:
            df = pd.DataFrame(attributes_list)

            df = spark.createDataFrame(df)

            # Iterate over each row in the DataFrame and execute the statement
            for row in df.collect():  # collect() brings all rows to the driver
                if not row.name is None:
                    name = row.name.replace("'", r"''")
                if not row.unitname is None:
                    unitname = row.unitname.replace("'", r"''")
                if not row.maintainer is None:
                    maintainer = row.maintainer.replace("'", r"''")

                try:
                    statement = f"EXEC dbo.InsertGeodataTrails '{row.id}', 'IE', '{name}', '{name}', {None}, '{unitname}', '{None}', '{maintainer}', '{row.linestring}'"

                    # Create callable statement and execute it
                    exec_statement = con.prepareCall(statement)
                    exec_statement.execute()
                    exec_statement.close()  # Close the statement after execution
                except Exception as e:
                    print("Error while parsing row")
    else:
        # if no features break out of loop - no more trails
        break    

    offset = limit + offset

# Close the connection
con.close()

# METADATA ********************

# META {
# META   "language": "python",
# META   "language_group": "synapse_pyspark"
# META }
