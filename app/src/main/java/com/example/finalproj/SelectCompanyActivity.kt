package com.example.finalproj

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SelectCompanyActivity : AppCompatActivity() {

    private val TAG = "SelectCompanyActivity"
    private var selectionToken: String? = null
    private var companies: List<Company> = emptyList()
    private lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_company)

        selectionToken = intent.getStringExtra("selectionToken")
        Log.d(TAG, "Received selectionToken: $selectionToken")
        
        spinner = findViewById<Spinner>(R.id.spinnerCompanies)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmCompany)

        fetchCompanies()

        btnConfirm.setOnClickListener {
            if (companies.isNotEmpty()) {
                val selectedIndex = spinner.selectedItemPosition
                val selectedCompany = companies[selectedIndex]
                val selectedCompanyId = selectedCompany.id
                
                if (selectedCompanyId != null) {
                    Log.d(TAG, "Confirming selection for company ID: $selectedCompanyId")
                    confirmSelection(selectedCompanyId)
                } else {
                    Toast.makeText(this, "שגיאה: מזהה חברה חסר", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "אין חברות זמינות", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchCompanies() {
        val rawToken = selectionToken ?: ""
        val authHeader = if (rawToken.isNotEmpty()) "Bearer $rawToken" else ""
        
        Log.d(TAG, "Fetching companies with header: $authHeader")
        
        RetrofitClient.instance.getCompanies(authHeader).enqueue(object : Callback<List<Company>> {
            override fun onResponse(call: Call<List<Company>>, response: Response<List<Company>>) {
                Log.d(TAG, "Fetch companies response code: ${response.code()}")
                if (response.isSuccessful) {
                    companies = response.body() ?: emptyList()
                    Log.d(TAG, "Fetched ${companies.size} companies: $companies")
                    
                    if (companies.isEmpty()) {
                        Toast.makeText(this@SelectCompanyActivity, "רשימת החברות ריקה", Toast.LENGTH_SHORT).show()
                    }

                    val companyNames = companies.map { it.name ?: "Unknown Company" }
                    val adapter = ArrayAdapter(this@SelectCompanyActivity, android.R.layout.simple_spinner_item, companyNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Failed to load companies. Code: ${response.code()}, Error: $errorBody")
                    Toast.makeText(this@SelectCompanyActivity, "שגיאה בטעינת חברות: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Company>>, t: Throwable) {
                Log.e(TAG, "Error fetching companies", t)
                Toast.makeText(this@SelectCompanyActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun confirmSelection(companyId: String) {
        val rawToken = selectionToken ?: ""
        val authHeader = if (rawToken.isNotEmpty()) "Bearer $rawToken" else ""
        
        val request = mapOf(
            "companyId" to companyId
        )

        RetrofitClient.instance.selectCompany(authHeader, request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val token = response.body()?.token
                    if (token != null) {
                        val prefs = getSharedPreferences("FleetManager", MODE_PRIVATE)
                        prefs.edit().putString("token", token).apply()
                        
                        startActivity(Intent(this@SelectCompanyActivity, MenuActivity::class.java))
                        finish()
                    }
                } else {
                    Log.e(TAG, "Selection failed: ${response.errorBody()?.string()}")
                    Toast.makeText(this@SelectCompanyActivity, "בחירת חברה נכשלה", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@SelectCompanyActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}