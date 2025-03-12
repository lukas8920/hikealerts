#Geotrek configuration

1) Add SRID=3857 in conf/env.in
2) Change to # Data projection
   SRID = int(os.getenv('SRID', '3857'))  # Lambert-93 for Metropolitan France in geotrek/settings/base.py
3) Create external network
   docker network create geotrek-network