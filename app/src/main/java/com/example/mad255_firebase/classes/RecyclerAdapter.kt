package com.example.mad255_firebase.classes

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mad255_firebase.R

class RecyclerAdapter(val listData: MutableList<User>):
    RecyclerView.Adapter<RecyclerAdapter.ViewHolder>(){

    //rv: implementing listener.
    private lateinit var itemListener: onItemClickListener
    interface onItemClickListener{
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener: onItemClickListener){
        itemListener = listener
    }

    fun deleteItem(i : Int){
        listData.removeAt(i)
        notifyDataSetChanged()
    }
    fun addItem(i: Int, item: User){
        listData.add(i, item)
    }

    //define the rv item class
    //NOTE: setup for onclick listener is happening here.
    class ViewHolder(view: View, listener: onItemClickListener) : RecyclerView.ViewHolder(view) {
        val itemInfo = view.findViewById<TextView>(R.id.itemViewinfo)
        init {
            view.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    //this is loader for the item: item_view: res file
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewHolder = LayoutInflater.from(parent.context).inflate(
            R.layout.item_view, parent, false) //load up the item res file.
        return ViewHolder(viewHolder, itemListener)
    }

    override fun getItemCount(): Int {
        return listData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.i("recycler", "binding item")
        holder.itemInfo.text = "${listData[position].firstName} | ${listData[position].lastName} " +
                "| ${listData[position].email} | ${listData[position].address}"

    }
}