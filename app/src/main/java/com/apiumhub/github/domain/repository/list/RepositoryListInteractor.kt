package com.apiumhub.github.domain.repository.list

import com.apiumhub.github.data.GithubRepository
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.entity.RepositorySearchDto
import com.apiumhub.github.domain.repository.BaseInteractor
import com.apiumhub.github.domain.repository.BaseService
import com.apiumhub.github.domain.repository.Event
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject

interface RepositoryListService : BaseService {
  fun search(query: CharSequence = "")
  fun onDataFound(func: (List<Repository>) -> Unit)

  companion object {
    fun create(repository: GithubRepository, observeOn: Scheduler, subscribeOn: Scheduler): RepositoryListService =
      RepositoryListInteractor(repository, observeOn, subscribeOn)
  }
}

class RepositoryListInteractor(
  private val repository: GithubRepository, observeOn: Scheduler, subscribeOn: Scheduler
) : BaseInteractor(observeOn, subscribeOn), RepositoryListService {

  private val successStream: PublishSubject<List<Repository>> = PublishSubject.create()

  override fun search(query: CharSequence) {
    execute(if (query.isEmpty()) repository.findAllRepositories() else repository.searchRepositories(query.trim().toString())) {
      if (it is RepositorySearchDto) {
        when {
          it.items == null -> subject.onNext(Event.ERROR_NULL)
          it.items.isEmpty() -> subject.onNext(Event.EMPTY)
          else -> successStream.onNext(it.items)
        }
      } else {
        successStream.onNext(it as List<Repository>)
      }
    }
  }

  override fun onDataFound(func: (List<Repository>) -> Unit) {
    disposeBag.add(successStream.subscribe { func(it) })
  }
}
