package com.apiumhub.github.core.di

import com.apiumhub.github.data.GithubApi
import com.apiumhub.github.data.IGithubRepository
import com.apiumhub.github.domain.repository.list.RepositoryListService
import com.apiumhub.github.presentation.list.RepositoryListPresenter
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module.module

val apiModule = module {
  factory { GithubApi.create() }
}

val repositoriesModule = module {
  factory { IGithubRepository.create(get(), PublishSubject.create()) }
}

val servicesModule = module {
  factory { RepositoryListService.create(get(), PublishSubject.create(), Dispatchers.Main) }
}

val presenterModule = module {
  factory { RepositoryListPresenter(it[0], get()) }
}