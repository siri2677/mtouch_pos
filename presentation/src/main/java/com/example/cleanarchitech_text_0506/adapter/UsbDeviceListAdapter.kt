package com.lilly.ble.adapter

import android.content.Context
import android.hardware.usb.UsbDevice
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cleanarchitech_text_0506.R
import com.mtouch.domain.model.UsbDeviceInfo
import java.util.*
import kotlin.collections.ArrayList

class UsbDeviceListAdapter
    : RecyclerView.Adapter<UsbDeviceListAdapter.BleViewHolder>() {

    lateinit var mContext: Context
    private var items: ArrayList<UsbDevice> = ArrayList()
    private lateinit var itemClickListner: ItemClickListener
    lateinit var itemView: View

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BleViewHolder {
        mContext = parent.context
        itemView = LayoutInflater.from(mContext).inflate(R.layout.rv_ble_item, parent, false)
        return BleViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: BleViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            itemClickListner.onClick(it, items?.get(position))
        }

    }

    override fun getItemId(position: Int): Long {
        return items[position].hashCode().toLong()
    }

    override fun getItemCount(): Int {
        Log.w("count", items.size.toString());
        return items.size
    }

    fun setItem(item: ArrayList<UsbDevice>?){
        if(item==null) return
        items.clear()
        items.addAll(item)
        var Test = items?.get(0)?.deviceName
        Log.w("items", Test.toString());
        this.notifyDataSetChanged()
    }


    inner class BleViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(currentDevice: UsbDevice?) {
            val bleName = itemView.findViewById<TextView>(R.id.ble_name)

            if(currentDevice?.deviceName != null) {
                bleName.text = currentDevice?.deviceName
            } else {
                bleName.text = currentDevice?.productName
            }

            val bleAddress = itemView.findViewById<TextView>(R.id.ble_address)
            bleAddress.text = String.format(Locale.US, "Name %s, Vendor %d, Product %s", currentDevice?.deviceName, currentDevice?.vendorId, currentDevice?.productName)

        }
    }

    interface ItemClickListener {
        fun onClick(view: View, device: UsbDevice?)
    }

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListner = itemClickListener
    }

}
