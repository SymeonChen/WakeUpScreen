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
import com.blankj.utilcode.util.LanguageUtils
import com.symeonchen.uicomponent.views.SCSettingItem
import com.symeonchen.wakeupscreen.R
import com.symeonchen.wakeupscreen.ScBaseFragment
import com.symeonchen.wakeupscreen.data.CurrentMode
import com.symeonchen.wakeupscreen.data.LanguageInfo
import com.symeonchen.wakeupscreen.data.ScConstant
import com.symeonchen.wakeupscreen.model.SettingViewModel
import com.symeonchen.wakeupscreen.model.ViewModelInjection
import com.symeonchen.wakeupscreen.utils.AppInfoUtils
import kotlinx.android.synthetic.main.fragment_layout_setting.*
import java.util.*

/**
 * Created by SymeonChen on 2019-10-27.
 */
class ScSettingFragment : ScBaseFragment() {

    private var alertDialog: AlertDialog? = null
    private lateinit var settingModel: SettingViewModel

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
    }

    private fun setListener() {
        item_setting_language.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                initLanguageSettingDialog()
            }
        }

        item_setting_wake_screen_time.listener = object : SCSettingItem.OnItemClickListener {
            override fun onItemCLick() {
                context?.let { mContext -> WakeUptimeSettingActivity.actionStart(mContext) }
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
            context?.let { mContext -> AdvanceSettingPageActivity.actionStart(mContext) }
        }

        item_setting_about_this.setOnClickListener {
            context?.let { mContext -> AboutThisPageActivity.actionStart(mContext) }
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
            if (it == CurrentMode.MODE_WHITE_LIST) {
                item_setting_white_list_entry.visibility = View.VISIBLE
                item_setting_black_list_entry.visibility = View.GONE
            } else if (it == CurrentMode.MODE_BLACK_LIST) {
                item_setting_white_list_entry.visibility = View.GONE
                item_setting_black_list_entry.visibility = View.VISIBLE
            } else {
                item_setting_white_list_entry.visibility = View.GONE
                item_setting_black_list_entry.visibility = View.GONE
            }
        })


        settingModel.languageSelected.observe(viewLifecycleOwner, Observer {
            item_setting_language.bindData(
                null,
                when (it) {
                    LanguageInfo.CHINESE_SIMPLE -> "简体中文"
                    LanguageInfo.ENGLISH -> "English"
                    LanguageInfo.ITALIAN -> "Italiano"
                    else -> "跟随系统(Follow System)"
                }
            )
        })

    }

    private fun initLanguageSettingDialog() {
        alertDialog?.dismiss()
        val builder = AlertDialog.Builder(context!!)
        // TODO: take the strings that are in the res folder to create the array
        val secList =
            arrayOf("跟随系统(Follow System)", "English", "简体中文", "Italiano")
        var switch = settingModel.languageSelected.value!!.ordinal
        val checkedItem: Int = LanguageInfo.getModeFromValue(switch).ordinal

        alertDialog = builder.setSingleChoiceItems(
            secList, checkedItem
        ) { _, which -> switch = LanguageInfo.getModeFromValue(which).ordinal }
            .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                if (switch == LanguageInfo.FOLLOW_SYSTEM.ordinal) {
                    settingModel.languageSelected.postValue(LanguageInfo.FOLLOW_SYSTEM)
                    LanguageUtils.applySystemLanguage("")
                } else if (switch == LanguageInfo.ENGLISH.ordinal) {
                    settingModel.languageSelected.postValue(LanguageInfo.ENGLISH)
                    LanguageUtils.applyLanguage(Locale.US, "")
                } else if (switch == LanguageInfo.ITALIAN.ordinal) {
                    settingModel.languageSelected.postValue(LanguageInfo.ITALIAN)
                    LanguageUtils.applyLanguage(Locale.ITALY, "")
                } else {
                    settingModel.languageSelected.postValue(LanguageInfo.CHINESE_SIMPLE)
                    LanguageUtils.applyLanguage(Locale.CHINA, "")
                }
            }
            .create().apply { show() }

    }


    private fun initCurrentModeDialog() {
        alertDialog?.dismiss()
        val builder = AlertDialog.Builder(context!!)
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