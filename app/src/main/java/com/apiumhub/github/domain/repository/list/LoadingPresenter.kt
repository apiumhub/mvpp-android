package com.apiumhub.github.domain.repository.list

interface LoadingService {
    var onLoading: () -> Unit
    var stopLoading: () -> Unit
    var hideLoading: () -> Unit
}

interface LoadingView {
    fun showLoading()
    fun hideLoading()
    fun stopLoading()
}

class LoadingPresenter(view: LoadingView, service: LoadingService) {
    init {
        service.onLoading = view::showLoading
        service.stopLoading = view::stopLoading
        service.hideLoading = view::hideLoading
    }
}