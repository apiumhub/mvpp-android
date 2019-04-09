package com.apiumhub.github.list.old

import com.apiumhub.github.core.domain.entity.Repository

interface RepositoryListViewOld {
  fun showLoading()
  fun hideLoading()

  fun showData(items: List<Repository>)
  fun showEmptyData()
  fun showNetworkError()
  fun showOtherError()
}

interface RepositoryListServiceOld {
  fun search(
    query: String,
    onError: (Throwable) -> Unit = {},
    onNext: (List<Repository>?) -> Unit = {},
    onComplete: () -> Unit = {}
  )

  fun cancel()
}

sealed class Action {
  class Search(val query: String) : Action()
  object Destroy : Action()
}