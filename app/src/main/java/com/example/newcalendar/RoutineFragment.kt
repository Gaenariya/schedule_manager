package com.example.newcalendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.newcalendar.databinding.FragmentRoutineBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.collections.ArrayList
import android.graphics.Color
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class RoutineFragment : Fragment(), View.OnClickListener {

    private lateinit var binding : FragmentRoutineBinding
    private lateinit var todayDate : String
    private lateinit var selectedDate: String
    private val dateSaveModule : DateSaveModule by inject()
    private val viewModel : ViewModel by inject()
    //private val scope : CoroutineScope by lazy { CoroutineScope(Dispatchers.Main) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRoutineBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.calendarView.selectedDate = CalendarDay.today()
        binding.calendarView.addDecorators(SaturdayDecorator(), SundayDecorator())

        val context = requireContext()
        val adapter = ScheduleAdapter(context, viewModel)
        //binding.scheduleListview.layoutManager= LinearLayoutManager(context)

        var year = binding.calendarView.selectedDate!!.year
        var month = binding.calendarView.selectedDate!!.month + 1
        var day = binding.calendarView.selectedDate!!.day
        selectedDate = "$year-$month-$day"
        Log.e(TAG, selectedDate)
        lifecycleScope.launch {
            dateSaveModule.setDate(selectedDate)
        }

        adapter.itemClick = object : ScheduleAdapter.ItemClick{
            override fun onClick(view: View, position: Int, list: ArrayList<Schedule>) {
                val serialNum = list[position].serialNum
                val alarmCode = list[position].alarm_code
                val date = list[position].date
                val dialog = DeleteDialogFragment(serialNum,alarmCode, date, list.size)
                activity?.let {
                    dialog.show(it.supportFragmentManager, "ShowListFragment")
                }
            }
        }

        binding.addScheduleBtn.setOnClickListener(this)
        binding.openScheduleBtn.setOnClickListener(this)

        binding.calendarView.setOnDateChangedListener { widget, date, selected ->
            year = binding.calendarView.selectedDate!!.year
            month = binding.calendarView.selectedDate!!.month + 1
            day = binding.calendarView.selectedDate!!.day
            val selectedDate = "$year-$month-$day"
            lifecycleScope.launch {
                dateSaveModule.setDate(selectedDate)
            }

            viewModel.getAllSchedule().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                binding.noItemText.visibility = View.VISIBLE
                adapter.removeAll()
                for(i in it.indices){
                    if (it[i].date == selectedDate){
                        binding.noItemText.visibility = View.GONE
                        val data = Schedule(
                            it[i].serialNum,
                            it[i].date,
                            it[i].content,
                            it[i].alarm,
                            it[i].alarm_code,
                            it[i].importance)
                        adapter.addItems(data)
                    }
                }
                binding.scheduleListview.adapter = adapter
                binding.scheduleListview.layoutManager= LinearLayoutManager(context)
            })
            Log.e(TAG, selectedDate)
        }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.addScheduleBtn -> {
                val dialog = AddDialogFragment()
                dialog.show(parentFragmentManager, "AddScheduleDialog")
            }
            R.id.openScheduleBtn -> {
                val dialog = ShowListFragment()
                dialog.show(parentFragmentManager, "ShowListFragment")
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.e(TAG, "onStart()")
        // 일정이 저장된 특정 날짜들을 가져와서 리스트에 넣기
        viewModel.getAllDates().observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            for (i in it.indices){
                val eventDate = it[i].date.split("-")
                val year = Integer.parseInt(eventDate[0])
                val month = Integer.parseInt(eventDate[1])
                val day = Integer.parseInt(eventDate[2])
                binding
                    .calendarView
                    .addDecorator(
                        EventDecorator(
                            Color.parseColor("#BE89E3"),
                            Collections.singleton(CalendarDay.from(year, month-1, day))))
            }
        })

    }

    companion object{
        const val TAG = "RoutineFragment"
    }
}