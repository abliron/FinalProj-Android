package com.example.finalproj

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VehiclesActivity : AppCompatActivity() {

    private val TAG = "VehiclesActivity"
    private lateinit var rvVehicles: RecyclerView
    private lateinit var pbLoading: ProgressBar
    private lateinit var adapter: VehiclesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vehicles)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvVehicles = findViewById(R.id.rvVehicles)
        pbLoading = findViewById(R.id.pbVehiclesLoading)

        rvVehicles.layoutManager = LinearLayoutManager(this)
        adapter = VehiclesAdapter(emptyList())
        rvVehicles.adapter = adapter

        fetchVehicles()
    }

    private fun fetchVehicles() {
        val prefs = getSharedPreferences("FleetManager", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        val authHeader = if (token.isNotEmpty()) "Bearer $token" else ""

        Log.d(TAG, "Fetching vehicles with header: $authHeader")
        pbLoading.visibility = View.VISIBLE
        
        RetrofitClient.instance.getVehicles(authHeader).enqueue(object : Callback<List<Vehicle>> {
            override fun onResponse(call: Call<List<Vehicle>>, response: Response<List<Vehicle>>) {
                pbLoading.visibility = View.GONE
                Log.d(TAG, "Response code: ${response.code()}")
                if (response.isSuccessful) {
                    val vehicles = response.body() ?: emptyList()
                    Log.d(TAG, "Fetched ${vehicles.size} vehicles")
                    if (vehicles.isEmpty()) {
                        Toast.makeText(this@VehiclesActivity, "לא נמצאו רכבים", Toast.LENGTH_SHORT).show()
                    }
                    adapter.updateData(vehicles)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Failed to load vehicles. Error: $errorBody")
                    Toast.makeText(this@VehiclesActivity, "שגיאה בטעינת נתונים: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Vehicle>>, t: Throwable) {
                pbLoading.visibility = View.GONE
                Log.e(TAG, "Network error fetching vehicles", t)
                Toast.makeText(this@VehiclesActivity, "שגיאת רשת: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
