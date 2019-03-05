package com.apiumhub.github.domain.repository.list

import com.apiumhub.github.data.IGithubRepository
import com.apiumhub.github.domain.entity.Repository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

interface RepositoryListService {
  suspend fun findAll(): List<Repository>
  suspend fun search(query: String): List<Repository>

  companion object {
    fun create(): RepositoryListService = RepositoryListServiceImpl(IGithubRepository.create())
    fun create(repository: IGithubRepository): RepositoryListService = RepositoryListServiceImpl(repository)
  }
}

class RepositoryListServiceImpl(private val repository: IGithubRepository) : RepositoryListService {

  override suspend fun findAll() = repository.findAllRepositories()

  override suspend fun search(query:String): List<Repository>  = repository.searchRepositories(query).items.orEmpty()
}