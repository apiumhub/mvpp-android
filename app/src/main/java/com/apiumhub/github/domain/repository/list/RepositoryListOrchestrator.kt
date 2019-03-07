package com.apiumhub.github.domain.repository.list

import android.annotation.SuppressLint
import com.apiumhub.github.data.IGithubRepository
import com.apiumhub.github.domain.entity.Repository
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.UnknownHostException

sealed class SearchEvent {
  class Found(val list: List<Repository>) : SearchEvent()
  object Empty : SearchEvent()
  object ErrorNullList : SearchEvent()
  object ErrorNoInternet : SearchEvent()
  object ErrorOther : SearchEvent()
  object Start : SearchEvent()
  object Stop : SearchEvent()
}

interface LoadingInteractor {
  //output
  fun onStartLoading(func: () -> Unit)

  fun onStopLoading(func: () -> Unit)
}

interface RepositoryListOrchestrator : LoadingInteractor {
  //input
  fun findAll()

  fun search(query: String)

  //output
  fun onData(func: (List<Repository>) -> Unit)

  fun onEmpty(func: () -> Unit)
  fun onErrorNullList(func: () -> Unit)
  fun onErrorNoInternet(func: () -> Unit)
  fun onErrorOther(func: () -> Unit)

  companion object {
    fun create(): RepositoryListOrchestrator = RepositoryListOrchestratorImpl(IGithubRepository.create())
  }
}


class RepositoryListOrchestratorImpl(private val repository: IGithubRepository) : RepositoryListOrchestrator {

  private val subject = PublishSubject.create<SearchEvent>()

  override fun findAll() {
    GlobalScope.launch(Job() + Dispatchers.Main) {
      subject.onNext(SearchEvent.Start)

      try {
        val items = repository.findAllRepositories()

        when {
          items.isEmpty() -> subject.onNext(SearchEvent.Empty)
          else -> subject.onNext(SearchEvent.Found(items))
        }
        subject.onNext(SearchEvent.Stop)
      } catch (exception: Exception) {
        if (exception is UnknownHostException) {
          subject.onNext(SearchEvent.ErrorNoInternet)
        } else {
          subject.onNext(SearchEvent.ErrorOther)
        }
        subject.onNext(SearchEvent.Stop)
      }
    }
  }

  override fun search(query: String) {
    GlobalScope.launch(Job() + Dispatchers.Main) {
      subject.onNext(SearchEvent.Start)

      try {
        val result = repository.searchRepositories(query)
        val items = result.items
        when {
          items == null -> subject.onNext(SearchEvent.ErrorNullList)
          items.isEmpty() -> subject.onNext(SearchEvent.Empty)
          else -> subject.onNext(SearchEvent.Found(items))
        }
        subject.onNext(SearchEvent.Stop)
      } catch (exception: Exception) {
        if (exception is UnknownHostException) {
          subject.onNext(SearchEvent.ErrorNoInternet)
        } else {
          subject.onNext(SearchEvent.ErrorOther)
        }
        subject.onNext(SearchEvent.Stop)
      }
    }
  }

  @SuppressLint("CheckResult")
  override fun onData(func: (List<Repository>) -> Unit) {
    subject.filter { it is SearchEvent.Found }.subscribe { func((it as SearchEvent.Found).list) }
  }

  @SuppressLint("CheckResult")
  override fun onEmpty(func: () -> Unit) {
    subject.filter { it is SearchEvent.Empty }.subscribe { func() }
  }

  @SuppressLint("CheckResult")
  override fun onErrorNullList(func: () -> Unit) {
    subject.filter { it is SearchEvent.ErrorNullList }.subscribe { func() }
  }

  @SuppressLint("CheckResult")
  override fun onErrorNoInternet(func: () -> Unit) {
    subject.filter { it is SearchEvent.ErrorNoInternet }.subscribe { func() }
  }

  @SuppressLint("CheckResult")
  override fun onErrorOther(func: () -> Unit) {
    subject.filter { it is SearchEvent.ErrorOther }.subscribe { func() }
  }

  @SuppressLint("CheckResult")
  override fun onStartLoading(func: () -> Unit) {
    subject.filter { it is SearchEvent.Start }.subscribe { func() }
  }

  @SuppressLint("CheckResult")
  override fun onStopLoading(func: () -> Unit) {
    subject.filter { it is SearchEvent.Stop }.subscribe { func() }
  }
}
