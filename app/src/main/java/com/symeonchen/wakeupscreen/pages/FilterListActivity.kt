package com.symeonchen.wakeupscreen.pages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.symeonchen.uicomponent.views.UiTools
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.data.AppInfo
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.states.AppListState
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.FilterListUtils
import kotlinx.android.synthetic.main.activity_app_filter_list.*
import kotlinx.android.synthetic.main.activity_debug_page.iv_back
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by SymeonChen on 2019-10-27.
 */
class FilterListActivity : ScBaseActivity() {

    private var visibleList: MutableList<AppInfo> = arrayListOf()
    private var dataList: MutableList<AppInfo> = arrayListOf()
    private var adapter: WhiteListViewAdapter? = null
    private var map: HashMap<String, Int>? = null
    private var currentModeValue = CurrentMode.MODE_WHITE_LIST.ordinal
    private val textWatcher: TextWatcher by lazy {
        object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (dataList.isEmpty()) {
                    return
                }
                val keyStr = p0.toString()
                val result = if (keyStr.isNotEmpty()) {
                    FilterListUtils.splitWithSelected(dataList.filter {
                        it.simpleName.contains(
                            keyStr,
                            true
                        )
                    }, map)

                } else {
                    FilterListUtils.splitWithSelected(dataList, map)
                }

                visibleList.clear()
                visibleList.addAll(result)
                updateView()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_filter_list)
        initView()
        setListener()
        getData()
    }

    companion object {
        private const val CURRENT_MODE = "currentMode"

        fun actionStartWithMode(context: Context?, currentMode: CurrentMode) {
            context ?: return
            val intent = Intent(context, FilterListActivity::class.java)
            intent.putExtra(CURRENT_MODE, currentMode.ordinal)
            context.startActivity(intent)
        }
    }


    private fun initView() {

        UiTools.instance.showLoading(this, view_loading)

        currentModeValue = intent.getIntExtra(CURRENT_MODE, CurrentMode.MODE_WHITE_LIST.ordinal)
        map = if (currentModeValue == CurrentMode.MODE_BLACK_LIST.ordinal) {
            FilterListUtils.getMapFromString(DataInjection.appBlackListStringOfNotify)
        } else {
            FilterListUtils.getMapFromString(DataInjection.appWhiteListStringOfNotify)
        }

        tv_title.text = if (currentModeValue == CurrentMode.MODE_BLACK_LIST.ordinal) {
            resources.getString(R.string.black_list)
        } else {
            resources.getString(R.string.white_list)
        }

        adapter = WhiteListViewAdapter(rv_app_list, visibleList)
        rv_app_list.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rv_app_list.adapter = adapter

        val hints = if (currentModeValue == CurrentMode.MODE_BLACK_LIST.ordinal) {
            resources.getString(R.string.black_list_hints)
        } else {
            resources.getString(R.string.white_list_hints)
        }
        tv_log_head_hint.text = hints
    }

    private fun setListener() {
        iv_back.setOnClickListener { finish() }

        tv_save.setOnClickListener {
            val result = HashMap<String, Int>()
            for (item in dataList) {
                if (item.selected) {
                    result[item.packageName] = 1
                }
            }
            if (currentModeValue == CurrentMode.MODE_BLACK_LIST.ordinal) {
                DataInjection.appBlackListStringOfNotify = FilterListUtils.saveMapToString(result)
            } else {
                DataInjection.appWhiteListStringOfNotify = FilterListUtils.saveMapToString(result)
            }
            DataInjection.appListUpdateFlag = System.currentTimeMillis()
            ToastUtils.showLong(resources.getString(R.string.saved_successfully))
            finish()
        }

        et_search_filter.addTextChangedListener(textWatcher)

    }

    override fun onDestroy() {
        try {
            et_search_filter?.removeTextChangedListener(textWatcher)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    private fun getData() {
        lifecycleScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                val appList = AppListState.getInstalledAppList(this@FilterListActivity, true)
                dataList = FilterListUtils.splitWithSelected(appList, map)
                visibleList.clear()
                visibleList.addAll(dataList)
            }
            updateView()
        }
    }

    private fun updateView() {
        UiTools.instance.hideLoading()
        adapter?.dataList?.clear()
        adapter?.dataList?.addAll(visibleList)
        adapter?.notifyDataSetChanged()
    }


    class WhiteListViewAdapter(recyclerView: RecyclerView, dataList: List<AppInfo>?) :
        RecyclerView.Adapter<WhiteListViewAdapter.WhiteListHolder>() {

        private var mContext: Context? = null
        var dataList: MutableList<AppInfo> = arrayListOf()

        init {
            mContext = recyclerView.context
            this.dataList.clear()
            this.dataList.addAll(dataList ?: arrayListOf())
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhiteListHolder {
            val v = LayoutInflater.from(mContext).inflate(R.layout.item_white_list, parent, false)
            return WhiteListHolder(v)
        }

        override fun getItemCount(): Int {
            return this.dataList.size
        }

        override fun onBindViewHolder(holder: WhiteListHolder, position: Int) {
            holder.tvAppSimpleName?.text = this.dataList[position].simpleName
            holder.tvAppPackageName?.text = this.dataList[position].packageName
            holder.cbAppSelect?.isChecked = this.dataList[position].selected
            try {
                Glide.with(mContext!!)
                    .load(dataList[position].iconDrawable)
                    .into(holder.ivIcon!!)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            holder.itemView.setOnClickListener {
                this.dataList[position].selected = !this.dataList[position].selected
                notifyItemChanged(position, this.dataList[position])
            }
            holder.cbAppSelect?.setOnClickListener {
                this.dataList[position].selected = !this.dataList[position].selected
                notifyItemChanged(position, this.dataList[position])
            }
        }

        class WhiteListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var ivIcon: ImageView? = null
            var tvAppSimpleName: TextView? = null
            var tvAppPackageName: TextView? = null
            var cbAppSelect: CheckBox? = null

            init {
                ivIcon = itemView.findViewById(R.id.iv_app_icon)
                tvAppSimpleName = itemView.findViewById(R.id.tv_app_simple_name)
                tvAppPackageName = itemView.findViewById(R.id.tv_app_package_name)
                cbAppSelect = itemView.findViewById(R.id.cb_app_select)
            }
        }

    }
}
