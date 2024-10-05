package com.example.anarcomarombismo.Controller.Util

class NumberFormatter {
    companion object {
        fun formatDoubleNumber(value: Double,numDecimalPlaces: Int = 0): String {
            return "%.${numDecimalPlaces}f".format(value).replace(",", ".")
        }
    }
}