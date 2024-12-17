# 1. Microsoft Fabric and AI Hackathon: Hiking Alerts

## 1.1. Use Case Description
When viewing and planning tracks, popular hiking apps do not display current information by official institutions for hiking tracks in a standard format. Instead, those apps heavily rely on their respective communities updating the information manually.

This relevant information for hikers from institutions might include dangers, weather impacts, closures etc (in the following called events).

Microsoft's AI and Fabric services enable to standardize the events issued by different institutions in a global context. The purpose of the website and the API is to provide transparency on the accessibility of hiking tracks, making hiking safer and a more enjoyable experience!

## 1.2. Project Structure
### 1.2.1. System Setup
![Screenshot from 2024-11-23 12-57-28](https://github.com/user-attachments/assets/2bfca025-da49-47f6-90d1-b0023bc5b01f)

### 1.2.2. Deployment

Microsoft Setup:
1. Set up Azure Services (Blob Storage with queues, Openai Service, Keyvault, MS SQL Server, RSignal)
2. Configure Fabric data gateways to access Azure services
3. Schedule Fabric notebooks in directory
4. Configure Keyvault keys used in the Fabric notebooks & specified in [KeyvaultProdProvider.java](https://github.com/lukas8920/hikealerts/blob/main/spring-boot/src/main/java/org/hikingdev/microsoft_hackathon/security/keyvault/KeyVaultProdProvider.java)

Angular / Spring Boot deployment:
6. Configure environment variables in [application.properties](https://github.com/lukas8920/hikealerts/blob/main/spring-boot/src/main/resources/application.properties) and [application-prod.properties](https://github.com/lukas8920/hikealerts/blob/main/spring-boot/src/main/resources/application-prod.properties)
7. Adjust & build docker containers [Spring Boot Dockerfile](https://github.com/lukas8920/hikealerts/tree/main/spring-boot), [Angular Dockerfile](https://github.com/lukas8920/hikealerts/tree/main/angular) & [GPG Dockerfile](https://github.com/lukas8920/hikealerts/tree/main/gpg)
8. Move config files to locations as specified in docker-compose.yaml
9. Spin up docker-compose

## 1.3 Contributions
### 1.3.1 Data Providers
Anybody with an account can contribute events via the chat interface on [hiking.alerts.org](hiking.alerts.org) and via the API.
By default a user is part of the 'Community' group and events show up with the 'Community' tag. Please reach out via info.hikingalerts@gmail.com to tag events with an organisation.

### 1.3.2 Improvements
1) Create an issue in [Jira](https://hiking-alerts.atlassian.net/jira/software/projects/CCS/boards/1)
2) If you would like to contribute code, please reference the relevant Jira case in the pull request

Unit Tests helps to ensure that pull requests for bugs and improvements are processed quickly.

## 1.4 Licenses
This project is licensed under the **MIT License**. See [LICENSE](https://github.com/lukas8920/hikealerts/blob/main/LICENSE) for details.

This project uses third-party libraries that have their own licenses. For a complete list of dependencies and their licenses, please refer to [THIRD-PARTY-LICENSES](https://github.com/lukas8920/hikealerts/blob/main/THIRD-PARTY-LICENSES).
