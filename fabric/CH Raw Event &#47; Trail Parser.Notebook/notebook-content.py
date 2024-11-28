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
import pandas as pd
from shapely.geometry import LineString
from azure.storage.queue import QueueClient
from pyproj import Transformer


def transform_line(line):
    transformed_coords = [transformer.transform(x, y) for x, y in line.coords]
    return LineString(transformed_coords)


def to_8_digits(number):
    num_str = str(number).replace('.', '')
    return num_str[:8].ljust(8, '0')


jdbc_url = "jdbc:sqlserver://hiking-sql-server.database.windows.net:1433;database=hiking-sql-db;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"
sql_username = notebookutils.credentials.getSecret('https://lk-keyvault-93.vault.azure.net/', 'sql-server-username')
sql_password = notebookutils.credentials.getSecret('https://lk-keyvault-93.vault.azure.net/', 'sql-server-password')

# Fetch the driver manager from your Spark context
driver_manager = spark._sc._gateway.jvm.java.sql.DriverManager

# Create a connection object using a JDBC URL, SQL username & password
con = driver_manager.getConnection(jdbc_url, sql_username, sql_password)

# Define the API endpoint
conn = http.client.HTTPSConnection("api3.geo.admin.ch")

# Initialize the transformer from LV03 (EPSG:21781) to WGS84 (EPSG:4326)
transformer = Transformer.from_crs("EPSG:21781", "EPSG:4326", always_xy=True)

offset = 0
limit = 50
id_list = []

while True:
    print(offset)
    conn.request("GET", "/rest/services/api/MapServer/identify?geometry=%7B%22rings%22%3A%5B%5B%5B420000%2C70000%5D%2C%5B840000%2C70000%5D%2C%5B840000%2C300000%5D%2C%5B420000%2C300000%5D%2C%5B420000%2C70000%5D%5D%5D%7D&geometryType=esriGeometryPolygon&imageDisplay=500%2C600%2C96&mapExtent=420000%2C70000%2C860000%2C305000&tolerance=5&layers=all:ch.astra.wanderland-sperrungen_umleitungen&lang=en&offset=" + str(offset))

    # Get the response
    response = conn.getresponse()
    data = response.read()

    data = json.loads(data)
    results = data.get('results', [])
    results = results[0:limit]

    if len(results) > 0:
        trail_attributes = []
        event_attributes = []

        for result in results:
            event_attribute = {}

            properties = result.get('attributes', {})

            val1 = properties['abstract_en']
            val2 = properties['reason_en']
            val3 = properties['duration_en']
            val4 = properties['sperrungen_type_en']

            if val4 in ["Closure", "Closure and diversion"]:
                # Not relevant for us, therefore ignore in processing
                event_attribute['description'] = ""
                event_attribute['description'] += f"{val4}. " if val4 else ""
                event_attribute['description'] += f"{val1}. " if val1 else ""
                event_attribute['description'] += f"{val2}. " if val2 else ""
                event_attribute['description'] += f"{val3}." if val3 else ""
                event_attribute['url'] = properties['url1_link_en']

                paths = result['geometry']['paths']
                for p in paths:
                    transformed_linestring = transform_line(LineString(p))

                    event_attribute['id'] = to_8_digits(transformed_linestring.coords[0][0]) + "-" + to_8_digits(transformed_linestring.coords[0][1])
                    id_list.append(event_attribute['id'])

                    trail_attribute = {'id': event_attribute['id'], 'name': properties['title_en'],
                                'maintainer': properties['content_provider_en'], 'linestring': transformed_linestring.wkt}

                    trail_attributes.append(trail_attribute)
                    event_attributes.append(event_attribute)

        df_trails = pd.DataFrame(trail_attributes)
        df_trails = spark.createDataFrame(df_trails)


        # Iterate over each trail in the DataFrame and execute the statement
        for row in df_trails.collect():
            if row.name is not None:
                name = row.name.replace("'", r"''")

            statement = f"EXEC dbo.InsertGeodataTrails '{row.id}', 'CH', '{name}', '{name}', '{None}', '{None}', '{None}', '{row.maintainer}', '{row.linestring}'"

            # Create callable statement and execute it
            exec_statement = con.prepareCall(statement)
            exec_statement.execute()
            exec_statement.close()

        df_events = pd.DataFrame(event_attributes)
        df_events = spark.createDataFrame(df_events)

        # Iterate over each event in the DataFrame and execute the statement
        for row in df_events.collect():
            if row.description is not None:
                description = row.description.replace("'", r"''")

            statement = f"EXEC dbo.InsertRawEvents '{row.id}', 'CH', {None}, {None}, '{description}', '{row.url}', 5, {None}, {None}, {None}"

            # Create callable statement and execute it
            exec_statement = con.prepareCall(statement)
            exec_statement.execute()
            exec_statement.close()
    else:
        break
    offset = offset + limit

id_list = list(map(str, id_list))
print("length of id list: " + str(len(id_list)))
# Convert to json
content = {
    "country": "CH",
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
