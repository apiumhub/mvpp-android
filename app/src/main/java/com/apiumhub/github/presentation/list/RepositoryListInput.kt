package com.apiumhub.github.presentation.list

import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.repository.list.RepositoryListOrchestrator

interface LoadingView {
  fun startLoading()
  fun stopLoading()
}

interface RepositoryListView : LoadingView {
  //output
  fun onData(data: List<Repository>)

  fun onEmpty()
  fun onError()
}

class RepositoryListInput(view: RepositoryListView, private val orchestrator: RepositoryListOrchestrator) {

  init {
    orchestrator.onData(view::onData)
    orchestrator.onEmpty(view::onEmpty)
    orchestrator.onErrorNoInternet(view::onError)
    orchestrator.onErrorNullList(view::onError)
    orchestrator.onErrorOther(view::onError)

    orchestrator.onStartLoading(view::startLoading)
    orchestrator.onStopLoading(view::stopLoading)
  }

  fun findAll() = orchestrator.findAll()
  fun search(query: String) = orchestrator.search(query)
}

