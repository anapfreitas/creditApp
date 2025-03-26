package com.example.mycreditcard

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var edtCardNumber: EditText
    private lateinit var edtCardHolder: EditText
    private lateinit var edtExpiryDate: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        edtCardNumber = findViewById(R.id.edtCardNumber)
        edtCardHolder = findViewById(R.id.edtCardHolder)
        edtExpiryDate = findViewById(R.id.edtExpiryDate)
        val btnSubmit = findViewById<Button>(R.id.btnSubmit)

        setupTextWatchers()

        btnSubmit.setOnClickListener {
            if (validateFields()) {
                Toast.makeText(this, "Cartão válido!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupTextWatchers() {

        edtCardNumber.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false
            private var deletingBackspace = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                deletingBackspace = count > after
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                val original = s.toString().replace(" ", "")
                if (original.length > 16) {
                    s?.replace(0, s.length, original.substring(0, 16))
                    isFormatting = false
                    return
                }

                val formatted = StringBuilder()
                for (i in original.indices) {
                    if (i > 0 && i % 4 == 0) formatted.append(" ")
                    formatted.append(original[i])
                }

                val cursorPos = edtCardNumber.selectionStart
                s?.replace(0, s.length, formatted.toString())
                edtCardNumber.setSelection(cursorPos.coerceAtMost(formatted.length))

                isFormatting = false
            }
        })


        edtExpiryDate.addTextChangedListener(object : TextWatcher {
            private var isFormatting = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isFormatting) return

                if (s != null && s.length >= 2) {
                    val month = s.substring(0, 2).toIntOrNull() ?: 0
                    if (month > 12) {
                        isFormatting = true
                        edtExpiryDate.setText("12")
                        edtExpiryDate.setSelection(2)
                        isFormatting = false
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return
                isFormatting = true

                val text = s.toString().replace("/", "")
                if (text.length > 4) {
                    s?.replace(0, s.length, text.substring(0, 4))
                    isFormatting = false
                    return
                }

                val formatted = StringBuilder()
                for (i in text.indices) {
                    if (i == 2) formatted.append("/")
                    if (i < 4) formatted.append(text[i])
                }

                val cursorPos = edtExpiryDate.selectionStart
                s?.replace(0, s.length, formatted.toString())


                if (cursorPos == 2 && formatted.length > 2) {
                    edtExpiryDate.setSelection(3)
                } else {
                    edtExpiryDate.setSelection(cursorPos.coerceAtMost(formatted.length))
                }

                isFormatting = false
            }
        })
    }

    private fun validateFields(): Boolean {

        val cardNumber = edtCardNumber.text.toString().replace(" ", "")
        if (cardNumber.length != 16 || !cardNumber.matches(Regex("\\d+"))) {
            edtCardNumber.error = "Digite exatamente 16 dígitos"
            return false
        }


        val cardHolder = edtCardHolder.text.toString().trim()
        if (cardHolder.length < 3 || !cardHolder.matches(Regex("^[a-zA-ZÀ-ú ]+$"))) {
            edtCardHolder.error = "Apenas letras (mín. 3 caracteres)"
            return false
        }


        val expiryDate = edtExpiryDate.text.toString()
        if (!expiryDate.matches(Regex("(0[1-9]|1[0-2])/\\d{2}"))) {
            edtExpiryDate.error = "Formato MM/AA"
            return false
        }


        val parts = expiryDate.split("/")
        val month = parts[0].toInt()
        val year = 2000 + parts[1].toInt()
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

        if (year < currentYear || (year == currentYear && month < currentMonth)) {
            edtExpiryDate.error = "Cartão expirado"
            return false
        }

        return true
    }
}