package com.example.roulette03

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.Context

class MainActivity : AppCompatActivity() {

    private var balance = 1000

    private lateinit var balanceText: TextView
    private lateinit var numberInput: EditText
    private lateinit var betInput: EditText
    private lateinit var playButton: Button
    private lateinit var resetButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        balanceText = findViewById(R.id.balanceText)
        numberInput = findViewById(R.id.numberInput)
        betInput = findViewById(R.id.betInput)
        playButton = findViewById(R.id.playButton)
        resetButton = findViewById(R.id.reset)

        updateBalance()

        resetButton.setOnClickListener {
            vibrate()
            balance = 1000
            updateBalance()
            clearInputs()
            showToast("Game reset. Balance is $balance tokens")
        }

        playButton.setOnClickListener {
            vibrate()
            val (number, bet) = validateInput() ?: return@setOnClickListener

            balance -= bet
            updateBalance()

            lifecycleScope.launch(Dispatchers.Main) {
                val winningNumber = (0..100).random()
                val won = number == winningNumber

                if (won) balance += bet * 50

                val message = if (won) {
                    "You won! Number was $winningNumber. Balance: $balance tokens"
                } else {
                    "You lost. Number was $winningNumber. Balance: $balance tokens"
                }

                updateBalance()
                showToast(message)
                clearInputs()
            }
        }
    }

    private fun validateInput(): Pair<Int, Int>? {
        val number = numberInput.text.toString().toIntOrNull()
        val bet = betInput.text.toString().toIntOrNull()

        return when {
            number == null || number !in 0..100 -> {
                showToast("Enter a valid number (0–100)")
                null
            }
            bet == null || bet <= 0 -> {
                showToast("Enter a valid bet amount")
                null
            }
            bet > balance -> {
                showToast("Insufficient balance")
                null
            }
            else -> Pair(number, bet)
        }
    }

    private fun updateBalance() {
        balanceText.text = "Balance: $balance tokens"
    }

    private fun clearInputs() {
        numberInput.text.clear()
        betInput.text.clear()
    }

    private fun vibrate(duration: Long = 50) {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}