package com.example.newcalendar

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.newcalendar.databinding.DeleteDialogBinding
import com.shashank.sony.fancytoastlib.FancyToast
import io.github.muddz.styleabletoast.StyleableToast
import kotlinx.android.synthetic.main.schedule_item.*
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject

class DeleteDialogFragment()  : DialogFragment(), View.OnClickListener{

    private var serialNum = 0
    private var alarmCode : Int = 0
    private lateinit var binding : DeleteDialogBinding
    private val alarmFunctions by lazy { AlarmFunctions(requireContext()) }
    private var job : Job? = null
    private val viewModel : ViewModel by inject()

    constructor(serialNum: Int, code: Int) : this() {
        this.serialNum = serialNum
        this.alarmCode = code
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       binding = DeleteDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.deleteOkBtn.setOnClickListener(this)
        binding.modifyBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val id = v?.id
        if (id == R.id.deleteOkBtn){ // 삭제
            job = lifecycleScope.launch {
                withContext(Dispatchers.IO){
                    viewModel.deleteSchedule(serialNum)
                    viewModel.deleteAlarm(alarmCode)
                }
            }
            alarmFunctions.cancelAlarm(viewModel, alarmCode)
            context?.let { StyleableToast.makeText(it, "삭제", R.style.deleteToast).show() }
            this.dismiss()
        }
        if (id == R.id.modifyBtn){ // 변경 다이얼로그 추가
            val dialog = ModifyDialogFragment(serialNum)
            activity?.let {
                dialog.show(it.supportFragmentManager, "ShowListFragment")
            }
            this.dismiss()
        }
    }

    override fun onStop() {
        super.onStop()
        job?.cancel()
    }
}