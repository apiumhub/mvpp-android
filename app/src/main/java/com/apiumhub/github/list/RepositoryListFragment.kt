package com.apiumhub.github.list

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.apiumhub.github.R
import com.apiumhub.github.core.domain.entity.Repository
import com.apiumhub.github.core.presentation.Navigator
import com.apiumhub.github.core.presentation.base.BaseFragment
import com.apiumhub.github.core.presentation.base.Event
import com.apiumhub.github.core.presentation.base.EventView
import com.apiumhub.github.databinding.ContentMainBinding
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_main.*
import org.koin.android.ext.android.get
import org.koin.core.parameter.ParameterList
import java.util.concurrent.TimeUnit

interface RepositoryListView : EventView {
  fun bindSearch(func: (String) -> Unit)
  fun showData(items: List<Repository>)

  companion object {
    fun create() = RepositoryListFragment.newInstance()
  }
}

class RepositoryListFragment : BaseFragment<ContentMainBinding>(), RepositoryListView {
  override fun getLayoutId(): Int = R.layout.content_main

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    get<RepositoryListPresenter> { ParameterList(this as RepositoryListView) }
    subject.onNext(Event.Single(""))

    setupSearch()
    binding.contentMainList.adapter = RepoListAdapter(disposeBag) {
      Navigator.openRepositoryDetails(fragmentManager!!, it)
    }
    binding.contentMainList.layoutManager = LinearLayoutManager(context)
  }

  //region -- Actions --
  override fun bindSearch(func: (String) -> Unit) = bindSingle(func)
  //endregion

  //region -- View Events --
  override fun showData(items: List<Repository>) {
    (binding.contentMainList.adapter as RepoListAdapter).apply {
      setItems(items)
      notifyDataSetChanged()
    }
  }

  override fun showLoading() {
    progress.visibility = View.VISIBLE
  }

  override fun hideLoading() {
    progress.visibility = View.GONE
  }

  override fun showEmptyData() {
    (binding.contentMainList.adapter as RepoListAdapter).apply {
      setItems(emptyList())
      notifyDataSetChanged()
    }
  }

  override fun showNetworkError() {
    Toast.makeText(context, "network error", Toast.LENGTH_SHORT).show()
  }

  override fun showGenericError() {
    Toast.makeText(context, "generic error", Toast.LENGTH_SHORT).show()
  }
  //endregion

  //region -- Private methods --
  private fun setupSearch() {
    disposeBag.add(
      RxTextView
      .textChanges(binding.contentMainSearch)
      .debounce(300, TimeUnit.MILLISECONDS)
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .map { it.trim().toString() }
      .subscribe {
        subject.onNext(Event.Single(it))
      })
  }
  //endregion

  companion object {
    fun newInstance(): RepositoryListFragment = RepositoryListFragment()
  }
}
