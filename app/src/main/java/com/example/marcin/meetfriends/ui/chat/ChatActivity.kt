package com.example.marcin.meetfriends.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import com.example.marcin.meetfriends.R
import com.example.marcin.meetfriends.mvp.BaseActivity
import com.example.marcin.meetfriends.ui.chat.adapter.ChatAdapter
import com.example.marcin.meetfriends.ui.chat.viewmodel.Message
import com.example.marcin.meetfriends.ui.common.params.EventBasicInfoParams
import com.example.marcin.meetfriends.utils.DateTimeFormatters
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_chat_room.*
import org.joda.time.DateTime

class ChatActivity : BaseActivity<ChatContract.Presenter>(), ChatContract.View {

  private val chatAdapter = ChatAdapter()

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_chat_room)
    setUpSendMessageButton()
    setUpRecyclerView()
  }

  private fun setUpSendMessageButton() {
    sendMessageButton.setOnClickListener {
      presenter.sendMessage(inputMessageEditText.text.toString())
      inputMessageEditText.text.clear()
    }
  }

  override fun setUpActionBarTitle(title: String) {
    supportActionBar?.title = title
  }

  private fun setUpRecyclerView() {
    chatRecyclerView.layoutManager = LinearLayoutManager(this)
    chatRecyclerView.adapter = chatAdapter
  }

  override fun addMessage(message: Message) {
    chatAdapter.addMessage(message)
    chatRecyclerView.scrollToPosition(chatAdapter.itemCount - 1)

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

  companion object {
    fun newIntent(context: Context, eventBasicInfoParams: EventBasicInfoParams): Intent {
      val intent = Intent(context, ChatActivity::class.java)
      intent.putExtras(eventBasicInfoParams.data)
      return intent
    }
  }
}
