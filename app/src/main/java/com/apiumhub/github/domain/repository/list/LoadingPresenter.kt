package com.apiumhub.github.domain.repository.list

interface LoadingService {
  fun onLoading(func: () -> Unit)
  fun stopLoading(func: () -> Unit)
  fun hideLoading(func: () -> Unit)
}

interface LoadingView {
  fun showLoading()
  fun hideLoading()
  fun stopLoading()
}

class LoadingPresenter(view: LoadingView, service: LoadingService) {
  init {
    service.onLoading(view::showLoading)
    service.stopLoading(view::stopLoading)
    service.hideLoading(view::hideLoading)
  }
}