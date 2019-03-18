package com.apiumhub.github.domain.repository.list

import com.apiumhub.github.data.GithubRepository
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.entity.RepositorySearchDto
import com.apiumhub.github.domain.repository.EventInteractor
import com.apiumhub.github.domain.repository.EventService
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject

interface RepositoryListService : EventService {
  fun search(query: CharSequence)
  fun onDataFound(func: (List<Repository>) -> Unit)

  companion object {
    fun create(repository: GithubRepository, observeOn: Scheduler, subscribeOn: Scheduler): RepositoryListService =
      RepositoryListInteractor(repository, observeOn, subscribeOn)
  }
}

class RepositoryListInteractor(
  private val repository: GithubRepository, observeOn: Scheduler, subscribeOn: Scheduler
) : EventInteractor(observeOn, subscribeOn), RepositoryListService {

  private val successStream: PublishSubject<List<Repository>> = PublishSubject.create()

  override fun search(query: CharSequence) {
    execute(if (query.isEmpty()) repository.findAllRepositories() else repository.searchRepositories(query.trim().toString())) {
      successStream.onNext(
        if (it is RepositorySearchDto) it.items!! else (it as List<Repository>)
      )
    }
  }

  override fun onDataFound(func: (List<Repository>) -> Unit) {
    disposeBag.add(successStream.subscribe { func(it) })
  }
}
