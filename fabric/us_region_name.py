import json
import http.client

jdbc_url = "jdbc:sqlserver://lk-sql-server.database.windows.net:1433;database=prod-testapp-nz-sql;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"
sql_username = <<user>>
sql_password = <<password>>

# fetch total event count
conn = http.client.HTTPSConnection("developer.nps.gov")
# Define headers
headers = {
    "X-Api-Key": <<api-key>>
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
