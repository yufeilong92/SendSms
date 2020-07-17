package com.example.sendsms

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.telephony.SmsManager
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.yanzhenjie.permission.runtime.Permission
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.ArrayList


/**
 * @Author : YFL  is Creating a porject in del
 * @Package com.example.sendsms
 * @Email : yufeilong92@163.com
 * @Time :2020/7/16 13:51
 * @Purpose :发送短信
 */
class MainActivity : AppCompatActivity() {
    private var mArray: MutableList<String>? = null
    private var mAdapter: PhoneAdapter? = null
    private val spName = "phones"
    private var mSp: SharedPreferences? = null
    private val REQEUESTCODE = 1001
    private var mShowProgressDialog: MyProgreeDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clearData()
        mShowProgressDialog = MyProgreeDialog(this)
        mSp = getSharedPreferences(spName, 0)
        val phoneList = getPhoneList()
        addData(phoneList)
        initAdapter()
        initListener()
        initViewModel()
    }

    private fun initAdapter() {
        mAdapter = PhoneAdapter(this, mArray!!)
        val gl = GridLayoutManager(this, 1)
        rlv_send_content.layoutManager = gl
        rlv_send_content.adapter = mAdapter
        mAdapter?.setRecyclerListener(object : PhoneAdapter.RecyclerItemListener {
            override fun itemDeleteClickListener(phone: String) {
                val filter = mArray?.filter { it != phone } as MutableList<String>
                clearData()
                addData(filter)
                mAdapter?.notifyDataSetChanged()
            }
        })
    }


    private fun initListener() {
        //添加数据
        btn_add_phone.setOnClickListener {
            val phone = et_input_send.text.trim().toString()
            val phoneNum = isPhoneRight(this, et_input_send)
            if (!phoneNum) {
                return@setOnClickListener
            }
            if (!mArray.isNullOrEmpty()) {
                val any = mArray!!.any { it == phone }
                if (any) {
                    Toast.makeText(this, "重复添加$phone", Toast.LENGTH_SHORT).show();
                    return@setOnClickListener
                }
            }
            addData(mutableListOf(phone))
            mAdapter?.notifyDataSetChanged()
        }
        //导入
        btn_add_dao.setOnClickListener {
            KotlinPermissionUtils.showPermission(
                this, "导入手机号需要请求内存卡权限", arrayOf(
                    Permission.READ_EXTERNAL_STORAGE,
                    Permission.WRITE_EXTERNAL_STORAGE
                )
            ) {

                val intent1 = Intent(Intent.ACTION_GET_CONTENT)
                intent1.setType("*/*")
                intent1.addCategory(Intent.CATEGORY_OPENABLE)
                startActivityForResult(intent1, REQEUESTCODE)
            }
        }
        //发送
        btn_send.setOnClickListener {
            val com = et_input_send_content.text.trim().toString()
            if (TextUtils.isEmpty(com)) {
                Toast.makeText(this, "发送内容为空", Toast.LENGTH_SHORT).show();
                return@setOnClickListener
            }
            if (mArray.isNullOrEmpty()) {
                Toast.makeText(this, "接受人手机号为空", Toast.LENGTH_SHORT).show();
                return@setOnClickListener
            }
            KotlinPermissionUtils.showPermission(
                this,
                "发送短信需要允许该权限",
                arrayOf(Permission.SEND_SMS, Permission.READ_PHONE_STATE)
            ) {
                senMsm(com)
            }
        }
        //全部删除
        btn_delete_all.setOnClickListener {
            if (mArray.isNullOrEmpty()) {
                Toast.makeText(this, "内容为空", Toast.LENGTH_SHORT).show();
                return@setOnClickListener
            }
            clearPhone()
            clearData()
            mAdapter?.notifyDataSetChanged()
            Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
        }
    }

    private fun senMsm(com: String?) {
        mShowProgressDialog?.show()
        val sb = StringBuffer()
        mArray?.forEachIndexed { index, s ->
            if (index == mArray!!.size - 1) {
                sb.append(s)
            } else {
                sb.append(s)
                sb.append(";")
            }
        }
// 长短信发送方式1
        val sendIntent = Intent("SENT_SMS_ACTION");
        val sendPI = PendingIntent.getBroadcast(getApplicationContext(), 0, sendIntent, 0);
        val deliverIntent = Intent("DELIVERED_SMS_ACTION");
        val deliverPI = PendingIntent.getBroadcast(this, 0, deliverIntent, 0);
        val smsManager = SmsManager.getDefault();
        var divideContents = smsManager.divideMessage("$com")
        for (text in divideContents) {
            smsManager.sendTextMessage(sb.toString(), null, text, sendPI, deliverPI);
        }
        Handler().postDelayed({
            if (mShowProgressDialog != null && mShowProgressDialog!!.isShowing) {
                mShowProgressDialog?.dismiss()
            }
            Toast.makeText(this, "发送成功", Toast.LENGTH_SHORT).show();
        }, 2000)
    }

    private fun initViewModel() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }
        var path: String? = null
        if (requestCode == REQEUESTCODE) {
            val rui = data.data
            if ("file".equals(rui!!.scheme)) {
                path = rui.path
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                path = FileUtil.getPath(this, rui)
            } else
                path = FileUtil.getRealPathFromURI(this, rui)
            if (TextUtils.isEmpty(path)) {
                return
            } else {
                val contains = path!!.contains(".txt")
                if (!contains) {
                    Toast.makeText(this, "请选择txt 文件", Toast.LENGTH_SHORT).show();
                    return
                }
                bindViewData(path)
            }
        }


    }

    private fun bindViewData(path: String?) {
        if (TextUtils.isEmpty(path)) return
        val file = File(path)
        if (!file.exists()) return
        val it = FileInputStream(file)
        if (it != null) {
            val line = mReadTxtFile(path!!)
            val split = line?.split(";") as MutableList<String>
            //过滤为空的值
            val filter = split.filter {  "" != it }
            //去重
            val set = TreeSet(filter)
            val result = mutableListOf<String>()
            result.addAll(set)
            //并集
            mArray?.removeAll(result)
            addData(result)
            mAdapter?.notifyDataSetChanged()
        }


    }

    private fun clearData() {
        if (mArray == null) {
            mArray = ArrayList()
        } else {
            mArray!!.clear()
        }
    }

    private fun addData(list: MutableList<String>?) {
        if (list == null || list.isEmpty()) {
            return
        }
        if (mArray == null) {
            clearData()
        }
        mArray!!.addAll(list)
        clearPhone()
        save(mArray)
    }

    fun isPhoneRight(mContext: Context, et: EditText): Boolean {
        val phone = et.text.trim().toString()
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "请输入手机号码", Toast.LENGTH_SHORT).show();
            return false

        }
        if (!isPhoneNum(phone)) {
            Toast.makeText(this, "请输入正确手机号码", Toast.LENGTH_SHORT).show();
            return false
        }
        return true
    }

    /**
     * 判断是否为手机号码
     *
     * @return
     */
    fun isPhoneNum(phone: String): Boolean {
        val pattern = Pattern.compile("1[0-9]{10}")
        val matcher = pattern.matcher(phone)
        return matcher.matches()
    }

    private fun save(phones: MutableList<String>?) {
        if (phones.isNullOrEmpty()) return
        val sb = StringBuffer()
        phones?.forEachIndexed { index, s ->
            if (index == mArray!!.size - 1) {
                sb.append(s)
            } else {
                sb.append(s)
                sb.append(";")
            }
        }
        val edit = mSp?.edit()
        edit?.putString("$spName", sb.toString());
        edit?.commit()
    }

    private fun getPhoneList(): MutableList<String>? {
        val phones = mSp?.getString(spName, "")
        if (TextUtils.isEmpty(phones)) return mutableListOf<String>()
        val split = phones?.split(";")
        return split as MutableList<String>
    }

    private fun clearPhone() {
        mSp?.let {
            val edit = it.edit()
            edit.clear()
            edit.commit()
        }
    }

    fun mReadTxtFile(strFilePath: String): String? {
        var content = "" //文件内容字符串
        //打开文件
        val file = File(strFilePath)
        //如果path是传递过来的参数，可以做一个非目录的判断
        if (file.isDirectory) {
        } else {
            try {
                val instream: InputStream = FileInputStream(file)
                if (instream != null) {
                    var line: String? = null
                    val buffreader = BufferedReader(
                        InputStreamReader(
                            FileInputStream(file), "UTF-8"
                        )
                    )
                    //分行读取
                    while (buffreader.readLine().also { line = it } != null) {
                        content += "$line;"
                    }
                    instream.close()
                }
            } catch (e: FileNotFoundException) {
                Log.d("TestFile", "The File doesn't not exist.")
            } catch (e: IOException) {
                Log.d("TestFile", e.message!!)
            }
        }
        return content
    }

}