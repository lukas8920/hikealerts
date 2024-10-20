# Microsoft Fabric and AI Hackathon: Hiking Alerts

- [Concept](resources/doc/concept.md)

# System Setup
![system_setup_v1](https://github.com/user-attachments/assets/19f04a08-e16e-459c-b6bb-ca6f2edfe8b6)

# Use Case Description
**_Can be used as a prompt for spec generation_**

The purpose of the API is to provide information to hikers about dangers, weather impacts etc. (called mapEvents in the following)
that affect the accessibility of certain hiking paths.

An openAiEvent is described by:
- geospatial coordinates
- optional: validity (from date, to date)
- optional: publisher
- optional: response limit
- license

The API enables official institutions to manage and publish mapEvents.
The API provides a search for published mapEvents. The results are intended to be used by third party applications like hiking apps.

Events can be searched by:
- geospatial coordinate
- radius
- country
- name of region
- name of hiking trail
- issuing organization
- issuer
