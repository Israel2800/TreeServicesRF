package com.amaurypm.videogamesrf.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amaurypm.videogamesrf.data.remote.model.TreeServiceDto
import com.amaurypm.videogamesrf.databinding.GameElementBinding

class TreeServicesAdapter(
    private val treeServices: MutableList<TreeServiceDto>,
    private val onTreeSerivceClicked: (TreeServiceDto) -> Unit
): RecyclerView.Adapter<TreeServiceViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TreeServiceViewHolder {
        val binding = GameElementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TreeServiceViewHolder(binding)
    }

    override fun getItemCount(): Int = treeServices.size

    override fun onBindViewHolder(holder: TreeServiceViewHolder, position: Int) {

        val treeService = treeServices[position]

        holder.bind(treeService)

        holder.itemView.setOnClickListener {
            //Para los clicks a los servicios
            onTreeSerivceClicked(treeService)
        }

    }


}