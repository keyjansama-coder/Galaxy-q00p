package com.galaxytunnel

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "imported_servers")
data class ImportedServer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val config: String,
    val addedTime: Long = System.currentTimeMillis()
)

@Dao
interface ImportedServerDao {
    @Query("SELECT * FROM imported_servers ORDER BY addedTime DESC")
    fun getAllServers(): Flow<List<ImportedServer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: ImportedServer)

    @Query("DELETE FROM imported_servers WHERE id = :id")
    suspend fun deleteServerById(id: Int)

    @Query("DELETE FROM imported_servers")
    suspend fun deleteAll()
}

@Database(entities = [ImportedServer::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun importedServerDao(): ImportedServerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "galaxy_tunnel_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class ImportedServerRepository(private val dao: ImportedServerDao) {
    val allServers: Flow<List<ImportedServer>> = dao.getAllServers()

    suspend fun insert(server: ImportedServer) {
        dao.insertServer(server)
    }

    suspend fun delete(id: Int) {
        dao.deleteServerById(id)
    }

    suspend fun clearAll() {
        dao.deleteAll()
    }
}
