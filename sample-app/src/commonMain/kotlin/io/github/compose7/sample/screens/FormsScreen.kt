package io.github.compose7.sample.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import io.github.compose7.components.forms.Autocomplete
import io.github.compose7.components.forms.Calendar
import io.github.compose7.components.forms.CalendarDate
import io.github.compose7.components.forms.Checkbox
import io.github.compose7.components.forms.ColorPicker
import io.github.compose7.components.forms.DatePicker
import io.github.compose7.components.forms.DateRangePicker
import io.github.compose7.components.forms.RadioGroup
import io.github.compose7.components.forms.RadioOption
import io.github.compose7.components.forms.RangeSlider
import io.github.compose7.components.forms.Slider
import io.github.compose7.components.forms.SmartSelect
import io.github.compose7.components.forms.SmartSelectOption
import io.github.compose7.components.forms.Stepper
import io.github.compose7.components.forms.Switch
import io.github.compose7.components.forms.TextArea
import io.github.compose7.components.forms.TextField
import io.github.compose7.components.forms.dayOfWeek
import io.github.compose7.components.forms.formatted
import io.github.compose7.core.theme.Compose7ThemeColors
import io.github.compose7.core.theme.Compose7ThemeTypography

/** Basic + advanced form controls (PRD §7: "Forms — basic" and "Forms — advanced"). */
@Composable
fun FormsScreen() {
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var accepted by remember { mutableStateOf(false) }
    var plan by remember { mutableStateOf("pro") }
    var notifications by remember { mutableStateOf(true) }
    var quantity by remember { mutableStateOf(2) }
    var volume by remember { mutableStateOf(0.4f) }
    var priceRange by remember { mutableStateOf(10f..70f) }
    var fruit by remember { mutableStateOf<String?>(null) }
    var city by remember { mutableStateOf("") }
    var date by remember { mutableStateOf<CalendarDate?>(CalendarDate(2026, 10, 18)) }
    var trip by remember {
        mutableStateOf<ClosedRange<CalendarDate>?>(CalendarDate(2026, 10, 18)..CalendarDate(2026, 10, 24))
    }
    var accent by remember { mutableStateOf(Color(0xFF2F6FED)) }

    DemoSection("Text fields") {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = "Name",
            placeholder = "Ada Lovelace",
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            isError = password.isNotEmpty() && password.length < 6,
            supportingText = {
                BasicText(
                    text = "At least 6 characters",
                    style = Compose7ThemeTypography.bodySmall.copy(color = Compose7ThemeColors.onSurfaceVariant),
                )
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(10.dp))
        TextArea(
            value = bio,
            onValueChange = { bio = it },
            label = "Bio",
            placeholder = "Tell us something",
            modifier = Modifier.fillMaxWidth(),
        )
    }

    DemoSection("Toggles") {
        Checkbox(
            checked = accepted,
            onCheckedChange = { accepted = it },
            label = "Accept terms",
            isError = !accepted,
            supportingText = if (!accepted) {
                { BasicText("Required to continue", style = Compose7ThemeTypography.bodySmall) }
            } else {
                null
            },
        )
        Spacer(Modifier.height(8.dp))
        Checkbox(checked = true, onCheckedChange = {}, label = "Checked")
        Spacer(Modifier.height(8.dp))
        Checkbox(checked = false, onCheckedChange = {}, indeterminate = true, label = "Indeterminate")
        Spacer(Modifier.height(10.dp))
        DemoRow {
            BasicText(
                text = "Notifications",
                style = Compose7ThemeTypography.bodyLarge.copy(color = Compose7ThemeColors.onSurface),
            )
            Spacer(Modifier.width(8.dp))
            Switch(
                checked = notifications,
                onCheckedChange = { notifications = it },
                label = "Enabled",
            )
        }
        Spacer(Modifier.height(6.dp))
        RadioGroup(
            options = listOf(
                RadioOption("free", "Free"),
                RadioOption("pro", "Pro"),
                RadioOption("team", "Team"),
            ),
            selectedValue = plan,
            onSelectedChange = { plan = it },
        )
    }

    DemoSection("Steppers & sliders") {
        DemoRow(spacing = 24.dp) {
            Stepper(value = quantity, onValueChange = { quantity = it }, min = 0, max = 10)
            Stepper(value = 3, onValueChange = {}, enabled = false)
        }
        Spacer(Modifier.height(12.dp))
        DemoCaption("Volume: ${(volume * 100).toInt()}%")
        Slider(value = volume, onValueChange = { volume = it })
        Spacer(Modifier.height(12.dp))
        DemoCaption("Price: ${priceRange.start.toInt()} - ${priceRange.endInclusive.toInt()}")
        RangeSlider(
            value = priceRange,
            onValueChange = { priceRange = it },
            valueRange = 0f..100f,
        )
    }

    DemoSection("Pickers") {
        SmartSelect(
            value = fruit,
            options = listOf(
                SmartSelectOption("apple", "Apple", "Crisp"),
                SmartSelectOption("pear", "Pear", "Sweet"),
                SmartSelectOption("plum", "Plum", "Tart"),
            ),
            onValueChange = { fruit = it },
            label = "Favourite fruit",
            placeholder = "Choose one",
            sheetTitle = "Fruit",
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Autocomplete(
            value = city,
            onValueChange = { city = it },
            options = listOf("Amsterdam", "Athens", "Berlin", "Lisbon", "Oslo", "Prague", "Rome"),
            onSuggestionSelected = { city = it },
            label = "City",
            placeholder = "Start typing",
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        DatePicker(
            selectedDate = date,
            onDateSelected = { date = it },
            label = "Due date",
            minDate = CalendarDate(2026, 1, 1),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        DateRangePicker(
            selectedRange = trip,
            onRangeSelected = { trip = it },
            label = "Trip",
            minDate = CalendarDate(2026, 1, 1),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(12.dp))
        Calendar(
            selectedDate = date,
            onDateSelected = { date = it },
            isDateEnabled = { it.dayOfWeek() != 7 },
        )
        BasicText(
            text = "Selected: ${date?.formatted() ?: "none"}",
            modifier = Modifier.padding(top = 8.dp),
            style = Compose7ThemeTypography.bodyMedium.copy(color = Compose7ThemeColors.onSurfaceVariant),
        )
        Spacer(Modifier.height(12.dp))
        ColorPicker(
            color = accent,
            onColorChange = { accent = it },
            showAlpha = true,
            presets = listOf(Color(0xFF2F6FED), Color(0xFFE5484D), Color(0xFF30A46C)),
        )
    }
}
