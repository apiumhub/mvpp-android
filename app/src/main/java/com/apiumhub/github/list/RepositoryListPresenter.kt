package com.apiumhub.github.list

class RepositoryListPresenter(view: RepositoryListView, service: RepositoryListService) {
  init {
    view.onSearch = service::search
    view.onDestroy(service::cancel)

    service.onStart(view::showLoading)
    service.onStop(view::hideLoading)

    service.onDataFound(view::showData)
    service.onEmpty(view::showEmpty)
    service.onErrorNoInternet(view::showError)
    service.onErrorNullList(view::showError)
    service.onErrorOther(view::showError)
  }
}