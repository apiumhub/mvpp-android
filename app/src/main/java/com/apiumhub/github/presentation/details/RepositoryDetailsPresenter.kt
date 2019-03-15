package com.apiumhub.github.presentation.details

import com.apiumhub.github.domain.entity.RepositoryDetailsDto
import com.apiumhub.github.domain.repository.details.RepositoryDetailsService

interface IRepositoryDetailsView {
  var onLoadRepositoryDetails: (String, String) -> Unit
  var onDestroy: () -> Unit

  fun repositoryInformationLoaded(details: RepositoryDetailsDto)
  fun readmeLoaded(readme: String)
}

class RepositoryDetailsPresenter(view: IRepositoryDetailsView, service: RepositoryDetailsService) {
  init {
    view.onLoadRepositoryDetails = service::getRepositoryDetails
    view.onDestroy = service::cancel

    service.onDetailsLoaded(view::repositoryInformationLoaded)
    service.onReadmeLoaded(view::readmeLoaded)
  }
}