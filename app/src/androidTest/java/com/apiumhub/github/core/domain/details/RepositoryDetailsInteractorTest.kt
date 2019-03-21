package com.apiumhub.github.core.domain.details

import com.apiumhub.github.core.domain.MockGithubRepository
import com.apiumhub.github.details.IRepositoryDetailsService
import org.junit.Test
import java.util.concurrent.CountDownLatch

class RepositoryDetailsInteractorTest {

    private val sut: IRepositoryDetailsService = IRepositoryDetailsService.create(MockGithubRepository())

    private val someUser: String = "someUser"

    private val someRepository: String = "someRepository"

    @Test
    fun test_get_repository_details_should_call_on_readme_loaded_and_on_repository_details_loaded() {
        val latch = CountDownLatch(2)
        sut.onDetailsLoaded { latch.countDown() }
        sut.onReadmeLoaded { latch.countDown() }
        sut.getRepositoryDetails(someUser, someRepository)
        latch.await()
    }

}
