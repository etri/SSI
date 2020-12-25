package com.bd.ssishop.market.register

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.PermissionChecker.PERMISSION_DENIED
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import com.bd.ssishop.R
import com.bd.ssishop.SsiShopApplication
import com.bd.ssishop.api.ApiGenerator
import com.bd.ssishop.api.ApiResponse
import com.bd.ssishop.api.SsiApi
import com.bd.ssishop.data.Issuer
import com.bd.ssishop.data.SpnrUsedListData
import com.bd.ssishop.market.BuyListVO
import com.bd.ssishop.market.MarketActivity
import com.bd.ssishop.market.product.Product
import com.bd.ssishop.service.SsiHelper
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.aprilapps.easyphotopicker.EasyImage
import pl.aprilapps.easyphotopicker.MediaFile
import pl.aprilapps.easyphotopicker.MediaSource
import java.io.File
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * 상품 등록화면 프래그먼트
 */
class RegisterFragment : Fragment(), EasyImage.Callbacks {

    private val registerViewModel: RegisterViewModel by activityViewModels()

    lateinit var paymentDialog: AlertDialog
    lateinit var file: File

    lateinit var easyImage: EasyImage

    lateinit var p:Product

    var spinnerPosition:Int = 0

    var usedProductId:Int = -1

    var imgUrlOfNew:String = ""
    var imgUrlOfUsed:String = ""

    var savedLocalImg:String = ""

    companion object {
        //image pick code
        private val IMAGE_PICK_CODE = 1000;
        //Permission code
        private val PERMISSION_CODE = 1001;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_register, container, false)
        var spnrUsedList:ArrayList<SpnrUsedListData> = ArrayList<SpnrUsedListData>()
        var spnrNewList:ArrayList<SpnrUsedListData> = ArrayList<SpnrUsedListData>()
        var spinnerPosition:Int = -9
        val buyList = MutableLiveData<List<BuyListVO>>()

        //set title
//        (activity as MarketActivity).img_icon_toolbar.setVisibility(View.GONE)
        (activity as MarketActivity).toolbar_title.setText(R.string.menu_register)
        (activity as MarketActivity).toolbar_title.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            18.toFloat()
        )

        //set easy image
        easyImage = EasyImage.Builder(requireActivity())
            .setCopyImagesToPublicGalleryFolder(false)
            .setFolderName("easyImage SSI")
            .allowMultiple(false)
            .build()

//        root.radio_new.isChecked = false
//        root.radio_old.isChecked = false

        root.txtvw_select_used.visibility = View.GONE
        root.spnr_select_used.visibility = View.GONE
        root.spnr_select_new.visibility = View.GONE

        var type = when(root.radio_product_type.checkedRadioButtonId){
            R.id.radio_new -> root.radio_new.text.toString()
            R.id.radio_old -> root.radio_old.text.toString()
            else -> "새제품"
        }

        root.radio_new.setOnClickListener{
            val spnrArr = ArrayList<String>()
            val spnr = root.findViewById<Spinner>(R.id.spnr_select_new)

            GlobalScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.IO) {
                    val res: ApiResponse<List<Product>> = SsiApi.instance.getInitProductList(
                        SsiShopApplication.token
                    )
                    var products:List<Product> = ArrayList()

                    withContext(Dispatchers.Main) {
                        if (res.code == 1000) {
                            products = res.data!!
                            if (products.size < 1) {
                                spnrArr.add("초기 데이터가 없습니다.")
                                spnr?.adapter = ArrayAdapter<String>(requireContext(), R.layout.support_simple_spinner_dropdown_item, spnrArr)
                                Toast.makeText(requireContext(), "초기 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
                                btn_register.isEnabled = false
                                btn_register.setBackgroundColor(Color.GRAY)
                            } else {
                                var spnrNewData: SpnrUsedListData
                                var loop: Int = 0
                                products.forEach {
                                    val product = Product(it.productId.toInt())
                                    product.productName = it.productName
                                    product.description = it.description
                                    product.price = it.price.toLong()
                                    product.type = it.type
                                    product.did = it.did
                                    product.address = it.address
                                    product.createDate = it.createDate
                                    product.createUser = it.createUser
                                    product.usedProductId = it.productId.toInt()
                                    product.img = it.img
                                    spnrNewData = SpnrUsedListData(loop, it.productId, it.productName, it.price.toInt(), it.description, it.img, product)
                                    spnrNewList.add(spnrNewData)
                                    spnrArr.add(loop, it.productName)
                                    loop++
                                }
                                spnr.adapter = ArrayAdapter<String>(requireContext(), R.layout.support_simple_spinner_dropdown_item, spnrArr)
                            }
                        } else {
                            Log.d("### 구매내역 없음.", "${SsiShopApplication.user.did}")
                        }
                    }
                }
            }

            txtvw_select_used.visibility = View.VISIBLE
            spnr_select_used.visibility = View.GONE
            spnr_select_new.visibility = View.VISIBLE

            if( btn_register.isEnabled == false ){
                btn_register.isEnabled = true;
                btn_register.setBackgroundColor(R.drawable.btn_custom)
            }
        }

        root.radio_old.setOnClickListener{
            val spnrArr = ArrayList<String>()
            val spnr = root.findViewById<Spinner>(R.id.spnr_select_used)

            GlobalScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.IO) {
                    val res: ApiResponse<List<BuyListVO>> = SsiApi.instance.getBuyListBuyOk(SsiShopApplication.token, SsiShopApplication.user.did)

                    withContext(Dispatchers.Main) {
                        if (res.code == 1000) {
                            buyList.value = res.data
                            if (buyList.value!!.size < 1) {
                                spnrArr.add("구매내역이 없습니다.")
                                spnr?.adapter = ArrayAdapter<String>(requireContext(), R.layout.support_simple_spinner_dropdown_item, spnrArr)
                                Toast.makeText(requireContext(), "구매내역이 없어 중고품을 등록할 수 없습니다.", Toast.LENGTH_SHORT).show()
                                btn_register.isEnabled = false
                                btn_register.setBackgroundColor(Color.GRAY)
                            } else {
                                var spnrUsedData: SpnrUsedListData
                                var loop: Int = 0
                                buyList.value?.forEach {
                                    val product = Product(it.productId.toInt())
                                    product.productName = it.productName
                                    product.description = it.description
                                    product.price = it.price.toLong()
                                    product.type = it.type
                                    product.did = it.did
                                    product.address = it.address
                                    product.createDate = it.createDate
                                    product.createUser = it.createUser
                                    product.usedProductId = it.productId.toInt()
                                    product.img = it.img
                                    spnrUsedData = SpnrUsedListData(loop, it.productId.toInt(), it.productName, it.price, it.description, it.img, product)
                                    spnrUsedList.add(spnrUsedData)
                                    spnrArr.add(loop, it.productName)
                                    loop++
                                }
                                spnr.adapter = ArrayAdapter<String>(requireContext(), R.layout.support_simple_spinner_dropdown_item, spnrArr)
                            }
                        } else {
                            Log.d("### 구매내역 없음.", "${SsiShopApplication.user.did}")
                        }
                    }
                }
            }

            spnr_select_new.visibility = View.GONE
            txtvw_select_used.visibility = View.VISIBLE
            spnr_select_used.visibility = View.VISIBLE
        }

        root.spnr_select_new.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val item = parent.getItemAtPosition(position).toString()
                if( !item.contains("초기 데이터가 없습")) {
                    imgUrlOfNew = ApiGenerator.IMAGE_URL + spnrNewList.get(position).img
                    Picasso.get().load(imgUrlOfNew).into(root.img_camera_frame)
                    if(spnrNewList.get(position).img == null){
                        file = File("")
                    }else{
                        file = File(spnrNewList.get(position).img)
                    }
                    root.edit_product_name.setText(spnrNewList.get(position).productName)
                    root.edit_price.setText(spnrNewList.get(position).price.toString())
                    root.edit_description.setText(spnrNewList.get(position).description)
                }else{
                    root.edit_product_name.setText("")
                    root.edit_price.setText("")
                    root.edit_description.setText("")
                }
                spinnerPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        root.spnr_select_used.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val item = parent.getItemAtPosition(position).toString()
                if( !item.contains("구매내역이 없습")) {
                    imgUrlOfUsed = ApiGenerator.IMAGE_URL + spnrUsedList.get(position).img
                    Picasso.get().load(imgUrlOfUsed).into(root.img_camera_frame)
                    if(spnrUsedList.get(position).img == null) {
                        file = File("")
                    }else{
                        file = File(spnrUsedList.get(position).img)
                    }
                    root.edit_product_name.setText(spnrUsedList.get(position).productName)
                    root.edit_price.setText(spnrUsedList.get(position).price.toString())
                    root.edit_description.setText(spnrUsedList.get(position).description)
                }else{
                    root.edit_product_name.setText("")
                    root.edit_price.setText("")
                    root.edit_description.setText("")
                }
                spinnerPosition = position
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        root.btn_test.setOnClickListener {
            type = when(radio_product_type.checkedRadioButtonId) {
                R.id.radio_new -> radio_new.text.toString()
                else -> radio_old.text.toString()
            }

            if( type == "중고품" ){
                edit_product_name.setText("중고품-테스트용")
                edit_description.setText("중고품-테스트")
            }else {
                edit_product_name.setText("테스트용")
                edit_description.setText("테스트")
            }
            edit_price.setText("25,000")
            text_did.hint = "DID 선택해주세요"
        }

        //가격. 통화 포맷
        root.edit_price.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length ?: 0 > 0) {
                    var typedPrice: String = s.toString().replace(",", "")
                    val decimalFormat = DecimalFormat("###,###")
                    root.edit_price.removeTextChangedListener(this)
                    root.edit_price.setText(decimalFormat.format(typedPrice.toString().toInt()))
                    root.edit_price.setSelection(root.edit_price.length())
                    root.edit_price.addTextChangedListener(this)
                }
            }
        })

        root.btn_did.setOnClickListener{
            val paramMap:MutableMap<String, String> = mutableMapOf()
            SsiHelper.requestDID(this, "ssi://requestdid")
        }

        root.btn_register.setOnClickListener {
            if( radio_new.isChecked == false && radio_old.isChecked == false ){
                Toast.makeText(activity, "새제품 혹은 중고품을 선택해 주시기 바랍니다.", Toast.LENGTH_SHORT).show()
            }else if( !this::file.isInitialized ) {
//                if(radio_new.isChecked == true && imgUrlOfNew == null){
//                    Log.d("### New imgUrl", imgUrlOfNew)
//                    img_camera_frame.requestFocus()
//                    Toast.makeText(activity, "상품 이미지를 선택해 주시기 바랍니다.", Toast.LENGTH_SHORT).show()
//                }
//                if(radio_old.isChecked == true && imgUrlOfUsed == null){
//                    Log.d("### Used imgUrl", imgUrlOfUsed)
//                    img_camera_frame.requestFocus()
//                    Toast.makeText(activity, "상품 이미지를 선택해 주시기 바랍니다.", Toast.LENGTH_SHORT).show()
//                }
                img_camera_frame.requestFocus()
                Toast.makeText(activity, "상품 이미지를 선택해 주시기 바랍니다.", Toast.LENGTH_SHORT).show()
            }else if(edit_product_name.text.length < 1) {
                Toast.makeText(activity, "상품명을 입력해주세요", Toast.LENGTH_SHORT).show()
            }else if(edit_price.text.length < 1) {
                Toast.makeText(activity, "가격을 입력해주세요", Toast.LENGTH_SHORT).show()
            }else if(text_did.text.length < 1) {
                Toast.makeText(activity, "DID를 선택해주세요", Toast.LENGTH_SHORT).show()
            }else if(edit_description.text.length < 1){
                Toast.makeText(activity, "상품설명을 입력해주세요", Toast.LENGTH_SHORT).show()
            }else{
                p = Product(0).apply {
                    type = when (radio_product_type.checkedRadioButtonId) {
                        R.id.radio_new -> radio_new.text.toString()
                        R.id.radio_old -> radio_old.text.toString()
                        else -> "새제품"
                    }
                    productName = edit_product_name.text.toString()
                    if( edit_price.length() > 1 ) {
                        val editPrice: String = edit_price.text.toString().replace(",", "")
                        price = editPrice.toLong()
                    }else{
                        edit_price.setText("0")
                        price = 0
                    }
                    did = SsiShopApplication.user.did
                    didSelected = text_did.text.toString()
                    description = edit_description.text.toString()
                }

                val radioType = when (radio_product_type.checkedRadioButtonId) {
                    R.id.radio_new -> radio_new.text.toString()
                    R.id.radio_old -> radio_old.text.toString()
                    else -> "새제품"
                }

                if (radioType == "새제품") {
                    p.type = radioType
                    p.img = spnrNewList.get(spinnerPosition).img.toString()
                    paymentDialog = ProgressDialog.show(activity, null, "상품 등록중", true, true)
                    if( !this::file.isInitialized ){
                        file = File("")
                    }
                    registerViewModel.add(p, file) {
                        paymentDialog.hide()
                        findNavController().navigate(R.id.action_nav_product_detail_to_nav_product_purchase)
                    }
                } else if (radioType == "중고품") {
                    p.type = radioType
                    if( spinnerPosition == -9 ){
                        Toast.makeText(requireContext(), "등록할 중고품을 선택해 주시기 바랍니다.", Toast.LENGTH_SHORT).show()
                    }else {
                        //paymentDialog = ProgressDialog.show(activity, null, "상품 등록중", true, true)
                        val valueMap: MutableMap<String, String> = mutableMapOf()
                        valueMap.put("USER_DID", p.did.toString())
                        valueMap.put("didSelected", p.didSelected.toString())
                        valueMap.put("ISSUER_DID", Issuer.ISSUER_DID)
                        valueMap.put("productName", p.productName)
                        valueMap.put("price", p.price.toString())
                        valueMap.put("description", p.description)
                        valueMap.put("type", p.type)
                        valueMap.put("img", spnrUsedList.get(spinnerPosition).img.toString())
                        valueMap.put("usedProductId", spnrUsedList.get(spinnerPosition).productId.toString())
                        usedProductId = spnrUsedList.get(spinnerPosition).productId
                        p.img = spnrUsedList.get(spinnerPosition).img.toString()

                        if( !this::file.isInitialized ){
                            file = File("")
                        }

                        //VP요청
                        SsiHelper.getPurchaseVPForUsed(this@RegisterFragment, valueMap)
                    }
                }
            }
        }

        root.img_camera_frame.setOnClickListener {
            //check runtime permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PERMISSION_DENIED){
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE);
                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_CODE);
                } else {
                    //permission already granted
                    pickImageFromGallery();
                }
            } else {
                //system OS is < Marshmallow
                pickImageFromGallery();
            }
        }

        return root
    }

    private fun pickImageFromGallery() {
        //Intent to pick image
//        val intent = Intent(Intent.ACTION_PICK)
//        intent.type = "image/*"
//        startActivityForResult(intent, IMAGE_PICK_CODE)
        easyImage.openGallery(this);
    }

    //handle requested permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode){
            PERMISSION_CODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission from popup granted
                    pickImageFromGallery()
                } else {
                    //permission from popup denied
                    Toast.makeText(requireActivity(), "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    //handle result of picked image
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if( resultCode == Activity.RESULT_OK ){
            //DID선택. SSI APP으로부터 리턴 받음.
            if(requestCode == SsiHelper.REQUEST_DID){
                text_did.text = data!!.getStringExtra("did")
            }
            //중고품 등록하기 버튼 클릭 후 SSI APP으로 부터 리턴 받음.
            if(requestCode == SsiHelper.VP_USED_REGI){
                if( resultCode != Activity.RESULT_OK ){
                    paymentDialog.hide()
                    Toast.makeText(activity, "등록실패", Toast.LENGTH_SHORT).show()
                }else{
                    //SSI APP에서 정상적으로 VP 수령.
                    val rcvVp: String = data!!.getStringExtra("vp")
//                    val sendVpSplit = rcvVp.split(".")
//                    val presentationDec = String(Base64.getDecoder().decode(sendVpSplit[1]))
                    val sendVpMap: Map<*, *> = Gson().fromJson(rcvVp, MutableMap::class.java)
                    val sendVp: String = sendVpMap.get("Presentations").toString()
                    var result: String = ""

                    GlobalScope.launch(Dispatchers.Main) {
                        withContext(Dispatchers.IO) {
                            val map: MutableMap<String, String> = mutableMapOf()
                            map.put("vp", sendVp)
//                            map.put("vp", rcvVp)
                            map.put("uuid", SsiShopApplication.uuid)
                            /**
                             * 현재는(2020.10.06) SSI APP에서 받은 VP를 검증하고 같은 VP를 PG에 verifyvp로 넘기고 있음.
                             * 향후 credential에 따라 vc들이 정해지면, 이들 vc를 metadium sdk에 넣어 vp 생성하여 map.put("vp", vp)로 활용.
                             */
                            val res: ApiResponse<String> = SsiApi.instance.doVerifyingVpForUsedRegi(SsiShopApplication.token, map)

                            if(res.code == 9999) {
                                //VP 검증 실패
                                result = "false"
                            }else{
                                //VP 검증 성공, PG 요청 진행.
                                p.usedProductId = usedProductId
                                if( !file.exists() ){
                                    file = File("")
                                }
                                registerViewModel.addUsed(p, file) {
//                                        paymentDialog.hide()
                                    findNavController().navigate(R.id.action_nav_product_detail_to_nav_product_purchase)
                                }
                            }
                        }
                    }
                }
            }else{
                easyImage.handleActivityResult(requestCode, resultCode, data, requireActivity(), this)
            }
        }
    }

    override fun onCanceled(source: MediaSource) {
    }

    override fun onImagePickerError(error: Throwable, source: MediaSource) {
    }

    override fun onMediaFilesPicked(imageFiles: Array<MediaFile>, source: MediaSource) {
        file = imageFiles[0].file
        img_camera_frame.setImageURI(Uri.fromFile(file))
    }
}