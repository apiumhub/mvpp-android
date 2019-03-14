package com.apiumhub.github.domain.repository.list

import com.apiumhub.github.data.IGithubRepository
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.presentation.list.RepositoryListEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext

interface RepositoryListService {
  fun search(query: String)

  fun onStart(func: () -> Unit)
  fun onStop(func: () -> Unit)
  fun onDataFound(func: (List<Repository>) -> Unit)
  fun onEmpty(func: () -> Unit)
  fun onErrorNullList(func: () -> Unit)
  fun onErrorNoInternet(func: () -> Unit)
  fun onErrorOther(func: () -> Unit)

  companion object {
    fun create(repository: IGithubRepository, subject: PublishSubject<RepositoryListEvent>, dispatcher: CoroutineDispatcher): RepositoryListService =
      RepositoryListServiceImpl(repository,subject, dispatcher)
  }
}

class RepositoryListServiceImpl(
  private val repository: IGithubRepository,
  private val subject: PublishSubject<RepositoryListEvent>,
  private val dispatcher: CoroutineDispatcher
) : RepositoryListService, CoroutineScope {
  private val job = Job()
  override val coroutineContext: CoroutineContext
    get() = job + dispatcher

  private val disposeBag = CompositeDisposable()

  override fun search(query: String) {
    launch {
      subject.onNext(RepositoryListEvent.Start)

      try {
        val result = if (query.isNotEmpty()) {
          repository.searchRepositories(query).items
        } else {
          repository.findAllRepositories()
        }

        when {
          result == null -> subject.onNext(RepositoryListEvent.ErrorNullList)
          result.isEmpty() -> subject.onNext(RepositoryListEvent.Empty)
          else -> subject.onNext(RepositoryListEvent.Found(result))
        }

      } catch (exception: Exception) {
        if (exception is UnknownHostException) subject.onNext(RepositoryListEvent.ErrorNoInternet)
        else subject.onNext(RepositoryListEvent.ErrorOther)
      }

      subject.onNext(RepositoryListEvent.Stop)
    }
  }


  // output events
  override fun onStart(func: () -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryListEvent.Start }.subscribe { func() })
  }

  override fun onDataFound(func: (List<Repository>) -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryListEvent.Found }.subscribe { func((it as RepositoryListEvent.Found).list) })
  }

  override fun onEmpty(func: () -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryListEvent.Empty }.subscribe { func() })
  }

  override fun onErrorNullList(func: () -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryListEvent.ErrorNullList }.subscribe { func() })
  }

  override fun onErrorNoInternet(func: () -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryListEvent.ErrorNoInternet }.subscribe { func() })
  }

  override fun onErrorOther(func: () -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryListEvent.ErrorOther }.subscribe { func() })
  }

  override fun onStop(func: () -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryListEvent.Stop }.subscribe { func() })
  }
}
