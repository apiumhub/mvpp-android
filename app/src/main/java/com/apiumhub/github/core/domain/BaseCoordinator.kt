package com.apiumhub.github.core.domain

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import java.net.ConnectException
import java.net.UnknownHostException

enum class Event {
  EMPTY, ERROR_NO_INTERNET, ERROR_OTHER, START, STOP, UNKNOWN
}

interface BaseService {
  fun cancel()

  fun bindStart(func: () -> Unit)
  fun bindStop(func: () -> Unit)
  fun bindEmptyData(func: () -> Unit)
  fun bindNetworkError(func: () -> Unit)
  fun bindOtherError(func: () -> Unit)
}

abstract class BaseCoordinator(private val observeOn: Scheduler, private val subscribeOn: Scheduler) :
  BaseService {
  protected val subject: PublishSubject<Event> = PublishSubject.create()
  protected val disposeBag = CompositeDisposable()

  fun <T : Any> execute(observable: Observable<T>, onNext: (T) -> Unit) {
    subject.onNext(Event.START)

    disposeBag.add(
      observable
        .observeOn(observeOn)
        .subscribeOn(subscribeOn)
        .subscribeBy(
          onError = {
            when (it) {
              is UnknownHostException, is ConnectException -> subject.onNext(Event.ERROR_NO_INTERNET)
              else -> subject.onNext(Event.ERROR_OTHER)
            }
            subject.onNext(Event.STOP)
          },
          onNext = {
            if (it is List<*> && it.isEmpty()) {
              subject.onNext(Event.EMPTY)
            } else {
              onNext(it)
            }
          },
          onComplete = {
            subject.onNext(Event.STOP)
          }
        )
    )
  }

  override fun cancel() {
    disposeBag.clear()
  }

  override fun bindStart(func: () -> Unit) {
    subscribeEvent(Event.START, func)
  }

  override fun bindStop(func: () -> Unit) {
    subscribeEvent(Event.STOP, func)
  }

  override fun bindEmptyData(func: () -> Unit) {
    subscribeEvent(Event.EMPTY, func)
  }

  override fun bindNetworkError(func: () -> Unit) {
    subscribeEvent(Event.ERROR_NO_INTERNET, func)
  }

  override fun bindOtherError(func: () -> Unit) {
    subscribeEvent(Event.ERROR_OTHER, func)
  }

  private fun subscribeEvent(event: Event, func: () -> Unit) {
    disposeBag.add(subject.filter { it == event }.subscribe { func() })
  }
}