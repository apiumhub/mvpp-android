package com.apiumhub.github

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import com.apiumhub.github.presentation.errors.GenericErrorsPresenter
import com.apiumhub.github.presentation.errors.IErrorsInteractor
import com.apiumhub.github.presentation.errors.IErrorsView
import com.apiumhub.github.presentation.list.RepositoryListView
import io.reactivex.Observable

class MainActivity : AppCompatActivity(), IErrorsView {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    setContentFragment()
    GenericErrorsPresenter(this, IErrorsInteractor.create(Observable.empty()))
  }

  private fun setContentFragment() {
    supportFragmentManager.beginTransaction().apply {
      add(R.id.main_container, RepositoryListView.create())
      commit()
    }
  }

  override fun onNoInternetConnectionError() {
    Snackbar.make(findViewById(android.R.id.content), "No internet connection", Snackbar.LENGTH_SHORT).show()
  }

  override fun onUnauthorizedError() {
    Snackbar.make(findViewById(android.R.id.content), "Unauthorized error", Snackbar.LENGTH_SHORT).show()
  }

  override fun onForbiddenError() {
    Snackbar.make(
      findViewById(android.R.id.content),
      "Max requests limit reached. Try again later.",
      Snackbar.LENGTH_SHORT
    ).show()
  }
}
