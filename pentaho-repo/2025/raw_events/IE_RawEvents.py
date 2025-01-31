import sys
import os

sys.path.append('/opt/pentaho/repo/2025/keyvault')

import http.client
import json
import uuid
from pyspark.sql import SparkSession
import pandas as pd
from azure.storage.queue import QueueClient
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

# Fetch the driver manager from your Spark context
driver_manager = spark._sc._gateway.jvm.java.sql.DriverManager

# Create a connection object using a JDBC URL, SQL username & password
con = driver_manager.getConnection(jdbc_url, sql_username, sql_password)

# Define the API endpoint
conn = http.client.HTTPSConnection("services-eu1.arcgis.com")

offset = 0
limit = 100

id_list = []
while True:
    # Make the API call
    conn.request("GET",
                 "/CltcWyRoZmdwaB7T/ArcGIS/rest/services/GetIrelandActiveTrailRoutes/FeatureServer/0/query?where=Activity='Walking'%20AND%20Notes<>''&outFields=*&returnGeometry=false&f=geojson&resultOffset=" + str(offset) + "&resultRecordCount=" + str(limit))

    # Get the response
    response = conn.getresponse()
    data = response.read()

    data = json.loads(data)

    # Extract the features from the ESRI JSON
    features = data.get('features', [])
    if len(features) > 0:
        # Create a list to hold the attributes
        attributes_list = []

        for feature in features:
            attributes = {}
            properties = feature.get('properties', {})
            attributes['unitname'] = properties['County']
            attributes['id'] = properties['OBJECTID']
            attributes['description'] = properties['Name'] + ". " + properties['Notes']
            attributes['url'] = properties['Website']
            attributes_list.append(attributes)

            # cache id for deletion
            id_list.append(attributes['id'])

        df = pd.DataFrame(attributes_list)

        df = spark.createDataFrame(df)

        # Iterate over each row in the DataFrame and execute the statement
        for row in df.collect():
            if not row.description is None:
                description = row.description.replace("'", r"''")
            if not row.unitname is None:
                unitname = row.unitname.replace("'", r"''")

            statement = f"EXEC dbo.InsertRawEvents '{row.id}', 'IE', {None}, {None}, '{description}', '{row.url}', 4, '{unitname}', {None}, {None}"

            # Create callable statement and execute it
            exec_statement = con.prepareCall(statement)
            exec_statement.execute()
            exec_statement.close()
    else:
        break
    offset = limit + offset

id_list = list(map(str, id_list))
# Convert to json
content = {
    "country": "IE",
    "ids": id_list
}
content = json.dumps(content)


# Post to deleted-events queue
connect_str = sc.get_secret('queue-connection-string').value
queue_client = QueueClient.from_connection_string(connect_str, "deleted-events")
queue_client.send_message(content)

con.close()

spark.stop()
