package com.apiumhub.github.domain.repository.list

import com.apiumhub.github.data.IGithubRepository
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.presentation.list.RepositoryListOutput
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.UnknownHostException

interface RepositoryListService {
  //input
  fun findAll()
  fun search(query: String)

  //output
  fun onData(func: (List<Repository>) -> Unit)
  fun onEmpty(func: () -> Unit)
  fun onErrorNullList(func: () -> Unit)
  fun onErrorNoInternet(func: () -> Unit)
  fun onErrorOther(func: () -> Unit)
  fun onStartLoading(func: () -> Unit)
  fun onStopLoading(func: () -> Unit)

  companion object {
    fun create(): RepositoryListService =
      RepositoryListServiceImpl(IGithubRepository.create())
  }
}

class RepositoryListServiceImpl(private val repository: IGithubRepository) :
  RepositoryListService {

  private val subject = PublishSubject.create<RepositoryListOutput>()
  private val disposeBag = CompositeDisposable()

  override fun findAll() {
    GlobalScope.launch(Job() + Dispatchers.Main) {
      subject.onNext(RepositoryListOutput.Start)

      try {
        val items = repository.findAllRepositories()

        when {
          items.isEmpty() -> subject.onNext(RepositoryListOutput.Empty)
          else -> subject.onNext(RepositoryListOutput.Found(items))
        }
        subject.onNext(RepositoryListOutput.Stop)
      } catch (exception: Exception) {
        if (exception is UnknownHostException) {
          subject.onNext(RepositoryListOutput.ErrorNoInternet)
        } else {
          subject.onNext(RepositoryListOutput.ErrorOther)
        }
        subject.onNext(RepositoryListOutput.Stop)
      }
    }
  }

  override fun search(query: String) {
    GlobalScope.launch(Job() + Dispatchers.Main) {
      subject.onNext(RepositoryListOutput.Start)

      try {
        val result = repository.searchRepositories(query)
        val items = result.items
        when {
          items == null -> subject.onNext(RepositoryListOutput.ErrorNullList)
          items.isEmpty() -> subject.onNext(RepositoryListOutput.Empty)
          else -> subject.onNext(RepositoryListOutput.Found(items))
        }
        subject.onNext(RepositoryListOutput.Stop)
      } catch (exception: Exception) {
        if (exception is UnknownHostException) {
          subject.onNext(RepositoryListOutput.ErrorNoInternet)
        } else {
          subject.onNext(RepositoryListOutput.ErrorOther)
        }
        subject.onNext(RepositoryListOutput.Stop)
      }
    }
  }

  // output events
  override fun onData(func: (List<Repository>) -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryListOutput.Found }.subscribe { func((it as RepositoryListOutput.Found).list) })
  }

  override fun onEmpty(func: () -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryListOutput.Empty }.subscribe { func() })
  }

  override fun onErrorNullList(func: () -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryListOutput.ErrorNullList }.subscribe { func() })
  }

  override fun onErrorNoInternet(func: () -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryListOutput.ErrorNoInternet }.subscribe { func() })
  }

  override fun onErrorOther(func: () -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryListOutput.ErrorOther }.subscribe { func() })
  }

  override fun onStartLoading(func: () -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryListOutput.Start }.subscribe { func() })
  }

  override fun onStopLoading(func: () -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryListOutput.Stop }.subscribe { func() })
  }
}
