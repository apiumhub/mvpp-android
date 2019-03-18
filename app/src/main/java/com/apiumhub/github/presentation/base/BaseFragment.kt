package com.apiumhub.github.presentation.base

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

abstract class BaseFragment<Binding : ViewDataBinding> : Fragment(), EventView {

  protected lateinit var binding: Binding

  protected val disposeBag = CompositeDisposable()
  private val destroySubject = PublishSubject.create<Unit>()

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
    destroySubject.onNext(Unit)
    disposeBag.clear()
    super.onDestroyView()
  }

  override fun showEmpty() {}
  override fun showError() {}
  override fun showLoading() {}
  override fun hideLoading() {}

  override fun onDestroy(func: () -> Unit) {
    disposeBag.add(destroySubject.subscribe { func() })
  }
}