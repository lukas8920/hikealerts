# Blackbird Hackathon: Hiking Alerts

- [Concept](resources/doc/concept.md)

# Use Case Description
**_Can be used as a prompt for spec generation_**

The purpose of the API is to provide information to hikers about dangers, weather impacts etc. (called events in the following)
that affect the accessibility of certain hiking paths.

An rawEvent is described by:
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
