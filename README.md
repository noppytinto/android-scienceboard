# NoppysBoard - Simple News Reader

STATUS: ~~published~~ no more available

## Description
Welcome to NoppysBoard!
- customize your home feed
- go back in time to see old news (time travel mode)
- save articles in bookmarks to read them later
- maybe you were reading an interesting article, but didn't have enough time, use the history to track previous visited articles
- when you're reading an article, you can do some research on the web without leaving the app:
- search similar articles on the web
- quick keywords based search
- or just search everything you want
- dark mode


//--------------------------
Supported languages:
- English

## Stack
- Android (Java)
- Rxjava
- Firebase
- server for fetching news (Java) ([code](https://github.com/noppytinto/java-spring-scienceboardserver))
- NLP to search articles by keywords (server side)


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



