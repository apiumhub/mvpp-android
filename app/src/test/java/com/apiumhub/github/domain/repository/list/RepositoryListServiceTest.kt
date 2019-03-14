package com.apiumhub.github.domain.repository.list

import com.apiumhub.github.data.IGithubRepository
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.entity.RepositorySearchDto
import io.mockk.coEvery
import io.mockk.mockk
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.net.UnknownHostException
import java.util.concurrent.CountDownLatch

class RepositoryListServiceTest {

  private val repository: IGithubRepository = mockk()
  private val subject: PublishSubject<RepositoryListEvent> = PublishSubject.create()

  lateinit var sut: RepositoryListService

  @Before
  fun setUp() {
    sut = RepositoryListService.create(repository, subject, Dispatchers.Unconfined)
  }

  @Test
  fun `should send empty event when find all repositories and retrieved list is empty`() = runBlocking {
    coEvery { repository.findAllRepositories() } returns emptyList()
    subscription {
      assert(it is RepositoryListEvent.Empty)
    }
  }

  @Test
  fun `should send found event when find all repositories and retrieved list is not empty`() = runBlocking {
    val expected = listOf(Repository(0, "", "", null, true, "", ""))
    coEvery { repository.findAllRepositories() } returns expected
    subscription {
      assert(it is RepositoryListEvent.Found)
      assert((it as RepositoryListEvent.Found).list == expected)
    }
  }

  @Test
  fun `should send error null event when find all repositories and throws illegal argument exception`() = runBlocking {
    coEvery { repository.findAllRepositories() } throws IllegalArgumentException()
    subscription {
      assert(it is RepositoryListEvent.ErrorNull)
    }
  }

  @Test
  fun `should send error no internet event when find all repositories and throws unknown host exception`() =
    runBlocking {
      coEvery { repository.findAllRepositories() } throws UnknownHostException()
      subscription {
        assert(it is RepositoryListEvent.ErrorNoInternet)
      }
    }

  @Test
  fun `should send error other event when find all repositories and throws any exception`() = runBlocking {
    coEvery { repository.findAllRepositories() } throws Exception()
    subscription {
      assert(it is RepositoryListEvent.ErrorOther)
    }
  }

  @Test
  fun `should send empty event when search by query and retrieved list is empty`() = runBlocking {
    val query = "query"
    coEvery { repository.searchRepositories(query) } returns RepositorySearchDto(null, null, emptyList())
    subscription(query) {
      assert(it is RepositoryListEvent.Empty)
    }
  }

  @Test
  fun `should send error null event when search by query and retrieved list is null`() = runBlocking {
    val query = "query"
    coEvery { repository.searchRepositories(query) } returns RepositorySearchDto(null, null, null)
    subscription(query) {
      assert(it is RepositoryListEvent.ErrorNull)
    }
  }

  @Test
  fun `should send found event when search by query and retrieved list have items`() = runBlocking {
    val query = "query"
    val expected = RepositorySearchDto(
      null,
      null,
      listOf(Repository(0, "", "", null, true, "", ""))
    )
    coEvery { repository.searchRepositories(query) } returns expected
    subscription(query) {
      assert(it is RepositoryListEvent.Found)
      assert((it as RepositoryListEvent.Found).list == expected.items)
    }
  }

  @Test
  fun `should send error null event when search by query throws illegal argument exception`() = runBlocking {
    val query = "query"
    coEvery { repository.searchRepositories(query) } throws IllegalArgumentException()
    subscription(query) {
      assert(it is RepositoryListEvent.ErrorNull)
    }
  }

  @Test
  fun `should send error no internet event when search by query throws unknown host exception`() = runBlocking {
    val query = "query"
    coEvery { repository.searchRepositories(query) } throws UnknownHostException()
    subscription(query) {
      assert(it is RepositoryListEvent.ErrorNoInternet)
    }
  }

  @Test
  fun `should send error other event when search by query throws any other exception`() = runBlocking {
    val query = "query"
    coEvery { repository.searchRepositories(query) } throws Exception()
    subscription(query) {
      assert(it is RepositoryListEvent.ErrorOther)
    }
  }

  private fun subscription(query: String = "", func: (RepositoryListEvent) -> Unit) {
    val countDownLatch = CountDownLatch(3)

    subject.subscribe {
      when {
        countDownLatch.count > 2 -> assert(it is RepositoryListEvent.Start)
        countDownLatch.count > 1 -> func(it)
        else -> assert(it is RepositoryListEvent.Stop)
      }
      countDownLatch.countDown()
    }

    sut.search(query)

    countDownLatch.await()
  }
}
