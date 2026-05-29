package com.example.finalproj

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DriversAdapter(private var drivers: List<Driver>) : RecyclerView.Adapter<DriversAdapter.DriverViewHolder>() {

    class DriverViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvDriverName)
        val tvLicense: TextView = view.findViewById(R.id.tvLicenseNumber)
        val tvID: TextView = view.findViewById(R.id.tvDriverID)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DriverViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_driver, parent, false)
        return DriverViewHolder(view)
    }

    override fun onBindViewHolder(holder: DriverViewHolder, position: Int) {
        val driver = drivers[position]
        holder.tvName.text = "${driver.firstname ?: ""} ${driver.lastname ?: ""}"
        holder.tvLicense.text = driver.drivinglicensenumber ?: "---"
        holder.tvID.text = driver.idNumber ?: "---"
    }

    override fun getItemCount() = drivers.size

    fun updateData(newDrivers: List<Driver>) {
        drivers = newDrivers
        notifyDataSetChanged()
    }
}