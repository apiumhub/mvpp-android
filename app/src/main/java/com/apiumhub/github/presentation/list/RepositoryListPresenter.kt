package com.apiumhub.github.presentation.list

import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.repository.list.RepositoryListService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

interface IRepositoryListView {
  fun hideLoading()
  fun showLoading()

  fun itemsEmpty()
  fun itemsLoaded(items: List<Repository>)
  fun showError(error: Throwable)

  companion object {
    fun create() = RepositoryListFragment.newInstance()
  }
}

class RepositoryListPresenterBinder(private val service: RepositoryListService) : CoroutineScope {
  private lateinit var job: Job
  override val coroutineContext: CoroutineContext
    get() = job + Dispatchers.Main

  fun onInitialize() {
    this.job = Job()
  }

  fun onDestroy() {
    this.job.cancel()
  }

  fun fetchRepositoryList(onSuccess: (List<Repository>) -> Unit = {}, onError: (Throwable) -> Unit = {}) {
    launch {
      try {
        val result = service.findAll()
        onSuccess(result)
      } catch (exception: Exception) {
        onError(exception)
      }
    }
  }

  fun fetchRepositoryListByQuery(query: String, onSuccess: (List<Repository>) -> Unit = {}, onError: (Throwable) -> Unit = {}) {
    launch {
      try {
        val result = service.search(query)
        onSuccess(result)
      } catch (exception: Exception) {
        onError(exception)
      }
    }
  }
}

class RepositoryListPresenter(private val view: IRepositoryListView, private val binder: RepositoryListPresenterBinder) {

  fun onViewCreated() {
    binder.onInitialize()
    findAll()
  }

  fun onDestroyView() {
    binder.onDestroy()
  }

  fun findAll() {
    binder.fetchRepositoryList(::onRepositoryListFound, ::onRepositoryListError)
    view.showLoading()
  }

  fun findFilterByQuery(query: String) {
    binder.fetchRepositoryListByQuery(query, ::onRepositoryListFound, ::onRepositoryListError)
    view.showLoading()
  }

  fun onRepositoryListFound(list: List<Repository>) {
    view.hideLoading()

    if (list.isEmpty()) {
      view.itemsEmpty()
    } else {
      view.itemsLoaded(list)
    }
  }

  fun onRepositoryListError(throwable: Throwable) {
    view.hideLoading()
    view.showError(throwable)
  }
}