import sys
import os

sys.path.append('/opt/pentaho/repo/2025/keyvault')

import SecretClient as sc

scp = sc.get_secret_client()
secrets = {
    "SQL_SERVER_USER": scp.get_secret('sql-server-username').value,
    "SQL_SERVER_PW": scp.get_secret('sql-server-password').value,
    "PENTAHO_SERVER_USER": scp.get_secret('pentaho-server-user').value,
    "PENTAHO_SERVER_PW": scp.get_secret('pentaho-server-password').value
}

with open("/tmp/secret_file.env", "w") as secret_file:
    for secret_name, secret_value in secrets.items():
        safe_value = str(secret_value).replace('`', '\\`')
        safe_value = str(safe_value).replace('<', '\\<')
        secret_file.write(f"{secret_name}={safe_value}\n")