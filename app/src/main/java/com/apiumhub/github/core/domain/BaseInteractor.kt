package com.apiumhub.github.core.domain

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.subjects.PublishSubject
import java.net.ConnectException
import java.net.UnknownHostException


enum class Event {
  EMPTY, ERROR_NULL, ERROR_NO_INTERNET, ERROR_OTHER, START, STOP, UNKNOWN
}

interface BaseService {
  fun cancel()

  fun onStart(func: () -> Unit)
  fun onStop(func: () -> Unit)
  fun onEmpty(func: () -> Unit)
  fun onErrorNullList(func: () -> Unit)
  fun onErrorNoInternet(func: () -> Unit)
  fun onErrorOther(func: () -> Unit)
}

abstract class BaseInteractor(private val observeOn: Scheduler, private val subscribeOn: Scheduler) :
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
              is IllegalArgumentException -> subject.onNext(Event.ERROR_NULL)
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

  override fun onStart(func: () -> Unit) {
    subscribeEvent(Event.START, func)
  }

  override fun onStop(func: () -> Unit) {
    subscribeEvent(Event.STOP, func)
  }

  override fun onEmpty(func: () -> Unit) {
    subscribeEvent(Event.EMPTY, func)
  }

  override fun onErrorNullList(func: () -> Unit) {
    subscribeEvent(Event.ERROR_NULL, func)
  }

  override fun onErrorNoInternet(func: () -> Unit) {
    subscribeEvent(Event.ERROR_NO_INTERNET, func)
  }

  override fun onErrorOther(func: () -> Unit) {
    subscribeEvent(Event.ERROR_OTHER, func)
  }

  private fun subscribeEvent(event: Event, func: () -> Unit) {
    disposeBag.add(subject.filter { it == event }.subscribe { func() })
  }
}