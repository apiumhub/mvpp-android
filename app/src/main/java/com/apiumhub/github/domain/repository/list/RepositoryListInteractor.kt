package com.apiumhub.github.domain.repository.list

import com.apiumhub.github.data.GithubRepository
import com.apiumhub.github.data.oncache.OnMemoryRepository
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.repository.BaseInteractor
import com.apiumhub.github.domain.repository.BaseService
import com.apiumhub.github.domain.repository.Event
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject

interface RepositoryListService : BaseService {
  fun search(query: String = "")
  fun onDataFound(func: (List<Repository>) -> Unit)

  companion object {
    fun create(
      repository: GithubRepository,
      observeOn: Scheduler,
      subscribeOn: Scheduler,
      onMemoryRepository: OnMemoryRepository
    ): RepositoryListService =
      RepositoryListInteractor(repository, observeOn, subscribeOn, onMemoryRepository)
  }
}

class RepositoryListInteractor(
  private val repository: GithubRepository,
  observeOn: Scheduler,
  subscribeOn: Scheduler,
  private val onMemoryRepository: OnMemoryRepository
) : BaseInteractor(observeOn, subscribeOn), RepositoryListService {

  private val successStream: PublishSubject<List<Repository>> = PublishSubject.create()

  override fun search(query: String) {
    if (query.isEmpty()) {
      val items = onMemoryRepository.getRepositories()
      if (items.isEmpty()) {
        execute(repository.findAllRepositories()) {
          onMemoryRepository.addOrUpdateRepositories(it)
          successStream.onNext(it)
        }
      } else {
        successStream.onNext(items)
      }
    } else {
      val searchDto = onMemoryRepository.getRepositoriesByQuery()
      if (searchDto.items == null || searchDto.items.isEmpty()) {
        execute(repository.searchRepositories(query)) {
          onMemoryRepository.addOrUpdateRepositoriesByQuery(it)
          when {
            it.items == null -> subject.onNext(Event.ERROR_NULL)
            it.items.isEmpty() -> subject.onNext(Event.EMPTY)
            else -> successStream.onNext(it.items)
          }
        }
      } else {
        successStream.onNext(searchDto.items)
      }
    }
  }

  override fun onDataFound(func: (List<Repository>) -> Unit) {
    disposeBag.add(successStream.subscribe { func(it) })
  }
}
