package com.apiumhub.github.core.di

import com.apiumhub.github.data.GithubApi
import com.apiumhub.github.data.GithubRepository
import com.apiumhub.github.domain.repository.details.RepositoryDetailsService
import com.apiumhub.github.domain.repository.list.RepositoryListService
import com.apiumhub.github.presentation.details.RepositoryDetailsPresenter
import com.apiumhub.github.presentation.list.RepositoryListPresenter
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module.module

val apiModule = module {
  factory { GithubApi.create() }
}

val repositoriesModule = module {
  factory { GithubRepository.create(get()) }
}

val servicesModule = module {
  factory { RepositoryListService.create(get(), PublishSubject.create(), Dispatchers.Main) }
  factory { RepositoryDetailsService.create(get(), PublishSubject.create(), Dispatchers.Main) }
}

val presenterModule = module {
  factory { RepositoryListPresenter(it[0], get()) }
  factory { RepositoryDetailsPresenter(it[0], get()) }
}