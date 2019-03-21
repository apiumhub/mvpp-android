package com.apiumhub.github.domain

import com.apiumhub.github.data.RepositoryListRepository
import com.apiumhub.github.domain.common.BaseInteractor
import com.apiumhub.github.domain.common.BaseService
import com.apiumhub.github.domain.common.Event
import com.apiumhub.github.domain.entity.Repository
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject

interface RepositoryListService : BaseService {
  fun search(query: String)
  fun onDataFound(func: (List<Repository>) -> Unit)

  companion object {
    fun create(
      networkRepository: RepositoryListRepository,
      inMemoryRepository: RepositoryListRepository,
      observeOn: Scheduler,
      subscribeOn: Scheduler
    ): RepositoryListService = RepositoryListInteractor(networkRepository, inMemoryRepository, observeOn, subscribeOn)
  }
}

class RepositoryListInteractor(
  private val networkRepository: RepositoryListRepository,
  private val inMemoryRepository: RepositoryListRepository,
  observeOn: Scheduler,
  subscribeOn: Scheduler
) : BaseInteractor(observeOn, subscribeOn), RepositoryListService {

  private val successStream: PublishSubject<List<Repository>> = PublishSubject.create()

  override fun search(query: String) {
    if (query.isEmpty()) {
      findAll()
    } else {
      searchByQuery(query)
    }
  }

  private fun findAll() {
    execute(inMemoryRepository.findAllRepositories().flatMap {
      if (it.isNotEmpty()) successStream.onNext(it)
      networkRepository.findAllRepositories()
    }) {
      inMemoryRepository.addOrUpdateRepositories(it)
      successStream.onNext(it)
    }
  }

  private fun searchByQuery(query: String) {
    execute(inMemoryRepository.searchRepositories(query)
      .flatMap {
        if (it.items!!.isNotEmpty()) successStream.onNext(it.items)
        networkRepository.searchRepositories(query)
      }
    ) {
      inMemoryRepository.addOrUpdateRepositorySearch(it)
      when {
        it.items == null -> subject.onNext(Event.ERROR_NULL)
        it.items.isEmpty() -> subject.onNext(Event.EMPTY)
        else -> successStream.onNext(it.items)
      }
    }
  }

  override fun onDataFound(func: (List<Repository>) -> Unit) {
    disposeBag.add(successStream.subscribe { func(it) })
  }
}
