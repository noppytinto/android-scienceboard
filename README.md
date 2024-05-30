# NoppysBoard - Simple News Reader

STATUS: ~~published~~ no more available

## Description
Welcome to NoppysBoard!
- Customized User Experience: Implemented personalized home feeds allowing users to tailor their news consumption.
- Unique "Time Travel" Mode: Enabled users to revisit old news articles, providing a comprehensive historical perspective.
- Enhanced Readability: Added bookmark functionality for users to save articles for later reading, and a history feature to track previously visited articles.
- In-App Research Tools: Developed in-app search capabilities for users to perform web research without leaving the app, including:
    - Similar Articles Search: Quickly find related news content.
    - Keyword-Based Search: Efficiently locate information using keywords.
    - General Web Search: Perform comprehensive searches within the app.
- User-Centric Design: Included a dark mode for improved readability and reduced eye strain.


//--------------------------
Supported languages:
- English

## Stack & Tools
- Android (Java)
- Rxjava
- Firebase
- server for fetching news (Java) ([code](https://github.com/noppytinto/java-spring-scienceboardserver))
- NLP to search articles by keywords (server side)
- Axure for mocking


```mermaid
sequenceDiagram
    participant App
    participant Server
    participant Firebase 
    Server->>+Server: cron job: Fetch RSS (every hour)
    Server->>+Server: Parse RSS into "Articles"
    Server->>+Server: Extract keywords using NLP
    Server->>+Firebase: save articles



    App->>+Firebase: Fetch articles
    Firebase-->>+App: return articles
```


## Screenshots

<img width="383" alt="image" src="https://github.com/noppytinto/android-scienceboard/assets/34626569/3f6a507f-c173-409f-bae6-7926168f7858">

<img width="376" alt="image" src="https://github.com/noppytinto/android-scienceboard/assets/34626569/29dbdcbc-f04c-4cef-aded-85ddc757bdb6">

<img width="373" alt="image" src="https://github.com/noppytinto/android-scienceboard/assets/34626569/413c27cd-213d-42d4-a9be-af700221d2a6">

<img width="424" alt="image" src="https://github.com/noppytinto/android-scienceboard/assets/34626569/7a291405-2876-48a6-80ef-75f2052191aa">

<img width="398" alt="image" src="https://github.com/noppytinto/android-scienceboard/assets/34626569/88ccc1ab-7973-468b-9c30-db18b2ed62b1">

<img width="387" alt="image" src="https://github.com/noppytinto/android-scienceboard/assets/34626569/5712560e-3b97-49ff-adb6-1708550fa8f1">



