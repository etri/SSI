package com.iitp.iitp_demo.activity.dids

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.iitp.iitp_demo.Constants
import com.iitp.iitp_demo.FinishListener
import com.iitp.iitp_demo.R
import com.iitp.iitp_demo.activity.BaseActivity
import com.iitp.iitp_demo.activity.MainActivity
import com.iitp.iitp_demo.activity.model.BlockChainType
import com.iitp.iitp_demo.activity.model.DidDataVo
import com.iitp.iitp_demo.chain.Icon
import com.iitp.iitp_demo.chain.Metadium
import com.iitp.iitp_demo.chain.indy.Indy
import com.iitp.iitp_demo.databinding.ActivityDidManageBinding
import com.iitp.iitp_demo.util.BusProvider
import com.iitp.iitp_demo.util.PreferenceUtil
import com.iitp.iitp_demo.util.PrintLog
import com.iitp.iitp_demo.util.ViewUtils
import java.util.*

class DidManageActivity : BaseActivity() {
    private var layout: ActivityDidManageBinding? = null
    private var index = 0;
    private var didNick: String? = null
    private var didList = ArrayList<DidDataVo>()
    private var data: DidDataVo? = null
    private var preferenceUtil :PreferenceUtil? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layout = DataBindingUtil.setContentView(this, R.layout.activity_did_manage)
        setActionBarSet(layout!!.toolbar, getString(R.string.did_manage_title), true)
        val intent = intent
        preferenceUtil = PreferenceUtil.getInstance(this)
        init(intent.getStringExtra(Constants.DID_NAME))
    }

    private fun init(did: String) {
        didList = MainActivity.getDidList(this@DidManageActivity)
        var didIndex = 0;
        for(didInfo in didList){
            if( didInfo.did.equals(did) ) {
                index = didIndex
                break
            }
            didIndex++
        }
        data = didList[index]
        didNick = data!!.nickName
        layout!!.changeNickBtn.setOnClickListener {
            val intent = Intent(this@DidManageActivity, DidChangeNameActivity::class.java)
            intent.putExtra(Constants.DID_INDEX, index)
            startActivity(intent)
        }
        layout!!.setDefaultBtn.setOnClickListener { showDialogSetDefault() }
        layout!!.detailBtn.setOnClickListener {
            val intent = Intent(this@DidManageActivity, DidDetailActivity::class.java)
            intent.putExtra(Constants.DID_INDEX, index)
            startActivity(intent)
        }
        layout!!.deleteBtn.setOnClickListener { showDialogDelete() }
    }

    override fun onDestroy() {
        BusProvider.getInstance().post("DidManageActivity")
        super.onDestroy()
        PrintLog.e("onDestroy")
    }

    private fun showDialogSetDefault() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom, null)
        val customDialog = AlertDialog.Builder(this)
                .setView(dialogView)
//                .setMessage(getString(R.string.did_manage_set_default, didNick))
                .setCancelable(false)
                .show()
        val width = ViewUtils.dp2px(this@DidManageActivity, 300f)
        val height = ViewUtils.dp2px(this@DidManageActivity, 252f)
        customDialog.window!!.setLayout(width.toInt(), height.toInt())
        val textview = dialogView.findViewById<TextView>(R.id.messageTextView)
        val title = dialogView.findViewById<TextView>(R.id.titleTextView)
        val btPositive = dialogView.findViewById<Button>(R.id.positive)
        val btNegative = dialogView.findViewById<Button>(R.id.negative)
        textview.text = getString(R.string.did_manage_set_default, didNick)
        title.text = "기본 DID 설정"
        btPositive.setOnClickListener {
            for (i in didList.indices) {
                didList[i].favorite = false
            }
            didList[index].favorite = true
            MainActivity.setDidList(didList, this@DidManageActivity)
            customDialog.dismiss()
            showDialogFinish("DID 설정", "기본 DID 설정이 완료 되었습니다.")
        }
        btNegative.setOnClickListener {
            customDialog.dismiss()
        }
    }


    private fun showDialogDelete() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom, null)
        val customDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .show()
        val width = ViewUtils.dp2px(this@DidManageActivity, 300f)
        val height = ViewUtils.dp2px(this@DidManageActivity, 252f)
        customDialog.window!!.setLayout(width.toInt(), height.toInt())
        val btPositive = dialogView.findViewById<Button>(R.id.positive)
        val btNegative = dialogView.findViewById<Button>(R.id.negative)
        val textview = dialogView.findViewById<TextView>(R.id.messageTextView)
        val title = dialogView.findViewById<TextView>(R.id.titleTextView)
        title.text = "DID 삭제"
        btPositive.text = getString(R.string.ok)
        btNegative.text = getString(R.string.cancel)
        textview.text = getString(R.string.did_manage_delete_did, didNick)
        btPositive.setOnClickListener {
            customDialog.dismiss()

            if (BlockChainType.METADIUM == didList[index].blackChain) {
                runOnUiThread { layout!!.progresslayout.visibility = View.VISIBLE }
                Handler().postDelayed({
                    this.deleteMetadiumDid()
                    showDialogDeleteFinish("DID 삭제", "DID 삭제가 완료되었습니다.")
                }, 500)
            } else if (BlockChainType.ICON == didList[index].blackChain) {
                runOnUiThread { layout!!.progresslayout.visibility = View.VISIBLE }
                Handler().postDelayed({
                    this.deleteIconDid()
                    showDialogDeleteFinish("DID 삭제", "DID 삭제가 완료되었습니다.")
                }, 500)
            } else if (BlockChainType.INDY == didList[index].blackChain) {
                runOnUiThread { layout!!.progresslayout.visibility = View.VISIBLE }
                Handler().postDelayed({
                    this.deleteIndyDid()
                    showDialogDeleteFinish("DID 삭제", "DID 삭제가 완료되었습니다.")
                }, 500)
            }
        }
        btNegative.setOnClickListener {
            customDialog.dismiss()
        }
    }

    private fun showDialogFinish(titleText: String, message: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom, null)
        val customDialog = AlertDialog.Builder(this@DidManageActivity)
                .setView(dialogView)
                .setCancelable(false)
                .show()
        val width = ViewUtils.dp2px(this@DidManageActivity, 300f)
        val height = ViewUtils.dp2px(this@DidManageActivity, 252f)
        customDialog.window?.setLayout(width.toInt(), height.toInt())
        val title = dialogView.findViewById<TextView>(R.id.titleTextView)
        val textview = dialogView.findViewById<TextView>(R.id.messageTextView)
        val btPositive = dialogView.findViewById<Button>(R.id.cancel1)
        val oneBtn = dialogView.findViewById<LinearLayout>(R.id.oneBtn)
        val twoBtn = dialogView.findViewById<LinearLayout>(R.id.twoBtn)
        oneBtn.visibility = View.VISIBLE
        twoBtn.visibility = View.INVISIBLE
        title.text = titleText
        btPositive.setText(R.string.ok)
        textview.text = message
        btPositive.setOnClickListener { v: View? -> customDialog.dismiss() }
        customDialog.show()
    }

    private fun showDialogDeleteFinish(titleText: String, message: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_custom, null)
        val customDialog = AlertDialog.Builder(this@DidManageActivity)
                .setView(dialogView)
                .setCancelable(false)
                .show()
        val width = ViewUtils.dp2px(this@DidManageActivity, 300f)
        val height = ViewUtils.dp2px(this@DidManageActivity, 252f)
        customDialog.window?.setLayout(width.toInt(), height.toInt())
        val title = dialogView.findViewById<TextView>(R.id.titleTextView)
        val textview = dialogView.findViewById<TextView>(R.id.messageTextView)
        val btPositive = dialogView.findViewById<Button>(R.id.cancel1)
        val oneBtn = dialogView.findViewById<LinearLayout>(R.id.oneBtn)
        val twoBtn = dialogView.findViewById<LinearLayout>(R.id.twoBtn)
        oneBtn.visibility = View.VISIBLE
        twoBtn.visibility = View.INVISIBLE
        title.text = titleText
        btPositive.setText(R.string.ok)
        textview.text = message
        btPositive.setOnClickListener { v: View? ->
                customDialog.dismiss()
                finish()
        }
        customDialog.show()
    }

    private fun deleteMetadiumDid() {
        val metadium = Metadium.getInstance()
        val finish: FinishListener = object : FinishListener {
            override fun finishOK(did: String?) {
                didList.removeAt(index)
                MainActivity.setDidList(didList, this@DidManageActivity)
                runOnUiThread { layout!!.progresslayout.visibility = View.INVISIBLE }
                PrintLog.e("deleteMetadiumDid finish")
//                finish()
            }

            override fun finishError(error: String) {
                runOnUiThread { layout!!.progresslayout.visibility = View.INVISIBLE }
                PrintLog.e("error = $error")
            }
        }
        metadium.deleteMetaDID(this@DidManageActivity, didList[index], finish)

    }

    private fun deleteIconDid() {
        val icon = Icon.getInstance()
        val finish: FinishListener = object : FinishListener {
            override fun finishOK(did: String?) {
                didList.removeAt(index)
                MainActivity.setDidList(didList, this@DidManageActivity)
                PrintLog.e("deleteIconDid finish")
                runOnUiThread { layout!!.progresslayout.visibility = View.INVISIBLE }
//                finish()
            }

            override fun finishError(error: String) {
                PrintLog.e("error = $error")
                runOnUiThread { layout!!.progresslayout.visibility = View.INVISIBLE }
            }
        }
        icon.deleteIconDID(this@DidManageActivity, didList[index], finish)
    }

    private fun deleteIndyDid() {
        val indy = Indy.getInstance(this@DidManageActivity)
        val finish: FinishListener = object : FinishListener {
            override fun finishOK(did: String?) {
                PrintLog.e("index did= "+didList[index])
                PrintLog.e("pref did= "+preferenceUtil?.indyDID)
                if(didList[index].did.contains(preferenceUtil?.indyDID.toString())){
                    preferenceUtil?.removeIndyDID()
                }
                didList.removeAt(index)
                MainActivity.setDidList(didList, this@DidManageActivity)
                PrintLog.e("deleteIndyDid finish")
                runOnUiThread { layout!!.progresslayout.visibility = View.INVISIBLE }
//                finish()
            }

            override fun finishError(error: String) {
                PrintLog.e("error = $error")
                runOnUiThread { layout!!.progresslayout.visibility = View.INVISIBLE }
            }
        }
        indy.deleteIndyDid(didList[index], finish)
    }

}