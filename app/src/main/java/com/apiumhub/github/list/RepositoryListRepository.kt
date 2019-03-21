package com.apiumhub.github.list

import com.apiumhub.github.core.data.GithubApi
import com.apiumhub.github.core.data.NetworkRepository
import com.apiumhub.github.core.domain.entity.Repository
import com.apiumhub.github.core.domain.entity.RepositorySearchDto
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*
import kotlin.collections.HashMap

abstract class InMemory(var expire: Long = Calendar.getInstance().timeInMillis) {
  fun isExpired() = Calendar.getInstance().timeInMillis > (expire + EXPIRES_IN)

  companion object {
    private const val EXPIRES_IN: Long = 1000 * 30
  }
}

data class RepositoryListInMemory(val list: List<Repository>) : InMemory()
data class RepositorySearchInMemory(val searchDto: RepositorySearchDto) : InMemory()

interface RepositoryListRepository {
  fun addOrUpdateRepositories(list: List<Repository>)
  fun addOrUpdateRepositorySearch(query: String, searchDto: RepositorySearchDto)

  fun findAllRepositories(): Observable<List<Repository>>
  fun searchRepositories(query: String): Observable<RepositorySearchDto>

  companion object {
    fun create(): RepositoryListRepository =
      InMemoryRepositoryListRepository()

    fun create(api: GithubApi, errorsStream: PublishSubject<Throwable>): RepositoryListRepository =
      GithubRepositoryListRepository(api, errorsStream)
  }
}


class InMemoryRepositoryListRepository : RepositoryListRepository {
  private var inMemoryList = RepositoryListInMemory(emptyList())
  private var inMemoryMapByQuery: HashMap<String, RepositorySearchInMemory> = HashMap()

  override fun addOrUpdateRepositories(list: List<Repository>) {
    this.inMemoryList = RepositoryListInMemory(list)
  }

  override fun addOrUpdateRepositorySearch(query: String, searchDto: RepositorySearchDto) {
    this.inMemoryMapByQuery[query] = RepositorySearchInMemory(searchDto)
  }

  override fun findAllRepositories(): Observable<List<Repository>> =
    Observable.just(if (!inMemoryList.isExpired()) inMemoryList.list else emptyList())

  override fun searchRepositories(query: String): Observable<RepositorySearchDto> =
    Observable.just(inMemoryMapByQuery[query]?.let { it.searchDto }
      ?: RepositorySearchDto(null, null, emptyList()))

}

class GithubRepositoryListRepository(private val api: GithubApi, errorsStream: PublishSubject<Throwable>) :
  NetworkRepository(errorsStream), RepositoryListRepository {
  override fun addOrUpdateRepositories(list: List<Repository>) {}

  override fun addOrUpdateRepositorySearch(query: String, searchDto: RepositorySearchDto) {}

  override fun findAllRepositories(): Observable<List<Repository>> =
    executeRequest(api.findAllRepositories())

  override fun searchRepositories(query: String): Observable<RepositorySearchDto> =
    executeRequest(api.searchRepositories(query))
}