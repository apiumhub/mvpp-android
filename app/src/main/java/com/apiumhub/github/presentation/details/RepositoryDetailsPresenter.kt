package com.apiumhub.github.presentation.details

import com.apiumhub.github.data.IGithubRepository
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.entity.RepositoryDetailsDto
import com.apiumhub.github.domain.repository.details.RepositoryDetailsInteractor

interface IRepositoryDetailsService {

    fun getRepositoryDetails(user: String, repositoryName: String)
    fun onDetailsLoaded(func: (details: RepositoryDetailsDto) -> Unit)
    fun onReadmeLoaded(func: (readme: String) -> Unit)

    companion object {
        fun create() = RepositoryDetailsInteractor(IGithubRepository.create())
    }

}

interface IRepositoryDetailsView {

    fun loadRepositoryDetails(func: (user: String, repository: String) -> Unit)
    fun repositoryInformationLoaded(details: RepositoryDetailsDto)
    fun readmeLoaded(readme: String)

    companion object {
        fun create(repository: Repository) = RepositoryDetailsFragment.newInstance(repository)
    }
}

class RepositoryDetailsPresenter(view: IRepositoryDetailsView, service: IRepositoryDetailsService) {
    init {
        view.loadRepositoryDetails { user, repository -> service.getRepositoryDetails(user, repository) }
        service.onDetailsLoaded { view.repositoryInformationLoaded(it) }
        service.onReadmeLoaded { view.readmeLoaded(it) }
    }
}