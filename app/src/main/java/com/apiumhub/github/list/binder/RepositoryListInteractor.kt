package com.apiumhub.github.list.binder

import com.apiumhub.github.core.domain.entity.Repository
import com.apiumhub.github.list.RepositoryListRepository
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy

class RepositoryListInteractor(
  private val networkRepository: RepositoryListRepository,
  private val inMemoryRepository: RepositoryListRepository,
  private val observeOn: Scheduler,
  private val subscribeOn: Scheduler
) : RepositoryListService {

  private val disposeBag = CompositeDisposable()

  override fun search(
    query: String, onNext: (List<Repository>?) -> Unit, onError: (Throwable) -> Unit, onComplete: () -> Unit
  ) {
    disposeBag.add(
      if (query.isEmpty()) {
        findAll(onNext, onError, onComplete)
      } else {
        searchByQuery(query, onNext, onError, onComplete)
      }
    )
  }

  override fun cancel() {
    disposeBag.clear()
  }

  private fun findAll(onNext: (List<Repository>?) -> Unit, onError: (Throwable) -> Unit, onComplete: () -> Unit) =
    inMemoryRepository.findAllRepositories()
      .flatMap {
        if (it.isNotEmpty()) onNext(it)
        networkRepository.findAllRepositories()
      }
      .observeOn(observeOn)
      .subscribeOn(subscribeOn)
      .subscribeBy(
        onError = onError,
        onNext = {
          inMemoryRepository.addOrUpdateRepositories(it)
          onNext(it)
        },
        onComplete = onComplete
      )


  private fun searchByQuery(
    query: String,
    onNext: (List<Repository>?) -> Unit,
    onError: (Throwable) -> Unit,
    onComplete: () -> Unit
  ) =
    inMemoryRepository.searchRepositories(query)
      .flatMap {
        it.items?.isNotEmpty()?.let { isNotEmpty ->
          if (isNotEmpty) onNext(it.items)
        }
        networkRepository.searchRepositories(query)
      }
      .observeOn(observeOn)
      .subscribeOn(subscribeOn)
      .subscribeBy(
        onError = onError,
        onNext = {
          inMemoryRepository.addOrUpdateRepositorySearch(query, it)
          onNext(it.items)
        },
        onComplete = onComplete
      )

}
