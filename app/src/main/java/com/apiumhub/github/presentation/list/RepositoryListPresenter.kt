package com.apiumhub.github.presentation.list

import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.repository.list.RepositoryListService


sealed class RepositoryListInput {
  object FIND_ALL : RepositoryListInput()
  class SEARCH(val query: String) : RepositoryListInput()
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
  fun findAll(func: () -> Unit)
  fun search(func: (String) -> Unit)

  //output
  fun onData(data: List<Repository>)
  fun onEmpty()
  fun onError()
  fun startLoading()
  fun stopLoading()

  companion object {
    fun create() = RepositoryListFragment.newInstance()
    fun createPresenter(view: RepositoryListView) = RepositoryListPresenter(view, RepositoryListService.create())
  }
}

class RepositoryListPresenter(view: RepositoryListView, service: RepositoryListService) {
  init {
    view.findAll(service::findAll)
    view.search(service::search)

    service.onData(view::onData)
    service.onEmpty(view::onEmpty)
    service.onErrorNoInternet(view::onError)
    service.onErrorNullList(view::onError)
    service.onErrorOther(view::onError)
    service.onStartLoading(view::startLoading)
    service.onStopLoading(view::stopLoading)
  }
}

