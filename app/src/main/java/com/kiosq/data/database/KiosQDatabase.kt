package com.kiosq.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kiosq.data.dao.BarangDao
import com.kiosq.data.dao.TransaksiDao
import com.kiosq.data.entity.Barang
import com.kiosq.data.entity.Transaksi

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
        @Volatile private var INSTANCE: KiosQDatabase? = null

        fun getInstance(context: Context): KiosQDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KiosQDatabase::class.java,
                    "kiosq_database"
                ).fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}