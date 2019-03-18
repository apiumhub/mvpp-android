package com.apiumhub.github.core.di

import com.apiumhub.github.data.GithubApi
import com.apiumhub.github.data.GithubRepository
import com.apiumhub.github.domain.repository.details.RepositoryDetailsService
import com.apiumhub.github.domain.repository.list.RepositoryListService
import com.apiumhub.github.presentation.details.RepositoryDetailsPresenter
import com.apiumhub.github.presentation.list.RepositoryListPresenter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.koin.dsl.module.module

val apiModule = module {
  factory { GithubApi.create() }
}

val repositoriesModule = module {
  factory { GithubRepository.create(get(), PublishSubject.create()) }
}

val servicesModule = module {
  factory { RepositoryListService.create(get(), AndroidSchedulers.mainThread(), Schedulers.newThread()) }
  factory { RepositoryDetailsService.create(get(), AndroidSchedulers.mainThread(), Schedulers.newThread()) }
}

val presenterModule = module {
  factory { RepositoryListPresenter(it[0], get()) }
  factory { RepositoryDetailsPresenter(it[0], get()) }
}