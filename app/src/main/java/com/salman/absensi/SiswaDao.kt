package com.salman.absensi

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SiswaDao {
    @Query("SELECT * FROM siswa ORDER BY nama ASC")
    fun getAllSiswa(): Flow<List<Siswa>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSiswa(siswa: Siswa)

    @Update
    suspend fun updateSiswa(siswa: Siswa)

    @Query("DELETE FROM siswa")
    suspend fun deleteAllSiswa()

    // Tugas
    @Query("SELECT * FROM tugas ORDER BY id DESC")
    fun getAllTugas(): Flow<List<Tugas>>

    @Insert
    suspend fun insertTugas(tugas: Tugas)

    @Delete
    suspend fun deleteTugas(tugas: Tugas)

    @Query("DELETE FROM tugas")
    suspend fun deleteAllTugas()

    @Query("SELECT * FROM absensi_history WHERE tanggal >= :fromTimestamp AND tanggal <= :toTimestamp")
    suspend fun getRecordsInRange(fromTimestamp: Long, toTimestamp: Long): List<AbsensiRecord>

    @Query("DELETE FROM absensi_history")
    suspend fun deleteAllHistory()

    @Query("UPDATE siswa SET status = ''")
    suspend fun resetAllStatuses()

    @Insert
    suspend fun insertHistory(record: AbsensiRecord)

}
