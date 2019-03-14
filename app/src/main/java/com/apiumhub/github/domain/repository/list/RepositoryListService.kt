package com.apiumhub.github.domain.repository.list

import com.apiumhub.github.data.IGithubRepository
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.presentation.list.RepositoryListEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.*
import java.net.UnknownHostException

interface RepositoryListService {
  //input
  fun search(query: String)

  //output
  fun onStart(func: () -> Unit)
  fun onStop(func: () -> Unit)

  fun onDataFound(func: (List<Repository>) -> Unit)
  fun onEmpty(func: () -> Unit)
  fun onErrorNullList(func: () -> Unit)
  fun onErrorNoInternet(func: () -> Unit)
  fun onErrorOther(func: () -> Unit)

  companion object {
    fun create(repository: IGithubRepository = IGithubRepository.create()): RepositoryListService =
      RepositoryListServiceImpl(repository)
  }
}

class RepositoryListServiceImpl(private val repository: IGithubRepository) :
  RepositoryListService {

  private val subject = PublishSubject.create<RepositoryListEvent>()
  private val disposeBag = CompositeDisposable()

  override fun search(query: String) {
    GlobalScope.launch(Job() + Dispatchers.Main) {
      subject.onNext(RepositoryListEvent.Start)

      try{
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

      }catch (exception: Exception) {
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
