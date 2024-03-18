package com.example.phoenixmobile.ui.search.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.phoenixmobile.R
import com.google.android.material.card.MaterialCardView
import java.text.DecimalFormat
import java.util.TreeMap

class PriceAdapter(
    private val onClickListener: OnClickListener,
    private var priceList: TreeMap<String, Double>
) :
    RecyclerView.Adapter<PriceAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val inflatedView: View = layoutInflater.inflate(R.layout.device_state_line, parent, false)
        return MyViewHolder(inflatedView)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: TreeMap<String, Double>) {
        priceList = list
        notifyDataSetChanged()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: MyViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val price = priceList.toList()[position]
        holder.bind(price)

        // listener to open selected card
        holder.card.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_UP -> onClickListener.onClick(position)
            }
            v?.onTouchEvent(event) ?: true
        }
    }

    override fun getItemCount() = priceList.size

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.device_name)
        val brand: TextView = itemView.findViewById(R.id.device_brand)
        val price: TextView = itemView.findViewById(R.id.current_price)
        val card: MaterialCardView = itemView.findViewById(R.id.card)
        fun bind(test: Pair<String, Double>) {
            val brandStr = test.first.split(";")[0]
            val nameStr = test.first.split(";")[1]
            brand.text = brandStr
            name.text = nameStr
            price.text = DecimalFormat("#,##0.00").format(test.second) + " $"
        }
    }

    class OnClickListener(val clickListener: (id: Int) -> Unit) {
        fun onClick(id: Int) =
            clickListener(id)
    }
}