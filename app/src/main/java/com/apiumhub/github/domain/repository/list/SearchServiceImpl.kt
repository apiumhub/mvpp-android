package com.apiumhub.github.domain.repository.list

import android.annotation.SuppressLint
import com.apiumhub.github.data.GithubRepository
import com.apiumhub.github.domain.entity.Repository
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.*
import java.net.UnknownHostException

enum class SearchEvent {
  LOADING_EVENT,
  NEW_DATA_EVENT,
  EMPTY_DATA_EVENT,
  NO_INTERNET_ERROR,
  GENERIC_ERROR,
  STOP_LOADING,
  HIDE_LOADING
}

class SearchServiceImpla(private val repository: GithubRepository, private val dispatcher: CoroutineDispatcher) :
  SearchService, LoadingService {
  var job = Job()
  val subject: PublishSubject<SearchEvent> = PublishSubject.create()

  override fun search(query: String) {
    subject.onNext(SearchEvent.LOADING_EVENT)
    GlobalScope.launch(job + dispatcher) {
      try {
        val result = repository.searchRepositories(query)
        when {
          result.items == null -> subject.onNext(SearchEvent.GENERIC_ERROR)
          result.items.isEmpty() -> subject.onNext(SearchEvent.EMPTY_DATA_EVENT)
          else -> subject.onNext(SearchEvent.NEW_DATA_EVENT)
        }
        subject.onNext(SearchEvent.HIDE_LOADING)
      } catch (exception: Exception) {
        if (exception is UnknownHostException) {
          subject.onNext(SearchEvent.NO_INTERNET_ERROR)
        } else {
          subject.onNext(SearchEvent.GENERIC_ERROR)
        }
        subject.onNext(SearchEvent.STOP_LOADING)
      }
    }
  }

  @SuppressLint("CheckResult")
  //TODO: Solve implement object
  override fun onData(func: (List<Repository>) -> Unit) {
    subject.filter { it == SearchEvent.NEW_DATA_EVENT }.subscribe { func(it) }
  }

  @SuppressLint("CheckResult")
  override fun onEmpty(func: () -> Unit) {
    subject.filter { it == SearchEvent.EMPTY_DATA_EVENT }.subscribe { func() }
  }

  @SuppressLint("CheckResult")
  override fun onNoInternet(func: () -> Unit) {
    subject.filter { it == SearchEvent.NO_INTERNET_ERROR }.subscribe { func() }
  }

  @SuppressLint("CheckResult")
  override fun onOtherError(func: () -> Unit) {
    subject.filter { it == SearchEvent.GENERIC_ERROR }.subscribe { func() }
  }

  @SuppressLint("CheckResult")
  override fun onLoading(func: () -> Unit) {
    subject.filter { it == SearchEvent.LOADING_EVENT }.subscribe { func() }
  }

  @SuppressLint("CheckResult")
  override fun stopLoading(func: () -> Unit) {
    subject.filter { it == SearchEvent.STOP_LOADING }.subscribe { func() }
  }

  @SuppressLint("CheckResult")
  override fun hideLoading(func: () -> Unit) {
    subject.filter { it == SearchEvent.HIDE_LOADING }.subscribe { func() }
  }
}
