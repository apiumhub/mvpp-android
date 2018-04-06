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
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

open class RepositoryListFragmentInternal : BaseFragment<ContentMainBinding>(), IRepositoryListView {
    override fun getLayoutId(): Int = R.layout.content_main

    private val loadItemsSubject: PublishSubject<Any> = PublishSubject.create()
    private val searchItemsSubject: PublishSubject<String> = PublishSubject.create()

    private val adapter = RepoListAdapter {
        Navigator.openRepositoryDetails(fragmentManager!!, it)
    }

    private val unsubscribe = PublishSubject.create<Any>()

    override fun loadItems(func: () -> Unit) {
        loadItemsSubject.takeUntil(unsubscribe).subscribe { func() }
    }

    override fun itemsLoaded(items: List<Repository>) {
        adapter.setItems(items)
        adapter.notifyDataSetChanged()
    }

    override fun searchItems(func: (query: String) -> Unit) {
        searchItemsSubject.takeUntil(unsubscribe).subscribe { func(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSearch()
        binding.contentMainList.adapter = adapter
        binding.contentMainList.layoutManager = LinearLayoutManager(context)
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
                        loadItemsSubject.onNext(Any())
                    } else {
                        searchItemsSubject.onNext(it.trim().toString())
                    }
                }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribe.onNext(Any())
    }
}

class RepositoryListFragment : RepositoryListFragmentInternal() {

    companion object {
        fun newInstance(): RepositoryListFragment = RepositoryListFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        RepositoryListPresenter(this, IRepositoryListService.create())
        super.onViewCreated(view, savedInstanceState)
    }
}
