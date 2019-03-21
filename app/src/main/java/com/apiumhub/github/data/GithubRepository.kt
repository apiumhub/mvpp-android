package com.apiumhub.github.data

import com.apiumhub.github.data.common.GithubApi
import com.apiumhub.github.data.exception.StatsCachingException
import com.apiumhub.github.domain.entity.BranchDto
import com.apiumhub.github.domain.entity.CommitsDto
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

interface GithubRepository {
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

  private fun <T> executeRequest(request: Observable<T>, returnOnError: T? = null): Observable<T> {
    return request.onErrorReturn {
      errorsStream.onNext(it)
      returnOnError
    }
  }

  private fun retryThrowable(
    throwable: Throwable, condition: Boolean, maxRetries: Int = 3, delaySeconds: Long = 3L
  ): Observable<Long>? {
    var retryCount = 0

    return if (condition && ++retryCount < maxRetries) {
      Observable.timer(delaySeconds, TimeUnit.SECONDS)
    } else Observable.error(throwable)
  }
}