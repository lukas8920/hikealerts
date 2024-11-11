# 1) Microsoft Fabric and AI Hackathon: Hiking Alerts

## 1.1) Use Case Description
When viewing and planning tracks, popular hiking apps do not display current information by official institutions for hiking tracks in a standard format. Instead, those apps heavily rely on their respective communities updating the information manually.

This relevant information for hikers from institutions might include dangers, weather impacts, closures etc (in the following called events).

Microsoft's AI and Fabric services enable to standardize the events issued by different institutions in a global context. The purpose of the website and the API is to provide transparency on the accessibility of hiking tracks, making hiking safer and a more enjoyable experience!

## 1.2) Video


## 1.3) Project Structure
### 1.3.1) System Setup
![system_setup_v1](https://github.com/user-attachments/assets/19f04a08-e16e-459c-b6bb-ca6f2edfe8b6)

### 1.3.2) Content Overview

### 1.3.3) How to set up
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
