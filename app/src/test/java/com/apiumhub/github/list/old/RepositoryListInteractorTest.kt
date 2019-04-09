package com.apiumhub.github.list.old

import com.apiumhub.github.core.domain.entity.Repository
import com.apiumhub.github.list.RepositoryListRepository
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class RepositoryListInteractorTest {

  private val networkRepository: RepositoryListRepository = mockk()
  private val inMemoryRepository: RepositoryListRepository = mockk()

  lateinit var sut: RepositoryListServiceOld
  private var countDownLatch = CountDownLatch(1)

  @Before
  fun setUp() {
    sut = RepositoryListInteractor(
      networkRepository,
      inMemoryRepository,
      Schedulers.trampoline(),
      Schedulers.trampoline()
    )

    `when inMemory repository addOrUpdate returns unit`()
  }

  @Test(timeout = 3000)
  fun `given a empty query, when in memory and network repositories returns emptyList then search trigger empty`() {
    val expected = emptyList<Repository>()

    `when inMemory repository find all returns`(expected)
    `when network repository find all returns`(expected)
    `then on sut search`(onNext = {
      assertEquals(expected, it)
      countDownLatch.countDown()
    })
  }

  @Test(timeout = 3000)
  fun `given empty query, when in memory returns emptylist and network throws an error, then sut search triggers error`() {
    val expected = Error()

    `when inMemory repository find all returns`(emptyList())
    `when network repository find all throws`(expected)
    `then on sut search`(onError = {
      assertEquals(expected, it)
      countDownLatch.countDown()
    })
  }

  @Test(timeout = 3000)
  fun `given empty query, when in memory throws an error and network returns emptyList, then sut search triggers error`() {
    val expected = emptyList<Repository>()
    val error = Error()

    `when inMemory repository find all throws`(Error())
    `when network repository find all returns`(expected)
    `then on sut search`(onError = {
      assertEquals(error, it)
      countDownLatch.countDown()
    })
  }

  private fun `then on sut search`(query: String = "", onError: (Throwable) -> Unit = {}, onNext: (List<Repository>?) -> Unit = {}, onComplete: () -> Unit = {}){
    countDownLatch = CountDownLatch(1)
    sut.search(query, onError, onNext, onComplete)
    countDownLatch.await()
  }


  private fun `when network repository find all returns`(expected: List<Repository>) {
    every { networkRepository.findAllRepositories() } returns Observable.just(expected)
  }

  private fun `when network repository find all throws`(error: Throwable) {
    every { networkRepository.findAllRepositories() } returns Observable.error(error)
  }

  private fun `when inMemory repository find all returns`(expected: List<Repository>) {
    every { inMemoryRepository.findAllRepositories() } returns Observable.just(expected)
  }

  private fun `when inMemory repository find all throws`(error: Throwable) {
    every { inMemoryRepository.findAllRepositories() } returns Observable.error(error)
  }

  private fun `when inMemory repository addOrUpdate returns unit`() {
    every { inMemoryRepository.addOrUpdateRepositories(any()) } returns Unit
    every { inMemoryRepository.addOrUpdateRepositorySearch(any(), any()) } returns Unit
  }
}