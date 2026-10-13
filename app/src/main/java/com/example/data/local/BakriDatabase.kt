package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.BakriDao
import com.example.data.model.ClientSmsMessage
import com.example.data.model.MaterialEntity
import com.example.data.model.OrderEntity
import com.example.data.model.TaskEntity
import com.example.data.model.VendorEntity
import com.example.data.model.VoucherEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MaterialEntity::class,
        OrderEntity::class,
        VendorEntity::class,
        VoucherEntity::class,
        TaskEntity::class,
        ClientSmsMessage::class,
        com.example.data.model.VendorJobEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BakriDatabase : RoomDatabase() {
    abstract fun bakriDao(): BakriDao

    companion object {
        @Volatile
        private var INSTANCE: BakriDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): BakriDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BakriDatabase::class.java,
                    "bakri_print_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialMaterials(database.bakriDao())
                    }
                }
            }

            private suspend fun populateInitialMaterials(dao: BakriDao) {
                val defaultMaterials = listOf(
                    MaterialEntity(
                        nameAr = "فلكس بنر عادي (Flex 320g)",
                        nameEn = "Flex Banner 320g",
                        pricePerSqmYer = 1200.0,
                        category = "بانر",
                        isDefault = true
                    ),
                    MaterialEntity(
                        nameAr = "فلكس بنر كوري ثقيل (Flex Korean 440g)",
                        nameEn = "Korean Flex 440g",
                        pricePerSqmYer = 1800.0,
                        category = "بانر",
                        isDefault = false
                    ),
                    MaterialEntity(
                        nameAr = "استكر فينيل لامع (Vinyl Glossy)",
                        nameEn = "Glossy Vinyl Sticker",
                        pricePerSqmYer = 2200.0,
                        category = "استكرات",
                        isDefault = false
                    ),
                    MaterialEntity(
                        nameAr = "استكر فينيل مطفي (Vinyl Matte)",
                        nameEn = "Matte Vinyl Sticker",
                        pricePerSqmYer = 2400.0,
                        category = "استكرات",
                        isDefault = false
                    ),
                    MaterialEntity(
                        nameAr = "قماش كانفس فني (Fabric Canvas)",
                        nameEn = "Art Canvas Fabric",
                        pricePerSqmYer = 3500.0,
                        category = "أقمشة",
                        isDefault = false
                    ),
                    MaterialEntity(
                        nameAr = "ميش مخرم للنوافذ (One Way / Mesh)",
                        nameEn = "Perforated Mesh / One-Way",
                        pricePerSqmYer = 2800.0,
                        category = "ميش",
                        isDefault = false
                    ),
                    MaterialEntity(
                        nameAr = "أكريليك كريستال شفاف 3 مم (Acrylic 3mm)",
                        nameEn = "Clear Acrylic 3mm",
                        pricePerSqmYer = 8500.0,
                        category = "صلب وحروف",
                        isDefault = false
                    ),
                    MaterialEntity(
                        nameAr = "لوح فوم بورد فوركس 5 مم (Forex Board)",
                        nameEn = "Forex Foam Board 5mm",
                        pricePerSqmYer = 4200.0,
                        category = "صلب وحروف",
                        isDefault = false
                    )
                )
                dao.insertAllMaterials(defaultMaterials)
            }
        }
    }
}
