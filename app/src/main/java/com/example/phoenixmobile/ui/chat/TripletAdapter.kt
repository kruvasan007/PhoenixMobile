package com.example.phoenixmobile.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.phoenixmobile.R

class TripletAdapter(private var data: List<Triplet>) : RecyclerView.Adapter<TripletAdapter.TripletVH>() {
    fun submit(list: List<Triplet>) {
        data = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripletVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_triplet, parent, false)
        return TripletVH(v)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: TripletVH, position: Int) = holder.bind(data[position])

    class TripletVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val subject: TextView = itemView.findViewById(R.id.subject)
        private val relation: TextView = itemView.findViewById(R.id.relation)
        private val obj: TextView = itemView.findViewById(R.id.obj)
        fun bind(t: Triplet) {
            subject.text = t.subject
            relation.text = t.relation
            obj.text = t.`object`
        }
    }
}

