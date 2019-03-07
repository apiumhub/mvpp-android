package com.apiumhub.github.presentation.list.presenter

import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.presentation.list.IRepositoryListView
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test



class RepositoryListPresenterClassicTest {
  val view: IRepositoryListView = mockk(relaxed = true)

  private lateinit var sut: RepositoryListPresenterClassic

  init {
    sut = RepositoryListPresenterClassic(view, mockk())
  }

  @Test
  fun `when find all, view should show loading`() = runBlocking {
    sut.findAll()
    verify { view.showLoading() }
  }

  @Test
  fun `when find filter by query, view should show loading`() = runBlocking {
    sut.findFilterByQuery("query")
    verify { view.showLoading() }
  }

  @Test
  fun `on repository list found and list is empty, view should hide loading and show items empty`() {
    val items = emptyList<Repository>()
    sut.onRepositoryListFound(items)
    verify {
      view.hideLoading()
      view.itemsEmpty()
    }
  }

  @Test
  fun `on repository list found and list have items, view should hide loading and show items loaded`() {
    val items = listOf(Repository(0, "name", "full_name", null, true, "url", "description"))
    sut.onRepositoryListFound(items)
    verify {
      view.hideLoading()
      view.itemsLoaded(items)
    }
  }

  @Test
  fun `on repository list error, view should hide loading and show error`() {
    val error = Exception("not found")
    sut.onRepositoryListError(error)
    verify {
      view.hideLoading()
      view.showError(error)
    }
  }
}