package com.example.finalproj

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VehiclesAdapter(private var vehicles: List<Vehicle>) : RecyclerView.Adapter<VehiclesAdapter.VehicleViewHolder>() {

    class VehicleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvModel: TextView = view.findViewById(R.id.tvVehicleModel)
        val tvPlate: TextView = view.findViewById(R.id.tvLicensePlate)
        val tvYear: TextView = view.findViewById(R.id.tvVehicleYear)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehicleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_vehicle, parent, false)
        return VehicleViewHolder(view)
    }

    override fun onBindViewHolder(holder: VehicleViewHolder, position: Int) {
        val vehicle = vehicles[position]
        holder.tvModel.text = "${vehicle.make ?: ""} ${vehicle.model ?: ""}"
        holder.tvPlate.text = formatLicensePlate(vehicle.licensePlate)
        holder.tvYear.text = vehicle.year?.toString() ?: "---"
    }

    private fun formatLicensePlate(plate: String?): String {
        if (plate == null) return "---"
        val clean = plate.replace("-", "").replace(" ", "")
        return when (clean.length) {
            7 -> "${clean.substring(0, 2)}-${clean.substring(2, 5)}-${clean.substring(5, 7)}"
            8 -> "${clean.substring(0, 3)}-${clean.substring(3, 5)}-${clean.substring(5, 8)}"
            else -> plate
        }
    }

    override fun getItemCount() = vehicles.size

    fun updateData(newVehicles: List<Vehicle>) {
        vehicles = newVehicles
        notifyDataSetChanged()
    }
}
