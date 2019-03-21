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
  fun onDestroy(func: () -> Unit)

  fun showEmpty()
  fun showError()
  fun showLoading()
  fun hideLoading()
}

sealed class Event {
  class Send<T>(val value: T) : Event()
  class Sender<A, B>(val p1: A, val p2: B) : Event()
  object Destroy : Event()
}

abstract class BaseFragment<Binding : ViewDataBinding> : Fragment(), EventView {

  protected lateinit var binding: Binding

  protected val disposeBag = CompositeDisposable()
  protected val subject: PublishSubject<Event> = PublishSubject.create()

  abstract fun getLayoutId(): Int

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.lifecycleOwner = this
  }

  override fun onDestroyView() {
    subject.onNext(Event.Destroy)
    disposeBag.clear()
    super.onDestroyView()
  }

  override fun showEmpty() {}
  override fun showError() {}
  override fun showLoading() {}
  override fun hideLoading() {}

  protected fun <T>onSend(func: (T) -> Unit) {
    disposeBag.add(subject.filter { it is Event.Send<*> }.subscribe { func((it as Event.Send<T>).value) })
  }

  protected fun <A, B>onSend(func: (A, B) -> Unit) {
    disposeBag.add(subject.filter { it is Event.Sender<*,*> }.subscribe { func((it as Event.Sender<A,B>).p1, it.p2) })
  }

  override fun onDestroy(func: () -> Unit) {
    disposeBag.add(subject.filter { it is Event.Destroy }.subscribe { func() })
  }
}