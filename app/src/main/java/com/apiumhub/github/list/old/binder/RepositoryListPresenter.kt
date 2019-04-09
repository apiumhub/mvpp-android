package com.apiumhub.github.list.old.binder

import com.apiumhub.github.core.domain.entity.Repository
import com.apiumhub.github.list.old.Action
import com.apiumhub.github.list.old.RepositoryListServiceOld
import com.apiumhub.github.list.old.RepositoryListViewOld
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import java.net.ConnectException
import java.net.UnknownHostException

class RepositoryListPresenterBinder(view: RepositoryListViewOld, service: RepositoryListServiceOld): RepositoryListPresenter(view) {
  init {
    bindSearch { query ->
      service.search(query,
        onError = { error ->
          onGetRepositoryListError(error)
        },
        onNext = { list ->
          onGetRepositoryListNext(list)
        },
        onComplete = {
          onGetRepositoryListComplete()
        }
      )
    }

    bindDestroy {
      service.cancel()
    }
  }
}

open class RepositoryListPresenter(private val view: RepositoryListViewOld) {
  private val subject: PublishSubject<Action> = PublishSubject.create()
  private val disposeBag = CompositeDisposable()

  //region -- Bind actions --
  protected fun bindSearch(func: (String) -> Unit) {
    disposeBag.add(subject.filter { it is Action.Search }.subscribe { func((it as Action.Search).query) })
  }

  protected fun bindDestroy(func: () -> Unit) {
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

  protected fun onGetRepositoryListNext(list: List<Repository>?) {
    if (list == null || list.isEmpty()) {
      view.showEmptyData()
    } else {
      view.showData(list)
    }
  }

  protected fun onGetRepositoryListError(error: Throwable) {
    view.hideLoading()
    when (error) {
      is ConnectException, is UnknownHostException -> view.showNetworkError()
      else -> view.showOtherError()
    }
  }

  protected fun onGetRepositoryListComplete() {
    view.hideLoading()
  }
}
