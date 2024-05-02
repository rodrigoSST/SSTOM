package dji.sampleV5.aircraft.views.base

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<VH : BaseViewHolder<T>, T>(
    var items: MutableList<T>,
    val listener: BaseAdapterListener<T>? = null,
) : RecyclerView.Adapter<VH>() {

    lateinit var recyclerView: RecyclerView

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemCount(): Int = items.size

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            if (items.size > position) listener?.onItemTouched(holder.itemView, items[position])
        }
        holder.itemView.setOnLongClickListener {
            if (items.size > position) listener?.onItemLongTouched(items[position])
            true
        }
    }

    open fun populateItems(list: List<T>) {
        val lastIndex = items.lastIndex
        notifyItemRangeRemoved(0, items.size)

        items.clear()
        items.addAll(list)

        notifyItemRangeInserted(lastIndex, list.size)
    }

    interface BaseAdapterListener<T> {
        fun onItemTouched(view: View, item: T)
        fun onItemLongTouched(item: T)
    }
}
