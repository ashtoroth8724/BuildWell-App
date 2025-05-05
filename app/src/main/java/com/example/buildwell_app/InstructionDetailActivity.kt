package com.example.buildwell_app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text

class InstructionDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instruction_detail) // Make sure this matches your layout file name

        val instructionsTextView: TextView = findViewById(R.id.instructionsTextView) // Replace with the actual ID of your TextView

        // Get the instructions from the intent
        val instructions = intent.getStringExtra("instructions")

        // Display the instructions
        instructionsTextView.text = instructions
    }
}