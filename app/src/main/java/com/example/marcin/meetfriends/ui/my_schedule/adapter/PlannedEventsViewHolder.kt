package com.example.marcin.meetfriends.ui.my_schedule.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.marcin.meetfriends.R
import com.example.marcin.meetfriends.models.Event
import com.example.marcin.meetfriends.utils.DateTimeFormatters
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_planned_event.view.*
import org.joda.time.DateTime

/**
 * Created by marci on 2017-11-28.
 */
class PlannedEventsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

  fun bind(event: Event) {
    Picasso.with(itemView.context).load(event.iconId.let { it!! }.toInt()).into(itemView.eventIcon)
    itemView.eventNameTextView.text = event.name
    itemView.eventDateTextView.text = itemView.context.getString(
        R.string.date,
        itemView.resources.getQuantityString(R.plurals.date, event.date?.first()?.toInt() ?: 1, DateTimeFormatters.formatToShortTimeDate(DateTime(event.date?.toLong())))
    )
    itemView.amountOfParticipantsTextView.text = itemView.context.getString(R.string.amount_of_participants, event.participants.size)
  }

  companion object {
    fun create(parent: ViewGroup): PlannedEventsViewHolder {
      return PlannedEventsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_planned_event, parent, false))
    }
  }
}