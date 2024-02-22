package com.example.phoenixmobile.ui.device.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.phoenixmobile.R
import com.google.android.material.chip.Chip
import java.util.TreeMap

class LoadingAdapter(private var testList: TreeMap<String, Boolean>) :
    RecyclerView.Adapter<LoadingAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val inflatedView: View = layoutInflater.inflate(R.layout.check_item, parent, false)
        return MyViewHolder(inflatedView)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: TreeMap<String, Boolean>) {
        testList = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val test = testList.toList()[position]
        holder.bind(test)
        holder.name.text = test.first
    }

    override fun getItemCount() = testList.size

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: Chip = itemView.findViewById(R.id.chip)
        fun bind(test: Pair<String, Boolean>) {
            name.isChecked = test.second
        }
    }

}