package com.apiumhub.github.presentation.list

import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.repository.list.RepositoryListService

sealed class RepositoryListInput {
  class SEARCH(val query: String = "") : RepositoryListInput()
}

sealed class RepositoryListOutput {
  class Found(val list: List<Repository>) : RepositoryListOutput()
  object Empty : RepositoryListOutput()
  object ErrorNullList : RepositoryListOutput()
  object ErrorNoInternet : RepositoryListOutput()
  object ErrorOther : RepositoryListOutput()
  object Start : RepositoryListOutput()
  object Stop : RepositoryListOutput()
}

interface RepositoryListView {
  //input
  fun search(func: (String) -> Unit)

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
    view.search(service::search)

    service.onStart(view::showLoading)
    service.onStop(view::hideLoading)

    service.onDataFound(view::showData)
    service.onEmpty(view::showEmpty)
    service.onErrorNoInternet(view::showError)
    service.onErrorNullList(view::showError)
    service.onErrorOther(view::showError)
  }
}

