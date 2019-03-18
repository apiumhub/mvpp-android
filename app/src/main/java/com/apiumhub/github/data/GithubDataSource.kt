package com.apiumhub.github.data

import com.apiumhub.github.domain.entity.BranchDto
import com.apiumhub.github.domain.entity.CommitsDto
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.entity.RepositorySearchDto
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface GithubRepository {
  fun findAllRepositories(): Observable<List<Repository>>
  fun searchRepositories(query: String): Observable<RepositorySearchDto>
  fun getCommitsForRepository(user: String, repository: String): Observable<List<CommitsDto>>
  fun getBranchesForRepository(user: String, repository: String): Observable<List<BranchDto>>
  fun getReadmeForRepository(user: String, repository: String): Observable<String>

  companion object {
    fun create(api: GithubApi, errorsStream: PublishSubject<Throwable>): GithubRepository =
      GithubDataSource(api, errorsStream)
  }
}

class GithubDataSource(private val api: GithubApi, private val errorsStream: PublishSubject<Throwable>) :
  GithubRepository {

  override fun findAllRepositories() =
    api.findAllRepositories()

  override fun searchRepositories(query: String) =
    api.searchRepositories(query)

  override fun getCommitsForRepository(user: String, repository: String) =
    api.getCommitsForRepository(user, repository)

  override fun getBranchesForRepository(user: String, repository: String) =
    api.getBranchesForRepository(user, repository)

  override fun getReadmeForRepository(user: String, repository: String) =
    api.getReadmeForRepository(user, repository)
}