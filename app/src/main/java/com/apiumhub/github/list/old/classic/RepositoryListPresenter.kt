package com.apiumhub.github.list.old.classic

import com.apiumhub.github.core.domain.entity.Repository
import com.apiumhub.github.list.old.Action
import com.apiumhub.github.list.old.RepositoryListServiceOld
import com.apiumhub.github.list.old.RepositoryListViewOld
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.net.ConnectException
import java.net.UnknownHostException

class RepositoryListPresenter(private val view: RepositoryListViewOld, private val service: RepositoryListServiceOld) {
  private val subject: PublishSubject<Action> = PublishSubject.create()
  private val disposeBag = CompositeDisposable()

  init {
    bindSearch {
      service.search(
        it,
        ::onGetRepositoryListError,
        ::onGetRepositoryListNext,
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
