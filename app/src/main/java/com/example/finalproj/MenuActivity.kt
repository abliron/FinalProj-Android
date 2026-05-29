package com.example.finalproj

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_menu)
        
        findViewById<android.view.View>(R.id.main_menu_container)?.let { view ->
            ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        val btnVehicles = findViewById<Button>(R.id.btnVehiclesList)
        val btnDrivers = findViewById<Button>(R.id.btnDriversList)
        val btnForm = findViewById<Button>(R.id.btnPickupReturnForm)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        btnVehicles.setOnClickListener {
            val intent = Intent(this, VehiclesActivity::class.java)
            startActivity(intent)
        }

        btnDrivers.setOnClickListener {
            val intent = Intent(this, DriversActivity::class.java)
            startActivity(intent)
        }

        btnForm.setOnClickListener {
            val intent = Intent(this, HandoverActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            // Clear the token
            val prefs = getSharedPreferences("FleetManager", MODE_PRIVATE)
            prefs.edit().remove("token").apply()

            // Go back to Sign In
            val intent = Intent(this, SingInPage::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}