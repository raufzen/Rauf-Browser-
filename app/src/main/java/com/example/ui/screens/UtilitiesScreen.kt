package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Base64
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.local.NoteEntity
import com.example.data.local.ReminderEntity
import com.example.data.local.TodoEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.SubScreenTopBar
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.MonoFontFamily
import com.example.ui.theme.SecureGreen
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.security.MessageDigest
import java.security.SecureRandom
import kotlin.math.pow
import kotlin.math.roundToInt

private enum class UtilityTab(val label: String) {
    CALCULATOR("Calculator"),
    NOTES("Notes"),
    TODO("To-Do"),
    REMINDERS("Reminders"),
    PASSWORD("Password"),
    HASH("Hash"),
    BASE64("Base64"),
    JSON("JSON"),
    UNITS("Converters")
}

@Composable
fun UtilitiesScreen(
    notes: List<NoteEntity>,
    todos: List<TodoEntity>,
    reminders: List<ReminderEntity>,
    onBack: () -> Unit,
    onSaveNote: (Long, String, String) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onAddTodo: (String) -> Unit,
    onToggleTodo: (TodoEntity) -> Unit,
    onDeleteTodo: (Long) -> Unit,
    onAddReminder: (String, String) -> Unit,
    onToggleReminder: (ReminderEntity) -> Unit,
    onDeleteReminder: (Long) -> Unit,
    onNotify: (String) -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(UtilityTab.CALCULATOR) }

    val copy: (String) -> Unit = { value ->
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("RAUF Utility", value))
        onNotify("Copied to clipboard")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        SubScreenTopBar(
            title = "Offline Utilities",
            subtitle = "Works 100% locally without internet",
            onBack = onBack
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(UtilityTab.entries) { tab ->
                FilterChip(
                    selected = activeTab == tab,
                    onClick = { activeTab = tab },
                    label = { Text(tab.label) },
                    modifier = Modifier.testTag("util_tab_${tab.name.lowercase()}")
                )
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (activeTab) {
                UtilityTab.CALCULATOR -> item { CalculatorCard(onCopy = copy) }
                UtilityTab.NOTES -> {
                    item { NotesEditorCard(onSave = { t, c -> onSaveNote(0L, t, c) }) }
                    items(notes, key = { it.id }) { note ->
                        GlassCard(shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(note.title, style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(4.dp))
                                    Text(note.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { copy("${note.title}\n${note.content}") }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy note", modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { onDeleteNote(note.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete note", tint = DangerCrimson, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
                UtilityTab.TODO -> {
                    item { TodoCreatorCard(onAdd = onAddTodo) }
                    items(todos, key = { it.id }) { todo ->
                        GlassCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = todo.isDone, onCheckedChange = { onToggleTodo(todo) })
                                Text(
                                    text = todo.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textDecoration = if (todo.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { onDeleteTodo(todo.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete to-do", tint = DangerCrimson, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
                UtilityTab.REMINDERS -> {
                    item { ReminderCreatorCard(onAdd = onAddReminder) }
                    items(reminders, key = { it.id }) { rem ->
                        GlassCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = rem.isCompleted, onCheckedChange = { onToggleReminder(rem) })
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = rem.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textDecoration = if (rem.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    Text(
                                        text = "Due: ${rem.dueLabel}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(onClick = { onDeleteReminder(rem.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete reminder", tint = DangerCrimson, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
                UtilityTab.PASSWORD -> item { PasswordGeneratorCard(onCopy = copy) }
                UtilityTab.HASH -> item { HashGeneratorCard(onCopy = copy) }
                UtilityTab.BASE64 -> item { Base64ToolCard(onCopy = copy) }
                UtilityTab.JSON -> item { JsonFormatterCard(onCopy = copy) }
                UtilityTab.UNITS -> item { UnitConverterCard(onCopy = copy) }
            }
        }
    }
}

@Composable
private fun CalculatorCard(onCopy: (String) -> Unit) {
    var expr by remember { mutableStateOf("") }
    val evaluated = remember(expr) { if (expr.isBlank()) "" else evaluateMathExpr(expr) }

    GlassCard(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Scientific Calculator", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = expr,
                onValueChange = { expr = it },
                placeholder = { Text("e.g. (125 * 4) / 5 + 2^3") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (evaluated.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "= $evaluated",
                        style = MaterialTheme.typography.headlineMedium.copy(fontFamily = MonoFontFamily),
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = { onCopy(evaluated) }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy result")
                    }
                }
            }
            val keypadRows = listOf(
                listOf("7", "8", "9", "/"),
                listOf("4", "5", "6", "*"),
                listOf("1", "2", "3", "-"),
                listOf("0", ".", "(", ")"),
                listOf("C", "⌫", "^", "+")
            )
            keypadRows.forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { key ->
                        OutlinedButton(
                            onClick = {
                                when (key) {
                                    "C" -> expr = ""
                                    "⌫" -> if (expr.isNotEmpty()) expr = expr.dropLast(1)
                                    else -> expr += key
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(key)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesEditorCard(onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    GlassCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("New Local Note", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Note content") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = {
                    if (title.isNotBlank() || content.isNotBlank()) {
                        onSave(title, content)
                        title = ""
                        content = ""
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Save Note")
            }
        }
    }
}

@Composable
private fun TodoCreatorCard(onAdd: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    GlassCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = text, onValueChange = { text = it }, placeholder = { Text("Add a task...") }, singleLine = true, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(8.dp))
            Button(onClick = {
                if (text.isNotBlank()) {
                    onAdd(text)
                    text = ""
                }
            }) { Text("Add") }
        }
    }
}

@Composable
private fun ReminderCreatorCard(onAdd: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var due by remember { mutableStateOf("Today, 6:00 PM") }
    GlassCard(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("New Reminder", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Reminder") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = due, onValueChange = { due = it }, label = { Text("When") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, due)
                        title = ""
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) { Text("Add Reminder") }
        }
    }
}

@Composable
private fun PasswordGeneratorCard(onCopy: (String) -> Unit) {
    var length by remember { mutableFloatStateOf(16f) }
    var useUpper by remember { mutableStateOf(true) }
    var useLower by remember { mutableStateOf(true) }
    var useDigits by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }
    var seed by remember { mutableStateOf(0) }

    val password = remember(length, useUpper, useLower, useDigits, useSymbols, seed) {
        generateSecurePassword(length.roundToInt(), useUpper, useLower, useDigits, useSymbols)
    }

    GlassCard(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Password Generator", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = password,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = MonoFontFamily),
                    color = SecureGreen,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { seed++ }) { Icon(Icons.Default.Refresh, contentDescription = "Regenerate password") }
                IconButton(onClick = { onCopy(password) }) { Icon(Icons.Default.ContentCopy, contentDescription = "Copy password") }
            }
            Text("Length: ${length.roundToInt()} characters", style = MaterialTheme.typography.labelLarge)
            Slider(value = length, onValueChange = { length = it }, valueRange = 8f..48f)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Uppercase (A-Z)"); Switch(checked = useUpper, onCheckedChange = { useUpper = it })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Lowercase (a-z)"); Switch(checked = useLower, onCheckedChange = { useLower = it })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Numbers (0-9)"); Switch(checked = useDigits, onCheckedChange = { useDigits = it })
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Symbols (!@#\$%)"); Switch(checked = useSymbols, onCheckedChange = { useSymbols = it })
            }
        }
    }
}

@Composable
private fun HashGeneratorCard(onCopy: (String) -> Unit) {
    var input by remember { mutableStateOf("") }
    val algos = listOf("MD5", "SHA-1", "SHA-256", "SHA-512")

    GlassCard(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Cryptographic Hash Generator", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Input text") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            algos.forEach { algo ->
                val digest = remember(input, algo) {
                    if (input.isEmpty()) "" else hashHex(algo, input)
                }
                if (digest.isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(algo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text(digest, style = MaterialTheme.typography.labelSmall.copy(fontFamily = MonoFontFamily))
                        }
                        IconButton(onClick = { onCopy(digest) }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy $algo hash", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Base64ToolCard(onCopy: (String) -> Unit) {
    var input by remember { mutableStateOf("") }
    var output by remember { mutableStateOf("") }
    GlassCard(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Base64 Encoder / Decoder", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = input, onValueChange = { input = it }, label = { Text("Input") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    output = Base64.encodeToString(input.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                }, modifier = Modifier.weight(1f)) { Text("Encode") }
                OutlinedButton(onClick = {
                    output = runCatching {
                        String(Base64.decode(input.trim(), Base64.DEFAULT), Charsets.UTF_8)
                    }.getOrElse { "Invalid Base64 string" }
                }, modifier = Modifier.weight(1f)) { Text("Decode") }
            }
            if (output.isNotBlank()) {
                Text(output, style = MaterialTheme.typography.bodySmall.copy(fontFamily = MonoFontFamily))
                OutlinedButton(onClick = { onCopy(output) }) { Text("Copy Output") }
            }
        }
    }
}

@Composable
private fun JsonFormatterCard(onCopy: (String) -> Unit) {
    var raw by remember { mutableStateOf("{\"app\":\"Rauf Browser\",\"status\":\"ready\",\"themes\":5}") }
    var formatted by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    GlassCard(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("JSON Formatter & Validator", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = raw, onValueChange = { raw = it }, label = { Text("JSON input") }, minLines = 4, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    runCatching {
                        val parsed = JSONTokener(raw.trim()).nextValue()
                        when (parsed) {
                            is JSONObject -> parsed.toString(2)
                            is JSONArray -> parsed.toString(2)
                            else -> error("Not a JSON object or array")
                        }
                    }.onSuccess { formatted = it; isError = false }
                        .onFailure { formatted = "Error: ${it.localizedMessage}"; isError = true }
                }, modifier = Modifier.weight(1f)) { Text("Prettify") }
                OutlinedButton(onClick = {
                    runCatching {
                        val parsed = JSONTokener(raw.trim()).nextValue()
                        when (parsed) {
                            is JSONObject -> parsed.toString()
                            is JSONArray -> parsed.toString()
                            else -> error("Not a JSON object or array")
                        }
                    }.onSuccess { formatted = it; isError = false }
                        .onFailure { formatted = "Error: ${it.localizedMessage}"; isError = true }
                }, modifier = Modifier.weight(1f)) { Text("Minify") }
            }
            if (formatted.isNotBlank()) {
                Text(
                    text = formatted,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = MonoFontFamily),
                    color = if (isError) DangerCrimson else MaterialTheme.colorScheme.onSurface
                )
                if (!isError) OutlinedButton(onClick = { onCopy(formatted) }) { Text("Copy JSON") }
            }
        }
    }
}

@Composable
private fun UnitConverterCard(onCopy: (String) -> Unit) {
    var category by remember { mutableStateOf("Length") }
    var inputStr by remember { mutableStateOf("100") }
    val value = inputStr.toDoubleOrNull() ?: 0.0

    val conversions = remember(category, value) {
        when (category) {
            "Length" -> listOf(
                "Meters → Kilometers" to "${"%.4f".format(value / 1000.0)} km",
                "Meters → Feet" to "${"%.3f".format(value * 3.28084)} ft",
                "Kilometers → Miles" to "${"%.3f".format(value * 0.621371)} mi",
                "Centimeters → Inches" to "${"%.3f".format(value / 2.54)} in"
            )
            "Weight" -> listOf(
                "Kilograms → Pounds" to "${"%.3f".format(value * 2.20462)} lb",
                "Pounds → Kilograms" to "${"%.3f".format(value / 2.20462)} kg",
                "Grams → Ounces" to "${"%.3f".format(value / 28.3495)} oz"
            )
            "Temp" -> listOf(
                "°C → °F" to "${"%.2f".format(value * 9.0 / 5.0 + 32.0)} °F",
                "°F → °C" to "${"%.2f".format((value - 32.0) * 5.0 / 9.0)} °C",
                "°C → Kelvin" to "${"%.2f".format(value + 273.15)} K"
            )
            else -> listOf(
                "MB → GB" to "${"%.4f".format(value / 1024.0)} GB",
                "GB → MB" to "${"%.2f".format(value * 1024.0)} MB",
                "MB → KB" to "${"%.2f".format(value * 1024.0)} KB"
            )
        }
    }

    GlassCard(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Unit Converters", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("Length", "Weight", "Temp", "Data")) { c ->
                    FilterChip(selected = category == c, onClick = { category = c }, label = { Text(c) })
                }
            }
            OutlinedTextField(value = inputStr, onValueChange = { inputStr = it }, label = { Text("Numeric value") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            conversions.forEach { (label, result) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(result, style = MaterialTheme.typography.titleSmall.copy(fontFamily = MonoFontFamily), color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { onCopy(result) }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy conversion", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun generateSecurePassword(len: Int, upper: Boolean, lower: Boolean, digits: Boolean, symbols: Boolean): String {
    val pool = buildString {
        if (upper) append("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
        if (lower) append("abcdefghijklmnopqrstuvwxyz")
        if (digits) append("0123456789")
        if (symbols) append("!@#\$%^&*()-_=+[]{}?")
    }.ifEmpty { "abcdefghijklmnopqrstuvwxyz0123456789" }
    val rng = SecureRandom()
    return (1..len).map { pool[rng.nextInt(pool.length)] }.joinToString("")
}

private fun hashHex(algorithm: String, text: String): String {
    val bytes = MessageDigest.getInstance(algorithm).digest(text.toByteArray(Charsets.UTF_8))
    return bytes.joinToString("") { "%02x".format(it) }
}

private fun evaluateMathExpr(expression: String): String {
    return runCatching {
        val parser = object {
            var pos = -1
            var ch = 0
            fun nextChar() { ch = if (++pos < expression.length) expression[pos].code else -1 }
            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) { nextChar(); return true }
                return false
            }
            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expression.length) error("Unexpected char")
                return x
            }
            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    x = when {
                        eat('+'.code) -> x + parseTerm()
                        eat('-'.code) -> x - parseTerm()
                        else -> return x
                    }
                }
            }
            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    x = when {
                        eat('*'.code) -> x * parseFactor()
                        eat('/'.code) -> x / parseFactor()
                        else -> return x
                    }
                }
            }
            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()
                var x: Double
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
                    while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                    x = expression.substring(startPos, pos).toDouble()
                } else {
                    error("Invalid token")
                }
                if (eat('^'.code)) x = x.pow(parseFactor())
                return x
            }
        }
        val res = parser.parse()
        if (res % 1.0 == 0.0) res.toLong().toString() else "%.6f".format(res).trimEnd('0').trimEnd('.')
    }.getOrDefault("Error")
}
