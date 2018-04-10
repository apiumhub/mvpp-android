package com.apiumhub.github.domain

import com.apiumhub.github.data.IGithubRepository
import com.apiumhub.github.domain.entity.*
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class MockGithubRepository: IGithubRepository {

    private val someOwner = RepositoryOwner("someLogin", 1, "http://someAvatarUrl.domain", "http://someUrl.domain")
    private val someRepository = Repository(1, "someRepository", "someName", someOwner, false, "http://someUrl.domain", "someDescription")

    private val someCommitDto = CommitsDto(10)

    private val someBranchDto = BranchDto("someBranchName")

    private val someRepositoryList: List<Repository> = listOf(someRepository, someRepository, someRepository)

    private val someRepositorySearchResult: RepositorySearchDto = RepositorySearchDto(3, false, someRepositoryList)

    private val someRepositoryCommitsList: List<CommitsDto> = listOf(someCommitDto, someCommitDto, someCommitDto)

    private val someBranchesList: List<BranchDto> = listOf(someBranchDto, someBranchDto, someBranchDto)

    private val someReadme: String = "someReadme"

    override fun findAllRepositories(): Observable<List<Repository>> = Observable.just(someRepositoryList).delay(500, TimeUnit.MILLISECONDS)

    override fun searchRepositories(query: String): Observable<RepositorySearchDto> = Observable.just(someRepositorySearchResult).delay(500, TimeUnit.MILLISECONDS)

    override fun getCommitsForRepository(user: String, repository: String): Observable<List<CommitsDto>> = Observable.just(someRepositoryCommitsList).delay(500, TimeUnit.MILLISECONDS)

    override fun getBranchesForRepository(user: String, repository: String): Observable<List<BranchDto>> = Observable.just(someBranchesList).delay(500, TimeUnit.MILLISECONDS)

    override fun getReadmeForRepository(user: String, repository: String): Observable<String> = Observable.just(someReadme).delay(500, TimeUnit.MILLISECONDS)
}