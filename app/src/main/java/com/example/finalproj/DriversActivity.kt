package com.example.finalproj

import android.util.Log
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DriversActivity : AppCompatActivity() {

    private val TAG = "DriversActivity"
    private lateinit var rvDrivers: RecyclerView
    private lateinit var pbLoading: ProgressBar
    private lateinit var adapter: DriversAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drivers)

        rvDrivers = findViewById(R.id.rvDrivers)
        pbLoading = findViewById(R.id.pbLoading)

        rvDrivers.layoutManager = LinearLayoutManager(this)
        adapter = DriversAdapter(emptyList())
        rvDrivers.adapter = adapter

        fetchDrivers()
    }

    private fun fetchDrivers() {
        val prefs = getSharedPreferences("FleetManager", MODE_PRIVATE)
        val token = prefs.getString("token", "") ?: ""
        val authHeader = if (token.isNotEmpty()) "Bearer $token" else ""

        pbLoading.visibility = View.VISIBLE
        Log.d(TAG, "Fetching drivers with header: $authHeader")
        RetrofitClient.instance.getDrivers(authHeader).enqueue(object : Callback<List<Driver>> {
            override fun onResponse(call: Call<List<Driver>>, response: Response<List<Driver>>) {
                pbLoading.visibility = View.GONE
                Log.d(TAG, "Response code: ${response.code()}")
                if (response.isSuccessful) {
                    val drivers = response.body() ?: emptyList()
                    Log.d(TAG, "Fetched ${drivers.size} drivers: $drivers")
                    if (drivers.isEmpty()) {
                        Toast.makeText(this@DriversActivity, "לא נמצאו נהגים", Toast.LENGTH_SHORT).show()
                    }
                    adapter.updateData(drivers)
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "Failed to load drivers. Error: $errorBody")
                    Toast.makeText(this@DriversActivity, "שגיאה בטעינת נתונים: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Driver>>, t: Throwable) {
                pbLoading.visibility = View.GONE
                Log.e(TAG, "Network error fetching drivers", t)
                Toast.makeText(this@DriversActivity, "שגיאת רשת: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}