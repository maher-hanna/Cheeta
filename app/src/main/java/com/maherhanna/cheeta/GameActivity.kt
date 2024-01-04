package com.maherhanna.cheeta

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.maherhanna.cheeta.core.Piece

class GameActivity : AppCompatActivity() {
    var drawing: Drawing? = null
    var game: Game? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_game)
        val humanPlayerColor = intent.getIntExtra("humanPiecesColor", Piece.WHITE)
        drawing = Drawing(this)
        game = Game(drawing!!, humanPlayerColor)
        drawing!!.game = game
    }

    override fun onResume() {
        super.onResume()
        if (game!!.paused) {
            game!!.paused = false
            game!!.resume()
            return
        }

        //delay drawing of board because at the start
        //of activity the chessboardView is not active yet
        Handler().postDelayed({
            drawing!!.clearBoard()
            drawing!!.drawAllPieces()
            drawing!!.show()
        }, 100)
        game!!.start()
    }

    override fun onPause() {
        super.onPause()
        game!!.paused = true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishGame(getString(R.string.finish_game_message))
    }

    private fun finishGame(alertMessage: String) {
        val dialogClickListner = DialogInterface.OnClickListener { dialogInterface, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> finish()
                DialogInterface.BUTTON_NEGATIVE -> {}
            }
        }
        val builder = AlertDialog.Builder(this@GameActivity)
        builder.setMessage(alertMessage)
            .setPositiveButton(R.string.finish_game_yes, dialogClickListner)
            .setNegativeButton(R.string.finish_game_no, dialogClickListner).show()
    }
}