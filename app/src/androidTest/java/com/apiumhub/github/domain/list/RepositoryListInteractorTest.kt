package com.apiumhub.github.domain.list

import com.apiumhub.github.domain.MockGithubRepository
import com.apiumhub.github.presentation.list.IRepositoryListService
import org.junit.Test
import java.util.concurrent.CountDownLatch

class RepositoryListInteractorTest {
    private val sut: IRepositoryListService = IRepositoryListService.create(MockGithubRepository())

    private val someSearchQuery = "someSearchString"

    @Test
    fun test_find_all_repositories_should_call_on_repos_found() {
        val countDownLatch = CountDownLatch(1)
        sut.onReposFound {
            countDownLatch.countDown()
        }
        sut.findAll()
        countDownLatch.await()
    }

    @Test
    fun test_search_repositories_should_call_on_repos_found() {
        val countDownLatch = CountDownLatch(1)
        sut.onReposFound {
            countDownLatch.countDown()
        }
        sut.search(someSearchQuery)
        countDownLatch.await()
    }
}