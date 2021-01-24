package com.symeonchen.wakeupscreen.pages

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.data.NotifyItem
import com.symeonchen.wakeupscreen.model.NotifyItemViewModel
import kotlinx.android.synthetic.main.activity_debug_page.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by SymeonChen on 2019-10-27.
 */
class DebugPageActivity : ScBaseActivity() {

    private var sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private var viewModel: NotifyItemViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug_page)
        initViewModel()
        setListener()
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[NotifyItemViewModel::class.java]
    }

    private fun setListener() {
        viewModel?.allNotifyItem?.observe(this, androidx.lifecycle.Observer {
            updateView(it)
        })
        iv_back.setOnClickListener { finish() }
        tv_clear_all.setOnClickListener {
            viewModel?.clearAll()
        }
    }

    private fun updateView(it: List<NotifyItem>?) {
        if (it.isNullOrEmpty()) {
            tv_log.text = ""
        } else {
            var str = ""
            for (item in it) {
                val itemStr = "\n ${sdf.format(item.time)} : ${item.appName}"
                str += itemStr
            }
            tv_log.text = str
        }
    }


}
