package com.apiumhub.github.list


class RepositoryListPresenter(view: RepositoryListView, service: RepositoryListService) {
  init {
    view.bindSearch(service::search)
    view.bindDestroy(service::cancel)

    service.bindStart(view::showLoading)
    service.bindStop(view::hideLoading)

    service.bindData(view::showData)
    service.bindEmptyData(view::showEmptyData)
    service.bindNetworkError(view::showNetworkError)
    service.bindGenericError(view::showGenericError)
  }
}
