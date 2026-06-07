package com.ourtimer.app.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ourtimer.app.R
import com.ourtimer.app.data.Challenge
import com.ourtimer.app.utils.formatPercentage
import java.util.Locale

class ChallengeAdapter(
    private var challenges: List<Challenge>,
    private val onItemClick: (Challenge) -> Unit,
    private val onDeleteClick: (Challenge) -> Unit
) : RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder>() {

    fun updateData(newChallenges: List<Challenge>) {
        this.challenges = newChallenges
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_challenge, parent, false)
        return ChallengeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        holder.bind(challenges[position])
    }

    override fun getItemCount(): Int = challenges.size

    inner class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvSub: TextView = itemView.findViewById(R.id.tv_sub)
        private val tvPct: TextView = itemView.findViewById(R.id.tv_pct)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete)

        fun bind(challenge: Challenge) {
            val displayName = if (challenge.name.length > 15) {
                challenge.name.take(15) + "..."
            } else {
                challenge.name
            }
            tvName.text = displayName

            val elapsedMillis = System.currentTimeMillis() - challenge.startTime
            val elapsedDays = elapsedMillis.toDouble() / (24.0 * 60.0 * 60.0 * 1000.0)
            
            val pct = ((elapsedDays / challenge.days) * 100.0).coerceIn(0.0, 100.0)
            tvPct.text = pct.formatPercentage(2)

            // Color code progress percentage
            val colorRes = when {
                pct < 30.0 -> R.color.red
                pct < 60.0 -> R.color.amber
                else -> R.color.emerald
            }
            tvPct.textColor(colorRes)

            val daysLeft = (challenge.days - elapsedDays).coerceAtLeast(0.0).toInt()
            tvSub.text = String.format(Locale.US, "%d days left · %s", daysLeft, challenge.alterEgo)
            tvSub.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_muted))

            itemView.setOnClickListener { onItemClick(challenge) }
            btnDelete.setOnClickListener { onDeleteClick(challenge) }
        }

        private fun TextView.textColor(colorResId: Int) {
            this.setTextColor(ContextCompat.getColor(itemView.context, colorResId))
        }
    }
}
