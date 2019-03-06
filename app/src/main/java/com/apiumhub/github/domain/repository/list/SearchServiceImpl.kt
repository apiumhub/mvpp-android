package com.apiumhub.github.domain.repository.list

import com.apiumhub.github.data.IGithubRepository
import com.apiumhub.github.domain.entity.Repository
import kotlinx.coroutines.*
import java.net.UnknownHostException

interface SearchService: LoadingService {
    fun search(query: String)
    fun findAll()

    var onData: (List<Repository>) -> Unit
    var onEmpty: () -> Unit
    var onNoInternet: () -> Unit
    var onOtherError: () -> Unit

    companion object {
        fun create() = SearchServiceImpl(IGithubRepository.create(), Dispatchers.Main)
    }
}

class SearchServiceImpl(private val repository: IGithubRepository, private val dispatcher: CoroutineDispatcher) : SearchService {
    var job = Job()

    override fun findAll() {
        onLoading()

        GlobalScope.launch(job + dispatcher) {
            try {
                val items = repository.findAllRepositories()
                when {
                    items.isEmpty() -> onEmpty()
                    else -> onData(items)
                }
                hideLoading()
            } catch (exception: Exception) {
                if (exception is UnknownHostException) {
                    onNoInternet()
                } else {
                    onOtherError()
                }
                stopLoading()
            }
        }
    }

    override fun search(query: String) {
        onLoading()

        GlobalScope.launch(job + dispatcher) {
            try {
                val result = repository.searchRepositories(query)
                val items = result.items
                when {
                    items == null -> onOtherError()
                    items.isEmpty() -> onEmpty()
                    else -> onData(items)
                }
                hideLoading()
            } catch (exception: Exception) {
                if (exception is UnknownHostException) {
                    onNoInternet()
                } else {
                    onOtherError()
                }
                stopLoading()
            }
        }
    }

    override lateinit var onData: (List<Repository>) -> Unit
    override lateinit var onEmpty: () -> Unit
    override lateinit var onNoInternet: () -> Unit
    override lateinit var onOtherError: () -> Unit
    override lateinit var onLoading: () -> Unit
    override lateinit var hideLoading: () -> Unit
    override lateinit var stopLoading: () -> Unit
}
