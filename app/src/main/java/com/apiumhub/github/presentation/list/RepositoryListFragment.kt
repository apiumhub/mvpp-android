package com.apiumhub.github.presentation.list

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.apiumhub.github.R
import com.apiumhub.github.databinding.ContentMainBinding
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.presentation.Navigator
import com.apiumhub.github.presentation.base.BaseFragment
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.content_main.*
import org.koin.android.ext.android.get

import org.koin.core.parameter.ParameterList
import java.util.concurrent.TimeUnit

sealed class RepositoryListAction {
  class Search(val query: CharSequence = "") : RepositoryListAction()
  object Destroy : RepositoryListAction()
}

interface RepositoryListView {
  //input
  fun onSearch(func: (CharSequence) -> Unit)
  fun onDestroy(func: () -> Unit)

  //output
  fun showData(data: List<Repository>)
  fun showEmpty()
  fun showError()
  fun showLoading()
  fun hideLoading()

  companion object {
    fun create() = RepositoryListFragment.newInstance()
  }
}

class RepositoryListFragment : BaseFragment<ContentMainBinding>(), RepositoryListView {
  private val disposeBag = CompositeDisposable()
  private val subject: PublishSubject<RepositoryListAction> = PublishSubject.create()

  init {
    get<RepositoryListPresenter> { ParameterList(this as RepositoryListView) }
  }

  override fun getLayoutId(): Int = R.layout.content_main

  private val adapter = RepoListAdapter(disposeBag) {
    Navigator.openRepositoryDetails(fragmentManager!!, it)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    subject.onNext(RepositoryListAction.Search())

    setupSearch()
    binding.contentMainList.adapter = adapter
    binding.contentMainList.layoutManager = LinearLayoutManager(context)
  }

  override fun onDestroyView() {
    subject.onNext(RepositoryListAction.Destroy)
    disposeBag.clear()
    super.onDestroyView()
  }

  override fun showLoading() {
    progress.visibility = View.VISIBLE
  }

  override fun hideLoading() {
    progress.visibility = View.GONE
  }

  override fun showEmpty() {
    adapter.setItems(emptyList())
    adapter.notifyDataSetChanged()
  }

  override fun showData(items: List<Repository>) {
    adapter.setItems(items)
    adapter.notifyDataSetChanged()
  }

  override fun showError() {
    Toast.makeText(context, "generic error", Toast.LENGTH_SHORT).show()
  }

  private fun setupSearch() {
    disposeBag.add(RxTextView
      .textChanges(binding.contentMainSearch)
      .debounce(300, TimeUnit.MILLISECONDS)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .map { it.trim() }
      .subscribe {
        subject.onNext(RepositoryListAction.Search(it))
      })
  }

  override fun onSearch(func: (CharSequence) -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryListAction.Search }.subscribe { func((it as RepositoryListAction.Search).query) })
  }

  override fun onDestroy(func: () -> Unit) {
    disposeBag.add(subject.filter { it is RepositoryListAction.Destroy }.subscribe { func() })
  }

  companion object {
    fun newInstance(): RepositoryListFragment = RepositoryListFragment()
  }
}
