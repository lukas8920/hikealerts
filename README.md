# 1. Microsoft Fabric and AI Hackathon: Hiking Alerts

## 1.1. Use Case Description
When viewing and planning tracks, popular hiking apps do not display current information by official institutions for hiking tracks in a standard format. Instead, those apps heavily rely on their respective communities updating the information manually.

This relevant information for hikers from institutions might include dangers, weather impacts, closures etc (in the following called events).

Microsoft's AI and Fabric services enable to standardize the events issued by different institutions in a global context. The purpose of the website and the API is to provide transparency on the accessibility of hiking tracks, making hiking safer and a more enjoyable experience!

## 1.2. Video
[![Watch the video](https://img.youtube.com/vi/QYnjiuvA630/0.jpg)](https://www.youtube.com/watch?v=QYnjiuvA630)

## 1.3. Project Structure
### 1.3.1. System Setup
![Screenshot from 2024-11-23 12-57-28](https://github.com/user-attachments/assets/2bfca025-da49-47f6-90d1-b0023bc5b01f)

### 1.3.2. Content Overview

1) Fabric Raw Events Query<br>
   US Raw Events from US National Park Service:<br>
   https://github.com/lukas8920/hikealerts/blob/main/fabric/US%20Event%20Response%20Parser.Notebook/notebook-content.py<br>
   NZ Raw Events from NZ Department of Conservation:<br>
   https://github.com/lukas8920/hikealerts/blob/main/fabric/NZ%20Raw%20Event%20Parser.Notebook/notebook-content.py<br>
3) Fabric Geodata Trails<br>
   US Trail data:<br>
   https://github.com/lukas8920/hikealerts/blob/main/fabric/US%20Geodata%20Trail%20Parser.Notebook/notebook-content.py<br>
   NZ Trail data:<br>
   https://github.com/lukas8920/hikealerts/blob/main/fabric/NZ%20Geodata%20Trail%20Parser.Notebook/notebook-content.py<br>
5) Fabric Geodata Regions<br>
   US Region data:<br>
   https://github.com/lukas8920/hikealerts/blob/main/fabric/US%20Geodata%20Region%20Parser.Notebook/notebook-content.py<br>
   NZ Region data:<br> https://github.com/lukas8920/hikealerts/blob/main/fabric/NZ%20Geodata%20Region%20Parser.Notebook/notebook-content.py<br>
7) Azure SQL Stored Procedures<br>
   Fetch latest event changes / insertions & updates:<br>
   https://github.com/lukas8920/hikealerts/blob/main/database/procedures/fetch_raw_event_changes.sql<br>
9) Fabric Openai Notebook<br> 
   Queries latest event changes via Stored Procedures:<br>
   https://github.com/lukas8920/hikealerts/blob/main/fabric/CDC%20OpenAI%20Connector.Notebook/notebook-content.py<br>
11) Spring Boot Application<br>
   Event Listener for MS Event Queue:<br>
   https://github.com/lukas8920/hikealerts/blob/main/spring-boot/src/main/java/org/hikingdev/microsoft_hackathon/event_handling/EventListenerService.java<br>
   Logic for mapping NZ events to trail geodata:<br>
   https://github.com/lukas8920/hikealerts/blob/main/spring-boot/src/main/java/org/hikingdev/microsoft_hackathon/event_handling/event_injection/countries/NZInjector.java<br>
   Logic for mapping US events to trail geodata:<br>
   https://github.com/lukas8920/hikealerts/blob/main/spring-boot/src/main/java/org/hikingdev/microsoft_hackathon/event_handling/event_injection/countries/USInjector.java<br>

### 1.3.3. How to set up
1) Set up Azure SQL database and tables, procedures & triggers with T-SQL statements<br>
   https://github.com/lukas8920/hikealerts/tree/main/database
2) Set up MS Queues for deleted and created events
3) Create NZ DOC account and US NPS account to retrieve access credentials<br>
   https://api.doc.govt.nz/ / https://www.nps.gov/subjects/developer/api-documentation.htm
4) Update connections in notebooks<br> 
   https://github.com/lukas8920/hikealerts/tree/main/fabric
5) Run raw event notebook jobs<br> 
   https://github.com/lukas8920/hikealerts/tree/main/fabric/NZ%20Raw%20Event%20Parser.Notebook
6) Run fetch event changes notebook job<br> 
   https://github.com/lukas8920/hikealerts/tree/main/fabric/CDC%20OpenAI%20Connector.Notebook

Run on server:

7) Set connection properties in angular services<br> 
   https://github.com/lukas8920/hikealerts/tree/main/angular/src/app/_service/api.service.ts & https://github.com/lukas8920/hikealerts/tree/main/angular/src/app/_service/user.service.ts
8) Set connection properties in application / application-prod properties<br> 
   https://github.com/lukas8920/hikealerts/tree/main/spring-boot/src/main/resources
9) Build docker images for angular applications<br> 
   https://github.com/lukas8920/hikealerts/tree/main/spring-boot/Dockerfile & https://github.com/lukas8920/hikealerts/tree/main/angular/Dockerfile
10) Update host connection details in docker-compose file<br>
    https://github.com/lukas8920/hikealerts/tree/main/docker-compose.yml1
11) Run docker-compose file

## 1.4 Licenses
This project is licensed under the **MIT License**. See [https://github.com/lukas8920/hikealerts/blob/main/LICENSE](LICENSE) for details.

This project uses third-party libraries that have their own licenses. For a complete list of dependencies and their licenses, please refer to [https://github.com/lukas8920/hikealerts/blob/main/THIRD-PARTY-LICENSES](THIRD-PARTY-LICENSES).