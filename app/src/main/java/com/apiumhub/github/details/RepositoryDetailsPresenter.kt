package com.apiumhub.github.details

class RepositoryDetailsPresenter(view: RepositoryDetailsView, service: RepositoryDetailsService) {
  init {
    view.bindLoadDetails(service::getRepositoryDetails)
    view.bindDestroy(service::cancel)

    service.onDetailsLoaded(view::repositoryInformationLoaded)
    service.onReadmeLoaded(view::readmeLoaded)
  }
}