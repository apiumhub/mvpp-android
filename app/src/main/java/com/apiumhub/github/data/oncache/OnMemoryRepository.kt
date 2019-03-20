package com.apiumhub.github.data.oncache

import com.apiumhub.github.domain.entity.BranchDto
import com.apiumhub.github.domain.entity.CommitsDto
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.entity.RepositorySearchDto

interface OnMemoryRepository {
  fun addOrUpdateRepositoriesByQuery(searchDto: RepositorySearchDto)
  fun getRepositoriesByQuery(): RepositorySearchDto

  fun addOrUpdateRepositories(list: List<Repository>)
  fun getRepositories(): List<Repository>

  fun addOrUpdateCommits(list: List<CommitsDto>)
  fun getCommitsForRepository(): List<CommitsDto>

  fun addOrUpdateBranches(list: List<BranchDto>)
  fun getBranches(): List<BranchDto>

  fun addOrUpdateReadme(readme: String)
  fun getReadme(): String

  companion object {
    fun create(): OnMemoryRepository = OnMemoryDataSource()
  }
}

class OnMemoryDataSource : OnMemoryRepository {
  private var repositories: List<Repository> = emptyList()
  private var repositorySearchDto: RepositorySearchDto = RepositorySearchDto(null, null, null)
  private var commits: List<CommitsDto> = emptyList()
  private var branches: List<BranchDto> = emptyList()
  private var readme: String = ""

  override fun getRepositories(): List<Repository> = repositories
  override fun getRepositoriesByQuery(): RepositorySearchDto = repositorySearchDto
  override fun getCommitsForRepository(): List<CommitsDto> = commits
  override fun getBranches(): List<BranchDto> = branches
  override fun getReadme(): String = readme

  override fun addOrUpdateRepositories(list: List<Repository>) {
    this.repositories = list
  }

  override fun addOrUpdateRepositoriesByQuery(searchDto: RepositorySearchDto) {
    this.repositorySearchDto = searchDto
  }

  override fun addOrUpdateCommits(list: List<CommitsDto>) {
    this.commits = list
  }

  override fun addOrUpdateBranches(list: List<BranchDto>) {
    this.branches = list
  }

  override fun addOrUpdateReadme(readme: String) {
    this.readme = readme
  }
}