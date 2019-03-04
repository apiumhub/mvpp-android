package com.apiumhub.github.domain.repository.list

import com.apiumhub.github.data.IGithubRepository
import com.apiumhub.github.domain.entity.Repository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

interface RepositoryListService {
  fun findAll(onSuccess: (List<Repository>) -> Unit, onError: (Throwable) -> Unit)
  fun search(query: String, onSuccess: (List<Repository>) -> Unit, onError: (Throwable) -> Unit)
  fun clear()

  companion object {
    fun create(): RepositoryListService = RepositoryListServiceImpl(IGithubRepository.create())
    fun create(repository: IGithubRepository): RepositoryListService = RepositoryListServiceImpl(repository)
  }
}

class RepositoryListServiceImpl(private val repository: IGithubRepository) : RepositoryListService {

  private val disposeBag = CompositeDisposable()

  override fun findAll(onSuccess: (List<Repository>) -> Unit, onError: (Throwable) -> Unit) {
    disposeBag.add(
      repository.findAllRepositories()
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(onError = onError, onSuccess = onSuccess)
    )
  }

  override fun search(query:String, onSuccess: (List<Repository>) -> Unit, onError: (Throwable) -> Unit) {
    disposeBag.add(
      repository.searchRepositories(query)
        .filter{ it.items != null}
        .map{
          it.items!!
        }
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeBy(onError = onError, onSuccess = onSuccess)
    )
  }

  override fun clear() {
    disposeBag.clear()
  }
}