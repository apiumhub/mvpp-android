package com.apiumhub.github.arch

import android.app.Fragment
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.View
import com.apiumhub.github.data.GithubRepository
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.repository.Event
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.internal.util.HalfSerializer.onNext
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.lang.IllegalArgumentException
import java.net.ConnectException
import java.net.UnknownHostException
import kotlin.reflect.KProperty

///////// DSL

enum class EventDelegate {
  EMPTY, ERROR_NULL, ERROR_NO_INTERNET, ERROR_OTHER, START, STOP, UNKNOWN
}

class OutDelegate<T, P : Any> {
  private lateinit var value: OutDelegateEvent<T, P>

  operator fun getValue(thisRef: Any?, property: KProperty<*>): OutDelegateEvent<T, P> {
    return value
  }

  operator fun setValue(thisRef: Any?, property: KProperty<*>, value: OutDelegateEvent<T, P>) {
    this.value = value
  }
}

class OutDelegateEvent<T, P : Any>(val value: T) {
  private val stream: PublishSubject<P> = PublishSubject.create()
  private val subject: PublishSubject<EventDelegate> = PublishSubject.create()
  private val disposeBag = CompositeDisposable()

  init {
    disposeBag.add(stream.subscribe { it })
    disposeBag.add(subject.subscribe { it })
  }

  fun next(observable: Observable<P>) {
    disposeBag.add(execute(observable))
  }

  private fun execute(observable: Observable<P>): Disposable {

    return observable
      .observeOn(AndroidSchedulers.mainThread())
      .subscribeOn(Schedulers.newThread())
      .startWith {
        subject.onNext(EventDelegate.START)
      }
      .subscribeBy(
        onError = {
          when (it) {
            is IllegalArgumentException -> subject.onNext(EventDelegate.ERROR_NULL)
            is UnknownHostException, is ConnectException -> subject.onNext(EventDelegate.ERROR_NO_INTERNET)
            else -> subject.onNext(EventDelegate.ERROR_OTHER)
          }
          subject.onNext(EventDelegate.STOP)
        },
        onNext = {
          if (it is List<*> && it.isEmpty()) {
            subject.onNext(EventDelegate.EMPTY)
          } else {
            stream.onNext(it)
          }
        },
        onComplete = {
          subject.onNext(EventDelegate.STOP)
        }
      )
  }
}

class InDelegateEvent<T, P>(val value: T) {
  private val subject: PublishSubject<P> = PublishSubject.create()
  private val disposeBag = CompositeDisposable()

  init {
    disposeBag.add(subject.subscribe { it })
  }

  fun next(param: P) {
    subject.onNext(param)
  }

  fun clear() {
    disposeBag.clear()
  }
}

class InDelegate<T, P> {
  private lateinit var value: InDelegateEvent<T, P>

  operator fun getValue(thisRef: Any?, property: KProperty<*>): InDelegateEvent<T, P> {
    return value
  }

  operator fun setValue(thisRef: Any?, property: KProperty<*>, value: InDelegateEvent<T, P>) {
    this.value = value
  }
}

///////////////// Implementation

interface DelegateService {
  fun search(query: CharSequence = "")
  var onDataFound: OutDelegateEvent<(List<Repository>) -> Unit, (List<Repository>)>
}

class DelegateInteractor(private val repository: GithubRepository) : DelegateService {
  override var onDataFound: OutDelegateEvent<(List<Repository>) -> Unit, List<Repository>> by OutDelegate()

  override fun search(query: CharSequence) {
    onDataFound.next(repository.findAllRepositories())
  }
}

class DelegatePresenter(view: DelegateView, service: DelegateService) {
  init {
    view.onSearch = InDelegateEvent(service::search)
    service.onDataFound = OutDelegateEvent(view::showData)
  }
}

interface DelegateView {
  var onSearch: InDelegateEvent<(CharSequence) -> Unit, CharSequence>
  var findAll: InDelegateEvent<() -> Unit, Unit>
  fun showData(data: List<Repository>)
}

class DelegateFragment : Fragment(), DelegateView {
  override var onSearch by InDelegate<(CharSequence) -> Unit, CharSequence>()
  override var findAll by InDelegate<() -> Unit, Unit>()

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    findAll.next(Unit)
  }

  override fun onDestroyView() {
    onSearch.clear()
    super.onDestroyView()
  }


  override fun showData(data: List<Repository>) {
    //TODO: Draw things
  }
}