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
import kotlinx.android.synthetic.main.content_main.*
import org.koin.android.ext.android.get
import org.koin.core.parameter.ParameterList
import java.util.concurrent.TimeUnit

class RepositoryListFragment : BaseFragment<ContentMainBinding>(),
  RepositoryListView {

  private val disposeBag = CompositeDisposable()
  private var onSearch: (String) -> Unit = {}

  init {
    get<RepositoryListPresenter> { ParameterList(this as RepositoryListView) }
  }

  override fun getLayoutId(): Int = R.layout.content_main

  private val adapter = RepoListAdapter(disposeBag) {
    Navigator.openRepositoryDetails(fragmentManager!!, it)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    onSearch("")

    setupSearch()
    binding.contentMainList.adapter = adapter
    binding.contentMainList.layoutManager = LinearLayoutManager(context)
  }

  override fun onDestroyView() {
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
        onSearch(it.trim().toString())
      })
  }

  // subscriptions
  override fun search(func: (String) -> Unit) {
    this.onSearch = func
  }

  companion object {
    fun newInstance(): RepositoryListFragment =
      RepositoryListFragment()
  }
}
