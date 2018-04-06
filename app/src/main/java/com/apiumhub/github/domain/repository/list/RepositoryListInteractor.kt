package com.apiumhub.github.domain.repository.list

import com.apiumhub.github.data.IGithubRepository
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.presentation.list.IRepositoryListService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class RepositoryListInteractor(private val repository: IGithubRepository) : IRepositoryListService {

    private val reposFoundSubject: PublishSubject<List<Repository>> = PublishSubject.create()

    override fun findAll() {
        repository
                .findAllRepositories()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    reposFoundSubject.onNext(it)
                }
    }

    override fun search(query: String) {
        repository.searchRepositories(query)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .filter { it.items != null }
                .map {
                    it.items!!
                }
                .subscribe {
                    reposFoundSubject.onNext(it)
                }
    }

    override fun onReposFound(func: (repositories: List<Repository>) -> Unit) {
        reposFoundSubject.subscribe {
            func(it)
        }
    }
}