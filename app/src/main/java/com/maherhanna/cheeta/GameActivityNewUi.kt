package com.maherhanna.cheeta

import android.content.DialogInterface
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.compose.material3.MaterialTheme
import com.maherhanna.cheeta.core.Piece

class GameActivityNewUi : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val humanPlayerColor = intent.getIntExtra("humanPiecesColor", Piece.WHITE)
        setContent{
            MaterialTheme{
                GameScreen(playerColor = humanPlayerColor)
            }
        }
    }

    private fun finishGame(alertMessage: String) {
        val dialogClickListner = DialogInterface.OnClickListener { dialogInterface, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> finish()
                DialogInterface.BUTTON_NEGATIVE -> {}
            }
        }
        val builder = AlertDialog.Builder(this@GameActivityNewUi)
        builder.setMessage(alertMessage)
            .setPositiveButton(R.string.finish_game_yes, dialogClickListner)
            .setNegativeButton(R.string.finish_game_no, dialogClickListner).show()
    }
}