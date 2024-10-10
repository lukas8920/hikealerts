import http.client
import json
from pyspark.sql import SparkSession
from pyspark.sql.functions import lit
from shapely import Polygon


def create_list(resultset):
    # Example for extracting the data from ResultSet
    metadata = resultset.getMetaData()
    num_cols = metadata.getColumnCount()
    # List to store rows
    rows = []
    # Fetch rows from the result set
    while resultset.next():
        row = [resultset.getObject(i + 1) for i in range(num_cols)]
        rows.append(row[0])
    return rows


jdbc_url = "jdbc:sqlserver://lk-sql-server.database.windows.net:1433;database=prod-testapp-nz-sql;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"
sql_username = <<user>>
sql_password = <<password>>

# Fetch the driver manager from your Spark context
driver_manager = spark._sc._gateway.jvm.java.sql.DriverManager

# Create a connection object using a JDBC URL, SQL username & password
con = driver_manager.getConnection(jdbc_url, sql_username, sql_password)

# get region codes
query = "select unitcode from dbo.us_geodata_trails where len(unitcode) = 4 group by unitcode"
statement = con.createStatement()
resultset = statement.executeQuery(query)
resultlist = create_list(resultset)

# nps connection details
conn = http.client.HTTPSConnection("developer.nps.gov")
# Define headers
headers = {
    "X-Api-Key": <<api-key>>
}

id_list = []

for unitcode in resultlist:
    # fetch region data
    url = "/api/v1/mapdata/parkboundaries/" + unitcode
    conn.request("GET", url, headers=headers)

    # Get multipolygon from the response
    response = conn.getresponse()
    data = json.loads(response.read())
    if data.get("status") == 404:
        continue

    features = data.get("features")
    for feature in features:
        try:
            geometry = feature.get('geometry', {})
            polygons = [Polygon(p[0]).wkt for p in geometry['coordinates']]
        except:
            continue

        # Create a DataFrame from the list
        df = spark.createDataFrame([(polygon,) for polygon in polygons], ['boundaries'])

        # assign unitcode and name
        df = df.withColumn("unitcode", lit(unitcode))
        df = df.withColumn("name", lit(None))

    # Iterate over each row in the DataFrame and execute the statement
    counter = 1
    for row in df.collect():  # collect() brings all rows to the driver
        # assign and increment id
        id = unitcode + "_" + str(counter)
        id_list.append(id)
        df = df.withColumn("id", lit(id))
        counter = counter + 1

        statement = f"EXEC dbo.InsertGeodataRegions '{id}', '{row.unitcode}', '{row.name}', '{row.boundaries}'"

        # Create callable statement and execute it
        exec_statement = con.prepareCall(statement)
        exec_statement.execute()
        exec_statement.close()  # Close the statement after execution

# Convert the list to a string with values separated by commas
id_string = ', '.join(f"'{id}'" for id in id_list)

# remove entries not in id list
if len(id_string) != 0:
    statement = f"DELETE FROM dbo.us_geodata_regions WHERE id not in ({id_string})"
    exec_statement = con.prepareCall(statement)
    exec_statement.execute()
    exec_statement.close()
