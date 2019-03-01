package com.apiumhub.github.data

import com.apiumhub.github.DataProvider
import com.apiumhub.github.domain.entity.Repository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.util.concurrent.CountDownLatch


class GithubRepositoryTest {
  private val api = mockk<GithubApi>()

  private lateinit var sut: IGithubRepository

  private val errorsStream = PublishSubject.create<Throwable>()

  @Before
  fun setUp() {
    sut = IGithubRepository.create(api, errorsStream)
  }

  @Test
  fun `should publish error when returned error exception from api`() {
    val countDownLatch = CountDownLatch(2)

    val expected = Response.error<List<Repository>>(500, ResponseBody.create(null, "[]"))

    every { api.findAllRepositories() } returns Observable.error(HttpException(expected))

    errorsStream.subscribe {
      assertTrue(it is HttpException)
      assertTrue((it as HttpException).code() == 500)
      countDownLatch.countDown()
    }

    sut.findAllRepositories().subscribe {
      verify { api.findAllRepositories() }
      assertTrue(it.isEmpty())
      countDownLatch.countDown()
    }

    countDownLatch.await()
  }

  @Test
  fun `should find all repositories when returned data from api`() {
    val countDownLatch = CountDownLatch(1)

    val expected = DataProvider.repositories


    every { api.findAllRepositories() } returns Observable.just(expected)

    sut.findAllRepositories().subscribe {
      verify { api.findAllRepositories() }
      assertEquals(expected, it)
      countDownLatch.countDown()
    }

    countDownLatch.await()
  }

  @Test
  fun `should find all commits when returned data from api`() {
    val countDownLatch = CountDownLatch(1)
    val expected = Response.success(DataProvider.commitsDto)
    val user = "someUser"
    val repository = "someRepository"

    every { api.getCommitsForRepository(user, repository) } returns Observable.just(expected)

    sut.getCommitsForRepository(user, repository).subscribe {
      verify { api.getCommitsForRepository(user, repository) }
      assertEquals(expected.body(), it)
      countDownLatch.countDown()
    }

    countDownLatch.await()
  }
}