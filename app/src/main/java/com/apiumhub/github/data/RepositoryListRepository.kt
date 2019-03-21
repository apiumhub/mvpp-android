package com.apiumhub.github.data

import com.apiumhub.github.data.common.GithubApi
import com.apiumhub.github.data.common.NetworkRepository
import com.apiumhub.github.data.common.InMemoryRepository
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.entity.RepositorySearchDto
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface RepositoryListRepository {
  fun addOrUpdateRepositories(list: List<Repository>)
  fun addOrUpdateRepositorySearch(searchDto: RepositorySearchDto)

  fun findAllRepositories(): Observable<List<Repository>>
  fun searchRepositories(query: String): Observable<RepositorySearchDto>

  companion object {
    fun create(): RepositoryListRepository =
      InMemoryRepositoryListRepository()

    fun create(api: GithubApi, errorsStream: PublishSubject<Throwable>): RepositoryListRepository =
      GithubRepositoryListRepository(api, errorsStream)
  }
}

class InMemoryRepositoryListRepository : InMemoryRepository(), RepositoryListRepository {
  private var repositoryList: List<Repository> = emptyList()
  private var repositorySearchDto: RepositorySearchDto = RepositorySearchDto(null, null, emptyList())

  override fun addOrUpdateRepositories(list: List<Repository>) =
    refresh().also { this.repositoryList = list }


  override fun addOrUpdateRepositorySearch(searchDto: RepositorySearchDto) =
    refresh().also { this.repositorySearchDto = searchDto }

  override fun findAllRepositories(): Observable<List<Repository>> =
    Observable.just(if (!isExpired) repositoryList else emptyList())


  override fun searchRepositories(query: String): Observable<RepositorySearchDto> =
    Observable.just(if (!isExpired) repositorySearchDto else RepositorySearchDto(null, null, emptyList()))

}

class GithubRepositoryListRepository(private val api: GithubApi, errorsStream: PublishSubject<Throwable>) :
  NetworkRepository(errorsStream), RepositoryListRepository {
  override fun addOrUpdateRepositories(list: List<Repository>) {}

  override fun addOrUpdateRepositorySearch(searchDto: RepositorySearchDto) {}

  override fun findAllRepositories(): Observable<List<Repository>> =
    executeRequest(api.findAllRepositories())

  override fun searchRepositories(query: String): Observable<RepositorySearchDto> =
    executeRequest(api.searchRepositories(query))
}