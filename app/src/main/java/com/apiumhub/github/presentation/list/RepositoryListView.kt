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
import com.apiumhub.github.presentation.list.presenter.RepositoryListPresenterParent
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_main.*
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.parameter.ParameterList
import java.util.concurrent.TimeUnit

class RepositoryListFragment : BaseFragment<ContentMainBinding>(), IRepositoryListView {
  override fun getLayoutId(): Int = R.layout.content_main


//  private val presenter: RepositoryListPresenter = get { ParameterList(this as IRepositoryListView) }
  private val presenterChild: RepositoryListPresenterParent by inject()

  private val adapter = RepoListAdapter {
    Navigator.openRepositoryDetails(fragmentManager!!, it)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
//    presenter.onViewCreated()
    presenterChild.onViewCreated(this)
    setupSearch()
    binding.contentMainList.adapter = adapter
    binding.contentMainList.layoutManager = LinearLayoutManager(context)
  }

  override fun onDestroyView() {
    presenterChild.onDestroyView()
//    presenter.onDestroyView()
    super.onDestroyView()
  }

  override fun hideLoading() {
    progress.visibility = View.GONE
  }

  override fun showLoading() {
    progress.visibility = View.VISIBLE
  }

  override fun itemsEmpty() {
    adapter.setItems(emptyList())
    adapter.notifyDataSetChanged()
  }

  override fun itemsLoaded(items: List<Repository>) {
    adapter.setItems(items)
    adapter.notifyDataSetChanged()
  }

  override fun showError(error: Throwable) {
    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
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
//          presenter.findAll()
          presenterChild.findAll()
        } else {
//          presenter.findFilterByQuery(it.trim().toString())
          presenterChild.findFilterByQuery(it.trim().toString())
        }
      }
  }

  companion object {
    fun newInstance(): RepositoryListFragment = RepositoryListFragment()
  }
}
