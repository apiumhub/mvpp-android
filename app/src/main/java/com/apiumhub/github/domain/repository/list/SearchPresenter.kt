package com.apiumhub.github.domain.repository.list

import com.apiumhub.github.domain.entity.Repository

interface SearchView: LoadingView {
    fun onData(data: List<Repository>)
    fun onEmpty()
    fun onError()
}

class SearchPresenter(view: SearchView, service: SearchService) {
    val onFindAll: () -> Unit = service::findAll
    val onSearch: (String) -> Unit = service::search

    init {
        service.onData = view::onData
        service.onEmpty = view::onEmpty
        service.onNoInternet = view::onError
        service.onOtherError = view::onError

        service.onLoading = view::showLoading
        service.stopLoading = view::stopLoading
        service.hideLoading = view::hideLoading
    }
}

