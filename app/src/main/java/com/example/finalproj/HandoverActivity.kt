package com.example.finalproj

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class HandoverActivity : AppCompatActivity() {

    private val TAG = "HandoverActivity"
    
    private lateinit var spinnerVehicles: Spinner
    private lateinit var spinnerDrivers: Spinner
    private lateinit var etOdometer: EditText
    private lateinit var etFuelLevel: EditText
    private lateinit var etNotes: EditText
    private lateinit var btnSubmit: Button
    private lateinit var pbLoading: ProgressBar

    private var vehiclesList: List<Vehicle> = emptyList()
    private var driversList: List<Driver> = emptyList()

    private val photoBitmaps = mutableMapOf<Int, Bitmap>()

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            val viewId = currentPhotoViewId
            if (imageBitmap != null && viewId != -1) {
                photoBitmaps[viewId] = imageBitmap
                findViewById<ImageButton>(viewId).setImageBitmap(imageBitmap)
            }
        }
    }

    private var currentPhotoViewId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_handover)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        spinnerVehicles = findViewById(R.id.spinnerVehicles)
        spinnerDrivers = findViewById(R.id.spinnerDrivers)
        etOdometer = findViewById(R.id.etOdometer)
        etFuelLevel = findViewById(R.id.etFuelLevel)
        etNotes = findViewById(R.id.etNotes)
        btnSubmit = findViewById(R.id.btnSubmitForm)
        pbLoading = findViewById(R.id.pbFormLoading)

        setupPhotoButtons()
        loadInitialData()

        btnSubmit.setOnClickListener {
            validateAndSubmit()
        }
    }

    private fun setupPhotoButtons() {
        val buttons = listOf(R.id.btnPhotoFront, R.id.btnPhotoRear, R.id.btnPhotoLeft, R.id.btnPhotoRight)
        buttons.forEach { id ->
            findViewById<ImageButton>(id).setOnClickListener {
                currentPhotoViewId = id
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePhotoLauncher.launch(takePictureIntent)
            }
        }
    }

    private fun loadInitialData() {
        val prefs = getSharedPreferences("FleetManager", MODE_PRIVATE)
        val token = "Bearer ${prefs.getString("token", "")}"

        RetrofitClient.instance.getVehicles(token).enqueue(object : Callback<List<Vehicle>> {
            override fun onResponse(call: Call<List<Vehicle>>, response: Response<List<Vehicle>>) {
                if (response.isSuccessful) {
                    vehiclesList = response.body() ?: emptyList()
                    val adapter = ArrayAdapter(this@HandoverActivity, android.R.layout.simple_spinner_item, vehiclesList.map { "${it.licensePlate} - ${it.make}" })
                    spinnerVehicles.adapter = adapter
                }
            }
            override fun onFailure(call: Call<List<Vehicle>>, t: Throwable) {}
        })

        RetrofitClient.instance.getDrivers(token).enqueue(object : Callback<List<Driver>> {
            override fun onResponse(call: Call<List<Driver>>, response: Response<List<Driver>>) {
                if (response.isSuccessful) {
                    driversList = response.body() ?: emptyList()
                    val adapter = ArrayAdapter(this@HandoverActivity, android.R.layout.simple_spinner_item, driversList.map { "${it.firstname} ${it.lastname}" })
                    spinnerDrivers.adapter = adapter
                }
            }
            override fun onFailure(call: Call<List<Driver>>, t: Throwable) {}
        })
    }

    private fun validateAndSubmit() {
        val selectedVehicle = vehiclesList.getOrNull(spinnerVehicles.selectedItemPosition)
        val selectedDriver = driversList.getOrNull(spinnerDrivers.selectedItemPosition)
        val odometer = etOdometer.text.toString()
        
        if (selectedVehicle == null || selectedDriver == null || odometer.isEmpty()) {
            Toast.makeText(this, "אנא מלא את כל שדות החובה (רכב, נהג וקילומטראז')", Toast.LENGTH_LONG).show()
            return
        }

        if (photoBitmaps.size < 4) {
            Toast.makeText(this, "חובה לצלם 4 תמונות של הרכב", Toast.LENGTH_LONG).show()
            return
        }

        submitForm(selectedVehicle.vehicleId ?: 0, selectedDriver.driverId ?: 0)
    }

    private fun submitForm(vehicleId: Int, driverId: Int) {
        val prefs = getSharedPreferences("FleetManager", MODE_PRIVATE)
        val token = "Bearer ${prefs.getString("token", "")}"

        pbLoading.visibility = View.VISIBLE
        btnSubmit.isEnabled = false

        val vIdPart = vehicleId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val odometerPart = etOdometer.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val fuelPart = etFuelLevel.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val notesPart = etNotes.text.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        val handoverDatePart = todayDate.toRequestBody("text/plain".toMediaTypeOrNull())

        val frontPart = bitmapToMultipart(photoBitmaps[R.id.btnPhotoFront], "frontPhoto")
        val rearPart = bitmapToMultipart(photoBitmaps[R.id.btnPhotoRear], "rearPhoto")
        val leftPart = bitmapToMultipart(photoBitmaps[R.id.btnPhotoLeft], "leftPhoto")
        val rightPart = bitmapToMultipart(photoBitmaps[R.id.btnPhotoRight], "rightPhoto")

        RetrofitClient.instance.submitHandoverForm(
            token, driverId, vIdPart, odometerPart, fuelPart, notesPart, handoverDatePart,
            frontPart, rearPart, leftPart, rightPart
        ).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                pbLoading.visibility = View.GONE
                if (response.isSuccessful) {
                    Toast.makeText(this@HandoverActivity, "הטופס נשמר בהצלחה!", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                    Toast.makeText(this@HandoverActivity, "שגיאה בשמירת הטופס", Toast.LENGTH_LONG).show()
                    btnSubmit.isEnabled = true
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                pbLoading.visibility = View.GONE
                btnSubmit.isEnabled = true
                Toast.makeText(this@HandoverActivity, "שגיאת רשת: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun bitmapToMultipart(bitmap: Bitmap?, partName: String): MultipartBody.Part? {
        if (bitmap == null) return null
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val byteArray = stream.toByteArray()
        val requestBody = byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(partName, "$partName.jpg", requestBody)
    }
}
