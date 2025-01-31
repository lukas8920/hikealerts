import sys
import os

sys.path.append('/opt/pentaho/repo/2025/keyvault')

import http.client
import json
from pyspark.sql import SparkSession
from shapely.geometry import shape
from shapely.geometry import MultiPolygon
from shapely import wkt
import pandas as pd
import SecretClient as scp

spark = SparkSession.builder \
    .appName("MyApp") \
    .config("spark.driver.extraClassPath", "/opt/pentaho/repo/2025/mssql-jdbc-12.8.1.jre11.jar") \
    .getOrCreate()
spark.sparkContext.setLogLevel("ERROR")
sc = scp.get_secret_client()

jdbc_url = "jdbc:sqlserver://hiking-sql-server.database.windows.net:1433;database=hiking-sql-db;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"
sql_username = sc.get_secret('sql-server-username').value
sql_password = sc.get_secret('sql-server-password').value

# Define the API endpoint
conn = http.client.HTTPSConnection("services1.arcgis.com")

# Make the API call
url = "/3JjYDyG3oajxU6HO/arcgis/rest/services/DOC_Public_Conservation_Land/FeatureServer/0/query?outFields=*&where=1%3D1&f=geojson"
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
    attributes = {}
    attributes['OBJECTID'] = feature['properties']['OBJECTID']
    attributes['Name'] = feature['properties']['Name']

    # Add geometry as a separate column if needed
    attributes['wkt'] = shape(feature['geometry']).wkt

    attributes_list.append(attributes)

# Create a DataFrame from the list of attributes
df = pd.DataFrame(attributes_list)

# Display the DataFrame
filtered_df = df[['OBJECTID', 'Name', 'wkt']]
filtered_df = filtered_df.rename(columns={'OBJECTID': 'id', 'Name': 'unitname', 'wkt': 'coordinates'})

# Create an empty list to collect new rows
expanded_rows = []
# Iterate through each row in the DataFrame
for idx, row in filtered_df.iterrows():
    geom = row['coordinates']
    geom = wkt.loads(geom)
    counter = 1  # Start counter for each row

    # Check if the geometry is a MultiPolygon
    if isinstance(geom, MultiPolygon):
        # For each Polygon in the MultiPolygon, create a new row
        for poly in geom.geoms:  # Access individual polygons
            new_row = row.copy()
            new_row['coordinates'] = poly.wkt
            new_row['polygon_id'] = f"{row['id']}_{counter}"
            expanded_rows.append(new_row)
            counter += 1
    else:
        # If the geometry is not a MultiPolygon, add the row as is
        row['polygon_id'] = f"{row['id']}_1"
        expanded_rows.append(row)

# Convert the list of expanded rows back to a DataFrame
expanded_df = pd.DataFrame(expanded_rows)
df = spark.createDataFrame(expanded_df)

# Fetch the driver manager from your Spark context
driver_manager = spark._sc._gateway.jvm.java.sql.DriverManager

# Create a connection object using a JDBC URL, SQL username & password
con = driver_manager.getConnection(jdbc_url, sql_username, sql_password)

id_list = []
for row in df.collect():
    id_list.append(row.polygon_id)
    if not row.unitname is None:
        unitname = row.unitname.replace("'", r"''")
    statement = f"EXEC dbo.InsertGeodataRegions '{row.polygon_id}', 'NZ', '{None}', '{unitname}', '{row.coordinates}'"
        
    # Create callable statement and execute it
    exec_statement = con.prepareCall(statement)
    exec_statement.execute()
    exec_statement.close()  # Close the statement after execution

# remove not existent ids
# Convert the list to a string with values separated by commas
id_string = ', '.join(f"'{id}'" for id in id_list)

# remove entries not in id list
if len(id_string) != 0:
    statement = f"DELETE FROM dbo.geodata_regions WHERE region_id not in ({id_string}) and country = 'NZ'"
    exec_statement = con.prepareCall(statement)
    exec_statement.execute()
    exec_statement.close()
    
spark.close()
