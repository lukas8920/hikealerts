#!/bin/bash

python3 /opt/pentaho/secrets.py

echo "created secret file"

SECRETS_FILE="/tmp/secret_file.env"

if [ -f "$SECRETS_FILE" ]; then
    set -a
    source "$SECRETS_FILE"
    set +a

    rm -f "$SECRETS_FILE"
    echo "Secrets processed"
else
    echo "Secrets file not found!"
    exit 1
fi

echo "starting carte server"

/opt/pentaho/data-integration/carte.sh /opt/pentaho/carte-config.xml
