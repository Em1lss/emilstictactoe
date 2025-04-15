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

    private lateinit var buttons: Array<Array<Button>>
    private lateinit var playerOneName: String
    private lateinit var playerTwoName: String
    private var isPvCMode: Boolean = false
    private var playerOneTurn: Boolean = true // X always goes first
    private var roundCount: Int = 0
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Get player names and game mode from intent
        playerOneName = intent.getStringExtra("PLAYER_ONE_NAME") ?: "Player 1"
        playerTwoName = intent.getStringExtra("PLAYER_TWO_NAME") ?: "Player 2"
        isPvCMode = intent.getBooleanExtra("IS_PVC_MODE", false)

        statusText = findViewById(R.id.status_text)
        updateStatusText()

        // Initialize buttons array
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

                        // If it's computer's turn in PvC mode
                        if (isPvCMode && !playerOneTurn) {
                            // Delay computer move slightly for better UX
                            Handler(Looper.getMainLooper()).postDelayed({
                                computerMove()
                            }, 500)
                        }
                    }
                }
                button
            }
        }

        // Show custom greeting
        showGreetingMessage()

        // Setup reset button
        findViewById<Button>(R.id.reset_button).setOnClickListener {
            resetGame()
        }
    }

    private fun showGreetingMessage() {
        val greeting = buildString {
            append("Welcome to Tic-Tac-Toe!\n\n")

            if (isPvCMode) {
                append("$playerOneName, get ready to challenge the computer!")
            } else {
                append("$playerOneName vs $playerTwoName\nMay the best player win!")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Game Started")
            .setMessage(greeting)
            .setPositiveButton("Let's Play!", null)
            .show()
    }

    private fun updateStatusText() {
        statusText.text = if (playerOneTurn) {
            "$playerOneName's turn (X)"
        } else {
            "${if (isPvCMode) "Computer" else playerTwoName}'s turn (O)"
        }
    }

    private fun checkForWin(): Boolean {
        val field = Array(3) { row ->
            Array(3) { col ->
                buttons[row][col].text.toString()
            }
        }

        // Check rows
        for (i in 0..2) {
            if (field[i][0] == field[i][1] && field[i][0] == field[i][2] && field[i][0] != "") {
                return true
            }
        }

        // Check columns
        for (i in 0..2) {
            if (field[0][i] == field[1][i] && field[0][i] == field[2][i] && field[0][i] != "") {
                return true
            }
        }

        // Check diagonal (top-left to bottom-right)
        if (field[0][0] == field[1][1] && field[0][0] == field[2][2] && field[0][0] != "") {
            return true
        }

        // Check diagonal (top-right to bottom-left)
        if (field[0][2] == field[1][1] && field[0][2] == field[2][0] && field[0][2] != "") {
            return true
        }

        return false
    }

    private fun playerOneWins() {
        AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("$playerOneName Wins! 🎉")
            .setPositiveButton("Play Again") { _, _ -> resetGame() }
            .setNegativeButton("Return to Menu") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun playerTwoWins() {
        val winner = if (isPvCMode) "Computer" else playerTwoName
        AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("$winner Wins! 🎉")
            .setPositiveButton("Play Again") { _, _ -> resetGame() }
            .setNegativeButton("Return to Menu") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun draw() {
        AlertDialog.Builder(this)
            .setTitle("Game Over")
            .setMessage("It's a Draw! 🤝")
            .setPositiveButton("Play Again") { _, _ -> resetGame() }
            .setNegativeButton("Return to Menu") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    fun resetGame() {
        // Clear the board
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
        // Check if there's a winning move
        if (tryToWin()) {
            return
        }

        // Block player's winning move
        if (tryToBlock()) {
            return
        }

        // Try to take center if available
        if (buttons[1][1].text.toString() == "") {
            buttons[1][1].text = "O"
            afterComputerMove()
            return
        }

        // Make a random move
        makeRandomMove()
    }

    private fun tryToWin(): Boolean {
        // Check if computer can win in one move
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

    private fun tryToBlock(): Boolean {
        // Check if player can win in one move and block
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