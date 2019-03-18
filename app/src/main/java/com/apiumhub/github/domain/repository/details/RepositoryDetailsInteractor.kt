package com.apiumhub.github.domain.repository.details

import com.apiumhub.github.data.GithubRepository
import com.apiumhub.github.domain.entity.RepositoryDetailsDto
import com.apiumhub.github.domain.repository.EventInteractor
import com.apiumhub.github.domain.repository.EventService
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.PublishSubject

sealed class RepositoryDetailsEvent {
  class DetailsLoaded(val details: RepositoryDetailsDto) : RepositoryDetailsEvent()
  class ReadmeLoaded(val readme: String) : RepositoryDetailsEvent()
}

interface RepositoryDetailsService : EventService {
  fun getRepositoryDetails(user: String, repositoryName: String)

  fun onDetailsLoaded(func: (details: RepositoryDetailsDto) -> Unit)
  fun onReadmeLoaded(func: (readme: String) -> Unit)

  companion object {
    fun create(
      repository: GithubRepository,
      observeOn: Scheduler,
      subscribeOn: Scheduler
    ): RepositoryDetailsService = RepositoryDetailsInteractor(repository, observeOn, subscribeOn)
  }
}

class RepositoryDetailsInteractor(
  private val repository: GithubRepository,
  observeOn: Scheduler,
  subscribeOn: Scheduler
) : EventInteractor(observeOn, subscribeOn), RepositoryDetailsService {

  private val stream: PublishSubject<RepositoryDetailsEvent> = PublishSubject.create()

  override fun getRepositoryDetails(user: String, repositoryName: String) {
    getReadmeInternal(user, repositoryName)
    getDetailsInternal(user, repositoryName)
  }

  private fun getReadmeInternal(user: String, repositoryName: String) {
    execute(repository.getReadmeForRepository(user, repositoryName)) {
      stream.onNext(RepositoryDetailsEvent.ReadmeLoaded(it))
    }
  }

  private fun getDetailsInternal(user: String, repositoryName: String) {
    val observable = Observables.combineLatest(
      repository.getCommitsForRepository(user, repositoryName),
      repository.getBranchesForRepository(user, repositoryName)
    )

    execute(observable) {
      stream.onNext(RepositoryDetailsEvent.DetailsLoaded(RepositoryDetailsDto(it.first.size, it.second.size)))
    }
  }

  override fun onDetailsLoaded(func: (details: RepositoryDetailsDto) -> Unit) {
    disposeBag.add(stream.filter { it is RepositoryDetailsEvent.DetailsLoaded }.subscribe { func((it as RepositoryDetailsEvent.DetailsLoaded).details) })
  }

  override fun onReadmeLoaded(func: (readme: String) -> Unit) {
    disposeBag.add(stream.filter { it is RepositoryDetailsEvent.ReadmeLoaded }.subscribe { func((it as RepositoryDetailsEvent.ReadmeLoaded).readme) })
  }

//  private fun getCommitsInternal(user: String, repositoryName: String) {
//    disposeBag.add(repository
//      .getCommitsForRepository(user, repositoryName)
//      .subscribeOn(Schedulers.newThread())
//      .observeOn(AndroidSchedulers.mainThread())
//      .flatMapIterable { it }
//      .reduce(0) { acc, next ->
//        acc + next.total!!
//      }
//      .subscribe { count -> commitsCountSubject.onNext(count!!) })
//  }
//
//  private fun getBranchesInternal(user: String, repositoryName: String) {
//    disposeBag.add(repository
//      .getBranchesForRepository(user, repositoryName)
//      .subscribeOn(Schedulers.newThread())
//      .observeOn(AndroidSchedulers.mainThread())
//      .subscribe { branchesCountSubject.onNext(it.count()) })
//  }
//
//  private fun getReadmeInternal(user: String, repositoryName: String) {
//    disposeBag.add(repository.getReadmeForRepository(user, repositoryName)
//      .subscribeOn(Schedulers.newThread())
//      .observeOn(AndroidSchedulers.mainThread())
//      .subscribe { loadedReadmePublishSubject.onNext(it) })
//  }
//
//  private fun combineRepositoryDetailsInternal() {
//    disposeBag.add(Observables
//      .combineLatest(commitsCountSubject, branchesCountSubject) { commits, branches ->
//        RepositoryDetailsDto(commits, branches)
//      }
//      .subscribe { repositoryDetailsPublishSubject.onNext(it) })
//  }
}