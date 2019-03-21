package com.apiumhub.github.core.di

import com.apiumhub.github.core.data.GithubApi
import com.apiumhub.github.details.*
import com.apiumhub.github.list.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.koin.dsl.module.module

val apiModule = module {
  factory { GithubApi.create() }
}

val repositoriesModule = module {
  factory { RepositoryDetailsRepository.create() as InMemoryRepositoryDetailsRepository }
  factory { RepositoryDetailsRepository.create(get(), PublishSubject.create()) as GithubRepositoryDetailsRepository }
  factory { RepositoryListRepository.create() as InMemoryRepositoryListRepository }
  factory { RepositoryListRepository.create(get(), PublishSubject.create()) as GithubRepositoryListRepository }
}

val servicesModule = module {
  factory {
    RepositoryListService.create(
      get() as GithubRepositoryListRepository,
      get() as InMemoryRepositoryListRepository,
      AndroidSchedulers.mainThread(),
      Schedulers.newThread()
    )
  }
  factory {
    RepositoryDetailsService.create(
      get() as GithubRepositoryDetailsRepository,
      get() as InMemoryRepositoryDetailsRepository,
      AndroidSchedulers.mainThread(),
      Schedulers.newThread()
    )
  }
}

val presenterModule = module {
  factory { RepositoryListPresenter(it[0], get()) }
  factory { RepositoryDetailsPresenter(it[0], get()) }
}