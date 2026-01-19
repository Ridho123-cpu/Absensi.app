package com.salman.absensi

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tugas")
data class Tugas(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val mataPelajaran: String,
    val deskripsi: String,
    val deadline: String,
    val linkFoto: String = ""
)

@Entity(tableName = "absensi_history")
data class AbsensiRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val siswaId: Int,
    val namaSiswa: String,
    val status: String,
    val tanggal: Long
)
