package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nameAr: String,
    val nameEn: String,
    val pricePerSqmYer: Double,
    val category: String, // e.g., "Banner", "Sticker", "Board", "Rigid"
    val isDefault: Boolean = false
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientName: String,
    val clientPhone: String,
    val serviceType: String, // Material or Service description
    val widthM: Double,
    val heightM: Double,
    val quantity: Int,
    val totalAreaSqm: Double,
    val isDesignReady: Boolean, // true = client provided artwork, false = workshop created design
    val designFeeYer: Double,
    val unitPriceYer: Double,
    val subtotalYer: Double,
    val discountYer: Double,
    val paidDepositYer: Double,
    val remainingBalanceYer: Double,
    val status: OrderStatus = OrderStatus.PENDING,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val deadlineAt: Long? = null
)

@Entity(tableName = "vendors")
data class VendorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val location: String,
    val totalJobsCostYer: Double = 0.0,
    val totalPaidYer: Double = 0.0,
    val remainingDebtYer: Double = 0.0,
    val notes: String = ""
)

@Entity(tableName = "vouchers")
data class VoucherEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vendorId: Long,
    val amountYer: Double,
    val method: PaymentMethod,
    val remittanceCompany: RemittanceCompany? = null,
    val remittanceNumber: String = "",
    val receiptImagePath: String? = null, // Path in app internal storage
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long? = null,
    val title: String,
    val clientName: String = "",
    val stage: TaskStage = TaskStage.DESIGN,
    val deadlineEpochMs: Long? = null,
    val isCompleted: Boolean = false,
    val priority: Int = 1 // 1: normal, 2: urgent
)

@Entity(tableName = "client_sms_messages")
data class ClientSmsMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderPhone: String,
    val messageBody: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "vendor_jobs")
data class VendorJobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vendorId: Long,
    val jobTitle: String,
    val material: String,
    val dimensions: String,
    val costYer: Double,
    val executionNotes: String = "",
    val imagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

