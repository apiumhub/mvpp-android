package com.apiumhub.github.presentation.details

import com.apiumhub.github.domain.RepositoryDetailsService

class RepositoryDetailsPresenter(view: RepositoryDetailsView, service: RepositoryDetailsService) {
  init {
    view.onLoadRepositoryDetails(service::getRepositoryDetails)
    view.onDestroy(service::cancel)

    service.onDetailsLoaded(view::repositoryInformationLoaded)
    service.onReadmeLoaded(view::readmeLoaded)
  }
}