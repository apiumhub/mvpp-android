package com.apiumhub.github.domain.repository.list

import com.apiumhub.github.data.GithubRepository
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.entity.RepositorySearchDto
import io.mockk.every
import io.mockk.mockk
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import java.net.UnknownHostException
import java.util.concurrent.CountDownLatch

class RepositoryListServiceTest {

  private val repository: GithubRepository = mockk()

  lateinit var sut: RepositoryListService

  private var countDownLatch = CountDownLatch(3)

  @Before
  fun setUp() {
    sut = RepositoryListService.create(repository, Schedulers.trampoline(), Schedulers.trampoline())
  }

  @Test (timeout = 3000)
  fun `should send empty event when find all repositories and retrieved list is empty`() {
    every { repository.findAllRepositories() } returns Observable.just(emptyList<Repository>())
    sut.onEmpty { countDownLatch.countDown() }
    subscription()
  }

  @Test (timeout = 3000)
  fun `should send found event when find all repositories and retrieved list is not empty`() {
    val expected = listOf(Repository(0, "", "", null, true, "", ""))
    every { repository.findAllRepositories() } returns Observable.just(expected)
    sut.onDataFound {
      assert(it == expected)
      countDownLatch.countDown()
    }
    subscription()
  }

  @Test (timeout = 3000)
  fun `should send error null event when find all repositories and throws illegal argument exception`() {
    every { repository.findAllRepositories() } returns Observable.error(IllegalArgumentException())
    sut.onErrorNullList { countDownLatch.countDown() }
    subscription()
  }

  @Test (timeout = 3000)
  fun `should send error no internet event when find all repositories and throws unknown host exception`() {
    every { repository.findAllRepositories() } returns Observable.error(UnknownHostException())
    sut.onErrorNoInternet { countDownLatch.countDown() }
    subscription()
  }

  @Test (timeout = 3000)
  fun `should send error other event when find all repositories and throws any exception`() {
    every { repository.findAllRepositories() } returns Observable.error(Exception())
    sut.onErrorOther { countDownLatch.countDown() }
    subscription()
  }

  @Test (timeout = 3000)
  fun `should send empty event when search by query and retrieved list is empty`() {
    val query = "query"
    val expected = RepositorySearchDto(
      null,
      null,
      emptyList()
    )
    every { repository.searchRepositories(query) } returns Observable.just(expected)
    sut.onEmpty { countDownLatch.countDown() }
    subscription(query)
  }

  @Test (timeout = 3000)
  fun `should send error null event when search by query and retrieved list is null`() {
    val query = "query"
    val expected = RepositorySearchDto(null, null, null)
    every { repository.searchRepositories(query) } returns Observable.just(expected)
    sut.onErrorNullList { countDownLatch.countDown() }
    subscription(query)
  }

  @Test (timeout = 3000)
  fun `should send found event when search by query and retrieved list have items`() {
    val query = "query"
    val expected = RepositorySearchDto(
      null,
      null,
      listOf(Repository(0, "", "", null, true, "", ""))
    )
    every { repository.searchRepositories(query) } returns Observable.just(expected)
    sut.onDataFound {
      assert(it == expected.items)
      countDownLatch.countDown()
    }
    subscription(query)
  }

  @Test (timeout = 3000)
  fun `should send error null event when search by query throws illegal argument exception`() {
    val query = "query"
    every { repository.searchRepositories(query) } returns Observable.error(IllegalArgumentException())
    sut.onErrorNullList { countDownLatch.countDown() }
    subscription(query)
  }

  @Test (timeout = 3000)
  fun `should send error no internet event when search by query throws unknown host exception`() {
    val query = "query"
    every { repository.searchRepositories(query) } returns Observable.error(UnknownHostException())
    sut.onErrorNoInternet { countDownLatch.countDown() }
    subscription(query)
  }

  @Test (timeout = 3000)
  fun `should send error other event when search by query throws any other exception`() {
    val query = "query"
    every { repository.searchRepositories(query) } returns Observable.error(Exception())
    sut.onErrorOther { countDownLatch.countDown() }
    subscription(query)
  }

  private fun subscription(query: String = "") {
    this.countDownLatch = CountDownLatch(3)
    sut.onStart {
      countDownLatch.countDown()
    }
    sut.onStop {
      countDownLatch.countDown()
    }
    sut.search(query)
    countDownLatch.await()
  }
}
