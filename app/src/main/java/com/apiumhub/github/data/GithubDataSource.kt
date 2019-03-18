package com.apiumhub.github.data

import com.apiumhub.github.domain.entity.BranchDto
import com.apiumhub.github.domain.entity.CommitsDto
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.entity.RepositorySearchDto
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

interface GithubRepository {
  fun findAllRepositories(): Observable<List<Repository>>
  fun searchRepositories(query: String): Observable<RepositorySearchDto>
  fun getCommitsForRepository(user: String, repository: String): Observable<List<CommitsDto>>
  fun getBranchesForRepository(user: String, repository: String): Observable<List<BranchDto>>
  fun getReadmeForRepository(user: String, repository: String): Observable<String>

  companion object {
    fun create(api: GithubApi): GithubRepository = GithubDataSource(api)
  }
}

class GithubDataSource(private val api: GithubApi) : GithubRepository {

  override fun findAllRepositories() =
    api.findAllRepositories()

  override fun searchRepositories(query: String) =
    api.searchRepositories(query)

  override fun getCommitsForRepository(user: String, repository: String) =
    api.getCommitsForRepository(user, repository)
      .doOnNext { if (it.code() == 202) throw StatsCachingException() }
      .retryWhen {
        var retryCount = 0
        var maxRetries = 3
        var delaySeconds = 3L

        it.flatMap {
          if (it is StatsCachingException) {
            if (++retryCount < maxRetries) {
              return@flatMap Observable.timer(delaySeconds, TimeUnit.SECONDS)
            }
          }
          return@flatMap Observable.error<Throwable>(it)
        }
      }.map { it.body()?.let { list -> list } ?: emptyList() }

  override fun getBranchesForRepository(user: String, repository: String) =
    api.getBranchesForRepository(user, repository)

  override fun getReadmeForRepository(user: String, repository: String) =
    api.getReadmeForRepository(user, repository)
}