package com.apiumhub.github.core.di

import com.apiumhub.github.domain.repository.list.LoadingPresenter
import com.apiumhub.github.domain.repository.list.RepositoryListService
import com.apiumhub.github.domain.repository.list.SearchPresenter
import com.apiumhub.github.domain.repository.list.SearchService
import com.apiumhub.github.presentation.list.RepositoryListPresenter
import com.apiumhub.github.presentation.list.RepositoryListPresenterBinder
import com.apiumhub.github.presentation.list.presenter.RepositoryListPresenterChildBinder
import com.apiumhub.github.presentation.list.presenter.RepositoryListPresenterClassic
import com.apiumhub.github.presentation.list.presenter.RepositoryListPresenterParent
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.module.module

val binderModule = module {
  factory { RepositoryListPresenterBinder(get()) }
}

val servicesModule = module {
  factory { RepositoryListService.create() }
  factory { SearchService.create() }
}

val presenterModule = module {
  factory { RepositoryListPresenter(it[0], get()) }
  factory { RepositoryListPresenterChildBinder(get()) as RepositoryListPresenterParent }
  factory { RepositoryListPresenterClassic(it[0], get(), Dispatchers.Main) }
  factory { SearchPresenter(it[0], get()) }
}