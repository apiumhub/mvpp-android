package com.apiumhub.github.presentation.list

import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.repository.list.RepositoryListService

interface RepositoryListView {
  //input
  var onSearch: (String) -> Unit

  //output
  fun showData(data: List<Repository>)
  fun showEmpty()
  fun showError()
  fun showLoading()
  fun hideLoading()

  companion object {
    fun create() = RepositoryListFragment.newInstance()
  }
}

class RepositoryListPresenter(view: RepositoryListView, service: RepositoryListService) {
  init {
    view.onSearch = service::search

    service.onStart(view::showLoading)
    service.onStop(view::hideLoading)

    service.onDataFound(view::showData)
    service.onEmpty(view::showEmpty)
    service.onErrorNoInternet(view::showError)
    service.onErrorNullList(view::showError)
    service.onErrorOther(view::showError)
  }
}

