package com.apiumhub.github.core.di

import com.apiumhub.github.data.GithubApi
import com.apiumhub.github.data.IGithubRepository
import com.apiumhub.github.domain.repository.list.RepositoryListService
import com.apiumhub.github.presentation.list.RepositoryListPresenter
import io.reactivex.subjects.PublishSubject
import org.koin.dsl.module.module

val servicesModule = module {
  factory { RepositoryListService.create(get()) }
}

val apiModule = module {
  factory { GithubApi.create() }
}

val repositoriesModule = module {
  factory { IGithubRepository.create(get(), PublishSubject.create()) }
}

val presenterModule = module {
  factory { RepositoryListPresenter(it[0], get()) }
}