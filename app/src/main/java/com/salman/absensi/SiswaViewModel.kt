package com.salman.absensi

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.gestures.forEach
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

class SiswaViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).siswaDao()

    private val prefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    private val rosterMingguan = mapOf(
        1 to mapOf( // Minggu 1
            "Senin" to listOf("Sejarah", "Mulok", "Agama"),
            "Selasa" to listOf("Agama", "PPKN", "B.Indonesia"),
            "Rabu" to listOf("PJOK", "PPKN", "B.Indonesia", "Coding"),
            "Kamis" to listOf("PJOK", "Mulok", "Agama"),
            "Jumat" to listOf("Sejarah", "B.Indonesia"),
            "Sabtu" to listOf("Sejarah", "B.Indonesia", "PJOK")
        ),
        2 to mapOf( // Minggu 2
            "Senin" to listOf("IPAS", "B.Inggris"),
            "Selasa" to listOf("Matematika", "B. Inggris", "IPAS"),
            "Rabu" to listOf("Seni Budaya", "Matematika", "Coding", "B.Inggris"),
            "Kamis" to listOf("IPAS", "Matematika", "Seni Budaya"),
            "Jumat" to listOf("B.Inggris", "Matematika"),
            "Sabtu" to listOf("IPAS")
        ),
        3 to mapOf( // Minggu 3
            "Senin" to listOf("Kejuruan/Produktif"),
            "Selasa" to listOf("Informtika", "Kejuruan/Produktif", "Coding"),
            "Rabu" to listOf("Informatika", "Kejuruan/Produktif"),
            "Kamis" to listOf("Kejuruan/Produktif", "Informatika"),
            "Jumat" to listOf("Kejuruan/Produktif"),
            "Sabtu" to listOf("Kejuruan/Produktif")
        )
    )

    private val jadwalPiket = mapOf(
        "Senin" to listOf("NABILA AZZAHRA", "TEGUH FITRA ALASTA", "KANAYA ARSYA PUTRI", "TAZKIYA ASRAH", "MULKAN ALJABBARI"),
        "Selasa" to listOf("FILIPO ARIZKY PRATAMA", "SYAFIQA NAZWA", "SHERIN AZZURA NOVENTRI", "NIZAM MULKIL ALDAMAR", "AURA BALQIS"),
        "Rabu" to listOf("NADYA ZAHRA", "DWI SASKIA PUTRI", "HAFIZA SHAIRA", "ARGA AULIA", "MAULANA RIDHO SALMAN", "FILDZAH RASYIKA"),
        "Kamis" to listOf("ASTRIA AZELINA", "SAFA ALDAWIYAH", "TRI ARTIANTI", "MUHAMMAD ARIF", "MUHAMMAD ALFAHRI", "KIKI AULIA"),
        "Jumat" to listOf("NAYLA SYAIFANI YUSUF", "DESY HUSNADILLA", "SATRIA HADIWINATA", "MARIFA OCTAVIANI", "RUZAIN ALFARUQ MUSTAFA", "INDAH PRATIWI"),
        "Sabtu" to listOf("SARAH KHUMAIRA", "TALITHA MUSFIRAH BR TARIGAN", "HANIFA NABILAH", "MELANI", "MUHAMMAD AKBAR ARNALDI", "MUHAMMAD FAHMI SAHBANA")
    )
    // Jadwal Ambil MBG
    private val jadwalAmbilMBG = mapOf(
        "Senin" to listOf("ALFAHRIZI", "NABILA AZZAHRA", "TEGUH FITRA ALASTA", "KANAYA ARSYA PUTRI"),
        "Selasa" to listOf("FILIPO ARIZKY PRATAMA", "SHERIN AZZURA NOVENTRI", "NIZAM MULKIL ALDAMAR", "AURA BALQIS"),
        "Rabu" to listOf("NADYA ZAHRA", "DWI SASKIA PUTRI", "HAFIZA SHAIRA", "ARGA AULIA"),
        "Kamis" to listOf("ASTRIA AZELINA", "SAFA ALDAWIYAH", "MUHAMMAD ARIF", "TRI ARTIANTI"),
        "Jumat" to listOf("NAYLA SYAIFANI YUSUF", "DESY HUSNADILLA", "SATRIA HADIWINATA", "MUHAMMAD FAHMI SAHBANA"),
        "Sabtu" to listOf("KETUA KELAS", "WAKIL KETUA KELAS")
    )

    // Jadwal Balikin MBG (Wadah/Keranjang)
    private val jadwalBalikinMBG = mapOf(
        "Senin" to listOf("MULKAN ALJABBARI", "TAZKIYA ASRAH", "SARAH KHUMAIRA"),
        "Selasa" to listOf("TALITHA MUSFIRAH BR TARIGAN", "HANIFA NABILAH", "MELANI"),
        "Rabu" to listOf("MAULANA RIDHO SALMAN", "FILDZAH RASYIKA", "SYAFIQA NAZWA"),
        "Kamis" to listOf("MUHAMMAD ALFAHRI", "KIKI AULIA", "MUHAMMAD AKBAR ARNALDI"),
        "Jumat" to listOf("RUZAIN ALFARUQ MUSTAFA", "INDAH PRATIWI", "MARIFA OCTAVIANI"),
        "Sabtu" to listOf("KETUA KELAS", "WAKIL KETUA KELAS")
    )

    private fun getMingguKeberapa(): Int {
        val calendar = Calendar.getInstance()
        val weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
        // Logika: Modulo 3 untuk mendapatkan siklus 1, 2, 3
        return (weekOfYear % 3) + 1
    }

    private fun getHariIni(): String {
        return SimpleDateFormat("EEEE", Locale("id", "ID")).format(Date())
    }

    private fun currentClassName(): String {
        return prefs.getString("nama_kelas", "X RPL-1") ?: "X RPL-1"
    }

    val allSiswa: Flow<List<Siswa>> = dao.getAllSiswa()
    val allTugas: Flow<List<Tugas>> = dao.getAllTugas()

    fun updateStatus(siswa: Siswa, newStatus: String) {
        viewModelScope.launch {
            dao.updateSiswa(siswa.copy(status = newStatus))
        }
    }

    fun getNamaKelas(): String {
        return prefs.getString("nama_kelas", "X RPL-1") ?: "X RPL-1"
    }

    fun addTugas(mapel: String, desc: String, dl: String, link: String) {
        viewModelScope.launch {
            dao.insertTugas(Tugas(mataPelajaran = mapel, deskripsi = desc, deadline = dl, linkFoto = link))
        }
    }


    fun deleteTugas(tugas: Tugas) {
        viewModelScope.launch {
            dao.deleteTugas(tugas)
        }
    }

    fun saveAndShareAbsensi(context: Context, daftarSiswa: List<Siswa>) {
        val dateMillis = System.currentTimeMillis()
        val date = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())
        val hari = getHariIni()
        val namaKelas = getNamaKelas()

        // Ambil daftar nama tidak hadir (Gunakan trim() agar pencocokan nama akurat)
        val siswaTidakHadir = daftarSiswa
            .filter { it.status == "Izin" || it.status == "Alpa" }
            .map { it.nama.trim().uppercase() }

        // PERBAIKAN: Logika Coret Nama (Strikethrough)
        fun formatNamaPetugas(nama: String): String {
            return if (siswaTidakHadir.contains(nama.trim().uppercase())) {
                "~${nama}~ (Tidak Hadir)"
            } else {
                nama
            }
        }

        // Ambil Data Jadwal
        val piketHariIni = jadwalPiket[hari]?.joinToString("\n• ") { formatNamaPetugas(it) } ?: "Tidak ada jadwal"
        val ambilMBG = jadwalAmbilMBG[hari]?.joinToString("\n• ") { formatNamaPetugas(it) } ?: "Tidak ada jadwal"
        val balikinMBG = jadwalBalikinMBG[hari]?.joinToString("\n• ") { formatNamaPetugas(it) } ?: "Tidak ada jadwal"

        viewModelScope.launch {
            // --- PENTING: SIMPAN KE HISTORY AGAR REKAP TIDAK KOSONG ---
            daftarSiswa.forEach { siswa ->
                if (siswa.status.isNotEmpty()) {
                    dao.insertHistory(AbsensiRecord(
                        id = 0,
                        siswaId = siswa.id,
                        namaSiswa = siswa.nama,
                        status = siswa.status,
                        tanggal = dateMillis
                    ))
                }
            }

            // --- STRUKTUR PESAN ---
            val hadirCount = daftarSiswa.count { it.status == "Hadir" }
            val izinCount = daftarSiswa.count { it.status == "Izin" }
            val alpaCount = daftarSiswa.count { it.status == "Alpa" }

            val builder = StringBuilder()
            builder.append("*LAPORAN ABSENSI $namaKelas*\n")
            builder.append("*SMK NEGERI 1 KARANG BARU*\n")
            builder.append("Tanggal: $date ($hari)\n")
            builder.append("------------------------------------------\n\n")

            builder.append("✅ Hadir: $hadirCount\n")
            builder.append("ℹ️ Izin: $izinCount\n")
            builder.append("❌ Alpa: $alpaCount\n")
            builder.append("------------------------------------------\n\n")

            // Detail Tidak Hadir
            val listIzin = daftarSiswa.filter { it.status == "Izin" }
            val listAlpa = daftarSiswa.filter { it.status == "Alpa" }

            if (listIzin.isNotEmpty()) {
                builder.append("📝 *DAFTAR IZIN:*\n")
                listIzin.forEachIndexed { i, s -> builder.append("${i + 1}. ${s.nama}\n") }
                builder.append("\n")
            }
            if (listAlpa.isNotEmpty()) {
                builder.append("🚫 *DAFTAR ALPA:*\n")
                listAlpa.forEachIndexed { i, s -> builder.append("${i + 1}. ${s.nama}\n") }
                builder.append("\n")
            }

            builder.append("------------------------------------------\n")
            builder.append("🧹 *PETUGAS PIKET KELAS:*\n• $piketHariIni\n")
            builder.append("------------------------------------------\n")
            builder.append("🍱 *PETUGAS AMBIL MBG:*\n• $ambilMBG\n")
            builder.append("------------------------------------------\n")
            builder.append("🔄 *PETUGAS BALIKIN MBG:*\n• $balikinMBG\n")
            builder.append("------------------------------------------\n")
            builder.append("Sent via Absensi.io")

            // Update Last Sync
            val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            prefs.edit().putString("last_sync", "Hari ini pukul $timeNow").apply()

            // Kirim WhatsApp
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, builder.toString())
            }
            context.startActivity(Intent.createChooser(sendIntent, "Kirim Laporan"))
        }
    }

    fun getRecap(type: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val now = Calendar.getInstance()
            val from = Calendar.getInstance()
            val namaKelas = currentClassName()

            // --- LOGIKA PERHITUNGAN KALENDER ---
            when (type) {
                "Mingguan" -> {
                    // Set ke hari Senin di minggu ini
                    from.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    if (now.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        from.add(Calendar.DAY_OF_YEAR, -6)
                    }
                }
                "Bulanan" -> {
                    from.set(Calendar.DAY_OF_MONTH, 1)
                }
                "Semester" -> {
                    val month = from.get(Calendar.MONTH)
                    if (month >= 6) {
                        from.set(from.get(Calendar.YEAR), Calendar.JULY, 1)
                    } else {
                        from.set(from.get(Calendar.YEAR), Calendar.JANUARY, 1)
                    }
                }
            }

            from.set(Calendar.HOUR_OF_DAY, 0)
            from.set(Calendar.MINUTE, 0)
            from.set(Calendar.SECOND, 0)

            val records: List<AbsensiRecord> = dao.getRecordsInRange(from.timeInMillis, now.timeInMillis)

            if (records.isEmpty()) {
                onResult("❌ Belum ada data absensi tercatat untuk periode $type ini.")
                return@launch
            }

            // --- FORMATTING LAPORAN ---
            val recapMap = records.groupBy { it.namaSiswa }
            val df = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))

            val builder = StringBuilder()
            builder.append("📊 *REKAP ABSENSI $type - $namaKelas*\n")
            builder.append("🏫 SMK NEGERI 1 KARANG BARU\n")
            builder.append("------------------------------------------\n")
            builder.append("📅 Periode: ${df.format(from.time)} s/d ${df.format(now.time)}\n")
            builder.append("------------------------------------------\n\n")

            recapMap.keys.sorted().forEachIndexed { index, nama ->
                val stats = recapMap[nama]!!
                val h = stats.count { it.status == "Hadir" }
                val i = stats.count { it.status == "Izin" }
                val a = stats.count { it.status == "Alpa" }

                builder.append("${index + 1}. *$nama*\n")
                builder.append("   └─ [✅H:$h] [ℹ️I:$i] [❌A:$a]\n")
            }

            builder.append("\n------------------------------------------\n")
            builder.append("_Laporan dibuat otomatis oleh Absensi.io_ 🚀")

            onResult(builder.toString())
        }
    }

    fun resetTotalAplikasi() {
        viewModelScope.launch {
            dao.deleteAllSiswa()
            dao.deleteAllTugas()
            dao.deleteAllHistory()
        }
    }

    fun shareTugas(context: Context, tugas: Tugas) {
        val namaKelas = getNamaKelas()
        val pesan = """
        *TUGAS BARU - $namaKelas*
        📌 Mapel: ${tugas.mataPelajaran}
        📝 Tugas: ${tugas.deskripsi}
        📅 Kumpul Tugas: ${tugas.deadline}
    """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND

            if (tugas.linkFoto.isNotEmpty()) {
                val imageUri = Uri.parse(tugas.linkFoto)
                putExtra(Intent.EXTRA_STREAM, imageUri)
                type = "image/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                type = "text/plain"
            }

            putExtra(Intent.EXTRA_TEXT, pesan)
        }

        context.startActivity(Intent.createChooser(sendIntent, "Bagikan Tugas"))
    }
    fun shareRosterManual(context: Context, mingguKe: Int) {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1) // Deteksi Besok

        var hariEsok = SimpleDateFormat("EEEE", Locale("id", "ID")).format(calendar.time)
        if (hariEsok == "Minggu") hariEsok = "Senin"

        val namaKelas = getNamaKelas()

        // PENTING: Pastikan mengambil dari mingguKe yang benar
        val listMapel = rosterMingguan[mingguKe]?.get(hariEsok) ?: listOf("Tidak ada jadwal")
        val formatMapel = listMapel.joinToString("\n• ")

        val builder = StringBuilder()
        builder.append("📚 *JADWAL PELAJARAN BESOK ($hariEsok)*\n")
        builder.append("*MINGGU KE-$mingguKe - $namaKelas*\n")
        builder.append("------------------------------------------\n\n")
        builder.append("📖 *MATA PELAJARAN:*\n• $formatMapel\n\n")
        builder.append("------------------------------------------\n")
        builder.append("_Siapkan buku dan peralatanmu!_ 🚀\n")
        builder.append("Sent via Absensi.io")

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, builder.toString())
        }
        context.startActivity(Intent.createChooser(sendIntent, "Bagikan Roster"))
    }
    fun insertInitialData() {
        viewModelScope.launch {
            val initialSiswa = listOf(
                "ALFAHRIZI", "ARGA AULIA", "ASTRIA AZELINA", "AURA BALQIS", "DESY HUSNADILLA",
                "DWI SASKIA PUTRI", "FILDZAH RASYIKA", "FILIPO ARIZKY PRATAMA", "HAFIZA SHAIRA", "HANIFA NABILAH",
                "INDAH PRATIWI", "KANAYA ARSYA PUTRI", "KIKI AULIA", "MUHAMMAD AKBAR ARNALDI", "MUHAMMAD ALFAHRI",
                "MARIFA OCTAVIANI", "MAULANA RIDHO SALMAN", "MELANI", "MUHAMMAD ARIF", "MUHAMMAD FAHMI SAHBANA",
                "MULKAN ALJABBARI", "NABILA AZZAHRA", "NADYA ZAHRA", "NAYLA SYAIFANI YUSUF", "NIZAM MULKIL ALDAMAR",
                "RAUZAIN ALFARUQ MUSTAFA", "SAFA ALDAWIYAH", "SARAH KHUMAIRA", "SATRIA HADIWINATA", "SHERIN AZZURA NOVENTRI",
                "SYAFIQA NAZWA", "TALITHA MUSFIRAH BR TARIGAN", "TAZKIYA ASRAH", "TEGUH FITRA ALASTA", "TRI ARTIANTI"
            )
            initialSiswa.forEach { nama -> dao.insertSiswa(Siswa(nama = nama)) }
        }
    }
    fun resetStatusSemuaSiswa() {
        viewModelScope.launch {
            dao.resetAllStatuses()
        }
    }
    fun setupAplikasi(namaKelas: String) { // Hapus parameter daftarNama
        viewModelScope.launch {
            // 1. Simpan nama kelas ke SharedPreferences
            prefs.edit().putString("nama_kelas", namaKelas).apply()

            // 2. Bersihkan data lama
            dao.deleteAllSiswa()
            dao.deleteAllTugas()
            dao.deleteAllHistory()

            // 3. Masukkan nama siswa otomatis dari fungsi insertInitialData
            val initialSiswa = listOf(
                "ALFAHRIZI", "ARGA AULIA", "ASTRIA AZELINA", "AURA BALQIS", "DESY HUSNADILLA",
                "DWI SASKIA PUTRI", "FILDZAH RASYIKA", "FILIPO ARIZKY PRATAMA", "HAFIZA SHAIRA", "HANIFA NABILAH",
                "INDAH PRATIWI", "KANAYA ARSYA PUTRI", "KIKI AULIA", "MUHAMMAD AKBAR ARNALDI", "MUHAMMAD ALFAHRI",
                "MARIFA OCTAVIANI", "MAULANA RIDHO SALMAN", "MELANI", "MUHAMMAD ARIF", "MUHAMMAD FAHMI SAHBANA",
                "MULKAN ALJABBARI", "NABILA AZZAHRA", "NADYA ZAHRA", "NAYLA SYAIFANI YUSUF", "NIZAM MULKIL ALDAMAR",
                "RAUZAIN ALFARUQ MUSTAFA", "SAFA ALDAWIYAH", "SARAH KHUMAIRA", "SATRIA HADIWINATA", "SHERIN AZZURA NOVENTRI",
                "SYAFIQA NAZWA", "TALITHA MUSFIRAH BR TARIGAN", "TAZKIYA ASRAH", "TEGUH FITRA ALASTA", "TRI ARTIANTI"
            )

            initialSiswa.forEach { nama ->
                dao.insertSiswa(Siswa(nama = nama, status = ""))
            }
        }
    }
    fun hadirkanSemuaSiswa() {
        viewModelScope.launch {
            val daftarSiswa = allSiswa.first()
            daftarSiswa.forEach { siswa ->
                updateStatus(siswa, "Hadir")
            }
        }
    }
    fun shareFormatKhusus(context: Context, judul: String, listSiswa: List<Siswa>) {
        val namaKelas = getNamaKelas()
        val builder = StringBuilder()
        builder.append("*DAFTAR SISWA $judul - $namaKelas*\n")
        builder.append("------------------------------------------\n")

        if (listSiswa.isEmpty()) {
            builder.append("_Tidak ada data._")
        } else {
            listSiswa.forEachIndexed { index, siswa ->
                builder.append("${index + 1}. ${siswa.nama} (${siswa.status})\n")
            }
        }

        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, builder.toString())
        }
        context.startActivity(android.content.Intent.createChooser(intent, "Bagikan Daftar $judul"))
    }

    fun getLastSync(): String {
        return prefs.getString("last_sync", "Belum pernah dikirim") ?: "Belum pernah dikirim"
    }
}
