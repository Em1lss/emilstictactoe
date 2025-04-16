package com.example.test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    private lateinit var playerOneNameInput: EditText
    private lateinit var playerTwoNameInput: EditText
    private lateinit var modeSelection: RadioGroup
    private lateinit var pvpMode: RadioButton
    private lateinit var pvcMode: RadioButton
    private lateinit var startGameButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // sakam, ka izmantosim tieši šo izkārtojumu

        playerOneNameInput = findViewById(R.id.player_one_name)
        playerTwoNameInput = findViewById(R.id.player_two_name)
        modeSelection = findViewById(R.id.mode_selection)
        pvpMode = findViewById(R.id.pvp_mode)
        pvcMode = findViewById(R.id.pvc_mode)
        startGameButton = findViewById(R.id.start_game_button)

        // atrod visus XML komponentus un tos piešķir mainīgajiem

        pvpMode.isChecked = true

        modeSelection.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.pvc_mode) {
                playerTwoNameInput.isEnabled = false
                playerTwoNameInput.setText("Dators")
            } else {
                playerTwoNameInput.isEnabled = true
                playerTwoNameInput.setText("")
            }
        } // skatoties, kas ir izvēlēts, ļāuj ievadīt attiecīgo lauku vārdus

        startGameButton.setOnClickListener {
            startGame()
        }
    }

    private fun startGame() { // sākas pati spēle
        val playerOneName = playerOneNameInput.text.toString().trim()
        val playerTwoName = playerTwoNameInput.text.toString().trim()

        if (playerOneName.isEmpty()) {
            Toast.makeText(this, "Lūdzu ievadi 1. spēlētāja vārdu", Toast.LENGTH_SHORT).show()
            return
        }
        // pārbauda kāds ir gamemode un sāk nodot tālāk mainīgos uz "spēles īsto sākumu"
        if (pvpMode.isChecked && playerTwoName.isEmpty()) {
            Toast.makeText(this, "Lūdzu ievadi 2. spēlētāja vārdu", Toast.LENGTH_SHORT).show()
            return
        }

        val isPvC = pvcMode.isChecked

        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("PLAYER_ONE_NAME", playerOneName)
            putExtra("PLAYER_TWO_NAME", playerTwoName)
            putExtra("IS_PVC_MODE", isPvC)
        }
        startActivity(intent)
    }
}