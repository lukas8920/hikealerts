from azure.identity import ClientSecretCredential
from azure.keyvault.secrets import SecretClient
import http.client
import json


def query_param(conn, query_string):
  conn.request("GET", f"/get_password?entry={query_string}")
  response = conn.getresponse()
  data = json.loads(response.read())
  return data['password']

def get_secret_client():
  conn = http.client.HTTPConnection("hiking-alerts_gpg_agent_1:5000")

  tenant_id = query_param(conn, "keyvault/tenantid")
  client_id = query_param(conn, "keyvault/clientid")
  client_secret = query_param(conn, "keyvault/password")

  key_vault_url = "https://lk-keyvault-93.vault.azure.net"

  # Authenticate using client secret
  credential = ClientSecretCredential(tenant_id, client_id, client_secret)

  # Create a SecretClient instance
  secret_client = SecretClient(vault_url=key_vault_url, credential=credential) 
  return secret_client
