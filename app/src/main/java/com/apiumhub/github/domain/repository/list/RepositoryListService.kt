package com.apiumhub.github.domain.repository.list

import com.apiumhub.github.data.IGithubRepository
import com.apiumhub.github.domain.entity.Repository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.net.UnknownHostException

interface RepositoryListService {
  fun findAllGlue(onEmpty: () -> Unit, onError: (Throwable) -> Unit, onFound: (List<Repository>) -> Unit)
  suspend fun findAll(): List<Repository>
  suspend fun search(query: String): List<Repository>

  companion object {
    fun create(): RepositoryListService = RepositoryListServiceImpl(IGithubRepository.create())
    fun create(repository: IGithubRepository): RepositoryListService = RepositoryListServiceImpl(repository)
  }
}

class RepositoryListServiceImpl(private val repository: IGithubRepository) : RepositoryListService {
  override fun findAllGlue(
    onEmpty: () -> Unit,
    onError: (Throwable) -> Unit,
    onFound: (List<Repository>) -> Unit
  ) {

    GlobalScope.launch(Job() + Dispatchers.Main) {
      try {
        val result = repository.findAllRepositories()
        if (result.isEmpty()) {
          onEmpty()
        } else {
          onFound(result)
        }
      } catch (exception: Exception) {
        onError(exception)
      }
    }
  }

  override suspend fun findAll() = repository.findAllRepositories()

  override suspend fun search(query: String): List<Repository> = repository.searchRepositories(query).items.orEmpty()
}
