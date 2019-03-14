package com.apiumhub.github.domain.repository.details

import com.apiumhub.github.data.GithubRepository
import com.apiumhub.github.domain.entity.RepositoryDetailsDto
import com.apiumhub.github.domain.repository.common.EventService
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext

sealed class RepositoryDetailsEvent {
  class DetailsLoaded(val details: RepositoryDetailsDto) : RepositoryDetailsEvent()
  class ReadmeLoaded(val readme: String) : RepositoryDetailsEvent()
  object Empty : RepositoryDetailsEvent()
  object ErrorNull : RepositoryDetailsEvent()
  object ErrorNoInternet : RepositoryDetailsEvent()
  object ErrorOther : RepositoryDetailsEvent()
  object Start : RepositoryDetailsEvent()
  object Stop : RepositoryDetailsEvent()
}

interface RepositoryDetailsService : EventService {
  fun getRepositoryDetails(user: String, repositoryName: String)

  fun onDetailsLoaded(func: (details: RepositoryDetailsDto) -> Unit)
  fun onReadmeLoaded(func: (readme: String) -> Unit)

  companion object {
    fun create(
      repository: GithubRepository,
      subject: PublishSubject<RepositoryDetailsEvent>,
      dispatcher: CoroutineDispatcher
    ): RepositoryDetailsService = RepositoryDetailsInteractor(repository, subject, dispatcher)
  }
}

class RepositoryDetailsInteractor(
  private val repository: GithubRepository,
  private val subject: PublishSubject<RepositoryDetailsEvent>,
  private val dispatcher: CoroutineDispatcher
) : RepositoryDetailsService, CoroutineScope {

  private val job = Job()
  private val disposeBag = CompositeDisposable()

  override val coroutineContext: CoroutineContext
    get() = job + dispatcher

  override fun getRepositoryDetails(user: String, repositoryName: String) {
    launch {
      subject.onNext(RepositoryDetailsEvent.Start)
      try {
        val commitsInternal = repository.getCommitsForRepository(user, repositoryName)
        val branchesInternal = repository.getBranchesForRepository(user, repositoryName)

        val readmeInternal = repository.getReadmeForRepository(user, repositoryName)
        subject.onNext(RepositoryDetailsEvent.ReadmeLoaded(readmeInternal))
        val result = RepositoryDetailsDto(commitsInternal.size, branchesInternal.size)
        subject.onNext(RepositoryDetailsEvent.DetailsLoaded(result))
      } catch (exception: Exception) {
        when (exception) {
          is IllegalArgumentException -> subject.onNext(RepositoryDetailsEvent.ErrorNull)
          is UnknownHostException -> subject.onNext(RepositoryDetailsEvent.ErrorNoInternet)
          else -> subject.onNext(RepositoryDetailsEvent.ErrorOther)
        }
      }
      subject.onNext(RepositoryDetailsEvent.Stop)
    }
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

  override fun cancel() {
    job.cancel()
  }

  override fun onStart(func: () -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryDetailsEvent.Start }.subscribe { func() })
  }

  override fun onEmpty(func: () -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryDetailsEvent.Empty }.subscribe { func() })
  }

  override fun onErrorNullList(func: () -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryDetailsEvent.ErrorNull }.subscribe { func() })
  }

  override fun onErrorNoInternet(func: () -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryDetailsEvent.ErrorNoInternet }.subscribe { func() })
  }

  override fun onErrorOther(func: () -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryDetailsEvent.ErrorOther }.subscribe { func() })
  }

  override fun onStop(func: () -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryDetailsEvent.Stop }.subscribe { func() })
  }

  override fun onDetailsLoaded(func: (details: RepositoryDetailsDto) -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryDetailsEvent.DetailsLoaded }.subscribe { func((it as RepositoryDetailsEvent.DetailsLoaded).details) })
  }

  override fun onReadmeLoaded(func: (readme: String) -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryDetailsEvent.ReadmeLoaded }.subscribe { func((it as RepositoryDetailsEvent.ReadmeLoaded).readme) })
  }
}