package com.example.marcin.meetfriends.ui.planned_event_detail.event_questionnaire

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.marcin.meetfriends.R
import com.example.marcin.meetfriends.extensions.transformDistance
import com.example.marcin.meetfriends.models.FirebasePlace
import com.example.marcin.meetfriends.mvp.BaseFragment
import com.example.marcin.meetfriends.ui.charts.ChartsDialogFragment
import com.example.marcin.meetfriends.ui.common.params.EventIdParams
import com.example.marcin.meetfriends.ui.common.params.PlaceIdParams
import com.example.marcin.meetfriends.ui.planned_event_detail.event_questionnaire.adapter.VenuesAdapter
import com.example.marcin.meetfriends.ui.place_details.PlaceDetailsActivity
import com.example.marcin.meetfriends.utils.DateTimeFormatters
import com.squareup.picasso.Picasso
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_event_questionnaire.*
import kotlinx.android.synthetic.main.item_venue.*
import org.joda.time.DateTime
import org.joda.time.chrono.ISOChronology

/**
 * Created by marci on 2017-12-05.
 */
class EventQuestionnaireFragment : BaseFragment<EventQuestionnaireContract.Presenter>(), EventQuestionnaireContract.View {

  private var selectedDate: DateTime = DateTime().withChronology(ISOChronology.getInstance())
  private val venuesAdapter = VenuesAdapter()

  override fun onAttach(context: Context?) {
    AndroidSupportInjection.inject(this)
    super.onAttach(context)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_event_questionnaire, container, false)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    dateSuggestionTextView.text = getString(R.string.your_date_suggestion, DateTimeFormatters.formatToShortDate(selectedDate))
    timeSuggestionTextView.text = getString(R.string.your_time_suggestion, DateTimeFormatters.formatToShortTime(selectedDate))
    setUpDateChooserButton()
    setUpTimeChooserButton()
    venuesRecyclerView.layoutManager = GridLayoutManager(context, 2)
    venuesRecyclerView.adapter = venuesAdapter
    confirmDateSuggestionButton.setOnClickListener {
      presenter.sendDateVote(selectedDate)
    }
    changeDateVoteButton.setOnClickListener {
      presenter.clickedChangeDateButton()
    }
    changeVenueVoteButton.setOnClickListener {
      presenter.clickedChangeVenueButton()
    }
    dateTimeChartsButton.setOnClickListener {
      presenter.clickedChartsButton()
    }
    venueCardView.setOnClickListener {
      presenter.onClickSelectedVenue()
    }
  }

  override fun initializeVenuesAdapter(venues: List<FirebasePlace>) {
    venuesAdapter.addVenuesList(venues)
  }

  override fun setUpAdapterListeners() {
    presenter.handleChosenPlace(venuesAdapter.getClickEvent())
    presenter.handleClickedActionButton(venuesAdapter.getClickedActionButtonEvent())
  }

  override fun buildAlertMessageNoGps() {
    AlertDialog.Builder(context)
        .setMessage(getString(R.string.please_turn_on_gps))
        .setCancelable(false)
        .setPositiveButton("Yes", { _, _ -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
        .setNegativeButton("No", { dialog, _ -> dialog.cancel() })
        .show()
  }

  override fun showDateSectionProgressBar() {
    dateSectionProgressBar.visibility = View.VISIBLE
  }

  override fun hideDateSectionProgressBar() {
    dateSectionProgressBar.visibility = View.INVISIBLE
  }

  override fun showDateChooserLayout() {
    dateChooserLayout.visibility = View.VISIBLE
  }

  override fun showVenuesSectionProgressBar() {
    if (venuesSectionProgressBar != null) {
      venuesSectionProgressBar.visibility = View.VISIBLE
    }
  }

  override fun hideVenuesSectionProgressBar() {
    if (venuesSectionProgressBar != null) {
      venuesSectionProgressBar.visibility = View.INVISIBLE
    }
  }

  override fun showChosenDateSnackBar(selectedDate: DateTime, userId: String) {
    Snackbar.make(
        this.snackBarContainer,
        getString(R.string.chosen_date, DateTimeFormatters.formatToShortTimeDate(selectedDate)),
        Snackbar.LENGTH_LONG)
        .setAction(getString(R.string.undo), {
          presenter.removeChosenDateFromEvent(selectedDate, userId)
        }).show()
  }

  override fun showChosenVenueSnackBar(venue: FirebasePlace, userId: String) {
    Snackbar.make(
        this.snackBarContainer,
        getString(R.string.chosen_venue, venue.name.let { it!! }),
        Snackbar.LENGTH_LONG)
        .setAction(getString(R.string.undo), {
          presenter.removeChosenVenueFromEvent(venue.id.let { it!! }, userId)
        }).show()
  }

  override fun showFilledDateQuestionnaire(date: DateTime) {
    dateChooserLayout.visibility = View.INVISIBLE
    filledDateLayout.visibility = View.VISIBLE
    chosenDateTextView.text = getString(R.string.your_vote_for_event_date, DateTimeFormatters.formatToShortTimeDate(selectedDate))
  }

  override fun showFilledVenueQuestionnaire(venue: FirebasePlace) {
    venuesRecyclerView.visibility = View.GONE
    filledVenueLayout.visibility = View.VISIBLE
    venueNameTextView.text = venue.name
    distanceTextView.text = venue.distance?.transformDistance(context)
    ratingTextView.text = venue.rating.toString()
    venueVicinityTextView.text = venue.vicinity
    if (venue.photos.isNotEmpty()) {
      Picasso.with(context).load(venue.photos.first()).placeholder(context.getDrawable(R.drawable.placeholder)).into(venueImage)
    } else {
      Picasso.with(context).load(venue.placeIcon).placeholder(context.getDrawable(R.drawable.placeholder)).into(venueImage)
    }
  }

  override fun showDateChoserLayout() {
    filledDateLayout.visibility = View.INVISIBLE
    dateChooserLayout.visibility = View.VISIBLE
  }

  override fun showVenueChoserLayout() {
    filledVenueLayout.visibility = View.GONE
    venuesRecyclerView.visibility = View.VISIBLE
  }

  override fun startPlaceDetailsActivity(placeIdParams: PlaceIdParams) {
    startActivity(PlaceDetailsActivity.newIntent(context, placeIdParams))
  }

  override fun startChartsDialogFragment(eventIdParams: EventIdParams) {
    val chartsDialogFragment = ChartsDialogFragment()
    chartsDialogFragment.arguments = eventIdParams.data
    chartsDialogFragment.show(activity.supportFragmentManager, ChartsDialogFragment::class.java.toString())
  }

  private fun setUpDateChooserButton() {
    dateChooserButton.setOnClickListener {
      DatePickerDialog(
          context,
          DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            selectedDate = DateTime(year, monthOfYear + 1, dayOfMonth, selectedDate.hourOfDay, selectedDate.minuteOfHour, 0, 0)
            dateSuggestionTextView.text = getString(R.string.your_date_suggestion, DateTimeFormatters.formatToShortDate(selectedDate))
          },
          selectedDate.year,
          selectedDate.monthOfYear - 1,
          selectedDate.dayOfMonth
      ).show()
    }
  }

  private fun setUpTimeChooserButton() {
    timeChooserButton.setOnClickListener {
      TimePickerDialog(
          context,
          TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            selectedDate = DateTime(selectedDate.year, selectedDate.monthOfYear, selectedDate.dayOfMonth, hour, minute, 0, 0)
            timeSuggestionTextView.text = getString(R.string.your_time_suggestion, DateTimeFormatters.formatToShortTime(selectedDate))
          },
          selectedDate.hourOfDay,
          selectedDate.minuteOfHour,
          true
      ).show()
    }
  }
}