package com.apiumhub.github.core.arch

import android.app.Fragment
import android.os.Bundle
import android.view.View
import com.apiumhub.github.core.domain.entity.Repository
import com.apiumhub.github.list.RepositoryListRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.koin.android.ext.android.get
import org.koin.core.parameter.ParameterList
import java.net.ConnectException
import java.net.UnknownHostException
import kotlin.reflect.KProperty

// Events
sealed class Event {
  class Found<T>(val value: T) : Event()
  object Start : Event()
  object Stop : Event()
  object Empty : Event()
  object ErrorIllegalArgument : Event()
  object ErrorNoInternet : Event()
  object ErrorOther : Event()
}

class Output<T : Any> {
  private lateinit var value: EventOutput<T>

  operator fun getValue(thisRef: Any?, property: KProperty<*>): EventOutput<T> {
    return value
  }

  operator fun setValue(thisRef: Any?, property: KProperty<*>, value: EventOutput<T>) {
    this.value = value
  }
}

class EventOutput<T : Any>(list: List<(T) -> Unit>) {
  private val subject: PublishSubject<Event> = PublishSubject.create()
  private val disposeBag = CompositeDisposable()

  init {
    list.map { event ->
      disposeBag.add(subject.filter { it == event }.subscribe { event })
    }
  }

  fun <P: Any> next(observable: Observable<P>) {
    disposeBag.add(execute(observable))
  }

  fun clear(){
    disposeBag.clear()
  }

  private fun <P: Any> execute(observable: Observable<P>): Disposable {

    return observable
      .observeOn(AndroidSchedulers.mainThread())
      .subscribeOn(Schedulers.newThread())
      .startWith {
        subject.onNext(Event.Start)
      }
      .subscribeBy(
        onError = {
          when (it) {
            is IllegalArgumentException -> subject.onNext(Event.ErrorIllegalArgument)
            is UnknownHostException, is ConnectException -> subject.onNext(Event.ErrorNoInternet)
            else -> subject.onNext(Event.ErrorOther)
          }
          subject.onNext(Event.Stop)
        },
        onNext = {
          if (it is List<*> && it.isEmpty()) {
            subject.onNext(Event.Empty)
          } else {
            subject.onNext(Event.Found(it))
          }
        },
        onComplete = {
          subject.onNext(Event.Stop)
        }
      )
  }
}

//Actions

sealed class Action {
  class Search(val query: String) : Action()
  object Destroy : Action()
}

class TransitionAction<T>(val value: List<(T) -> Unit>) {
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

class Transition<T> {
  private lateinit var value: TransitionAction<T>

  operator fun getValue(thisRef: Any?, property: KProperty<*>): TransitionAction<T> {
    return value
  }

  operator fun setValue(thisRef: Any?, property: KProperty<*>, value: TransitionAction<T>) {
    this.value = value
  }
}

interface DelegateService {
  var events: EventOutput<Event>
  fun listen(actions: TransitionAction<Action>)
}

interface DelegateView {
  var actions: TransitionAction<Action>
  fun translate(events: Event)
}

class DelegatePresenter(private val view: DelegateView, private val service: DelegateService) {
  init {
    service.listen(view.actions)
    view.translate(service.events)
  }
}

// Service Implementation
class DelegateInteractor(private val repository: RepositoryListRepository) : DelegateService {
  override var events: EventOutput<Event> by Output()

  override fun listen(actions: TransitionAction<Action>) {
    when(actions.value) {
      is Action.Search -> search(actions.query)
      is Action.Destroy -> events.clear()
    }

  }

  private fun search(query: String) {
    events.next(repository.searchRepositories(query).map { it.items!! })
  }
}

// View Implementation
class DelegateFragment : Fragment(), DelegateView {
  override var actions: TransitionAction<Action> by Transition()

  override fun translate(event: Event) {
    when (event) {
      is Event.Found<*> -> showData(event.value as List<Repository>)
      is Event.Empty -> showEmpty()
      is Event.Start -> showLoading()
      is Event.Stop -> hideLoading()
      is Event.ErrorIllegalArgument, Event.ErrorNoInternet, Event.ErrorOther -> showError()
    }
  }

  init {
    get<DelegatePresenter> { ParameterList(this as DelegatePresenter) }
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    actions.next(Action.Search(""))
  }

  override fun onDestroyView() {
    actions.next(Action.Destroy)
    actions.clear()
    super.onDestroyView()
  }

  private fun showData(data: List<Repository>) {}
  private fun showEmpty() {}
  private fun showError() {}
  private fun showLoading() {}
  private fun hideLoading() {}

}