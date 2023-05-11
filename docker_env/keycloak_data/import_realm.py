import requests
import os
import time
import json

KEYCLOAK_URL = os.environ.get("KEYCLOAK_URL", "http://localhost:8080")
KEYCLOAK_USERNAME = os.environ["KEYCLOAK_USERNAME"]
KEYCLOAK_PASSWORD = os.environ["KEYCLOAK_PASSWORD"]
REALM_FILE_PATH = '/app/realm-export.json'


def wait_for_keycloak(url, timeout=300, interval=5):
    start_time = time.time()
    while time.time() - start_time < timeout:
        try:
            response = requests.get(f"{url}/auth/realms/master/.well-known/openid-configuration")
            if response.status_code == 200:
                print("Keycloak is available.")
                return
        except requests.exceptions.RequestException:
            pass
        time.sleep(interval)

    raise Exception(f"Keycloak not available after {timeout} seconds")


def get_keycloak_token(username, password):
    url = f"{KEYCLOAK_URL}/auth/realms/master/protocol/openid-connect/token"
    data = {
        "grant_type": "password",
        "client_id": "admin-cli",
        "username": username,
        "password": password,
    }
    response = requests.post(url, data=data)
    response.raise_for_status()
    return response.json()["access_token"]


wait_for_keycloak(KEYCLOAK_URL)

access_token = get_keycloak_token(KEYCLOAK_USERNAME, KEYCLOAK_PASSWORD)

# Import realm
print('Importing realm...')
url = KEYCLOAK_URL + '/auth/admin/realms/master/'
headers = {
    'Authorization': 'Bearer ' + access_token,
    'Content-Type': 'application/json'
}
with open(REALM_FILE_PATH, 'r') as f:
    data = f.read()
response = requests.put(url, data=data, headers=headers)
if response.status_code != 201:
    print('Failed to import realm or realm already exists:', response.text)
    exit(1)
print('Realm imported successfully.')
