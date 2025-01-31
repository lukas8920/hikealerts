FROM openjdk:11-jre-slim

ENV PDI_HOME=/opt/pentaho/data-integration \
    CARTE_CONFIG=/opt/pentaho/carte-config.xml \
    JAVA_OPTS="-Duser.timezone=Pacific/Auckland"

RUN mkdir -p /opt/pentaho

COPY Pentaho /opt/pentaho/data-integration
COPY carte-config.xml $CARTE_CONFIG
COPY fix-arch.sh /opt/pentaho/fix-arch.sh
COPY secrets.py /opt/pentaho/secrets.py
COPY start-pentaho.sh /opt/pentaho/start-pentaho.sh

RUN chmod +x /opt/pentaho/fix-arch.sh && /opt/pentaho/fix-arch.sh
RUN chmod +x /opt/pentaho/start-pentaho.sh

RUN apt-get update && apt-get install -y python3 python3-pip && rm -rf /var/lib/apt/lists/*
RUN apt-get update && apt-get install procps -y
RUN apt-get update && apt-get install -y curl

RUN pip3 install pyspark
RUN pip3 install azure-storage-queue
RUN pip3 install azure-identity
RUN pip3 install azure-keyvault-secrets
RUN pip3 install pyproj
RUN pip3 install shapely
RUN pip3 install pandas

WORKDIR $PDI_HOME