package com.stressmeter.myruns

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment

class FragmentStart : Fragment() {
    private lateinit var inputType : Spinner
    private lateinit var activityType : Spinner
    private lateinit var startButton : Button

    private var selectedInputType : Int = 0
    private var selectedActivityType : Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_start, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inputType = view.findViewById(R.id.inputTypeSpinner)
        activityType = view.findViewById(R.id.activityTypeSpinner)
        startButton = view.findViewById(R.id.startBtn)

        ArrayAdapter.createFromResource(view.context, R.array.inputTypeSpinnerEntries, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                inputType.adapter = adapter
            }

        ArrayAdapter.createFromResource(view.context, R.array.activityTypeSpinnerEntries, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                activityType.adapter = adapter
            }

        inputType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (view != null) {
                    selectedInputType = position
                    Log.d("xd", "selectedInputType: $selectedInputType")
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedInputType = 0
                Log.d("xd", "selectedInputType: $selectedInputType")
            }
        }

        activityType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (view != null) {
                    selectedActivityType = position
                    Log.d("xd", "selectedActivityType: $selectedActivityType")
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedActivityType = 0
                Log.d("xd", "selectedActivityType: $selectedActivityType")
            }
        }

        startButton.setOnClickListener{
            val intent : Intent = if(selectedInputType == 0) {
                Intent(activity, ManualActivity::class.java)
            } else {
                Intent(activity, MapActivity::class.java)
            }
            intent.putExtra("activityType", selectedActivityType)
            intent.putExtra("inputType", selectedInputType)
            startActivity(intent)
        }

    }
}
