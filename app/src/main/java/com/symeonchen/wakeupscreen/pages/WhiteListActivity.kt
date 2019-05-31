package com.symeonchen.wakeupscreen.pages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseActivity
import com.symeonchen.wakeupscreen.data.AppInfo
import com.symeonchen.wakeupscreen.states.AppListState
import com.symeonchen.wakeupscreen.utils.DataInjection
import com.symeonchen.wakeupscreen.utils.WhiteListUtils
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_debug_page.iv_back
import kotlinx.android.synthetic.main.activity_white_list.*

class WhiteListActivity : ScBaseActivity() {

    private var dataList: MutableList<AppInfo> = arrayListOf()
    private var adapter: WhiteListViewAdapter? = null
    private var map: HashMap<String, Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_white_list)
        initView()
        setListener()
        getData()
    }

    companion object {
        fun actionStart(context: Context?) {
            context ?: return
            context.startActivity(Intent(context, WhiteListActivity::class.java))
        }
    }


    private fun initView() {
        map = WhiteListUtils.getMapFromString(DataInjection.appListStringOfNotify)
        adapter = WhiteListViewAdapter(rv_app_list, dataList)
        rv_app_list.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        rv_app_list.adapter = adapter
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
            DataInjection.appListStringOfNotify = WhiteListUtils.saveMapToString(result)
            DataInjection.appListUpdateFlag = System.currentTimeMillis()
            finish()
        }
    }

    private fun getData() {
        Observable.create<List<AppInfo>> { e ->
            e.onNext(AppListState.getInstalledAppList(this))
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<List<AppInfo>> {
                override fun onComplete() {
                    updateView()
                }

                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(t: List<AppInfo>) {
                    val resultWithSelected: MutableList<AppInfo> = arrayListOf()
                    val resultWithUnselected: MutableList<AppInfo> = arrayListOf()
                    for (item in t) {
                        if (map?.containsKey(item.packageName) == true) {
                            item.selected = true
                            resultWithSelected.add(item)
                        } else {
                            resultWithUnselected.add(item)
                        }
                    }
                    dataList.clear()
                    dataList.addAll(resultWithSelected.sortedBy {
                        it.simpleName
                    })
                    dataList.addAll(resultWithUnselected.sortedBy {
                        it.simpleName
                    })
                    updateView()
                }

                override fun onError(e: Throwable) {
                }
            })
    }

    private fun updateView() {
        adapter?.dataList?.clear()
        adapter?.dataList?.addAll(dataList)
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
            holder.cbAppSelect?.setOnCheckedChangeListener { _, checked ->
                this.dataList[position].selected = checked
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