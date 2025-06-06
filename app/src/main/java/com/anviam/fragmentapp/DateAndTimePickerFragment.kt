package com.anviam.fragmentapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DateAndTimePickerFragment : Fragment(), TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener {

    private var btnTime: AppCompatButton ?= null
    private var btnDate: AppCompatButton ?= null
    private var inDate: AppCompatTextView ?= null
    private var inTime :AppCompatTextView ?= null
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_date_and_time_picker, container, false)

        btnTime = view.findViewById(R.id.btn_time)
        btnDate = view.findViewById(R.id.btn_date)
        inDate = view.findViewById(R.id.in_date)
        inTime = view.findViewById(R.id.in_time)

        btnTime?.setOnClickListener {
            showTimePicker()
        }

        btnDate?.setOnClickListener {
             showDatePicker()
        }

        return view
    }

    private fun showTimePicker() {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        TimePickerDialog(requireContext(), this, hour, minute, false).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
        calendar.set(Calendar.MINUTE, minute)
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        inTime?.text = timeFormat.format(calendar.time).toString()
    }

    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(requireContext(),  R.style.SpinnerDatePickerDialogTheme,this, year, month, day)

        // To set the minimum date of Calender
        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

   override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int){
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        inDate?.text = dateFormat.format(calendar.time).toString()

   }
}
