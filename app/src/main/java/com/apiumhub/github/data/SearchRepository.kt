package com.apiumhub.github.data

import com.apiumhub.github.data.common.GithubApi
import com.apiumhub.github.data.common.NetworkRepository
import com.apiumhub.github.domain.entity.RepositorySearchDto
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface SearchRepository {
  fun addOrUpdateSearch(searchDto: RepositorySearchDto)
  fun searchRepositories(query: String): Observable<RepositorySearchDto>

  companion object {
    fun create(): SearchRepository = InMemorySearchRepository()

    fun create(api: GithubApi, errorsStream: PublishSubject<Throwable>): SearchRepository =
      GithubSearchRepository(api, errorsStream)
  }
}

class InMemorySearchRepository : SearchRepository {
  private var searchDto: RepositorySearchDto = RepositorySearchDto(null, null, null)

  override fun addOrUpdateSearch(searchDto: RepositorySearchDto) {
    this.searchDto = searchDto
  }

  override fun searchRepositories(query: String): Observable<RepositorySearchDto> = Observable.just(searchDto)
}

class GithubSearchRepository(private val api: GithubApi, errorsStream: PublishSubject<Throwable>) :
  NetworkRepository(errorsStream), SearchRepository {

  override fun addOrUpdateSearch(searchDto: RepositorySearchDto) {
    // no-impl
  }

  override fun searchRepositories(query: String) = executeRequest(api.searchRepositories(query))
}