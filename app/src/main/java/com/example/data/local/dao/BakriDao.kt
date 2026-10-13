package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ClientSmsMessage
import com.example.data.model.MaterialEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.TaskEntity
import com.example.data.model.TaskStage
import com.example.data.model.VendorEntity
import com.example.data.model.VoucherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BakriDao {

    // Materials
    @Query("SELECT * FROM materials ORDER BY isDefault DESC, nameAr ASC")
    fun getAllMaterials(): Flow<List<MaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: MaterialEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMaterials(materials: List<MaterialEntity>)

    @Update
    suspend fun updateMaterial(material: MaterialEntity)

    @Delete
    suspend fun deleteMaterial(material: MaterialEntity)

    // Orders
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Long): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE id = :id")
    suspend fun updateOrderStatus(id: Long, status: OrderStatus)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    // Vendors
    @Query("SELECT * FROM vendors ORDER BY name ASC")
    fun getAllVendors(): Flow<List<VendorEntity>>

    @Query("SELECT * FROM vendors WHERE id = :id LIMIT 1")
    suspend fun getVendorById(id: Long): VendorEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendor(vendor: VendorEntity): Long

    @Update
    suspend fun updateVendor(vendor: VendorEntity)

    @Delete
    suspend fun deleteVendor(vendor: VendorEntity)

    // Vouchers
    @Query("SELECT * FROM vouchers ORDER BY createdAt DESC")
    fun getAllVouchers(): Flow<List<VoucherEntity>>

    @Query("SELECT * FROM vouchers WHERE vendorId = :vendorId ORDER BY createdAt DESC")
    fun getVouchersForVendor(vendorId: Long): Flow<List<VoucherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: VoucherEntity): Long

    @Delete
    suspend fun deleteVoucher(voucher: VoucherEntity)

    @Query("DELETE FROM vouchers WHERE vendorId = :vendorId")
    suspend fun deleteVouchersByVendorId(vendorId: Long)

    // Vendor Jobs (سجل الأعمال المنفذة لكل مطبعة)
    @Query("SELECT * FROM vendor_jobs WHERE vendorId = :vendorId ORDER BY createdAt DESC")
    fun getJobsForVendor(vendorId: Long): Flow<List<com.example.data.model.VendorJobEntity>>

    @Query("SELECT * FROM vendor_jobs ORDER BY createdAt DESC")
    fun getAllVendorJobs(): Flow<List<com.example.data.model.VendorJobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendorJob(job: com.example.data.model.VendorJobEntity): Long

    @Delete
    suspend fun deleteVendorJob(job: com.example.data.model.VendorJobEntity)

    @Query("DELETE FROM vendor_jobs WHERE vendorId = :vendorId")
    suspend fun deleteJobsByVendorId(vendorId: Long)

    // Tasks (Kanban)
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority DESC, deadlineEpochMs ASC, id DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET stage = :stage WHERE id = :id")
    suspend fun updateTaskStage(id: Long, stage: TaskStage)

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :id")
    suspend fun updateTaskCompleted(id: Long, completed: Boolean)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    // SMS Messages
    @Query("SELECT * FROM client_sms_messages ORDER BY timestamp DESC")
    fun getAllSmsMessages(): Flow<List<ClientSmsMessage>>

    @Query("SELECT * FROM client_sms_messages WHERE senderPhone LIKE '%' || :phoneSuffix || '%' ORDER BY timestamp DESC")
    fun getSmsForPhone(phoneSuffix: String): Flow<List<ClientSmsMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmsMessage(sms: ClientSmsMessage): Long
}
