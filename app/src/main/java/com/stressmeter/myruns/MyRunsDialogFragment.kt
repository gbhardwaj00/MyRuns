package com.stressmeter.myruns

import android.R
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider


class MyRunsDialogFragment : DialogFragment() , DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener, DialogInterface.OnClickListener {


    companion object {
        const val DIALOG_KEY = "dialog"
        const val datepickerDialog = 0
        const val timepickerDialog = 1
        const val durationDialog = 2
        const val distanceDialog = 3
        const val caloriesDialog = 4
        const val heartRateDialog = 5
        const val commentDialog = 6
    }

    private lateinit var manualActivityVM : ManualActivityVM

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        lateinit var ret: Dialog
        lateinit var input: EditText
        manualActivityVM = ViewModelProvider(requireActivity()).get(ManualActivityVM::class.java)

        val bundle = arguments
        when (bundle?.getInt(DIALOG_KEY)) {
            datepickerDialog -> {
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val dayOfMonth = c.get(Calendar.DAY_OF_MONTH)
                ret = DatePickerDialog(requireContext(), this, year, month, dayOfMonth)
            }

            timepickerDialog -> {
                val c = Calendar.getInstance()
                val hour = c.get(Calendar.HOUR_OF_DAY)
                val minute = c.get(Calendar.MINUTE)
                ret = TimePickerDialog(requireContext(), this, hour, minute, true)
            }

            durationDialog -> {
                val builder = AlertDialog.Builder(requireActivity())
                input = EditText(requireContext());
                builder.setView(input)
                input.id = R.id.edit
                builder.setTitle("Duration")
                builder.setPositiveButton("OK", this)
                builder.setNegativeButton("CANCEL", this)
                ret = builder.create()
            }

            distanceDialog -> {
                val builder = AlertDialog.Builder(requireActivity())
                input = EditText(requireContext())
                builder.setView(input)
                input.id = R.id.edit
                builder.setTitle("Distance(kms)")
                builder.setPositiveButton("OK", this)
                builder.setNegativeButton("CANCEL", this)
                ret = builder.create()
            }

            caloriesDialog -> {
                val builder = AlertDialog.Builder(requireActivity())
                input = EditText(requireContext());
                input.id = R.id.edit
                builder.setView(input)
                builder.setTitle("Calories")
                builder.setPositiveButton("OK", this)
                builder.setNegativeButton("CANCEL", this)
                ret = builder.create()
            }

            heartRateDialog -> {
                val builder = AlertDialog.Builder(requireActivity())
                input = EditText(requireContext())
                input.id = R.id.edit
                builder.setView(input)
                builder.setTitle("Heart Rate")
                builder.setPositiveButton("OK", this)
                builder.setNegativeButton("CANCEL", this)
                ret = builder.create()
            }

            commentDialog -> {
                val builder = AlertDialog.Builder(requireActivity())
                input = EditText(requireContext())
                input.id = R.id.edit
                builder.setView(input)
                builder.setTitle("Comment")
                builder.setPositiveButton("OK", this)
                builder.setNegativeButton("CANCEL", this)
                ret = builder.create()
            }
        }
        return ret
    }


    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        if (view != null) {
            manualActivityVM.date.value = "$dayOfMonth/${month+1}/$year"
        }
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        if (view != null) {
            manualActivityVM.time.value = "$hourOfDay:$minute"
        }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        val input = (dialog as AlertDialog).findViewById<EditText>(R.id.edit)
        Log.d("XD", "input: $input")
        if (which == DialogInterface.BUTTON_POSITIVE) {
            when (arguments?.getInt(DIALOG_KEY)) {
                durationDialog -> {
                    if (input != null && input.text.isNotEmpty()) {
                        if (input.text.toString().toDoubleOrNull() != null) {
                            manualActivityVM.duration.value = input.text.toString().toDouble()
                        }
                    }
                }
                distanceDialog -> {
                    if (input != null && input.text.isNotEmpty()) {
                        if (input.text.toString().toDoubleOrNull() != null) {
                            manualActivityVM.distance.value = input.text.toString().toDouble()
                        }
                    }
                }
                caloriesDialog -> {
                     if (input != null && input.text.isNotEmpty()) {
                         //consider case when it is not a number
                         if (input.text.toString().toDoubleOrNull() != null) {
                             manualActivityVM.calories.value = input.text.toString().toDouble()
                         }

                    }
                }
                heartRateDialog -> {
                    if (input != null && input.text.isNotEmpty()) {
                        if (input.text.toString().toDoubleOrNull() != null) {
                            manualActivityVM.heartRate.value = input.text.toString().toDouble()
                        }
                    }
                }
                commentDialog -> {
                    if (input != null) {
                        manualActivityVM.comment.value = input.text.toString()
                    }
                }
            }
        }
        else if (which == DialogInterface.BUTTON_NEGATIVE) {
            dialog.cancel()
        }
    }
}
