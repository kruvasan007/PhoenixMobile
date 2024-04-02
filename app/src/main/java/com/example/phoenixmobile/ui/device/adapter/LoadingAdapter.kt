package com.example.phoenixmobile.ui.device.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.phoenixmobile.R
import com.example.phoenixmobile.data.Repository
import com.google.android.material.chip.Chip
import java.util.TreeMap

class LoadingAdapter(private var testList: TreeMap<String, Int>) :
    RecyclerView.Adapter<LoadingAdapter.MyViewHolder>() {

        // This adapter is designed to display the current report collection process //
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val inflatedView: View = layoutInflater.inflate(R.layout.check_item, parent, false)
        return MyViewHolder(inflatedView)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: TreeMap<String, Int>) {
        // updating the status of the tests according to which we paint the chips element
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

        @SuppressLint("ResourceAsColor")
        fun bind(test: Pair<String, Int>) {
            // checking the status of this report item
            name.isChecked =
                test.second == Repository.REPORT_DONE || test.second == Repository.AUDIO_CHECK_DONE
            if (test.second == Repository.REPORT_ERROR)
                name.setChipBackgroundColorResource(R.color.error_color)
        }
    }

}