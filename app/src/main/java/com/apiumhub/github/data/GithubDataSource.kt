package com.apiumhub.github.data

import com.apiumhub.github.domain.entity.BranchDto
import com.apiumhub.github.domain.entity.CommitsDto
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.entity.RepositorySearchDto

interface GithubRepository {
  suspend fun findAllRepositories(): List<Repository>
  suspend fun searchRepositories(query: String): RepositorySearchDto
  suspend fun getCommitsForRepository(user: String, repository: String): List<CommitsDto>
  suspend fun getBranchesForRepository(user: String, repository: String): List<BranchDto>
  suspend fun getReadmeForRepository(user: String, repository: String): String

  companion object {
    fun create(api: GithubApi): GithubRepository = GithubDataSource(api)
  }
}

class GithubDataSource(private val api: GithubApi) :
  GithubRepository {

  override suspend fun findAllRepositories(): List<Repository> = api.findAllRepositories().await()

  override suspend fun searchRepositories(query: String): RepositorySearchDto = api.searchRepositories(query).await()

  override suspend fun getCommitsForRepository(user: String, repository: String): List<CommitsDto> =
    api.getCommitsForRepository(user, repository).await()

  override suspend fun getBranchesForRepository(user: String, repository: String): List<BranchDto> =
    api.getBranchesForRepository(user, repository).await()

  override suspend fun getReadmeForRepository(user: String, repository: String): String =
    api.getReadmeForRepository(user, repository).await()

//    executeRequest(api.getCommitsForRepository(user, repository)
//      //Github API returns 202 while is caching repo statistics. We're encouraged to try again in a few seconds
//      .doOnNext { if (it.code() == 202) throw StatsCachingException() }
//      .retryWhen {
//        //This can be moved outside to a properly parameterized class
//        var retryCount = 0
//        val maxRetries = 3
//        val delaySeconds = 3L
//
//        it.flatMap {
//          if (it is StatsCachingException) {
//            if (++retryCount < maxRetries) {
//              return@flatMap Observable.timer(delaySeconds, TimeUnit.SECONDS)
//            }
//          }
//          return@flatMap Observable.error<Throwable>(it)
//        }
//      }
//      .map { it.body()!! }, emptyList()
//    )

//  private fun <T> executeRequest(request: Observable<T>, returnOnError: T? = null): Observable<T> {
//    return request.onErrorReturn {
//      errorsStream.onNext(it)
//      returnOnError
//    }
//  }
}