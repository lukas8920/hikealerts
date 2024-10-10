import http.client
import json
from pyspark.sql import SparkSession

jdbc_url = "jdbc:sqlserver://lk-sql-server.database.windows.net:1433;database=prod-testapp-nz-sql;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;"
sql_username = <<user>>
sql_password = <<password>>

# fetch total event count
conn = http.client.HTTPSConnection("developer.nps.gov")
# Define headers
headers = {
    "X-Api-Key": "s4uV0AQhlglLURd58hRuaCogxzyc2Rd0SQRv2ayk"
}

conn.request("GET", "/api/v1/alerts?limit=1&start=0", headers=headers)

# Get the response
response = conn.getresponse()
data = response.read()

#load total count
totalCount = json.loads(data).get("total")

offset = 0
#todo replace 2 by 200
batchSize = 2
id_list = []
#todo remove totalCount
totalCount = 1

# Fetch the driver manager from your Spark context
driver_manager = spark._sc._gateway.jvm.java.sql.DriverManager

# Create a connection object using a JDBC URL, SQL username & password
con = driver_manager.getConnection(jdbc_url, sql_username, sql_password)

# insert/update events
while True:
    # fetch 200 datasets
    url = "/api/v1/alerts?limit=" + str(batchSize) + "&start=" + str(offset)
    conn.request("GET", url, headers=headers)

    # Get the response
    response = conn.getresponse()
    data = response.read()

    # generate list of ids
    data = json.loads(data)

    # Iterate through each JSON object and add the 'id' property to the list
    data = data.get("data")
    for item in data:
        if 'id' in item:
            id_list.append(item['id'])

            title = item['title'].replace("'", r"''")
            description = item['description'].replace("'", r"''")
            statement = f"EXEC dbo.InsertRawEvents '{item['id']}', '{title}', '{item['parkCode']}', '{item['description']}', '{item['url']}'"

            # Create callable statement and execute it
            exec_statement = con.prepareCall(statement)
            exec_statement.execute()
            exec_statement.close()  # Close the statement after execution

    # increment offset
    offset = offset + batchSize
    if offset > int(totalCount):
        break

# Convert the list to a string with values separated by commas
id_string = ', '.join(f"'{id}'" for id in id_list)

# remove entries not in id list
statement = f"DELETE FROM dbo.us_raw_events WHERE id not in ({id_string})"
exec_statement = con.prepareCall(statement)
exec_statement.execute()
exec_statement.close()
# todo: delete also from final event list

# Close the connection
con.close()
