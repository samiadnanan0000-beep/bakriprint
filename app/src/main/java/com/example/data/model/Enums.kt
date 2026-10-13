package com.example.data.model

enum class OrderStatus(val labelAr: String, val labelEn: String) {
    PENDING("قيد الانتظار", "Pending"),
    IN_PRINTING("قيد الطباعة", "In Printing"),
    READY("جاهز للاستلام", "Ready for Pickup"),
    COMPLETED("تم التسليم", "Completed")
}

enum class TaskStage(val labelAr: String, val labelEn: String) {
    DESIGN("تصميم", "Design"),
    PRINTING("طباعة", "Printing"),
    FINISHING("تجهيز وقص", "Finishing"),
    DELIVERY("تسليم", "Delivery")
}

enum class PaymentMethod(val labelAr: String) {
    CASH("نقداً"),
    REMITTANCE("حوالة مالية")
}

enum class RemittanceCompany(val labelAr: String) {
    AL_KURAIMI("الكريمي"),
    AL_NAJM("النجم"),
    AL_EMTYAZ("الامتياز"),
    OTHER("أخرى")
}

enum class CurrencyMode(val symbolAr: String, val symbolEn: String) {
    YER("ر.ي", "YER"),
    USD("$", "USD")
}
