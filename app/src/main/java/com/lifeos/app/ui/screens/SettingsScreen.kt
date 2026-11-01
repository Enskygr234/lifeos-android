package com.lifeos.app.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lifeos.app.BuildConfig
import com.lifeos.app.ui.AppViewModel
import com.lifeos.app.ui.components.SectionCard
import com.lifeos.app.ui.theme.Purple
import com.lifeos.app.work.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

@Composable
fun SettingsScreen(vm: AppViewModel) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var permissionTick by remember { mutableIntStateOf(0) }
    val canNotify = remember(permissionTick, settings) { ReminderScheduler.canNotify(context) }
    var pendingImport by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var askedOnce by remember { mutableStateOf(false) }
    LifecycleResumeEffect(Unit) {
        permissionTick++
        onPauseOrDispose { }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permissionTick++
    }
    fun ensurePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !ReminderScheduler.canNotify(context)) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        busy = true
        scope.launch {
            val msg = try {
                val json = vm.exportJson()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(json.toByteArray(Charsets.UTF_8)) }
                        ?: error("Dosya açılamadı")
                }
                "Yedek kaydedildi."
            } catch (e: Exception) {
                "Yedekleme başarısız: ${e.message}"
            }
            busy = false
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val text = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                        ?: error("Dosya açılamadı")
                }
                pendingImport = text
            } catch (e: Exception) {
                Toast.makeText(context, "Dosya okunamadı: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SectionCard(title = "Görünüm", icon = Icons.Filled.DarkMode) {
                Text("Tema", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("dark" to "Koyu", "light" to "Açık", "system" to "Sistem").forEach { (k, l) ->
                        FilterChip(
                            selected = settings.themeMode == k,
                            onClick = { vm.setTheme(k) },
                            label = { Text(l) },
                            modifier = Modifier.testTag("theme_$k"),
                        )
                    }
                }
            }
        }
        item {
            SectionCard(title = "Dil", icon = Icons.Filled.Language) {
                Text("Uygulama dili: Türkçe", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Tüm arayüz, tarih ve gün adları Türkçe biçimdedir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            SectionCard(title = "Bildirimler", icon = Icons.Filled.Notifications) {
                SettingSwitch(
                    title = "Görev hatırlatıcıları",
                    subtitle = "Hatırlatıcı açık görevler için son tarih/saatte bildirim",
                    checked = settings.taskReminders,
                    onChange = { vm.setTaskReminders(it); if (it) ensurePermission() },
                )
                SettingSwitch(
                    title = "Günlük özet bildirimi",
                    subtitle = "Her gün seçilen saatte günün özeti",
                    checked = settings.dailySummary,
                    onChange = { vm.setDailySummary(it, settings.summaryHour); if (it) ensurePermission() },
                )
                if (settings.dailySummary) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Özet saati", Modifier.weight(1f))
                        IconButton(onClick = { vm.setDailySummary(true, (settings.summaryHour + 23) % 24) }) {
                            Icon(Icons.Filled.Remove, "Saati azalt")
                        }
                        Text("%02d:00".format(settings.summaryHour), style = MaterialTheme.typography.titleMedium)
                        IconButton(onClick = { vm.setDailySummary(true, (settings.summaryHour + 1) % 24) }) {
                            Icon(Icons.Filled.Add, "Saati artır")
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    if (canNotify) "✅ Bildirim izni verildi." else "⚠️ Bildirim izni yok veya bildirimler kapalı.",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (canNotify) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                )
                if (!canNotify) {
                    TextButton(onClick = {
                        val needsRuntime = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                            PackageManager.PERMISSION_GRANTED
                        if (needsRuntime && !askedOnce) {
                            askedOnce = true
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            runCatching { context.startActivity(intent) }
                        }
                    }) { Text(if (askedOnce) "Bildirim ayarlarını aç" else "Bildirim izni ver") }
                }
            }
        }
        item {
            SectionCard(title = "Yedekleme", icon = Icons.Filled.Backup) {
                Text(
                    "Tüm görev, hedef, alışkanlık, not ve ayarlarınızı JSON dosyası olarak kaydedin veya geri yükleyin.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = { exportLauncher.launch("lifeos-yedek-${LocalDate.now()}.json") },
                    enabled = !busy,
                    colors = ButtonDefaults.buttonColors(containerColor = Purple),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.FileDownload, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("JSON olarak dışa aktar")
                }
                Spacer(Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Filled.FileUpload, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Yedekten geri yükle")
                }
            }
        }
        item {
            SectionCard(title = "Hakkında", icon = Icons.Filled.Info) {
                Text("LifeOS ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Çevrimdışı kişisel yaşam yönetimi. Veriler yalnızca cihazınızda saklanır; hesap veya internet gerekmez.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    pendingImport?.let { json ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text("Yedeği geri yükle") },
            text = { Text("Mevcut tüm veriler silinip yedekteki verilerle değiştirilecek. Devam edilsin mi?") },
            confirmButton = {
                TextButton(onClick = {
                    pendingImport = null
                    busy = true
                    scope.launch {
                        val err = vm.importJson(json)
                        busy = false
                        Toast.makeText(context, err ?: "Yedek başarıyla geri yüklendi.", Toast.LENGTH_LONG).show()
                    }
                }) { Text("Geri yükle", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pendingImport = null }) { Text("Vazgeç") } },
        )
    }
}

@Composable
private fun SettingSwitch(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
