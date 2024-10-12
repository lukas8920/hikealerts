# Microsoft Fabric and AI Hackathon: Hiking Alerts

- [Concept](resources/doc/concept.md)

# System Setup
![system_setup](https://github.com/user-attachments/assets/fe4e8f44-f797-4adc-b4f5-295da6b1bdaf)

# Use Case Description
**_Can be used as a prompt for spec generation_**

The purpose of the API is to provide information to hikers about dangers, weather impacts etc. (called events in the following)
that affect the accessibility of certain hiking paths.

An event is described by:
- geospatial coordinates
- optional: validity (from date, to date)
- optional: publisher
- optional: response limit
- license

The API enables official institutions to manage and publish events.
The API provides a search for published events. The results are intended to be used by third party applications like hiking apps.

Events can be searched by:
- geospatial coordinate
- radius
- country
- name of region
- name of hiking trail
- issuing organization
- issuer
