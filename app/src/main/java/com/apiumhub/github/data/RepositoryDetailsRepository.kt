package com.apiumhub.github.data

import com.apiumhub.github.data.common.GithubApi
import com.apiumhub.github.data.common.InMemoryRepository
import com.apiumhub.github.data.common.NetworkRepository
import com.apiumhub.github.data.common.exception.StatsCachingException
import com.apiumhub.github.domain.entity.BranchDto
import com.apiumhub.github.domain.entity.CommitsDto
import com.apiumhub.github.domain.entity.RepositoryDetailsDto
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface RepositoryDetailsRepository {
  fun addOrUpdateRepositoryDetails(repositoryDetails: RepositoryDetailsDto)
  fun addOrUpdateReadme(readme: String)
  fun getRepositoryDetails() : Observable<RepositoryDetailsDto>

  fun getCommitsForRepository(user: String, repository: String): Observable<List<CommitsDto>>
  fun getBranchesForRepository(user: String, repository: String): Observable<List<BranchDto>>
  fun getReadmeForRepository(user: String, repository: String): Observable<String>

  companion object {
    fun create(): RepositoryDetailsRepository = InMemoryRepositoryDetailsRepository()

    fun create(api: GithubApi, errorsStream: PublishSubject<Throwable>): RepositoryDetailsRepository =
      GithubRepositoryDetailsRepository(api, errorsStream)
  }
}

class InMemoryRepositoryDetailsRepository : InMemoryRepository(), RepositoryDetailsRepository {
  private var repositoryDetails: RepositoryDetailsDto = RepositoryDetailsDto(null, null)
  private var readme: String = ""

  override fun addOrUpdateRepositoryDetails(repositoryDetails: RepositoryDetailsDto) =
    refresh().also { this.repositoryDetails = repositoryDetails }

  override fun addOrUpdateReadme(readme: String) =
    refresh().also { this.readme = readme }

  override fun getRepositoryDetails(): Observable<RepositoryDetailsDto> =
    Observable.just(if (!isExpired) repositoryDetails else RepositoryDetailsDto(null, null))

  override fun getCommitsForRepository(user: String, repository: String): Observable<List<CommitsDto>> =
    Observable.just(emptyList())

  override fun getBranchesForRepository(user: String, repository: String): Observable<List<BranchDto>> =
    Observable.just(emptyList())

  override fun getReadmeForRepository(user: String, repository: String): Observable<String> =
    Observable.just(if (!isExpired) readme else "")
}


class GithubRepositoryDetailsRepository(private val api: GithubApi, errorsStream: PublishSubject<Throwable>) :
  NetworkRepository(errorsStream), RepositoryDetailsRepository {

  override fun getCommitsForRepository(user: String, repository: String) =
    executeRequest(api.getCommitsForRepository(user, repository)
      .doOnNext { if (it.code() == 202) throw StatsCachingException() }
      .retryWhen {
        it.flatMap { throwable -> retryThrowable(throwable, throwable is StatsCachingException) }
      }.map { it.body()?.let { list -> list } ?: emptyList() }, emptyList()
    )

  override fun getBranchesForRepository(user: String, repository: String) =
    executeRequest(api.getBranchesForRepository(user, repository), emptyList())

  override fun getReadmeForRepository(user: String, repository: String) =
    executeRequest(api.getReadmeForRepository(user, repository))

  override fun addOrUpdateReadme(readme: String) {}
  override fun addOrUpdateRepositoryDetails(repositoryDetails: RepositoryDetailsDto) {}
  override fun getRepositoryDetails(): Observable<RepositoryDetailsDto> = Observable.error(Exception())
}