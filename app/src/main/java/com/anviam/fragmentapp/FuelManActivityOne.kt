package com.anviam.fragmentapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class FuelManActivityOne : AppCompatActivity() {

    private var deliveryTimeSpinner : Spinner?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fuel_man_one)
        deliveryTimeSpinner = findViewById(R.id.spinnerDeliveryTime)
        Spinner()
    }

    private fun Spinner(){
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.delivery_times,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        deliveryTimeSpinner?.adapter = adapter
    }
}