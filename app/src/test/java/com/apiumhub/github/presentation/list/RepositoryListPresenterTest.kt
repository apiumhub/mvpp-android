package com.apiumhub.github.presentation.list

import com.apiumhub.github.domain.entity.Repository
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class RepositoryListPresenterTest() {
  private val view: IRepositoryListView = mockk(relaxed = true)

  lateinit var sut: RepositoryListPresenter

  @Before
  fun setUp() {
    sut = RepositoryListPresenter(view)
  }

  @Test
  fun `should load items when service found items on find all`() {
    val items = listOf<Repository>()
    sut.onRepositoryListFound(items)
    verify { view.itemsLoaded(items) }
  }
}