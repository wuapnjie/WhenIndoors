package com.xiaopo.flying.whenindoors.kits

import android.support.v7.widget.RecyclerView.ViewHolder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * @author wupanjie
 */
abstract class ItemBinder<T : Any, VH : ViewHolder> {
  var itemClickListener: ((item: T, position: Int) -> Unit)? = null

  lateinit var adapter: AnotherAdapter

  abstract fun createViewHolder(inflater: LayoutInflater, parent: ViewGroup): VH

  abstract fun bindViewHolder(holder: VH, item: T)

  fun clickWith(listener: (item: T, position: Int) -> Unit) = apply {
    itemClickListener = listener
  }
}

abstract class AnotherBinder<T : Any> : ItemBinder<T, AnotherViewHolder>() {
  override fun bindViewHolder(holder: AnotherViewHolder, item: T) {
    renderView(holder, holder.itemView, item)
  }

  abstract fun renderView(holder: AnotherViewHolder, itemView: View, item: T)
}