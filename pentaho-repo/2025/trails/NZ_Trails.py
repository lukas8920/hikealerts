import sys
import os

sys.path.append('/opt/pentaho/repo/2025/keyvault')

import http.client
import pandas as pd
import json
from pyspark.sql import SparkSession
from shapely.geometry import LineString
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
conn = http.client.HTTPSConnection("api.doc.govt.nz")

# Define headers
nz_doc_api_key = sc.get_secret('nz-doc-api-key').value
headers = {
    "x-api-key": nz_doc_api_key,
    "accept": "application/json"
}

# Make the API call
url = "/v1/tracks?coordinates=wgs84"
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

spark.close()
