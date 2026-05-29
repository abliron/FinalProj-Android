package com.example.finalproj

import android.util.Log
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SingInPage : AppCompatActivity() {
    private val TAG = "SingInPage"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sing_in_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etUsername = findViewById<EditText>(R.id.etSignInUsername)
        val etPassword = findViewById<EditText>(R.id.etSignInPassword)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)

        btnSignIn.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = User(username, password)
            signInUser(user)
        }
    }

    private fun signInUser(user: User) {
        Log.d(TAG, "Attempting to sign in user: ${user.username}")
        RetrofitClient.instance.signIn(user).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                Log.d(TAG, "Response code: ${response.code()}")
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d(TAG, "Response body: $body")
                    
                    if (body?.requiresCompanySelection == true) {
                        Log.d(TAG, "Company selection required")
                        val intent = Intent(this@SingInPage, SelectCompanyActivity::class.java)
                        intent.putExtra("selectionToken", body.selectionToken)
                        startActivity(intent)
                        finish()
                        return
                    }

                    val token = body?.token
                    if (token != null) {
                        Log.d(TAG, "Token received: $token")
                        val prefs = getSharedPreferences("FleetManager", MODE_PRIVATE)
                        prefs.edit().putString("token", token).apply()

                        Toast.makeText(this@SingInPage, "Login successful!", Toast.LENGTH_LONG).show()
                        
                        val intent = Intent(this@SingInPage, MenuActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.e(TAG, "Response successful but no token and no selection required. Body: $body")
                        Toast.makeText(this@SingInPage, "שגיאה: לא התקבל טוקן מהשרת", Toast.LENGTH_LONG).show()
                    }
                }
else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Login failed. Code: ${response.code()}, Error: $errorBody")
                    Toast.makeText(this@SingInPage, "Login failed: $errorBody", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e(TAG, "Network error: ${t.message}", t)
                Toast.makeText(this@SingInPage, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
