package com.maherhanna.cheeta

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    lateinit var btn_start: Button
    var rb_white: RadioButton? = null
    lateinit var rb_black: RadioButton
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        setContentView(R.layout.activity_main)
        btn_start = findViewById(R.id.btn_start) as Button
        rb_white = findViewById(R.id.rb_white)
        rb_black = findViewById(R.id.rb_black)
        btn_start.setOnClickListener(View.OnClickListener {
            val gameIntent = Intent(this@MainActivity, GameActivity::class.java)
            var humanPiecesColor = Piece.WHITE
            if (rb_black.isChecked()) humanPiecesColor = Piece.BLACK
            gameIntent.putExtra("humanPiecesColor", humanPiecesColor)
            startActivity(gameIntent)
        })
    }
}