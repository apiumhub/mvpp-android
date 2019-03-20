package com.apiumhub.github.arch

import android.app.Fragment
import android.os.Bundle
import android.view.View
import com.apiumhub.github.data.GithubRepository
import com.apiumhub.github.domain.entity.Repository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.net.ConnectException
import java.net.UnknownHostException
import kotlin.reflect.KProperty

///////// DSL

sealed class EventDelegate {
  class Found<T>(val value: T) : EventDelegate()
  object Start : EventDelegate()
  object Stop : EventDelegate()
  object Empty : EventDelegate()
  object ErrorIllegalArgument : EventDelegate()
  object ErrorNoInternet : EventDelegate()
  object ErrorOther : EventDelegate()
}

class OutDelegate<T : Any> {
  private lateinit var value: OutDelegateEvent<T>

  operator fun getValue(thisRef: Any?, property: KProperty<*>): OutDelegateEvent<T> {
    return value
  }

  operator fun setValue(thisRef: Any?, property: KProperty<*>, value: OutDelegateEvent<T>) {
    this.value = value
  }
}

class OutDelegateEvent<T : Any>(list: List<(T) -> Unit>) {
  private val subject: PublishSubject<EventDelegate> = PublishSubject.create()
  private val disposeBag = CompositeDisposable()

  init {
    list.map { event ->
      disposeBag.add(subject.filter { it == event }.subscribe { event })
    }
  }

  fun next(observable: Observable<T>) {
    disposeBag.add(execute(observable))
  }

  private fun execute(observable: Observable<T>): Disposable {

    return observable
      .observeOn(AndroidSchedulers.mainThread())
      .subscribeOn(Schedulers.newThread())
      .startWith {
        subject.onNext(EventDelegate.Start)
      }
      .subscribeBy(
        onError = {
          when (it) {
            is IllegalArgumentException -> subject.onNext(EventDelegate.ErrorIllegalArgument)
            is UnknownHostException, is ConnectException -> subject.onNext(EventDelegate.ErrorNoInternet)
            else -> subject.onNext(EventDelegate.ErrorOther)
          }
          subject.onNext(EventDelegate.Stop)
        },
        onNext = {
          if (it is List<*> && it.isEmpty()) {
            subject.onNext(EventDelegate.Empty)
          } else {
            subject.onNext(EventDelegate.Found(it))
          }
        },
        onComplete = {
          subject.onNext(EventDelegate.Stop)
        }
      )
  }
}

class InDelegateEvent<T>(val value: List<(T) -> Unit>) {
  private val subject: PublishSubject<T> = PublishSubject.create()
  private val disposeBag = CompositeDisposable()

  init {
    value.map {
      disposeBag.add(subject.subscribe { it })
    }
  }

  fun next(param: T) {
    subject.onNext(param)
  }

  fun clear() {
    disposeBag.clear()
  }
}

class InDelegate<T> {
  private lateinit var value: InDelegateEvent<T>

  operator fun getValue(thisRef: Any?, property: KProperty<*>): InDelegateEvent<T> {
    return value
  }

  operator fun setValue(thisRef: Any?, property: KProperty<*>, value: InDelegateEvent<T>) {
    this.value = value
  }
}

///////////////// Implementation

interface DelegateService {
  fun findAll()
  fun search(query: CharSequence = "")
  var onDataFound: OutDelegateEvent<(List<Repository>)>
}

class DelegateInteractor(private val repository: GithubRepository) : DelegateService {
  override var onDataFound: OutDelegateEvent<List<Repository>> by OutDelegate()

  override fun findAll() {
    onDataFound.next(repository.findAllRepositories())
  }

  override fun search(query: CharSequence) {
    onDataFound.next(
      repository.searchRepositories(query.trim().toString()).map { it.items })
  }
}

class DelegatePresenter(view: DelegateView, service: DelegateService) {
  init {
    view.onSearch = InDelegateEvent(listOf(service::search))
    service.onDataFound = OutDelegateEvent(listOf(view::showData))
  }
}

interface DelegateView {
  var onSearch: InDelegateEvent<CharSequence>
  var findAll: InDelegateEvent<Unit>
  var onDestroy: InDelegateEvent<Unit>

  fun showData(data: List<Repository>)
  fun showEmpty()
  fun showError()
  fun showLoading()
  fun hideLoading()
}

class DelegateFragment : Fragment(), DelegateView {
  override var onSearch by InDelegate<CharSequence>()
  override var findAll by InDelegate<Unit>()
  override var onDestroy by InDelegate<Unit>()

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    onSearch.next("")
    findAll.next(Unit)
  }

  override fun onDestroyView() {
    onSearch.clear()
    super.onDestroyView()
  }

  override fun showData(data: List<Repository>) {}
  override fun showEmpty() {}
  override fun showError() {}
  override fun showLoading() {}
  override fun hideLoading() {}

}