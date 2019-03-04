package com.apiumhub.github.presentation.list

import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.repository.list.RepositoryListService

interface IRepositoryListView {
  fun itemsLoaded(items: List<Repository>)

  companion object {
    fun create() = RepositoryListFragment.newInstance()
  }
}

class RepositoryListPresenterBinder(view: IRepositoryListView, private val service: RepositoryListService) :
  RepositoryListPresenter(view) {

  override fun onViewCreated() {
    service.findAll(::onRepositoryListFound, ::onRepositoryListError)
  }

  override fun onSearch(query: String) {
    service.search(query, ::onRepositoryListFound, ::onRepositoryListError)
  }

  override fun onDestroyView() {
    service.clear()
  }
}

open class RepositoryListPresenter(private val view: IRepositoryListView) {
  open fun onViewCreated() {}
  open fun onDestroyView() {}
  protected open fun onSearch(query: String) {}

  fun findAll() {
    onViewCreated()
  }

  fun findFilterByQuery(query: String) {
    onSearch(query)
  }

  fun onRepositoryListFound(list: List<Repository>) {
    view.itemsLoaded(list)
  }

  fun onRepositoryListError(throwable: Throwable) {}
}