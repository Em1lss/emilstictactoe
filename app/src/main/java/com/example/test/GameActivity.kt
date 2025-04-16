package com.example.test

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import java.util.Random

class GameActivity : AppCompatActivity() {

    // spēles galvenie mainīgie
    private lateinit var buttons: Array<Array<Button>>
    private lateinit var playerOneName: String
    private lateinit var playerTwoName: String
    private var isPvCMode: Boolean = false
    private var playerOneTurn: Boolean = true
    private var roundCount: Int = 0
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        playerOneName = intent.getStringExtra("PLAYER_ONE_NAME") ?: "Player 1"
        playerTwoName = intent.getStringExtra("PLAYER_TWO_NAME") ?: "Player 2"
        isPvCMode = intent.getBooleanExtra("IS_PVC_MODE", false)
        // ir iegūti vārdi un spēles režīms

        statusText = findViewById(R.id.status_text)
        updateStatusText()

        // inicializē 9 "pogas", liek X un O, pārbauda vai nav nospiests aizņemts lauks un
        // pārbauda vai kāds nav uzvarējis pēc katra gājiena
        buttons = Array(3) { row ->
            Array(3) { col ->
                val buttonID = "button_$row$col"
                val resID = resources.getIdentifier(buttonID, "id", packageName)
                val button = findViewById<Button>(resID)

                button.setOnClickListener {
                    if (button.text.toString() != "") {
                        return@setOnClickListener
                    }

                    if (playerOneTurn) {
                        button.text = "X"
                    } else {
                        button.text = "O"
                    }

                    roundCount++

                    if (checkForWin()) {
                        if (playerOneTurn) {
                            playerOneWins()
                        } else {
                            playerTwoWins()
                        }
                    } else if (roundCount == 9) {
                        draw()
                    } else {
                        playerOneTurn = !playerOneTurn
                        updateStatusText()

                        if (isPvCMode && !playerOneTurn) {
                            Handler(Looper.getMainLooper()).postDelayed({
                                computerMove()
                            }, 500)
                        }
                    }
                }
                button
            }
        }

        showGreetingMessage()

        findViewById<Button>(R.id.reset_button).setOnClickListener {
            resetGame()
        }
    }


    private fun showGreetingMessage() {
        val greeting = buildString {
            append("Vai esi gatavs uzvarēt?\n\n")

            if (isPvCMode) {
                append("$playerOneName, pamēģini uzvarēt datoru ;) !")
            } else {
                append("$playerOneName vs $playerTwoName\nKurš nu uzvarēs :0 !")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Spēles sākums")
            .setMessage(greeting)
            .setPositiveButton("Sākt spēli!", null)
            .show()
    }

    private fun updateStatusText() {
        statusText.text = if (playerOneTurn) {
            "$playerOneName's turn (X)"
        } else {
            "${if (isPvCMode) "Computer" else playerTwoName}'s turn (O)"
        }
    }

    // tiešā pārbaude vai kāds nav uzvarējis
    private fun checkForWin(): Boolean {
        val field = Array(3) { row ->
            Array(3) { col ->
                buttons[row][col].text.toString()
            }
        }

        for (i in 0..2) {
            if (field[i][0] == field[i][1] && field[i][0] == field[i][2] && field[i][0] != "") {
                return true
            }
        }

        for (i in 0..2) {
            if (field[0][i] == field[1][i] && field[0][i] == field[2][i] && field[0][i] != "") {
                return true
            }
        }

        if (field[0][0] == field[1][1] && field[0][0] == field[2][2] && field[0][0] != "") {
            return true
        }

        if (field[0][2] == field[1][1] && field[0][2] == field[2][0] && field[0][2] != "") {
            return true
        }

        return false
    }

    // ja uzvar 1. spēlētājs
    private fun playerOneWins() {
        AlertDialog.Builder(this)
            .setTitle("Spēle beidzās")
            .setMessage("$playerOneName, Tu UZVARĒJI! 🎉")
            .setPositiveButton("Spēlēt atkal?") { _, _ -> resetGame() }
            .setNegativeButton("Uz izvēlni") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    // ja uzvar 2. spēlētājs
    private fun playerTwoWins() {
        val winner = if (isPvCMode) "Dators" else playerTwoName
        AlertDialog.Builder(this)
            .setTitle("Spēle beidzās")
            .setMessage("$winner uzvarēja...")
            .setPositiveButton("Spēlēt atkal?") { _, _ -> resetGame() }
            .setNegativeButton("Uz izvēlni") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    // ja ir neizšķirts
    private fun draw() {
        AlertDialog.Builder(this)
            .setTitle("Spēle beidzās")
            .setMessage("Neizšķirts! 🤝")
            .setPositiveButton("Spēlēt atkal?") { _, _ -> resetGame() }
            .setNegativeButton("Uz izvēlni") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    // notīra visus laukus un norestartē roundCount
    fun resetGame() {
        for (i in 0..2) {
            for (j in 0..2) {
                buttons[i][j].text = ""
            }
        }

        roundCount = 0
        playerOneTurn = true
        updateStatusText()
    }

    private fun computerMove() {
        if (tryToWin()) {
            return
        }

        if (tryToBlock()) {
            return
        }

        if (buttons[1][1].text.toString() == "") {
            buttons[1][1].text = "O"
            afterComputerMove()
            return
        }

        makeRandomMove()
    }

    // ja ir iespēja liek O, lai uzvarētu
    private fun tryToWin(): Boolean {
        for (i in 0..2) {
            for (j in 0..2) {
                if (buttons[i][j].text.toString() == "") {
                    buttons[i][j].text = "O"
                    if (checkForWin()) {
                        afterComputerMove()
                        return true
                    }
                    buttons[i][j].text = ""
                }
            }
        }
        return false
    }

    // ja nav iespēja uzvarēt, tad cenšas bloķēt liekot O
    private fun tryToBlock(): Boolean {
        for (i in 0..2) {
            for (j in 0..2) {
                if (buttons[i][j].text.toString() == "") {
                    buttons[i][j].text = "X"
                    if (checkForWin()) {
                        buttons[i][j].text = "O"
                        afterComputerMove()
                        return true
                    }
                    buttons[i][j].text = ""
                }
            }
        }
        return false
    }

    // citādi izpilda nejaušu gājienu
    private fun makeRandomMove() {
        val random = Random()
        var row: Int
        var col: Int

        do {
            row = random.nextInt(3)
            col = random.nextInt(3)
        } while (buttons[row][col].text.toString() != "")

        buttons[row][col].text = "O"
        afterComputerMove()
    }

    // saprot spēles stāvokli, attiecīgi izdara vienu no 3 darbībām
    private fun afterComputerMove() {
        roundCount++

        if (checkForWin()) {
            playerTwoWins()
        } else if (roundCount == 9) {
            draw()
        } else {
            playerOneTurn = true
            updateStatusText()
        }
    }
}