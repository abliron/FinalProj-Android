package com.example.finalproj

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody
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
        enableEdgeToEdge()
        setContentView(R.layout.activity_select_company)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        selectionToken = intent.getStringExtra("selectionToken")
        Log.d(TAG, "Received selectionToken: $selectionToken")
        
        val passedCompanies = intent.getSerializableExtra("companiesList") as? List<Company>
        
        spinner = findViewById<Spinner>(R.id.spinnerCompanies)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmCompany)

        if (!passedCompanies.isNullOrEmpty()) {
            Log.d(TAG, "Using companies list passed from intent")
            companies = passedCompanies
            displayCompanies()
        } else {
            fetchCompanies()
        }

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
        
        Log.d(TAG, "Fetching companies with header (token length: ${rawToken.length})")
        
        RetrofitClient.instance.getCompanies(authHeader).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d(TAG, "Fetch companies response code: ${response.code()}")
                if (response.isSuccessful) {
                    val rawJson = response.body()?.string() ?: ""
                    Log.d(TAG, "RAW JSON RESPONSE: $rawJson")
                    
                    try {
                        val gson = Gson()
                        // ננסה לפענח קודם כרשימה פשוטה
                        val listType = object : TypeToken<List<Company>>() {}.type
                        var fetchedCompanies: List<Company>? = null
                        
                        if (rawJson.trim().startsWith("[")) {
                            fetchedCompanies = gson.fromJson(rawJson, listType)
                        } else if (rawJson.trim().startsWith("{")) {
                            // אולי זה אובייקט שמכיל את הרשימה (למשל {"companies": [...]})
                            val mapType = object : TypeToken<Map<String, Any>>() {}.type
                            val map: Map<String, Any> = gson.fromJson(rawJson, mapType)
                            Log.d(TAG, "JSON is an object. Keys: ${map.keys}")
                            
                            // ננסה לחפש מפתח שנראה כמו רשימת חברות
                            val companiesJson = gson.toJson(map["companies"] ?: map["data"] ?: map["userCompanies"])
                            fetchedCompanies = gson.fromJson(companiesJson, listType)
                        }

                        companies = fetchedCompanies ?: emptyList()
                        Log.d(TAG, "Parsed ${companies.size} companies")

                        displayCompanies()
                        
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing JSON: ${e.message}", e)
                        Toast.makeText(this@SelectCompanyActivity, "שגיאה בפענוח נתונים", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Failed to load companies. Code: ${response.code()}, Error: $errorBody")
                    Toast.makeText(this@SelectCompanyActivity, "שגיאה בטעינת חברות: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(TAG, "Error fetching companies", t)
                Toast.makeText(this@SelectCompanyActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayCompanies() {
        if (companies.isEmpty()) {
            Toast.makeText(this@SelectCompanyActivity, "רשימת החברות ריקה", Toast.LENGTH_SHORT).show()
        }

        val companyNames = companies.map { it.name ?: "Unknown Company" }
        val adapter = ArrayAdapter(this@SelectCompanyActivity, android.R.layout.simple_spinner_item, companyNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun confirmSelection(companyId: String) {
        val rawToken = selectionToken ?: ""
        val authHeader = if (rawToken.isNotEmpty()) "Bearer $rawToken" else ""
        
        val request = mapOf(
            "companyid" to companyId
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