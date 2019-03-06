package com.apiumhub.github.domain.repository.list

import com.apiumhub.github.domain.entity.Repository

interface SearchService {
  fun search(query: String)
  fun onData(func: (List<Repository>) -> Unit)
  fun onEmpty(func: () -> Unit)
  fun onNoInternet(func: () -> Unit)
  fun onOtherError(func: () -> Unit)
}

interface SearchView {
  fun onSearch(func: (String) -> Unit)
  fun onData(data: List<Repository>)
  fun onEmpty()
  fun onError()
}

class SearchPresenter(view: SearchView, service: SearchService) {
  init {
    view.onSearch(service::search)
    service.onData(view::onData)
    service.onEmpty(view::onEmpty)
    service.onNoInternet(view::onError)
    service.onOtherError(view::onError)
  }
}

