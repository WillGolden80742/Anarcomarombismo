package com.example.anarcomarombismo.Controller.Util

import android.app.DatePickerDialog
import android.content.Context
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Calendars {

    companion object {
        fun selectDate(
            context: Context,
            textView: TextView,
            maxDate: Long,
            onDateSelected: (String) -> Unit
        ) {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                context,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val selectedDate =
                        "$selectedDay/${selectedMonth + 1}/$selectedYear" // Mês é base 0, por isso adicionamos 1

                    val formattedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        .parse(selectedDate)?.let {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                        }

                    if (formattedDate != textView.text.toString()) {
                        textView.text = formattedDate
                        onDateSelected(formattedDate.toString())
                    }
                }, year, month, day
            )

            // Set the max date passed as a parameter
            datePickerDialog.datePicker.maxDate = maxDate
            datePickerDialog.show()
        }
    }
}
