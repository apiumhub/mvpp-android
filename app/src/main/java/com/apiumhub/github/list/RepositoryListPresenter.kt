package com.apiumhub.github.list

import com.apiumhub.github.core.domain.entity.Repository
import io.reactivex.Scheduler


interface RepositoryListView {
  fun bindSearch(func: (String) -> Unit)

  fun showLoading()
  fun hideLoading()

  fun showData(items: List<Repository>)
  fun showEmptyData()
  fun showNetworkError()
  fun showOtherError()

  fun bindDestroy(func: () -> Unit)

  companion object {
    fun create() = RepositoryListFragment.newInstance()
  }
}

interface RepositoryListService {
  fun search(query: String)

  fun bindStart(func: () -> Unit)
  fun bindStop(func: () -> Unit)

  fun bindData(func: (List<Repository>) -> Unit)
  fun bindEmptyData(func: () -> Unit)
  fun bindNetworkError(func: () -> Unit)
  fun bindOtherError(func: () -> Unit)

  fun cancel()

  companion object {
    fun create(
      networkRepository: RepositoryListRepository,
      inMemoryRepository: RepositoryListRepository,
      observeOn: Scheduler,
      subscribeOn: Scheduler
    ): RepositoryListService =
      RepositoryListCoordinator(
        networkRepository,
        inMemoryRepository,
        observeOn,
        subscribeOn
      )
  }
}

class RepositoryListPresenter(view: RepositoryListView, service: RepositoryListService) {
  init {
    view.bindSearch(service::search)
    view.bindDestroy(service::cancel)

    service.bindStart(view::showLoading)
    service.bindStop(view::hideLoading)

    service.bindData(view::showData)
    service.bindEmptyData(view::showEmptyData)
    service.bindNetworkError(view::showNetworkError)
    service.bindOtherError(view::showOtherError)
  }
}
