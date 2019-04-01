package com.apiumhub.github.list.classic

import com.apiumhub.github.core.domain.entity.Repository
import com.apiumhub.github.list.RepositoryListFragment
import com.apiumhub.github.list.RepositoryListRepository
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.net.ConnectException
import java.net.UnknownHostException


interface RepositoryListView {
  fun showLoading()
  fun hideLoading()

  fun showData(items: List<Repository>)
  fun showEmptyData()
  fun showNetworkError()
  fun showOtherError()

  companion object {
    fun create() = RepositoryListFragment.newInstance()
  }
}

interface RepositoryListService {
  fun search(query: String, onNext: (List<Repository>?) -> Unit, onError: (Throwable) -> Unit, onComplete: () -> Unit)
  fun cancel()

  companion object {
    fun create(
      networkRepository: RepositoryListRepository,
      inMemoryRepository: RepositoryListRepository,
      observeOn: Scheduler,
      subscribeOn: Scheduler
    ): RepositoryListService =
      RepositoryListInteractor(
        networkRepository,
        inMemoryRepository,
        observeOn,
        subscribeOn
      )
  }
}

sealed class Action {
  class Search(val query: String) : Action()
  object Destroy : Action()
}

class RepositoryListPresenter(private val view: RepositoryListView, private val service: RepositoryListService) {
  private val subject: PublishSubject<Action> = PublishSubject.create()
  private val disposeBag = CompositeDisposable()

  init {
    bindSearch {
      service.search(it,
        ::onGetRepositoryListNext,
        ::onGetRepositoryListError,
        ::onGetRepositoryListComplete
      )
    }

    bindDestroy {
      service.cancel()
    }
  }

  //region -- Bind actions --
  private fun bindSearch(func: (String) -> Unit) {
    disposeBag.add(
      subject.filter { it is Action.Search }
        .subscribe { func((it as Action.Search).query) }
    )
  }

  private fun bindDestroy(func: () -> Unit) {
    disposeBag.add(subject.filter { it is Action.Destroy }.subscribe { func() })
  }
  //endregion

  //region -- Execute actions --
  fun onSearch(query: String = "") {
    subject.onNext(Action.Search(query))
    view.showLoading()
  }

  fun onDestroy() {
    subject.onNext(Action.Destroy)
  }
  //endregion

  private fun onGetRepositoryListNext(list: List<Repository>?) {
    if (list == null || list.isEmpty()) {
      view.showEmptyData()
    } else {
      view.showData(list)
    }
  }

  private fun onGetRepositoryListError(error: Throwable) {
    view.hideLoading()
    when (error) {
      is ConnectException, is UnknownHostException -> view.showNetworkError()
      else -> view.showOtherError()
    }
  }

  private fun onGetRepositoryListComplete() {
    view.hideLoading()
  }
}
