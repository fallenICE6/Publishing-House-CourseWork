package com.example.publishingapp.data.models

enum class OrderStatus(val title: String) {
    ALL("Все"),
    CREATED("Создан"),
    UNDER_REVIEW("На проверке"),
    EDITING("Редактируется"),
    READY_FOR_PRINT("Готов к печати"),
    COMPLETED("Завершён"),
    CANCELED("Отменён")
}