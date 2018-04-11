package com.apiumhub.github.data

import io.reactivex.subjects.PublishSubject
import junit.framework.Assert
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.io.File
import java.util.concurrent.CountDownLatch


class GithubRepositoryTest {

    private lateinit var sut: IGithubRepository
    private val mockWebServer = MockWebServer()

    private val errorsStream = PublishSubject.create<Throwable>()

    @Before
    fun setup() {
        mockWebServer.start()
        sut = IGithubRepository.create(GithubApi.create(mockWebServer.url("/").toString()), errorsStream)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun test_errors_stream_publishes_exception_when_error_occurs_on_http_request() {
        val latch = CountDownLatch(2)
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("[]"))
        errorsStream.subscribe {
            Assert.assertTrue(it is HttpException)
            Assert.assertTrue((it as HttpException).code() == 500)
            latch.countDown()
        }
        sut.findAllRepositories().subscribe {
            Assert.assertTrue(it.isEmpty())
            latch.countDown()
        }
        latch.await()
    }

    @Test
    fun test_find_all_repositories_return_provided_data() {
        val latch = CountDownLatch(1)
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(getJson("repositories.json")))
        sut.findAllRepositories().subscribe {
            Assert.assertTrue(it.isNotEmpty())
            latch.countDown()
        }
        latch.await()
    }

    @Test
    fun test_get_commits_for_repository_with_202_code_should_retry_call_after_some_time() {
        val latch = CountDownLatch(1)
        mockWebServer.enqueue(MockResponse().setResponseCode(202).setBody("[]"))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(getJson("commits_activity.json")))
        sut.getCommitsForRepository("someUser", "someRepository").subscribe {
            Assert.assertNotNull(it)
            latch.countDown()
        }
        latch.await()
    }

    private fun getJson(path: String): String {
        // Load the JSON response
        val uri = this.javaClass.classLoader.getResource(path)
        val file = File(uri.path)
        return String(file.readBytes())
    }
}