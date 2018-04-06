package com.apiumhub.github.domain.repository.details

import com.apiumhub.github.data.IGithubRepository
import com.apiumhub.github.data.StatsCachingException
import com.apiumhub.github.domain.entity.RepositoryDetailsDto
import com.apiumhub.github.presentation.details.IRepositoryDetailsService
import com.apiumhub.github.presentation.errors.IStatisticsCachingErrorInteractor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class RepositoryDetailsInteractor(private val repository: IGithubRepository) : IRepositoryDetailsService, IStatisticsCachingErrorInteractor {

    private val commitsCountSubject = PublishSubject.create<Int>()
    private val branchesCountSubject = PublishSubject.create<Int>()

    private val repositoryDetailsPublishSubject = PublishSubject.create<RepositoryDetailsDto>()

    private val loadedReadmePublishSubject = PublishSubject.create<String>()

    override fun getRepositoryDetails(user: String, repositoryName: String) {
        getCommitsInternal(user, repositoryName)
        getBranchesInternal(user, repositoryName)
        getReadmeInternal(user, repositoryName)
        combineRepositoryDetailsInternal()
    }

    private fun getCommitsInternal(user: String, repositoryName: String) {
        repository
                .getCommitsForRepository(user, repositoryName)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapIterable { it }
                .reduce(0, { acc, next -> acc + next.total!! })
                .subscribe { count -> commitsCountSubject.onNext(count!!) }
    }

    private fun getBranchesInternal(user: String, repositoryName: String) {
        repository
                .getBranchesForRepository(user, repositoryName)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { branchesCountSubject.onNext(it.count()) }
    }

    private fun getReadmeInternal(user: String, repositoryName: String) {
        repository.getReadmeForRepository(user, repositoryName)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { loadedReadmePublishSubject.onNext(it) }
    }

    private fun combineRepositoryDetailsInternal() {
        Observables
                .combineLatest(commitsCountSubject, branchesCountSubject, { commits, branches ->
                    RepositoryDetailsDto(commits, branches)
                })
                .subscribe { repositoryDetailsPublishSubject.onNext(it) }
    }

    override fun onStatisticsCachingError(func: (exception: StatsCachingException) -> Unit) {

    }

    override fun onDetailsLoaded(func: (details: RepositoryDetailsDto) -> Unit) {
        repositoryDetailsPublishSubject.subscribe { func(it) }
    }

    override fun onReadmeLoaded(func: (readme: String) -> Unit) {
        loadedReadmePublishSubject.subscribe { func(it) }
    }
}