package upvictoria.pm_ene_abr_2024.iti_271164.pi1u1.hernandez_garcia

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputFilter
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator
import com.luckycatlabs.sunrisesunset.dto.Location
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class MainActivity : AppCompatActivity() {

    private lateinit var datePickerButton: Button
    private lateinit var hemisphereNSSpinner: Spinner
    private lateinit var hemisphereEWSpinner: Spinner
    private lateinit var latitudeDegreesEditText: EditText
    private lateinit var latitudeMinutesEditText: EditText
    private lateinit var latitudeSecondsEditText: EditText
    private lateinit var longitudeDegreesEditText: EditText
    private lateinit var longitudeMinutesEditText: EditText
    private lateinit var longitudeSecondsEditText: EditText
    private lateinit var timeZoneSpinner: Spinner
    private lateinit var daylightSavingsSpinner: Spinner
    private lateinit var submitButton: Button
    private lateinit var resultTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setFiltersForInputFields()
        setUpDatePickerDialog()
        fillTimeZoneSpinnerDynamically()
        setUpHemisphereSpinners()
        setUpDaylightSavingsSpinner()
        setUpSubmitButtonListener()
    }



    private fun initializeViews() {
        datePickerButton = findViewById(R.id.datePickerButton)
        hemisphereNSSpinner = findViewById(R.id.hemisphereNS)
        hemisphereEWSpinner = findViewById(R.id.hemisphereEW)
        latitudeDegreesEditText = findViewById(R.id.latitudeDegrees)
        latitudeMinutesEditText = findViewById(R.id.latitudeMinutes)
        latitudeSecondsEditText = findViewById(R.id.latitudeSeconds)
        longitudeDegreesEditText = findViewById(R.id.longitudeDegrees)
        longitudeMinutesEditText = findViewById(R.id.longitudeMinutes)
        longitudeSecondsEditText = findViewById(R.id.longitudeSeconds)
        timeZoneSpinner = findViewById(R.id.timeZoneSpinner)
        daylightSavingsSpinner = findViewById(R.id.daylightSavingsSpinner)
        submitButton = findViewById(R.id.submitButton)
        resultTextView = findViewById(R.id.resultTextView)
    }

    private fun setFiltersForInputFields() {
        val degreeFilter = InputFilter.LengthFilter(3)
        val minuteSecondFilter = InputFilter.LengthFilter(2)
        val secondFilter = InputFilter.LengthFilter(5) // Including decimal places
        latitudeDegreesEditText.filters = arrayOf(degreeFilter)
        latitudeMinutesEditText.filters = arrayOf(minuteSecondFilter)
        latitudeSecondsEditText.filters = arrayOf(secondFilter)
        longitudeDegreesEditText.filters = arrayOf(degreeFilter)
        longitudeMinutesEditText.filters = arrayOf(minuteSecondFilter)
        longitudeSecondsEditText.filters = arrayOf(secondFilter)
    }
    private fun setUpDaylightSavingsSpinner() {
        val daylightSavingsOptions = resources.getStringArray(R.array.daylight_savings_options)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, daylightSavingsOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        daylightSavingsSpinner.adapter = adapter
    }


    private fun setUpDatePickerDialog() {
        datePickerButton.setOnClickListener {
            val currentCalendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedDate = "$dayOfMonth/${month + 1}/$year"
                    datePickerButton.text = selectedDate
                },
                currentCalendar.get(Calendar.YEAR),
                currentCalendar.get(Calendar.MONTH),
                currentCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun fillTimeZoneSpinnerDynamically() {
        val timeZoneIds = TimeZone.getAvailableIDs().sorted().map { id ->
            "$id (${TimeZone.getTimeZone(id).getDisplayName(false, TimeZone.SHORT)})"
        }.toMutableList()

        // Crear el adaptador con la lista de zonas horarias
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeZoneIds)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timeZoneSpinner.adapter = adapter

        // Establecer la zona horaria predeterminada a "America/Mexico_City (CST)" si está disponible
        val defaultTimeZoneIndex = timeZoneIds.indexOfFirst { it.startsWith("America/Mexico_City") }
        if (defaultTimeZoneIndex >= 0) {
            timeZoneSpinner.setSelection(defaultTimeZoneIndex)
        }
    }

    private fun setUpHemisphereSpinners() {
        val nsAdapter = ArrayAdapter.createFromResource(this, R.array.hemisphere_ns_options, android.R.layout.simple_spinner_item)
        nsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        hemisphereNSSpinner.adapter = nsAdapter

        val ewAdapter = ArrayAdapter.createFromResource(this, R.array.hemisphere_ew_options, android.R.layout.simple_spinner_item)
        ewAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        hemisphereEWSpinner.adapter = ewAdapter
    }

    private fun setUpSubmitButtonListener() {
        submitButton.setOnClickListener {
            val isValidInput = validateInput()
            if (!isValidInput) {
                Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val location = createLocationFromInput()
            val calculator = SunriseSunsetCalculator(location, timeZoneSpinner.selectedItem.toString().split(" ")[0])
            val calendar = Calendar.getInstance().apply {
                time = SimpleDateFormat("dd/MM/yyyy", Locale.US).parse(datePickerButton.text.toString()) ?: return@setOnClickListener
            }

            // Obtiene la hora del amanecer y el atardecer
            val sunrise = calculator.getOfficialSunriseForDate(calendar)
            val sunset = calculator.getOfficialSunsetForDate(calendar)

            // Calcula la duración del día
            val sunriseCalendar = calculator.getOfficialSunriseCalendarForDate(calendar)
            val sunsetCalendar = calculator.getOfficialSunsetCalendarForDate(calendar)
            val dayLength = getDayLength(sunriseCalendar, sunsetCalendar)

            resultTextView.text = "Sunrise: $sunrise\nSunset: $sunset\nDay Length: $dayLength"
        }
    }
    private fun getDayLength(sunriseCalendar: Calendar, sunsetCalendar: Calendar): String {
        val diff = sunsetCalendar.timeInMillis - sunriseCalendar.timeInMillis
        val hours = diff / (1000 * 60 * 60)
        val minutes = (diff / (1000 * 60)) % 60
        return String.format("%dh %02dm", hours, minutes)
    }

    private fun isDaylightSavingsSelected(): Boolean {
        return daylightSavingsSpinner.selectedItem.toString().equals("Yes", ignoreCase = true)
    }

    private fun validateInput(): Boolean {
        // Verificar si se ha seleccionado una fecha
        if (datePickerButton.text.toString().equals("Choose Date", ignoreCase = true)) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return false
        }

        // Validación básica de los campos de latitud y longitud
        try {
            val latitudeDegrees = latitudeDegreesEditText.text.toString().toDouble()
            val latitudeMinutes = latitudeMinutesEditText.text.toString().toDouble()
            val latitudeSeconds = latitudeSecondsEditText.text.toString().toDouble()
            val longitudeDegrees = longitudeDegreesEditText.text.toString().toDouble()
            val longitudeMinutes = longitudeMinutesEditText.text.toString().toDouble()
            val longitudeSeconds = longitudeSecondsEditText.text.toString().toDouble()


            if (latitudeDegrees < -90 || latitudeDegrees > 90) {
                Toast.makeText(this, "Latitude degrees must be between -90 and 90", Toast.LENGTH_SHORT).show()
                return false
            }

            // Y que los grados de longitud estén entre -180 y 180
            if (longitudeDegrees < -180 || longitudeDegrees > 180) {
                Toast.makeText(this, "Longitude degrees must be between -180 and 180", Toast.LENGTH_SHORT).show()
                return false
            }

            // Asegurarse de que los minutos y segundos estén en el rango correcto
            if (latitudeMinutes < 0 || latitudeMinutes >= 60 || longitudeMinutes < 0 || longitudeMinutes >= 60 ||
                latitudeSeconds < 0 || latitudeSeconds >= 60 || longitudeSeconds < 0 || longitudeSeconds >= 60) {
                Toast.makeText(this, "Minutes and seconds must be between 0 and 59", Toast.LENGTH_SHORT).show()
                return false
            }

        } catch (e: NumberFormatException) {
            // Captura si alguno de los campos no es un número válido
            Toast.makeText(this, "Please enter valid numbers for latitude and longitude", Toast.LENGTH_SHORT).show()
            return false
        }

        // Si todas las validaciones son correctas
        return true
    }


    private fun createLocationFromInput(): Location {
        var latitude = latitudeDegreesEditText.text.toString().toDouble() +
                latitudeMinutesEditText.text.toString().toDouble() / 60 +
                latitudeSecondsEditText.text.toString().toDouble() / 3600
        var longitude = longitudeDegreesEditText.text.toString().toDouble() +
                longitudeMinutesEditText.text.toString().toDouble() / 60 +
                longitudeSecondsEditText.text.toString().toDouble() / 3600

        // Ajustar la latitud basada en el hemisferio N/S
        if (hemisphereNSSpinner.selectedItem.toString() == "S") {
            latitude = -latitude
        }

        // Ajustar la longitud basada en el hemisferio E/W
        if (hemisphereEWSpinner.selectedItem.toString() == "W") {
            longitude = -longitude
        }

        return Location(latitude, longitude)
    }

}
