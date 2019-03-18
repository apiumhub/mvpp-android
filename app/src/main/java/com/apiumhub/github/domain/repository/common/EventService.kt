package com.apiumhub.github.domain.repository.common

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject


sealed class Event {
  object Empty : Event()
  object ErrorNull : Event()
  object ErrorNoInternet : Event()
  object ErrorOther : Event()
  object Start : Event()
  object Stop : Event()
}

interface EventService {
  fun cancel()

  fun onStart(func: () -> Unit)
  fun onStop(func: () -> Unit)
  fun onEmpty(func: () -> Unit)
  fun onErrorNullList(func: () -> Unit)
  fun onErrorNoInternet(func: () -> Unit)
  fun onErrorOther(func: () -> Unit)
}

abstract class EventInteractor : EventService {
  val disposeBag = CompositeDisposable()
  val subject = PublishSubject.create<Event>()

  override fun cancel() {
    disposeBag.clear()
  }

  override fun onStart(func: () -> Unit) {
    subscribeEvent(Event.Start, func)
  }

  override fun onStop(func: () -> Unit) {
    subscribeEvent(Event.Stop, func)
  }

  override fun onEmpty(func: () -> Unit) {
    subscribeEvent(Event.Empty, func)
  }

  override fun onErrorNullList(func: () -> Unit) {
    subscribeEvent(Event.ErrorNull, func)
  }

  override fun onErrorNoInternet(func: () -> Unit) {
    subscribeEvent(Event.ErrorNoInternet, func)
  }

  override fun onErrorOther(func: () -> Unit) {
    subscribeEvent(Event.ErrorOther, func)
  }

  private fun subscribeEvent(event: Event, func: () -> Unit) {
    disposeBag.add(subject.filter { it == event }.subscribe { func() })
  }
}