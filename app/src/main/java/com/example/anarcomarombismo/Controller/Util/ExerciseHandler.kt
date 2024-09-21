package com.example.anarcomarombismo.Controller.Util

import android.widget.EditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ExerciseHandler {
    companion object {
        fun formatRepetitionsAndCountSets(editTextSets: EditText, editTextRepetitions: EditText) {
            CoroutineScope(Dispatchers.Main).launch {
                val text = editTextRepetitions.text.toString()
                val newText = text.replace(Regex("[^0-9Xx×*,]"), "")
                if (text.contains("X") || text.contains("x") || text.contains("*") || text.contains(
                        "×"
                    )
                ) {
                    handleXFormat(editTextSets, editTextRepetitions, text)
                } else if (text.contains(",")) {
                    handleCommaFormat(editTextSets, editTextRepetitions, text)
                } else if (newText != text) {
                    editTextRepetitions.setText(newText)
                }
            }
        }

        private fun handleXFormat(
            editTextSets: EditText,
            editTextRepetitions: EditText,
            text: String
        ) {
            CoroutineScope(Dispatchers.Default).launch {
                val newText = async {
                    text.replace(Regex("[^0-9Xx×*]|X{2,}|x{2,}|×{2,}|\\*{2,}"), "")
                }
                val processedText = async {
                    val xCount =
                        newText.await().count { it == 'X' || it == 'x' || it == '*' || it == '×' }
                    if (xCount > 1) {
                        newText.await().dropLast(1)
                    } else {
                        newText.await()
                    }
                }
                val numbers = async {
                    processedText.await().split(Regex("[Xx×*]")).filter { it.isNotEmpty() }
                }
                withContext(Dispatchers.Main) {
                    try {
                        editTextSets.setText(numbers.await()[0])
                    } catch (e: Exception) {
                        editTextSets.setText("1")
                    }
                    editTextRepetitions.setText("")
                    if (processedText.await() != text) {
                        editTextRepetitions.setText(processedText.await())
                        editTextRepetitions.setSelection(processedText.await().length)
                    }
                }
            }
        }


        private fun handleCommaFormat(
            editTextSets: EditText,
            editTextRepetitions: EditText,
            text: String
        ) {
            CoroutineScope(Dispatchers.Default).launch {
                val newText = async {
                    text.replace(Regex("[^0-9,]|,{2,}"), "")
                }
                val numbers = async {
                    newText.await().split(",").filter { it.isNotEmpty() }
                }
                withContext(Dispatchers.Main) {
                    editTextSets.setText(numbers.await().size.toString())
                    if (newText.await() != text) {
                        editTextRepetitions.setText(newText.await())
                        editTextRepetitions.setSelection(newText.await().length)
                    }
                }
            }
        }
    }
}
