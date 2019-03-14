package com.apiumhub.github.presentation.details

import com.apiumhub.github.domain.entity.RepositoryDetailsDto
import com.apiumhub.github.domain.repository.details.RepositoryDetailsService

interface IRepositoryDetailsView {
  var loadRepositoryDetails: (String, String) -> Unit

  fun repositoryInformationLoaded(details: RepositoryDetailsDto)
  fun readmeLoaded(readme: String)
}

class RepositoryDetailsPresenter(view: IRepositoryDetailsView, service: RepositoryDetailsService) {
  init {
    view.loadRepositoryDetails = service::getRepositoryDetails
    service.onDetailsLoaded(view::repositoryInformationLoaded)
    service.onReadmeLoaded(view::readmeLoaded)
  }
}