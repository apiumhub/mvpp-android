package com.apiumhub.github.presentation.list.presenter

import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.repository.list.RepositoryListService
import com.apiumhub.github.presentation.list.IRepositoryListView
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class RepositoryListPresenterClassic(private var view: IRepositoryListView?, private val service: RepositoryListService, private val dispatcher: CoroutineDispatcher): CoroutineScope {
  private var job: Job = Job()
  override val coroutineContext: CoroutineContext
    get() = job + dispatcher

  fun onDestroyView(){
    this.job.cancel()
  }

  fun findAll() {
    fetchRepositoryList()
    view?.showLoading()
  }

  fun findFilterByQuery(query: String) {
    fetchRepositoryListByQuery(query)
    view?.showLoading()
  }

  fun onRepositoryListFound(list: List<Repository>) {
    view?.hideLoading()

    if (list.isEmpty()) {
      view?.itemsEmpty()
    } else {
      view?.itemsLoaded(list)
    }
  }

  fun onRepositoryListError(throwable: Throwable) {
    view?.hideLoading()
    view?.showError(throwable)
  }

  private fun fetchRepositoryList() {
    launch {
      try {
        val result = service.findAll()
        onRepositoryListFound(result)
      } catch (exception: Exception) {
        onRepositoryListError(exception)
      }
    }
  }

  private fun fetchRepositoryListByQuery(query: String) {
    launch {
      try {
        val result = service.search(query)
        onRepositoryListFound(result)
      } catch (exception: Exception) {
        onRepositoryListError(exception)
      }
    }
  }
}