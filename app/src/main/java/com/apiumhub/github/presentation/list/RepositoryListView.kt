package com.apiumhub.github.presentation.list

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.apiumhub.github.R
import com.apiumhub.github.databinding.ContentMainBinding
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.presentation.Navigator
import com.apiumhub.github.presentation.base.BaseFragment
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class RepositoryListFragment : BaseFragment<ContentMainBinding>(), IRepositoryListView {
  override fun getLayoutId(): Int = R.layout.content_main

  private val presenter: RepositoryListPresenter = RepositoryListPresenterBinder(this, RepositoryListService.create())

  private val adapter = RepoListAdapter {
    Navigator.openRepositoryDetails(fragmentManager!!, it)
  }

  override fun itemsLoaded(items: List<Repository>) {
    adapter.setItems(items)
    adapter.notifyDataSetChanged()
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.onViewCreated()
    setupSearch()
    binding.contentMainList.adapter = adapter
    binding.contentMainList.layoutManager = LinearLayoutManager(context)
  }


  override fun onDestroyView() {
    presenter.onDestroyView()
    super.onDestroyView()
  }

  private fun setupSearch() {
    RxTextView
      .textChanges(binding.contentMainSearch)
      .debounce(300, TimeUnit.MILLISECONDS)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .map { it.trim() }
      .subscribe {
        if (it.isEmpty()) {
          presenter.findAll()
        } else {
          presenter.findFilterByQuery(it.trim().toString())
        }
      }
  }

  companion object {
    fun newInstance(): RepositoryListFragment = RepositoryListFragment()
  }
}
