package com.apiumhub.github.data

import com.apiumhub.github.DataProvider
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test


class GithubRepositoryTest {
  private val api = mockk<GithubApi>()

  private lateinit var sut: GithubRepository

  @Before
  fun setUp() {
    sut = GithubRepository.create(api)
  }

//  @Test
//  fun `should publish error when returned error exception from api`() {
//    val countDownLatch = CountDownLatch(2)
//
//    val expected = Response.error<List<Repository>>(500, ResponseBody.create(null, "[]"))
//
//    every { api.findAllRepositories() } returns Observable.error(HttpException(expected))
//
//    errorsStream.subscribe {
//      assertTrue(it is HttpException)
//      assertTrue((it as HttpException).code() == 500)
//      countDownLatch.countDown()
//    }
//
//    sut.findAllRepositories().subscribe {
//      verify { api.findAllRepositories() }
//      assertTrue(it.isEmpty())
//      countDownLatch.countDown()
//    }
//
//    countDownLatch.await()
//  }

  @Test
  fun `should find all repositories when returned data from api`() = runBlocking {
    val expected = DataProvider.repositories

    val deferred = CompletableDeferred(expected)
    coEvery { api.findAllRepositories() } returns deferred

    val actual = sut.findAllRepositories()
    assertEquals(expected, actual)
  }

//  @Test
//  fun `should find all commits when returned data from api`() {
//    val countDownLatch = CountDownLatch(1)
//    val expected = Response.success(DataProvider.commitsDto)
//    val user = "someUser"
//    val repository = "someRepository"
//
//    every { api.getCommitsForRepository(user, repository) } returns Observable.just(expected)
//
//    sut.getCommitsForRepository(user, repository).subscribe {
//      verify { api.getCommitsForRepository(user, repository) }
//      assertEquals(expected.body(), it)
//      countDownLatch.countDown()
//    }
//
//    countDownLatch.await()
//  }
}