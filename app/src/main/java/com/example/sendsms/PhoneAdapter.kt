package com.example.sendsms

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * @Author : YFL  is Creating a porject in SendSms
 * @Package com.example.sendsms
 * @Email : yufeilong92@163.com
 * @Time :2020/7/16 14:30
 * @Purpose :手机适配
 */
class PhoneAdapter(var mContext: Context, var datas: MutableList<String>) :
    RecyclerView.Adapter<PhoneAdapter.PhoneViewModel>() {
    private var mLayoutInflater: LayoutInflater? = null

    init {
        mLayoutInflater = LayoutInflater.from(mContext)
    }

    interface RecyclerItemListener {
        fun itemDeleteClickListener(phone:String)
    }

    private var listener: RecyclerItemListener? = null;

    fun setRecyclerListener(listener: RecyclerItemListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhoneAdapter.PhoneViewModel {
        return PhoneViewModel(mLayoutInflater!!.inflate(R.layout.item_layout_phone, null))
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: PhoneAdapter.PhoneViewModel, position: Int) {
        val phone = datas[position]
        holder.tv.text=phone
        holder.btn.setOnClickListener {
            listener?.itemDeleteClickListener(phone)
        }
    }

    class PhoneViewModel(view: View) : RecyclerView.ViewHolder(view) {
       val tv=view.findViewById<TextView>(R.id.tv_item_delete_phone)
       val btn=view.findViewById<Button>(R.id.btn_item_delete_phone)
    }
}