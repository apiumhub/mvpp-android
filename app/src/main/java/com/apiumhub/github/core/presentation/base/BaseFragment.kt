package com.apiumhub.github.core.presentation.base

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

interface EventView {
  fun bindDestroy(func: () -> Unit)

  fun showEmptyData()
  fun showNetworkError()
  fun showGenericError()
  fun showLoading()
  fun hideLoading()
}

sealed class Event {
  class Single<T>(val value: T) : Event()
  class Pair<A, B>(val p1: A, val p2: B) : Event()
  object Destroy : Event()
}

abstract class BaseFragment : Fragment(), EventView {

  protected val disposeBag = CompositeDisposable()
  protected val subject: PublishSubject<Event> = PublishSubject.create()

  abstract fun getLayoutId(): Int

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(getLayoutId(), container, false)
  }

  override fun onDestroyView() {
    subject.onNext(Event.Destroy)
    disposeBag.clear()
    super.onDestroyView()
  }

  override fun showEmptyData() {}
  override fun showNetworkError() {}
  override fun showGenericError() {}
  override fun showLoading() {}
  override fun hideLoading() {}

  protected fun <T>bindSingle(func: (T) -> Unit) {
    disposeBag.add(subject.filter { it is Event.Single<*> }.subscribe { func((it as Event.Single<T>).value) })
  }

  protected fun <A, B>bindPair(func: (A, B) -> Unit) {
    disposeBag.add(subject.filter { it is Event.Pair<*, *> }.subscribe { func((it as Event.Pair<A, B>).p1, it.p2) })
  }

  override fun bindDestroy(func: () -> Unit) {
    disposeBag.add(subject.filter { it is Event.Destroy }.subscribe { func() })
  }
}