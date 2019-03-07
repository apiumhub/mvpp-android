package com.apiumhub.github.presentation.list.presenter

import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.repository.list.RepositoryListService
import com.apiumhub.github.presentation.list.IRepositoryListView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RepositoryListPresenterClassicBinder(
  private val service: RepositoryListService,
  private val onRepositoryListFound: (List<Repository>) -> Unit,
  private val onRepositoryListError: (Throwable) -> Unit
) {

  fun fetchRepositoryList() {
    GlobalScope.launch {
      try {
        val result = service.findAll()
        onRepositoryListFound(result)
      } catch (exception: Exception) {
        onRepositoryListError(exception)
      }
    }
  }

  fun fetchRepositoryListByQuery(query: String) {
    GlobalScope.launch {
      try {
        val result = service.search(query)
        onRepositoryListFound(result)
      } catch (exception: Exception) {
        onRepositoryListError(exception)
      }
    }
  }
}

class RepositoryListPresenterClassic(
  private var view: IRepositoryListView?,
  private val fetchRepositoryList: () -> Unit,
  private val fetchRepositoryListByQuery: (String) -> Unit
) {

  fun findAll() {
    fetchRepositoryList()
    view?.showLoading()
  }

  fun findFilterByQuery(query: String) {
    fetchRepositoryListByQuery(query)
    view?.showLoading()
  }

  private fun onRepoListError(throwable: Throwable) {

  }

  fun RepositoryListPresenterClassic.onRepositoryListError(throwable: Throwable) {
    view?.hideLoading()
    view?.showError(throwable)
  }

  fun RepositoryListPresenterClassic.onRepositoryListFound(list: List<Repository>) {
    view?.hideLoading()

    if (list.isEmpty()) {
      view?.itemsEmpty()
    } else {
      view?.itemsLoaded(list)
    }
  }
}