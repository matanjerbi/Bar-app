package com.matan.bar.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/** Always-dark "speakeasy" palette: warm black, copper and gold. */
private val BarColors = darkColorScheme(
    primary = Color(0xFFE9B872),
    onPrimary = Color(0xFF2A1C08),
    primaryContainer = Color(0xFF4A3214),
    onPrimaryContainer = Color(0xFFFBDDAA),
    secondary = Color(0xFFE08E62),
    onSecondary = Color(0xFF2E1408),
    secondaryContainer = Color(0xFF4A2618),
    onSecondaryContainer = Color(0xFFFFD8C4),
    tertiary = Color(0xFFD9637C),
    tertiaryContainer = Color(0xFF55202D),
    onTertiaryContainer = Color(0xFFFFD9DF),
    background = Color(0xFF110C0D),
    onBackground = Color(0xFFF3E8DC),
    surface = Color(0xFF110C0D),
    onSurface = Color(0xFFF3E8DC),
    surfaceVariant = Color(0xFF2B2124),
    onSurfaceVariant = Color(0xFFD7C6B8),
    surfaceContainerLowest = Color(0xFF0B0809),
    surfaceContainerLow = Color(0xFF181213),
    surfaceContainer = Color(0xFF1D1618),
    surfaceContainerHigh = Color(0xFF261D20),
    surfaceContainerHighest = Color(0xFF30262A),
    outline = Color(0xFF9A887C),
    outlineVariant = Color(0xFF4A3D38),
    error = Color(0xFFFF8A80),
    errorContainer = Color(0xFF5A1D1D),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val Display = FontFamily.Serif

private val BarType = Typography().let { t ->
    t.copy(
        displaySmall = t.displaySmall.copy(fontFamily = Display, fontWeight = FontWeight.Bold),
        headlineLarge = t.headlineLarge.copy(fontFamily = Display, fontWeight = FontWeight.Bold),
        headlineMedium = t.headlineMedium.copy(fontFamily = Display, fontWeight = FontWeight.Bold),
        headlineSmall = t.headlineSmall.copy(fontFamily = Display, fontWeight = FontWeight.Bold),
        titleLarge = t.titleLarge.copy(fontFamily = Display, fontWeight = FontWeight.Bold),
        titleMedium = t.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    )
}

private val BarShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
)

@Composable
fun BarTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = BarColors, typography = BarType, shapes = BarShapes, content = content)
}

/** Gold used for headings and highlights. */
val Gold = Color(0xFFE9B872)

val Green = Color(0xFF8BC34A)
val Red = Color(0xFFFF7A6E)
val Orange = Color(0xFFFFB74D)

@Composable
fun Loading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun TextInput(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier, multiline: Boolean = false) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) },
        singleLine = !multiline, minLines = if (multiline) 3 else 1, modifier = modifier.fillMaxWidth(),
    )
}

@Composable
fun NumberField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> Dropdown(label: String, options: List<T>, selected: T, labelOf: (T) -> String, onSelect: (T) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = labelOf(selected), onValueChange = {}, readOnly = true, label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { DropdownMenuItem(text = { Text(labelOf(it)) }, onClick = { onSelect(it); expanded = false }) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChoiceChips(options: List<T>, selected: T, labelOf: (T) -> String, onSelect: (T) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach { FilterChip(selected = it == selected, onClick = { onSelect(it) }, label = { Text(labelOf(it)) }) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> MultiChips(options: List<T>, selected: Set<T>, labelOf: (T) -> String, onChange: (Set<T>) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEach {
            FilterChip(
                selected = it in selected,
                onClick = { onChange(if (it in selected) selected - it else selected + it) },
                label = { Text(labelOf(it)) },
            )
        }
    }
}

@Composable
fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
fun ConfirmDialog(text: String, confirm: String = "מחק", onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(text) },
        confirmButton = { TextButton(onClick = { onDismiss(); onConfirm() }) { Text(confirm, color = Red) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("ביטול") } },
    )
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = modifier.padding(top = 8.dp, bottom = 4.dp))
}

fun Context.toast(msg: String) = android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()
