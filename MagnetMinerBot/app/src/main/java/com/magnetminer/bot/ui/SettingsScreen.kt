package com.magnetminer.bot.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.magnetminer.bot.utils.BotPreferences

@Composable
fun SettingsScreen() {
    val context = LocalContext.current

    var tapIntervalMs by remember {
        mutableStateOf(BotPreferences.getTapIntervalMs(context))
    }
    var scanIntervalMs by remember {
        mutableStateOf(BotPreferences.getScanIntervalMs(context))
    }
    val enabledLabels = remember {
        mutableStateOf(BotPreferences.getEnabledLabels(context).toMutableSet())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("Ayarlar", fontSize = 22.sp, fontWeight = FontWeight.Bold)

        SectionCard(title = "Tıklama Gecikmesi") {
            Text("Başarılı bir tıklamadan sonra kaç ms bekleneceği.", fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            SliderRow(
                value = tapIntervalMs,
                range = 100f..2000f,
                onValueChange = {
                    tapIntervalMs = it
                    BotPreferences.setTapIntervalMs(context, it)
                }
            )
        }

        SectionCard(title = "Tarama Aralığı") {
            Text("Ekranın ne sıklıkla taranacağı.", fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            SliderRow(
                value = scanIntervalMs,
                range = 100f..1000f,
                onValueChange = {
                    scanIntervalMs = it
                    BotPreferences.setScanIntervalMs(context, it)
                }
            )
        }

        SectionCard(title = "Hedef Buton Etiketleri") {
            Text("Botun tıklamasını istediğin butonları seç.", fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            BotPreferences.DEFAULT_LABELS.forEach { label ->
                val checked = label in enabledLabels.value
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { isChecked ->
                            val updated = enabledLabels.value.toMutableSet()
                            if (isChecked) updated.add(label) else updated.remove(label)
                            enabledLabels.value = updated
                            BotPreferences.setEnabledLabels(context, updated)
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(label, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
