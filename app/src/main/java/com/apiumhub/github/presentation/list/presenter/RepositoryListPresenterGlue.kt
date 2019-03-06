package com.apiumhub.github.presentation.list.presenter

import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.repository.list.RepositoryListService
import com.apiumhub.github.presentation.list.IRepositoryListView
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class RepositoryListPresenterGlue(private var view: IRepositoryListView?, private val service: RepositoryListService) {

  private fun findAll() {
    service.findAllGlue(
      {onRepositoryListEmpty()},
      {onRepositoryListError(it)},
      {onRepositoryListFound(it)}
    )
  }

  fun initialize() {
    findAll()
    view?.showLoading()
  }

  fun findFilterByQuery(query: String) {
    view?.showLoading()
  }

  private fun onRepositoryListFound(list: List<Repository>) {
    view?.hideLoading()
    view?.itemsLoaded(list)
  }

  private fun onRepositoryListEmpty() {
    view?.hideLoading()
    view?.itemsEmpty()
  }

  private fun onRepositoryListError(throwable: Throwable) {
    view?.hideLoading()
    view?.showError(throwable)
  }
}