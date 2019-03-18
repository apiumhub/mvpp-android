package com.apiumhub.github.domain.repository.list

import com.apiumhub.github.data.GithubRepository
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.repository.common.EventService
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext

sealed class RepositoryListEvent {
  class Found(val list: List<Repository>) : RepositoryListEvent()
  object Empty : RepositoryListEvent()
  object ErrorNull : RepositoryListEvent()
  object ErrorNoInternet : RepositoryListEvent()
  object ErrorOther : RepositoryListEvent()
  object Start : RepositoryListEvent()
  object Stop : RepositoryListEvent()
}


interface RepositoryListService : EventService {
  fun search(query: CharSequence)
  fun onDataFound(func: (List<Repository>) -> Unit)


  companion object {
    fun create(
      repository: GithubRepository,
      subject: PublishSubject<RepositoryListEvent>,
      dispatcher: CoroutineDispatcher
    ): RepositoryListService =
      RepositoryListInteractor(repository, subject, dispatcher)
  }
}

class RepositoryListInteractor(
  private val repository: GithubRepository,
  private val subject: PublishSubject<RepositoryListEvent>,
  private val dispatcher: CoroutineDispatcher
) : RepositoryListService, CoroutineScope {
  private val job = Job()
  override val coroutineContext: CoroutineContext
    get() = job + dispatcher

  private val disposeBag = CompositeDisposable()

  override fun search(query: CharSequence) {
    launch {
      subject.onNext(RepositoryListEvent.Start)

      try {
        val text = query.trim().toString()
        val result = if (text.isNotEmpty()) {
          repository.searchRepositories(text).items
        } else {
          repository.findAllRepositories()
        }

        when {
          result == null -> subject.onNext(RepositoryListEvent.ErrorNull)
          result.isEmpty() -> subject.onNext(RepositoryListEvent.Empty)
          else -> subject.onNext(RepositoryListEvent.Found(result))
        }

      } catch (exception: Exception) {
        when (exception) {
          is IllegalArgumentException -> subject.onNext(RepositoryListEvent.ErrorNull)
          is UnknownHostException -> subject.onNext(RepositoryListEvent.ErrorNoInternet)
          else -> subject.onNext(RepositoryListEvent.ErrorOther)
        }
      }

      subject.onNext(RepositoryListEvent.Stop)
    }
  }

  override fun cancel() {
    job.cancel()
  }

  // output events

  override fun onDataFound(func: (List<Repository>) -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryListEvent.Found }.subscribe { func((it as RepositoryListEvent.Found).list) })
  }

  override fun onStart(func: () -> Unit) {
    subscribeEvent(RepositoryListEvent.Start, func)
  }

  override fun onStop(func: () -> Unit) {
    subscribeEvent(RepositoryListEvent.Stop, func)
  }


  override fun onEmpty(func: () -> Unit) {
    subscribeEvent(RepositoryListEvent.Empty, func)
  }

  override fun onErrorNullList(func: () -> Unit) {
    subscribeEvent(RepositoryListEvent.ErrorNull, func)
  }

  override fun onErrorNoInternet(func: () -> Unit) {
    subscribeEvent(RepositoryListEvent.ErrorNoInternet, func)
  }

  override fun onErrorOther(func: () -> Unit) {
    subscribeEvent(RepositoryListEvent.ErrorOther, func)
  }

  private fun subscribeEvent(event: RepositoryListEvent, func: () -> Unit) {
    disposeBag.add(subject.filter { it == event }.subscribe { func() })
  }
}
