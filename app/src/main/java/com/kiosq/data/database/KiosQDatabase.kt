package com.kiosq.data.database

import android.content.Context
import androidx.room.*
import com.kiosq.data.dao.BarangDao
import com.kiosq.data.dao.TransaksiDao
import com.kiosq.data.entity.*
import com.kiosq.data.entity.SatuanConverter
import com.kiosq.data.entity.TransaksiConverter

@Database(
    entities = [Barang::class, Transaksi::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(SatuanConverter::class, TransaksiConverter::class)
abstract class KiosQDatabase : RoomDatabase() {

    abstract fun barangDao(): BarangDao
    abstract fun transaksiDao(): TransaksiDao

    companion object {

        @Volatile
        private var INSTANCE: KiosQDatabase? = null

        fun getInstance(context: Context): KiosQDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KiosQDatabase::class.java,
                    "kiosq_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}