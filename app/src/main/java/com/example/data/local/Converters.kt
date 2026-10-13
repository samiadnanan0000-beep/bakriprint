package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.RemittanceCompany
import com.example.data.model.TaskStage

class Converters {
    @TypeConverter
    fun fromOrderStatus(status: OrderStatus?): String? = status?.name

    @TypeConverter
    fun toOrderStatus(name: String?): OrderStatus? =
        name?.let { runCatching { OrderStatus.valueOf(it) }.getOrDefault(OrderStatus.PENDING) }

    @TypeConverter
    fun fromTaskStage(stage: TaskStage?): String? = stage?.name

    @TypeConverter
    fun toTaskStage(name: String?): TaskStage? =
        name?.let { runCatching { TaskStage.valueOf(it) }.getOrDefault(TaskStage.DESIGN) }

    @TypeConverter
    fun fromPaymentMethod(method: PaymentMethod?): String? = method?.name

    @TypeConverter
    fun toPaymentMethod(name: String?): PaymentMethod? =
        name?.let { runCatching { PaymentMethod.valueOf(it) }.getOrDefault(PaymentMethod.CASH) }

    @TypeConverter
    fun fromRemittanceCompany(company: RemittanceCompany?): String? = company?.name

    @TypeConverter
    fun toRemittanceCompany(name: String?): RemittanceCompany? =
        name?.let { runCatching { RemittanceCompany.valueOf(it) }.getOrNull() }
}
