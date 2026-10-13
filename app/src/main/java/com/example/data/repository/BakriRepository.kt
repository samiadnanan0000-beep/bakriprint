package com.example.data.repository

import com.example.data.local.dao.BakriDao
import com.example.data.model.ClientSmsMessage
import com.example.data.model.MaterialEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.TaskEntity
import com.example.data.model.TaskStage
import com.example.data.model.VendorEntity
import com.example.data.model.VoucherEntity
import kotlinx.coroutines.flow.Flow

class BakriRepository(private val dao: BakriDao) {

    // Materials
    val allMaterials: Flow<List<MaterialEntity>> = dao.getAllMaterials()
    suspend fun insertMaterial(material: MaterialEntity): Long = dao.insertMaterial(material)
    suspend fun updateMaterial(material: MaterialEntity) = dao.updateMaterial(material)
    suspend fun deleteMaterial(material: MaterialEntity) = dao.deleteMaterial(material)

    // Orders
    val allOrders: Flow<List<OrderEntity>> = dao.getAllOrders()
    suspend fun getOrderById(id: Long): OrderEntity? = dao.getOrderById(id)
    suspend fun insertOrder(order: OrderEntity): Long = dao.insertOrder(order)
    suspend fun updateOrder(order: OrderEntity) = dao.updateOrder(order)
    suspend fun updateOrderStatus(id: Long, status: OrderStatus) = dao.updateOrderStatus(id, status)
    suspend fun deleteOrder(order: OrderEntity) = dao.deleteOrder(order)

    // Vendors
    val allVendors: Flow<List<VendorEntity>> = dao.getAllVendors()
    suspend fun getVendorById(id: Long): VendorEntity? = dao.getVendorById(id)
    suspend fun insertVendor(vendor: VendorEntity): Long = dao.insertVendor(vendor)
    suspend fun updateVendor(vendor: VendorEntity) = dao.updateVendor(vendor)
    suspend fun deleteVendor(vendor: VendorEntity) {
        dao.deleteJobsByVendorId(vendor.id)
        dao.deleteVouchersByVendorId(vendor.id)
        dao.deleteVendor(vendor)
    }

    // Vendor Jobs (سجل الأعمال المنفذة لكل مطبعة)
    fun getJobsForVendor(vendorId: Long): Flow<List<com.example.data.model.VendorJobEntity>> = dao.getJobsForVendor(vendorId)

    suspend fun addVendorJob(job: com.example.data.model.VendorJobEntity): Long {
        val jobId = dao.insertVendorJob(job)
        // Automatically add cost to vendor ledger
        if (job.costYer > 0) {
            val vendor = dao.getVendorById(job.vendorId)
            if (vendor != null) {
                val newJobsCost = vendor.totalJobsCostYer + job.costYer
                val newDebt = (newJobsCost - vendor.totalPaidYer).coerceAtLeast(0.0)
                dao.updateVendor(vendor.copy(totalJobsCostYer = newJobsCost, remainingDebtYer = newDebt))
            }
        }
        return jobId
    }

    suspend fun deleteVendorJob(job: com.example.data.model.VendorJobEntity) {
        dao.deleteVendorJob(job)
        val vendor = dao.getVendorById(job.vendorId)
        if (vendor != null) {
            val newJobsCost = (vendor.totalJobsCostYer - job.costYer).coerceAtLeast(0.0)
            val newDebt = (newJobsCost - vendor.totalPaidYer).coerceAtLeast(0.0)
            dao.updateVendor(vendor.copy(totalJobsCostYer = newJobsCost, remainingDebtYer = newDebt))
        }
    }

    // Vouchers
    val allVouchers: Flow<List<VoucherEntity>> = dao.getAllVouchers()
    fun getVouchersForVendor(vendorId: Long): Flow<List<VoucherEntity>> = dao.getVouchersForVendor(vendorId)

    suspend fun addVoucherAndSettleDebt(voucher: VoucherEntity): Long {
        val voucherId = dao.insertVoucher(voucher)
        // Automatically deduct from vendor debt
        val vendor = dao.getVendorById(voucher.vendorId)
        if (vendor != null) {
            val newPaid = vendor.totalPaidYer + voucher.amountYer
            val newDebt = (vendor.totalJobsCostYer - newPaid).coerceAtLeast(0.0)
            dao.updateVendor(vendor.copy(totalPaidYer = newPaid, remainingDebtYer = newDebt))
        }
        return voucherId
    }

    suspend fun addVendorJobCost(vendorId: Long, jobCostYer: Double) {
        val vendor = dao.getVendorById(vendorId)
        if (vendor != null) {
            val newJobsCost = vendor.totalJobsCostYer + jobCostYer
            val newDebt = (newJobsCost - vendor.totalPaidYer).coerceAtLeast(0.0)
            dao.updateVendor(vendor.copy(totalJobsCostYer = newJobsCost, remainingDebtYer = newDebt))
        }
    }

    // Tasks (Kanban)
    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    suspend fun insertTask(task: TaskEntity): Long = dao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    suspend fun updateTaskStage(id: Long, stage: TaskStage) = dao.updateTaskStage(id, stage)
    suspend fun updateTaskCompleted(id: Long, completed: Boolean) = dao.updateTaskCompleted(id, completed)
    suspend fun deleteTask(task: TaskEntity) = dao.deleteTask(task)

    // SMS Messages
    val allSmsMessages: Flow<List<ClientSmsMessage>> = dao.getAllSmsMessages()
    fun getSmsForPhone(phoneSuffix: String): Flow<List<ClientSmsMessage>> = dao.getSmsForPhone(phoneSuffix)
    suspend fun insertSmsMessage(sms: ClientSmsMessage): Long = dao.insertSmsMessage(sms)
}
