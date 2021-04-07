package com.symeonchen.wakeupscreen.pages

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.symeonchen.uicomponent.views.SCSettingItem
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseFragment
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.data.LanguageInfo
import com.symeonchen.wakeupscreen.data.ScConstant
import com.symeonchen.wakeupscreen.model.SettingViewModel
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import com.symeonchen.wakeupscreen.utils.AppInfoUtils
import com.symeonchen.wakeupscreen.utils.PlayStoreTools
import com.symeonchen.wakeupscreen.utils.quickStartActivity
import kotlinx.android.synthetic.main.fragment_layout_setting.*

/**
 * Created by SymeonChen on 2019-10-27.
 */
class ScSettingFragment : ScBaseFragment() {

    private var alertDialog: AlertDialog? = null
    private lateinit var settingModel: SettingViewModel
    private var playStoreToolInstance: PlayStoreTools? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_layout_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val settingFactory = ViewModelInjection.provideSettingViewModelFactory()
        settingModel = ViewModelProvider(this, settingFactory).get(SettingViewModel::class.java)
        setListener()
        playStoreToolInstance = PlayStoreTools.newInstance(context)
        playStoreToolInstance?.prepare()
    }

    private fun setListener() {
        item_setting_language.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                initLanguageSettingDialog()
            }
        }

        item_setting_wake_screen_time.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                context?.quickStartActivity<WakeUptimeSettingActivity>()
            }
        }

        item_setting_address.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse("https://github.com/SymeonChen/WakeUpScreen")
                startActivity(i)
            }
        }

        item_setting_question.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                var mailBody = ScConstant.DEFAULT_MAIL_BODY
                var mailTitle = ScConstant.DEFAULT_MAIL_HEAD
                try {
                    mailBody = AppInfoUtils.getDeviceInfo(context)
                    mailTitle = resources.getString(R.string.mail_title)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "plain/text"
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(ScConstant.AUTHOR_MAIL))
                intent.putExtra(Intent.EXTRA_SUBJECT, mailTitle)
                intent.putExtra(Intent.EXTRA_TEXT, mailBody)
                startActivity(Intent.createChooser(intent, "Choose your mail app"))
            }
        }

        item_setting_current_mode.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                initCurrentModeDialog()
            }
        }

        item_setting_white_list_entry.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                FilterListActivity.actionStartWithMode(context, CurrentMode.MODE_WHITE_LIST)
            }
        }

        item_setting_black_list_entry.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                FilterListActivity.actionStartWithMode(context, CurrentMode.MODE_BLACK_LIST)
            }
        }

        item_setting_advanced_setting.setOnClickListener {
            context?.run { this.quickStartActivity<AdvanceSettingPageActivity>() }
        }

        item_setting_about_this.setOnClickListener {
            context?.run { this.quickStartActivity<AboutThisPageActivity>() }
        }

        item_setting_give_star.setOnClickListener {
            playStoreToolInstance?.openPlayStore(activity)
        }

        settingModel.modeOfCurrent.observe(viewLifecycleOwner, Observer {
            item_setting_current_mode.bindData(
                null,
                resources.getString(
                    when (it) {
                        CurrentMode.MODE_BLACK_LIST -> R.string.black_list
                        CurrentMode.MODE_WHITE_LIST -> R.string.white_list
                        CurrentMode.MODE_ALL_NOTIFY -> R.string.all_pass
                        else -> R.string.all_pass
                    }
                )
            )
            when (it) {
                CurrentMode.MODE_WHITE_LIST -> {
                    item_setting_white_list_entry.visibility = View.VISIBLE
                    item_setting_black_list_entry.visibility = View.GONE
                }
                CurrentMode.MODE_BLACK_LIST -> {
                    item_setting_white_list_entry.visibility = View.GONE
                    item_setting_black_list_entry.visibility = View.VISIBLE
                }
                else -> {
                    item_setting_white_list_entry.visibility = View.GONE
                    item_setting_black_list_entry.visibility = View.GONE
                }
            }
        })

        settingModel.languageSelected.observe(viewLifecycleOwner, Observer {
            item_setting_language.bindData(
                null,
                it.desc
            )
        })

    }

    private fun initLanguageSettingDialog() {
        alertDialog?.dismiss()
        val builder = AlertDialog.Builder(requireContext())
        val languageArray = LanguageInfo.values()
        val languageNameArray = languageArray.map { it.desc }.toTypedArray()
        val refNum = settingModel.languageSelected.value!!.referenceNum
        val checkedItem: Int = languageArray.map { it.referenceNum }.indexOf(refNum)
        var selectedItem: LanguageInfo? = null

        alertDialog = builder.setSingleChoiceItems(
            languageNameArray, checkedItem
        ) { _, which -> selectedItem = languageArray.get(which) }
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                selectedItem?.run {
                    settingModel.languageSelected.postValue(this)
                    this.applyLanguage()
                }
            }
            .create().apply { show() }

    }


    private fun initCurrentModeDialog() {
        alertDialog?.dismiss()
        val builder = AlertDialog.Builder(requireContext())
        val secList = arrayOf(
            resources.getString(R.string.all_pass),
            resources.getString(R.string.white_list),
            resources.getString(R.string.black_list)
        )
        var switch = settingModel.modeOfCurrent.value!!
        val checkedItem: Int = when (switch) {
            CurrentMode.MODE_ALL_NOTIFY -> 0
            CurrentMode.MODE_WHITE_LIST -> 1
            else -> 2
        }

        alertDialog = builder
            .setSingleChoiceItems(
                secList, checkedItem
            ) { _, which ->
                switch =
                    when (which) {
                        0 -> CurrentMode.MODE_ALL_NOTIFY
                        1 -> CurrentMode.MODE_WHITE_LIST
                        else -> CurrentMode.MODE_BLACK_LIST
                    }
            }
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                settingModel.modeOfCurrent.postValue(
                    CurrentMode.getModeFromValue(
                        when (switch) {
                            CurrentMode.MODE_ALL_NOTIFY -> 0
                            CurrentMode.MODE_WHITE_LIST -> 1
                            else -> 2
                        }
                    )
                )
            }
            .create().apply { show() }
    }


}