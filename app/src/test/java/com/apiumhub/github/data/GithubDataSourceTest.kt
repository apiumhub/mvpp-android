package com.apiumhub.github.data

import io.mockk.mockk
import org.junit.Before

import org.junit.Assert.*
import org.junit.Test

class GithubDataSourceTest {

  private val api = mockk<GithubApi>()
  private lateinit var sut: GithubRepository

  @Before
  fun setUp() {
    sut = GithubRepository.create(api)
  }

  @Test
  fun `should `
}