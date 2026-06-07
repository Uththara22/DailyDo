package com.dailydo.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dailydo.R
import com.dailydo.data.models.HydrationIntake
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter for hydration history list
 */
class HydrationHistoryAdapter : RecyclerView.Adapter<HydrationHistoryAdapter.HydrationHistoryViewHolder>() {

    private var intakes: List<HydrationIntake> = emptyList()
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    private val todayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun updateIntakes(newIntakes: List<HydrationIntake>) {
        intakes = newIntakes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HydrationHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_hydration_history, parent, false)
        return HydrationHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HydrationHistoryViewHolder, position: Int) {
        val intake = intakes[position]
        holder.bind(intake)
    }

    override fun getItemCount(): Int = intakes.size

    inner class HydrationHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvIntakeAmount: TextView = itemView.findViewById(R.id.tv_intake_amount)
        private val tvIntakeTime: TextView = itemView.findViewById(R.id.tv_intake_time)
        private val tvIntakeDate: TextView = itemView.findViewById(R.id.tv_intake_date)
        private val ivShare: android.widget.ImageView = itemView.findViewById(R.id.iv_share)
        private val ivDelete: android.widget.ImageView = itemView.findViewById(R.id.iv_delete)
        private val ivData: android.widget.ImageView = itemView.findViewById(R.id.iv_data)

        fun bind(intake: HydrationIntake) {
            tvIntakeAmount.text = "${intake.amountMl} ml"
            tvIntakeTime.text = timeFormat.format(intake.timestamp)

            // Check if it's today
            val today = todayFormat.format(Date())
            val intakeDate = todayFormat.format(intake.timestamp)
            
            if (intakeDate == today) {
                tvIntakeTime.text = "${timeFormat.format(intake.timestamp)} • Today"
                tvIntakeDate.visibility = View.GONE
            } else {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                val yesterday = todayFormat.format(calendar.time)
                
                if (intakeDate == yesterday) {
                    tvIntakeTime.text = "${timeFormat.format(intake.timestamp)} • Yesterday"
                    tvIntakeDate.visibility = View.GONE
                } else {
                    tvIntakeTime.text = timeFormat.format(intake.timestamp)
                    tvIntakeDate.text = dateFormat.format(intake.timestamp)
                    tvIntakeDate.visibility = View.VISIBLE
                }
            }

            // Hook up icon clicks
            ivShare.setOnClickListener {
                val shareText = "I drank ${intake.amountMl} ml of water at ${timeFormat.format(intake.timestamp)}"
                val shareIntent = android.content.Intent().apply {
                    action = android.content.Intent.ACTION_SEND
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                }
                itemView.context.startActivity(android.content.Intent.createChooser(shareIntent, itemView.context.getString(R.string.share_via)))
            }

            ivDelete.setOnClickListener {
                // Ask for confirmation before deleting
                androidx.appcompat.app.AlertDialog.Builder(itemView.context)
                    .setTitle(itemView.context.getString(R.string.delete))
                    .setMessage("Are you sure you want to delete this entry?")
                    .setPositiveButton(itemView.context.getString(R.string.delete)) { _, _ ->
                        try {
                            val prefs = com.dailydo.data.repository.SharedPreferencesManager.getInstance(itemView.context)
                            val current = prefs.getHydrationIntake().toMutableList()
                            current.removeAll { it.id == intake.id }
                            prefs.saveHydrationIntake(current)
                            android.widget.Toast.makeText(itemView.context, itemView.context.getString(R.string.delete), android.widget.Toast.LENGTH_SHORT).show()
                            // Optimistic local removal
                            val newList = intakes.toMutableList()
                            if (bindingAdapterPosition in newList.indices) {
                                newList.removeAt(bindingAdapterPosition)
                                updateIntakes(newList)
                            }
                        } catch (_: Exception) { }
                    }
                    .setNegativeButton(itemView.context.getString(R.string.cancel), null)
                    .show()
            }

            ivData.setOnClickListener {
                // Show detailed info dialog for this hydration entry
                val ctx = itemView.context
                val fullDate = java.text.SimpleDateFormat("EEEE, MMM dd, yyyy", java.util.Locale.getDefault()).format(intake.timestamp)
                val message = buildString {
                    append("Amount: ${intake.amountMl} ml\n")
                    append("Date: $fullDate\n")
                    append("Time: ${timeFormat.format(intake.timestamp)}\n")
                    if (!intake.note.isNullOrBlank()) {
                        append("Note: ${intake.note}")
                    }
                }

                androidx.appcompat.app.AlertDialog.Builder(ctx)
                    .setTitle("Hydration Entry Details")
                    .setMessage(message)
                    .setPositiveButton(ctx.getString(R.string.ok), null)
                    .show()
            }
        }
    }
}