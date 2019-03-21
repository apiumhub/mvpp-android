package com.apiumhub.github.domain

import com.apiumhub.github.data.RepositoryDetailsRepository
import com.apiumhub.github.domain.common.BaseInteractor
import com.apiumhub.github.domain.common.BaseService
import com.apiumhub.github.domain.entity.RepositoryDetailsDto
import io.reactivex.Scheduler
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.PublishSubject

sealed class RepositoryDetailsEvent {
  class DetailsLoaded(val details: RepositoryDetailsDto) : RepositoryDetailsEvent()
  class ReadmeLoaded(val readme: String) : RepositoryDetailsEvent()
}

interface RepositoryDetailsService : BaseService {
  fun getRepositoryDetails(user: String, repositoryName: String)

  fun onDetailsLoaded(func: (details: RepositoryDetailsDto) -> Unit)
  fun onReadmeLoaded(func: (readme: String) -> Unit)

  companion object {
    fun create(
      githubRepository: RepositoryDetailsRepository,
      inMemoryRepository: RepositoryDetailsRepository,
      observeOn: Scheduler,
      subscribeOn: Scheduler
    ): RepositoryDetailsService =
      RepositoryDetailsInteractor(githubRepository, inMemoryRepository, observeOn, subscribeOn)
  }
}

class RepositoryDetailsInteractor(
  private val githubRepository: RepositoryDetailsRepository,
  private val inMemoryRepository: RepositoryDetailsRepository,
  observeOn: Scheduler,
  subscribeOn: Scheduler
) : BaseInteractor(observeOn, subscribeOn), RepositoryDetailsService {

  private val stream: PublishSubject<RepositoryDetailsEvent> = PublishSubject.create()

  override fun getRepositoryDetails(user: String, repositoryName: String) {
    getReadmeInternal(user, repositoryName)
    getDetailsInternal(user, repositoryName)
  }

  private fun getReadmeInternal(user: String, repositoryName: String) {
    execute(inMemoryRepository.getReadmeForRepository(user, repositoryName)
      .flatMap {
        if (it.isNotEmpty()) stream.onNext(RepositoryDetailsEvent.ReadmeLoaded(it))
        githubRepository.getReadmeForRepository(user, repositoryName)
      }) {
      inMemoryRepository.addOrUpdateReadme(it)
      stream.onNext(RepositoryDetailsEvent.ReadmeLoaded(it))
    }
  }

  private fun getDetailsInternal(user: String, repositoryName: String) {
    val observable = Observables.combineLatest(
      githubRepository.getCommitsForRepository(user, repositoryName),
      githubRepository.getBranchesForRepository(user, repositoryName)
    )

    execute(observable) {
      var repositoryDetailsDto = RepositoryDetailsDto(
        it.first.size,
        it.second.size
      )

      inMemoryRepository.addOrUpdateRepositoryDetails(repositoryDetailsDto)
      stream.onNext(RepositoryDetailsEvent.DetailsLoaded(repositoryDetailsDto))
    }
  }

  override fun onDetailsLoaded(func: (details: RepositoryDetailsDto) -> Unit) {
    disposeBag.add(stream.filter { it is RepositoryDetailsEvent.DetailsLoaded }.subscribe { func((it as RepositoryDetailsEvent.DetailsLoaded).details) })
  }

  override fun onReadmeLoaded(func: (readme: String) -> Unit) {
    disposeBag.add(stream.filter { it is RepositoryDetailsEvent.ReadmeLoaded }.subscribe { func((it as RepositoryDetailsEvent.ReadmeLoaded).readme) })
  }

//  private fun getCommitsInternal(user: String, repositoryName: String) {
//    disposeBag.add(githubRepository
//      .getCommitsForRepository(user, repositoryName)
//      .subscribeOn(Schedulers.newThread())
//      .observeOn(AndroidSchedulers.mainThread())
//      .flatMapIterable { it }
//      .reduce(0) { acc, next ->
//        acc + next.total!!
//      }
//      .subscribe { count -> commitsCountSubject.onNext(count!!) })
//  }
}