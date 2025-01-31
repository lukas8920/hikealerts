import sys
import os

sys.path.append('/opt/pentaho/repo/2025/keyvault')

from pyspark.sql import SparkSession
import json
import http.client
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

# fetch total event count
conn = http.client.HTTPSConnection("developer.nps.gov")
us_nps_api_key = sc.get_secret('us-nps-api-key').value
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
    
spark.stop()
