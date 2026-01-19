package com.salman.absensi

import android.os.Bundle
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.copy
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.salman.absensi.ui.theme.AbsensiTheme
import kotlin.text.uppercase

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AbsensiTheme {
                val viewModel: SiswaViewModel = viewModel()
                val context = LocalContext.current

                // State observasi data
                val daftarSiswa by viewModel.allSiswa.collectAsState(initial = emptyList())
                val daftarTugas by viewModel.allTugas.collectAsState(initial = emptyList())

                // State UI
                var selectedTab by remember { mutableIntStateOf(0) }
                var showTugasDialog by remember { mutableStateOf(false) }
                var showRecapDialog by remember { mutableStateOf(false) }
                var showResetDialog by remember { mutableStateOf(false) }

                val namaKelasAktif = viewModel.getNamaKelas()

                // --- DIALOG PERINGATAN RESET ---
                if (showResetDialog) {
                    AlertDialog(
                        onDismissRequest = { showResetDialog = false },
                        title = { Text("⚠️ Reset Aplikasi") },
                        text = { Text("Semua data siswa, tugas, dan pengaturan kelas akan dihapus permanen. Anda harus mengatur ulang kelas dari awal. Lanjutkan?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.resetTotalAplikasi()
                                    showResetDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) { Text("Ya, Reset Semua") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showResetDialog = false }) { Text("Batal") }
                        }
                    )
                }

                if (daftarSiswa.isEmpty()) {
                    SetupScreen(onComplete = { namaKelas ->
                        viewModel.setupAplikasi(namaKelas)
                    })
                } else {
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                //1. TOMBOL REKAP DI KIRI
                                navigationIcon = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { showRecapDialog = true },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Assessment,
                                                contentDescription = "Rekap",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                },
                                title = {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "WE ARE • $namaKelasAktif",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 20.sp,
                                                letterSpacing = 0.5.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        )
                                        Text(
                                            text = "SOFTWARE ENGINEERING",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                letterSpacing = 2.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        )
                                    }
                                },
                                // 2. TOMBOL RESET DI KANAN
                                actions = {
                                    IconButton(onClick = { showResetDialog = true }) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteForever,
                                            contentDescription = "Reset",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    icon = { Icon(Icons.Default.Home, null) },
                                    label = { Text("Home") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    icon = { Icon(Icons.Default.Person, null) },
                                    label = { Text("Absensi") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 2,
                                    onClick = { selectedTab = 2 },
                                    icon = { Icon(Icons.Default.Assignment, null) },
                                    label = { Text("Tugas") }
                                )
                                NavigationBarItem(
                                    selected = selectedTab == 3,
                                    onClick = { selectedTab = 3 },
                                    icon = { Icon(Icons.Default.DateRange, null) },
                                    label = { Text("Roster") }
                                )
                            }
                        },
                        floatingActionButton = {
                            if (selectedTab == 2) {
                                FloatingActionButton(onClick = { showTugasDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = "Tambah Tugas")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()) {
                            Crossfade(targetState = selectedTab, label = "tab_fade") { screen ->
                                when (screen) {
                                    0 -> HomeScreen(viewModel = viewModel)
                                    1 -> AbsensiScreen(
                                        lastSync = viewModel.getLastSync(),
                                        daftarSiswa = daftarSiswa,
                                        onStatusChange = { s, st -> viewModel.updateStatus(s, st) },
                                        onSave = { viewModel.saveAndShareAbsensi(context, daftarSiswa) },
                                        onRefresh = { /* ... */ },
                                        onReset = { viewModel.resetStatusSemuaSiswa() },
                                        onHadirSemua = { viewModel.hadirkanSemuaSiswa() },
                                        onShareHadir = {
                                            val listHadir = daftarSiswa.filter { it.status == "Hadir" }
                                            viewModel.shareFormatKhusus(context, "HADIR", listHadir)
                                        },
                                        onShareTidakHadir = {
                                            val listTidakHadir = daftarSiswa.filter { it.status == "Izin" || it.status == "Alpa" }
                                            viewModel.shareFormatKhusus(context, "TIDAK HADIR", listTidakHadir)
                                        }
                                    )
                                    2 -> TugasScreen(
                                        daftarTugas = daftarTugas,
                                        onShare = { viewModel.shareTugas(context, it) },
                                        onDelete = { viewModel.deleteTugas(it) }
                                    )
                                    3 -> RosterScreen(onShareRoster = { minggu ->
                                        viewModel.shareRosterManual(context, minggu)
                                    })
                                }
                            }
                        }

                        // --- DIALOG TAMBAH TUGAS ---
                        if (showTugasDialog) {
                            AddTugasDialog(
                                onDismiss = { showTugasDialog = false },
                                onConfirm = { mapel, desc, dl, linkFoto ->
                                    viewModel.addTugas(mapel, desc, dl, linkFoto)
                                    showTugasDialog = false
                                }
                            )
                        }

                        // --- DIALOG REKAP ---
                        if (showRecapDialog) {
                            RecapDialog(
                                onDismiss = { showRecapDialog = false },
                                onRecapRequest = { type ->
                                    viewModel.getRecap(type) { recapText ->
                                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            this.type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_TEXT, recapText)
                                        }
                                        context.startActivity(android.content.Intent.createChooser(intent, "Bagikan Rekap $type"))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecapDialog(onDismiss: () -> Unit, onRecapRequest: (String) -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rekap Absensi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Pilih periode rekap yang ingin dibagikan ke Wali Kelas:")
                Button(onClick = { onRecapRequest("Mingguan") }, modifier = Modifier.fillMaxWidth()) { Text("Rekap Mingguan") }
                Button(onClick = { onRecapRequest("Bulanan") }, modifier = Modifier.fillMaxWidth()) { Text("Rekap Bulanan") }
                Button(onClick = { onRecapRequest("Semester") }, modifier = Modifier.fillMaxWidth()) { Text("Rekap Semester") }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Tutup") } }
    )
}

@Composable
fun HomeScreen(viewModel: SiswaViewModel) {
    val namaKelas = viewModel.getNamaKelas()
    val gradientColors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HEADER BANNER DENGAN IDENTITAS SEKOLAH ---
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.horizontalGradient(gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(20.dp)
                ) {
                    // LOGO ICON
                    Surface(
                        modifier = Modifier.size(85.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        border = BorderStroke(2.dp, Color.White)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_sekolah),
                            contentDescription = "Logo Sekolah",
                            modifier = Modifier.padding(12.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Absensi.io",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.Cyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )

                        Text(
                            text = "SMK NEGERI 1 KARANG BARU",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = namaKelas,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Divider(color = Color.White.copy(alpha = 0.5f), thickness = 1.dp, modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = "</Software Engineer>",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.Cyan,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                }
            }
            VerticalConnector()
        }

        // --- TAGLINE SEKOLAH
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "SMK BISA - SMK HEBAT • UNGGUL & BERKARAKTER",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
            VerticalConnector()
        }



        // --- TINGKAT 1: KEPALA SEKOLAH ---
        item {
            StrukturCard(
                "Kepala Sekolah",
                "Fahmi Putra S.Pd",
                Icons.Default.AccountBalance,
                Color(0xFFE91E63)
            )
            VerticalConnector()
        }

        // --- TINGKAT 2: WALI KELAS ---
        item {
            StrukturCard("Wali Kelas", "Rina Marlyanti S.Pd", Icons.Default.Face, Color(0xFF9C27B0))
            VerticalConnector()
        }

        // --- TINGKAT 3: KETUA & WAKIL (Berdampingan) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    StrukturCard(
                        "Ketua Kelas",
                        "Satria",
                        Icons.Default.Star,
                        Color(0xFFFF9800),
                        isSmall = true
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    StrukturCard(
                        "Wakil Ketua",
                        "Mulkan",
                        Icons.Default.Security,
                        Color(0xFF2196F3),
                        isSmall = true
                    )
                }
            }
            VerticalConnector()
        }

        // --- TINGKAT 4: SEKRETARIS & BENDAHARA ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    StrukturCard(
                        "Sekretaris",
                        "Nadya Zahra",
                        Icons.Default.Edit,
                        Color(0xFF4CAF50),
                        isSmall = true
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    StrukturCard(
                        "Bendahara",
                        "Melani",
                        Icons.Default.Payments,
                        Color(0xFFFFC107),
                        isSmall = true
                    )
                }
            }
            VerticalConnector()
        }


        // --- TINGKAT 5: SEKSI-SEKSI
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Seksi Bidang",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        items(
            listOf(
                Triple("Kebersihan", "Ruzain Alfaruq Mustafa", Icons.Default.CleaningServices),
                Triple("Keamanan", "Arga Aulia", Icons.Default.AdminPanelSettings),
                Triple("Keagamaan", "M. Fahmi Sahbana", Icons.Default.MenuBook)
            )
        ) { (jabatan, nama, ikon) ->
            StrukturCard(jabatan, nama, ikon, Color(0xFF607D8B), isSmall = true)
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            VerticalConnector()

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DevStatCard(
                    label = "VERSION",
                    value = "v1.0.0",
                    icon = Icons.Default.Terminal,
                    modifier = Modifier.weight(1f)
                )
                DevStatCard(
                    label = "STUDENTS",
                    value = "35 Students",
                    icon = Icons.Default.Groups,
                    modifier = Modifier.weight(1f)
                )
                DevStatCard(
                    label = "ENGINE",
                    value = "KOTLIN KSP",
                    icon = Icons.Default.Memory,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "© 2026 Maulana Ridho Salman Dev",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "RPL Team • SMK Negeri 1 Karang Baru",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
                Text(
                    text = "Aceh Tamiang, Indonesia",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun VerticalConnector() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.VerticalDivider(
            modifier = Modifier.fillMaxHeight(),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

@Composable
fun StrukturCard(
    jabatan: String,
    nama: String,
    icon: ImageVector,
    color: Color,
    isSmall: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isSmall) 10.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(if (isSmall) 36.dp else 44.dp),
                shape = RoundedCornerShape(12.dp),
                color = color
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(if (isSmall) 8.dp else 10.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = jabatan.uppercase(),
                    textAlign = TextAlign.Center,
                    style = (if (isSmall) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium)
                        .copy(
                            color = color,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                )
                Text(
                    text = nama,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = (if (isSmall) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium)
                        .copy(fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(if (isSmall) 36.dp else 44.dp))
        }
    }
}

@Composable
fun AbsensiScreen(
    lastSync: String,
    daftarSiswa: List<Siswa>,
    onStatusChange: (Siswa, String) -> Unit,
    onSave: () -> Unit,
    onRefresh: () -> Unit,
    onReset: () -> Unit,
    onHadirSemua: () -> Unit,
    onShareHadir: () -> Unit,
    onShareTidakHadir: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        if (daftarSiswa.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Button(onClick = onRefresh) { Text("Muat Seluruh Siswa") }
            }
        } else {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.History,
                    null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.Gray
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Terakhir dikirim: $lastSync",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Button(
                    onClick = onHadirSemua,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.DoneAll, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Hadir Semua", style = MaterialTheme.typography.labelMedium)
                }

                Button(
                    onClick = onReset,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Reset", style = MaterialTheme.typography.labelMedium)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val counts = listOf(
                    "Hadir: ${daftarSiswa.count { it.status == "Hadir" }}" to Color(0xFF4CAF50),
                    "Izin: ${daftarSiswa.count { it.status == "Izin" }}" to Color(0xFFFFC107),
                    "Alpa: ${daftarSiswa.count { it.status == "Alpa" }}" to Color(0xFFF44336)
                )

                counts.forEach { (text, color) ->
                    Surface(
                        color = color.copy(alpha = 0.2f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = text,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        )
                    }
                }
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(daftarSiswa) { siswa ->
                    SiswaItem(siswa = siswa, onStatusChange = { onStatusChange(siswa, it) })
                }
            }

            val isAdaYangSudahAbsen = daftarSiswa.any { it.status.isNotEmpty() }

            AnimatedVisibility(
                visible = isAdaYangSudahAbsen,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Button(
                        onClick = onSave,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Simpan & Kirim Semua")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onShareHadir,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Hanya Hadir", style = MaterialTheme.typography.labelSmall)
                        }

                        Button(
                            onClick = onShareTidakHadir,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Tidak Hadir", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RosterScreen(onShareRoster: (Int) -> Unit) {
    val mingguList = listOf(
        Triple("Minggu 1", "Blok A / Teori", Color(0xFF2196F3)),
        Triple("Minggu 2", "Blok B / Praktek 1", Color(0xFF4CAF50)),
        Triple("Minggu 3", "Blok C / Praktek 2", Color(0xFFFF9800))
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                "MANAGEMENT ROSTER",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            )
            Text(
                "Pilih minggu aktif untuk di kirim ke WhatsApp",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(24.dp))
        }

        itemsIndexed(mingguList) { index, (judul, sub, warna) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = warna.copy(alpha = 0.1f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, warna.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Badge Angka dengan Glassmorphism
                    Surface(
                        modifier = Modifier.size(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = warna
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "0${index + 1}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = judul.uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            text = sub,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Tombol Share Bergaya Tech
                    Button(
                        onClick = { onShareRoster(index + 1) },
                        colors = ButtonDefaults.buttonColors(containerColor = warna),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("SHARE", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(32.dp))
            Text(
                "Sistem deteksi esok hari aktif secara otomatis",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun TugasScreen(
    daftarTugas: List<Tugas>,
    onShare: (Tugas) -> Unit,
    onDelete: (Tugas) -> Unit
) {
    if (daftarTugas.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AssignmentLate,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Belum ada tugas.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "Klik tombol + untuk menambahkan tugas baru.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(daftarTugas) { tugas ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        if (tugas.linkFoto.isNotEmpty()) {
                            AsyncImage(
                                model = tugas.linkFoto,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }


                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(
                                    text = tugas.mataPelajaran,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row {
                                    IconButton(onClick = { onShare(tugas) }) {
                                        Icon(
                                            Icons.Default.Share,
                                            contentDescription = "Bagikan",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = { onDelete(tugas) }) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Hapus",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

                            Text(
                                text = tugas.deskripsi,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Event,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.Gray
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Kumpul Tugas: ${tugas.deadline}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddTugasDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var mapel by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var deadline by remember { mutableStateOf("") }
    var linkFoto by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            linkFoto = it.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Tugas Baru") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = mapel, onValueChange = { mapel = it }, label = { Text("Mata Pelajaran") })
                OutlinedTextField(value = deskripsi, onValueChange = { deskripsi = it }, label = { Text("Deskripsi Tugas") })
                OutlinedTextField(value = deadline, onValueChange = { deadline = it }, label = { Text("Kumpul Tugas") })

                Spacer(modifier = Modifier.height(8.dp))

                // TOMBOL NAVIGASI KE DRIVE
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (linkFoto.isEmpty()) "Ambil Foto dari Drive" else "Foto Terpilih ✅")
                }

                if (linkFoto.isNotEmpty()) {
                    Text(
                        "File URI: ${linkFoto.take(30)}...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(mapel, deskripsi, deadline, linkFoto) }) {
                Text("Simpan Tugas")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}

@Composable
fun SiswaItem(siswa: Siswa, onStatusChange: (String) -> Unit) {
    var fontSize by remember { mutableStateOf(16.sp) }

    val isDarkMode = isSystemInDarkTheme()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when(siswa.status) {
                "Hadir" -> if (isDarkMode) Color(0xFF1B3D1D) else Color(0xFFE8F5E9)
                "Izin"  -> if (isDarkMode) Color(0xFF423C1A) else Color(0xFFFFFDE7)
                "Alpa"  -> if (isDarkMode) Color(0xFF4D1C1C) else Color(0xFFFFEBEE)
                else    -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = siswa.nama,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color.Black
                ),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { textLayoutResult ->
                    if (textLayoutResult.hasVisualOverflow) {
                        fontSize = fontSize * 0.9f
                    }
                }
            )

            Spacer(Modifier.width(8.dp))

            Row {
                StatusButton("H", "Hadir", siswa.status, onStatusChange)
                StatusButton("I", "Izin", siswa.status, onStatusChange)
                StatusButton("A", "Alpa", siswa.status, onStatusChange)
            }
        }
    }
}

@Composable
fun StatusButton(l: String, s: String, cur: String, onClick: (String) -> Unit) {
    val sel = cur == s
    val color = when(s) {
        "Hadir" -> Color(0xFF4CAF50)
        "Izin" -> Color(0xFFFFC107)
        "Alpa" -> Color(0xFFF44336)
        else -> Color.Gray
    }

    OutlinedButton(
        onClick = { onClick(s) },
        modifier = Modifier.size(42.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if(sel) color else color.copy(alpha = 0.1f),
            contentColor = if(sel) Color.White else color
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(l, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun SetupScreen(onComplete: (String) -> Unit) {
    var namaKelas by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
            }
            Text(
                text = "Absensi.io",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = "Software Engineering Edition",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(40.dp))
            // LOGO APLIKASI
            Surface(
                modifier = Modifier.size(100.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Konfigurasi Kelas",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Siapkan aplikasi untuk kelasmu",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))


            OutlinedTextField(
                value = namaKelas,
                onValueChange = { namaKelas = it },
                label = { Text("Class Name") },
                placeholder = { Text("Contoh: \"IX RPL-1\"") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { if (namaKelas.isNotBlank()) onComplete(namaKelas) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = namaKelas.isNotBlank()
            ) {
                Text("MULAI SEKARANG", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Build Version: 1.0.0-Stable (SMKN 1 Karang Baru Edition)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun DevStatCard(label: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(6.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                ),
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}