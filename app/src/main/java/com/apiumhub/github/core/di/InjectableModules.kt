package com.apiumhub.github.core.di

import com.apiumhub.github.domain.repository.list.RepositoryListOrchestrator
import com.apiumhub.github.domain.repository.list.RepositoryListService
import com.apiumhub.github.presentation.list.RepositoryListInput
import com.apiumhub.github.presentation.list.RepositoryListPresenter
import com.apiumhub.github.presentation.list.presenter.RepositoryListPresenterChildBinder
import com.apiumhub.github.presentation.list.presenter.RepositoryListPresenterParent
import org.koin.dsl.module.module

val servicesModule = module {
  factory { RepositoryListService.create() }
}

val orchestratorModule = module {
  factory { RepositoryListOrchestrator.create() }
}

val inputModule = module {
  factory { RepositoryListInput(it[0], get()) }
}

val presenterModule = module {
  factory { RepositoryListPresenter(it[0], get()) }
  factory { RepositoryListPresenterChildBinder(get()) as RepositoryListPresenterParent }
}