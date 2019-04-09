package com.apiumhub.github.core.domain.networkRepository.list

import com.apiumhub.github.core.domain.entity.Repository
import com.apiumhub.github.core.domain.entity.RepositorySearchDto
import com.apiumhub.github.list.RepositoryListRepository
import com.apiumhub.github.list.RepositoryListService
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import java.net.UnknownHostException
import java.util.concurrent.CountDownLatch

class RepositoryListServiceTest {

  private val networkRepository: RepositoryListRepository = mockk()
  private val inMemoryRepository: RepositoryListRepository = mockk()

  lateinit var sut: RepositoryListService

  private var countDownLatch = CountDownLatch(3)

  @Before
  fun setUp() {
    sut = RepositoryListService.create(
      networkRepository,
      inMemoryRepository,
      Schedulers.trampoline(),
      Schedulers.trampoline()
    )
  }

  @Test(timeout = 3000)
  fun `should send empty event when find all repositories and retrieved list is empty`() {
    getOnMemoryEmptyRepositories()
    every { networkRepository.findAllRepositories() } returns Observable.just(emptyList())
    sut.bindEmptyData { countDownLatch.countDown() }
    subscription()
  }

  @Test(timeout = 3000)
  fun `should send found event when find all repositories and retrieved list is not empty`() {
    getOnMemoryEmptyRepositories()
    val expected = listOf(Repository(0, "", "", null, true, "", ""))
    every { networkRepository.findAllRepositories() } returns Observable.just(expected)
    every { inMemoryRepository.addOrUpdateRepositories(expected) } returns Unit
    sut.bindData {
      assert(it == expected)
      countDownLatch.countDown()
    }
    subscription()
  }

  @Test(timeout = 3000)
  fun `should send error no internet event when find all repositories and throws unknown host exception`() {
    getOnMemoryEmptyRepositories()
    every { networkRepository.findAllRepositories() } returns Observable.error(UnknownHostException())
    sut.bindNetworkError { countDownLatch.countDown() }
    subscription()
  }

  @Test(timeout = 3000)
  fun `should send error other event when find all repositories and throws any exception`() {
    getOnMemoryEmptyRepositories()
    every { networkRepository.findAllRepositories() } returns Observable.error(Exception())
    sut.bindOtherError { countDownLatch.countDown() }
    subscription()
  }

  @Test(timeout = 3000)
  fun `should send empty event when search by query and retrieved list is empty`() {
    val query = "query"
    getOnMemoryEmptyRepositories(query)
    val expected = RepositorySearchDto(
      null,
      null,
      emptyList()
    )
    every { networkRepository.searchRepositories(query) } returns Observable.just(expected)
    sut.bindEmptyData {
      countDownLatch.countDown()
    }
    subscription(query)
  }

  @Test(timeout = 3000)
  fun `should send found event when search by query and retrieved list have items`() {
    val query = "query"
    getOnMemoryEmptyRepositories(query)
    val expected = RepositorySearchDto(
      null,
      null,
      listOf(Repository(0, "", "", null, true, "", ""))
    )
    every { networkRepository.searchRepositories(query) } returns Observable.just(expected)
    sut.bindData {
      assert(it == expected.items)
      countDownLatch.countDown()
    }
    subscription(query)
  }

  @Test(timeout = 3000)
  fun `should send error no internet event when search by query throws unknown host exception`() {
    val query = "query"
    getOnMemoryEmptyRepositories(query)
    every { networkRepository.searchRepositories(query) } returns Observable.error(UnknownHostException())
    sut.bindNetworkError { countDownLatch.countDown() }
    subscription(query)
  }

  @Test(timeout = 3000)
  fun `should send error other event when search by query throws any other exception`() {
    val query = "query"
    getOnMemoryEmptyRepositories(query)
    every { networkRepository.searchRepositories(query) } returns Observable.error(Exception())
    sut.bindOtherError { countDownLatch.countDown() }
    subscription(query)
  }

  private fun subscription(query: String = "") {
    this.countDownLatch = CountDownLatch(3)
    sut.bindStart {
      countDownLatch.countDown()
    }
    sut.bindStop {
      countDownLatch.countDown()
    }
    sut.search(query)
    countDownLatch.await()
  }

  private fun getOnMemoryEmptyRepositories(query: String = "") {
    every { inMemoryRepository.findAllRepositories() } returns Observable.just(emptyList())
    every { inMemoryRepository.searchRepositories(query) } returns Observable.just(
      RepositorySearchDto(
        null,
        null,
        emptyList()
      )
    )
    every { inMemoryRepository.addOrUpdateRepositories(any()) } returns Unit
    every { inMemoryRepository.addOrUpdateRepositorySearch(any(), any()) } returns Unit
  }
}
