package com.apiumhub.github.data

import com.apiumhub.github.domain.entity.BranchDto
import com.apiumhub.github.domain.entity.CommitsDto
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.entity.RepositorySearchDto
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

interface IGithubRepository {
    fun findAllRepositories(): Observable<List<Repository>>
    fun searchRepositories(query: String): Observable<RepositorySearchDto>
    fun getCommitsForRepository(user: String, repository: String): Observable<List<CommitsDto>>
    fun getBranchesForRepository(user: String, repository: String): Observable<List<BranchDto>>
    fun getReadmeForRepository(user: String, repository: String): Observable<String>

    companion object {
        fun create(): IGithubRepository {
            return GithubRepository(GithubApi.create(), errorsStream)
        }

        fun create(api: GithubApi, errorsStream: PublishSubject<Throwable>): IGithubRepository {
            return GithubRepository(api, errorsStream)
        }

        val errorsStream: PublishSubject<Throwable> = PublishSubject.create()
    }
}

class GithubRepository(private val api: GithubApi, private val errorsStream: PublishSubject<Throwable>) : IGithubRepository {

    override fun findAllRepositories(): Observable<List<Repository>> =
            executeRequest(api.findAllRepositories(), emptyList())

    override fun searchRepositories(query: String): Observable<RepositorySearchDto> =
            executeRequest(api.searchRepositories(query), RepositorySearchDto(0, true, emptyList()))

    override fun getCommitsForRepository(user: String, repository: String): Observable<List<CommitsDto>> =
            executeRequest(api.getCommitsForRepository(user, repository)
                    //Github API returns 202 while is caching repo statistics. We're encouraged to try again in a few seconds
                    .doOnNext { if (it.code() == 202) throw StatsCachingException() }
                    .retryWhen{
                        //This can be moved outside to a properly parameterized class
                        var retryCount = 0
                        val maxRetries = 3
                        val delaySeconds = 3L

                        it.flatMap {
                            if (it is StatsCachingException){
                                if (++retryCount < maxRetries) {
                                    return@flatMap Observable.timer(delaySeconds, TimeUnit.SECONDS)
                                }
                            }
                            return@flatMap Observable.error<Throwable>(it)
                        }
                    }
                    .map { it.body()!! }, emptyList())

    override fun getBranchesForRepository(user: String, repository: String): Observable<List<BranchDto>> =
            executeRequest(api.getBranchesForRepository(user, repository), emptyList())

    override fun getReadmeForRepository(user: String, repository: String): Observable<String> =
            executeRequest(api.getReadmeForRepository(user, repository), String())

    private fun <T> executeRequest(request: Observable<T>, returnOnError: T? = null): Observable<T> {
        return request.onErrorReturn {
            errorsStream.onNext(it)
            returnOnError
        }
    }
}