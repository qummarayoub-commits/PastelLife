package com.pastellife.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [TaskEntity::class, AccountEntity::class, TransactionEntity::class, ContactEntity::class, DebtEntity::class, DebtPaymentEntity::class, SavingsGoalEntity::class, TaskAttachmentEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN repeatRule TEXT NOT NULL DEFAULT 'None'")
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN trackedSeconds INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE tasks ADD COLUMN timerStartedAt INTEGER")
            }
        }
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS savings_goals (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, targetAmount INTEGER NOT NULL, currentAmount INTEGER NOT NULL, dueDate TEXT NOT NULL, note TEXT NOT NULL)")
            }
        }
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS task_attachments (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, taskId INTEGER NOT NULL, uri TEXT NOT NULL, displayName TEXT NOT NULL, addedAt INTEGER NOT NULL)")
            }
        }
        @Volatile private var instance: AppDatabase? = null
        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "pastel-life.db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build().also { instance = it }
        }
    }
}
