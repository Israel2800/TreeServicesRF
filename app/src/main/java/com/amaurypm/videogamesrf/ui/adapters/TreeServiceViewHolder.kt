package com.amaurypm.videogamesrf.ui.adapters

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView
import com.amaurypm.videogamesrf.R
import com.amaurypm.videogamesrf.data.remote.model.TreeServiceDto
import com.amaurypm.videogamesrf.databinding.GameElementBinding
import com.bumptech.glide.Glide

class TreeServiceViewHolder(
    private val binding: GameElementBinding
): RecyclerView.ViewHolder(binding.root) {

    fun bind(treeService: TreeServiceDto){

        binding.apply {
            tvTitle.text = treeService.title
            tvDescription.text = treeService.description

            val context = binding.root.context

            tvPrice.text = context.getString(
                R.string.price,
                treeService.price
            )

            tvDuration.text = context.getString(
                R.string.duration,
                treeService.duration
            )
        }

        // binding.tvTitle.text = treeService.title

        //Con Glide
        Glide.with(binding.root.context)
            .load(treeService.thumbnail)
            .into(binding.ivThumbnail)

        //Con Picasso
        /*Picasso.get()
            .load(game.thumbnail)
            .into(binding.ivThumbnail)*/


    }

}