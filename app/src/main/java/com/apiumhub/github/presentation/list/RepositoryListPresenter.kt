package com.apiumhub.github.presentation.list

import com.apiumhub.github.data.IGithubRepository
import com.apiumhub.github.domain.repository.list.RepositoryListInteractor
import com.apiumhub.github.domain.entity.Repository

interface IRepositoryListView {
    fun loadItems(func: () -> Unit)
    fun searchItems(func: (query: String) -> Unit)
    fun itemsLoaded(items: List<Repository>)

    companion object {
        fun create() = RepositoryListFragment.newInstance()
    }
}

interface IRepositoryListService {
    fun findAll()
    fun search(query: String)
    fun onReposFound(func: (List<Repository>) -> Unit)

    companion object {
        fun create() = RepositoryListInteractor(IGithubRepository.create())
        fun create(repository: IGithubRepository) = RepositoryListInteractor(repository)
    }
}

class RepositoryListPresenter(view: IRepositoryListView, service: IRepositoryListService) {

    init {
        view.loadItems {
            service.findAll()
        }

        view.searchItems {
            service.search(it)
        }

        service.onReposFound {
            view.itemsLoaded(it)
        }
    }
}