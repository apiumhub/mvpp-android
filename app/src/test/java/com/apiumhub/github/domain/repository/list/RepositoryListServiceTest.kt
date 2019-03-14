package com.apiumhub.github.domain.repository.list

import com.apiumhub.github.data.IGithubRepository
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.presentation.list.RepositoryListEvent
import io.mockk.coEvery
import io.mockk.mockk
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
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
  fun `should send found event when search query is empty and retrieved list is not empty`() = runBlocking {
    val countDownLatch = CountDownLatch(3)
    val query = ""
    val expected = listOf(Repository(0, "", "", null, true, "", ""))

    coEvery { repository.findAllRepositories() } returns expected

    subject.subscribe {
      when {
        countDownLatch.count > 2 -> assert(it is RepositoryListEvent.Start)
        countDownLatch.count > 1 -> {
          assert(it is RepositoryListEvent.Found)
          assert((it as RepositoryListEvent.Found).list == expected)
        }
        else -> assert(it is RepositoryListEvent.Stop)
      }
      countDownLatch.countDown()
    }

    sut.search(query)

    countDownLatch.await()
  }
}
