package com.example.mibusv.utils

import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.regex.Pattern

object ValidationUtils {
    
    //Valida que un EditText no esté vacío

    fun validateNotEmpty(editText: EditText, errorMessage: String): Boolean {
        val text = editText.text.toString().trim()
        if (text.isEmpty()) {
            editText.error = errorMessage
            editText.requestFocus()
            return false
        }
        editText.error = null
        return true
    }
    
    //Valida el formato de teléfono (0000-0000)

    fun validatePhoneFormat(editText: EditText): Boolean {
        val phone = editText.text.toString().trim()
        val phonePattern = Pattern.compile("^\\d{4}-\\d{4}$")
        
        if (phone.isEmpty()) {
            editText.error = "El teléfono es requerido"
            editText.requestFocus()
            return false
        }
        
        if (!phonePattern.matcher(phone).matches()) {
            editText.error = "Formato de teléfono inválido. Use: 0000-0000"
            editText.requestFocus()
            return false
        }
        
        editText.error = null
        return true
    }
    
    //Valida que el email termine en @gmail.com

    fun validateGmailFormat(editText: EditText): Boolean {
        val email = editText.text.toString().trim()
        
        if (email.isEmpty()) {
            editText.error = "El email es requerido"
            editText.requestFocus()
            return false
        }
        
        if (!email.endsWith("@gmail.com")) {
            editText.error = "El email debe terminar en @gmail.com"
            editText.requestFocus()
            return false
        }
        
        // Validación adicional de formato de email
        val emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@gmail\\.com$")
        if (!emailPattern.matcher(email).matches()) {
            editText.error = "Formato de email inválido"
            editText.requestFocus()
            return false
        }
        
        editText.error = null
        return true
    }

    fun formatHorario(editText: EditText): Boolean {
        val horario = editText.text.toString().trim()
        
        if (horario.isEmpty()) {
            editText.error = "El horario es requerido"
            editText.requestFocus()
            return false
        }
        
        try {
            val formattedHorario = formatHorarioString(horario)
            editText.setText(formattedHorario)
            editText.error = null
            return true
        } catch (e: Exception) {
            editText.error = "Formato de horario inválido. Use: 7-8 o 7:00-8:00"
            editText.requestFocus()
            return false
        }
    }

    private fun formatHorarioString(horario: String): String {
        // Remover espacios extra
        val cleanHorario = horario.replace("\\s+".toRegex(), " ").trim()
        
        // Patrones que aceptamos
        val patterns = listOf(
            // 7-8, 7:00-8:00, 7:00 AM-8:00 PM
            "^(\\d{1,2})(?::(\\d{2}))?\\s*(AM|PM)?\\s*-\\s*(\\d{1,2})(?::(\\d{2}))?\\s*(AM|PM)?$".toRegex(),
            // 7 a 8, 7:00 a 8:00
            "^(\\d{1,2})(?::(\\d{2}))?\\s*a\\s*(\\d{1,2})(?::(\\d{2}))?$".toRegex()
        )
        
        for (pattern in patterns) {
            val match = pattern.find(cleanHorario)
            if (match != null) {
                val groups = match.groupValues
                
                when (groups.size) {
                    5 -> { // 7-8 o 7:00-8:00
                        val horaInicio = groups[1].toInt()
                        val minInicio = groups[2].ifEmpty { "00" }
                        val horaFin = groups[3].toInt()
                        val minFin = groups[4].ifEmpty { "00" }
                        
                        // Asignar AM al primer número y PM al segundo si no se especifica
                        val ampmInicio = "AM"
                        val ampmFin = "PM"
                        
                        val horaInicio24 = convertTo24Hour(horaInicio, ampmInicio)
                        val horaFin24 = convertTo24Hour(horaFin, ampmFin)
                        
                        return formatTime(horaInicio24, minInicio) + " - " + formatTime(horaFin24, minFin)
                    }
                    7 -> { // 7:00 AM-8:00 PM
                        val horaInicio = groups[1].toInt()
                        val minInicio = groups[2].ifEmpty { "00" }
                        val ampmInicio = groups[3]
                        val horaFin = groups[4].toInt()
                        val minFin = groups[5].ifEmpty { "00" }
                        val ampmFin = groups[6]
                        
                        val ampmInicioFinal = if (ampmInicio.isEmpty()) "AM" else ampmInicio
                        val ampmFinFinal = if (ampmFin.isEmpty()) "PM" else ampmFin
                        
                        val horaInicio24 = convertTo24Hour(horaInicio, ampmInicioFinal)
                        val horaFin24 = convertTo24Hour(horaFin, ampmFinFinal)
                        
                        return formatTime(horaInicio24, minInicio) + " - " + formatTime(horaFin24, minFin)
                    }
                }
            }
        }
        
        throw IllegalArgumentException("Formato no reconocido")
    }
    
    private fun formatTime(hora: Int, minutos: String): String {
        val hora12 = if (hora == 0) 12 else if (hora > 12) hora - 12 else hora
        val ampm = if (hora < 12) "AM" else "PM"
        return String.format("%d:%s %s", hora12, minutos.padStart(2, '0'), ampm)
    }
    
    private fun convertTo24Hour(hora: Int, ampm: String): Int {
        return when {
            ampm.isEmpty() -> hora
            ampm.uppercase() == "AM" -> if (hora == 12) 0 else hora
            ampm.uppercase() == "PM" -> if (hora == 12) 12 else hora + 12
            else -> hora
        }
    }

    fun showErrorToast(context: AppCompatActivity, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    fun validateAllFieldsNotEmpty(fields: List<Pair<EditText, String>>): Boolean {
        var allValid = true
        for ((editText, errorMessage) in fields) {
            if (!validateNotEmpty(editText, errorMessage)) {
                allValid = false
            }
        }
        return allValid
    }
}
