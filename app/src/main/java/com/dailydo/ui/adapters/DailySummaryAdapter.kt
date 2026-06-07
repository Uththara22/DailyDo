package com.dailydo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dailydo.R
import com.dailydo.data.models.DailySummary
import java.text.SimpleDateFormat
import java.util.Locale

class DailySummaryAdapter(
    private var items: List<DailySummary>
) : RecyclerView.Adapter<DailySummaryAdapter.SummaryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_daily_summary, parent, false)
        return SummaryViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    fun submitList(newItems: List<DailySummary>) {
        items = newItems
        notifyDataSetChanged()
    }

    class SummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tv_date)
        private val tvHabits: TextView = itemView.findViewById(R.id.tv_habits)
        private val tvMood: TextView = itemView.findViewById(R.id.tv_mood)
        private val tvHydration: TextView = itemView.findViewById(R.id.tv_hydration)
        private val humanFmt = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())

        fun bind(summary: DailySummary) {
            // summary.date is yyyy-MM-dd; parse for human format
            val date = try {
                val iso = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(summary.date)!!
                humanFmt.format(iso)
            } catch (_: Exception) {
                summary.date
            }
            tvDate.text = date
            tvHabits.text = "Habits ${summary.habitsPercent.toInt()}%"
            tvMood.text = "Mood ${summary.moodPercent.toInt()}%"
            tvHydration.text = "Hydration ${summary.hydrationPercent.toInt()}%"
        }
    }
}


