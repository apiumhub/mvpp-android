package com.apiumhub.github.presentation.list.presenter

import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.repository.list.RepositoryListService
import com.apiumhub.github.presentation.list.IRepositoryListView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class RepositoryListPresenterChildBinder(private val service: RepositoryListService): RepositoryListPresenterParent(), CoroutineScope {
  private lateinit var job: Job
  override val coroutineContext: CoroutineContext
    get() = job + Dispatchers.Main

  override fun onInitialize() {
    this.job = Job()
  }

  override fun onDestroy() {
    this.job.cancel()
  }

  override fun fetchRepositoryList() {
    launch {
      try {
        val result = service.findAll()
        onRepositoryListFound(result)
      } catch (exception: Exception) {
        onRepositoryListError(exception)
      }
    }
  }

  override fun fetchRepositoryListByQuery(query: String) {
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

open class RepositoryListPresenterParent {
  protected open fun onInitialize() {}
  protected open fun onDestroy() {}
  protected open fun fetchRepositoryList() {}
  protected open fun fetchRepositoryListByQuery(query: String) {}

  private var view: IRepositoryListView? = null

  fun onViewCreated(view: IRepositoryListView) {
    this.view = view
    onInitialize()
    findAll()
  }

  fun onDestroyView(){
    onDestroy()
    this.view = null
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
}