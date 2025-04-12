package com.example.phoenixmobile.ui.device.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.phoenixmobile.R
import java.util.TreeMap

class ReportAdapter(private var reportList: Map<String, String>) :
    RecyclerView.Adapter<ReportAdapter.MyViewHolder>() {

        // we display a list of data that the server has sent as a report
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val inflatedView: View = layoutInflater.inflate(R.layout.report_item, parent, false)
        return MyViewHolder(inflatedView)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: Map<String, String>) {
        reportList = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val report = reportList.toList()[position]
        holder.bind(report)
        holder.name.text = report.first
    }

    override fun getItemCount() = reportList.size

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.report_name)
        val descr: TextView = itemView.findViewById(R.id.report_descr)
        @SuppressLint("ResourceAsColor")
        fun bind(report: Pair<String, String>) {
            name.text = report.first
            descr.text = report.second
        }
    }

}