# Publishing-House-CourseWork
## Мобильное приложение "Издательство"
## 📖 О проекте

**Мобильное приложение "Издательство"** — это курсовая работа, представляющая собой полнофункциональное решение для автоматизации взаимодействия между авторами и издательским сервисом.

## 🏗 Архитектура и Технологии

```mermaid
graph TB
    subgraph "Клиентская часть (Android)"
        A[Мобильное приложение<br/>Kotlin + Jetpack Compose]
        style A fill:#7F52FF,color:white
    end
    
    subgraph "Серверная часть (Backend)"
        B[Spring Boot Application]
        C[(PostgreSQL<br/>Database)]
        style B fill:#6DB33F,color:white
        style C fill:#4169E1,color:white
    end
    
    subgraph "Внешние сервисы"
        D[FCM Firebase Cloud<br/>Messaging]
        style D fill:#FFA000,color:white
    end
    
    A <-->|REST API / WebSocket| B
    B <-->|JDBC| C
