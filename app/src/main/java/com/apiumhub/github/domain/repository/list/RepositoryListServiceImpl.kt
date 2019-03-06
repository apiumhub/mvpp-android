package com.apiumhub.github.domain.repository.list

import com.apiumhub.github.data.IGithubRepository
import com.apiumhub.github.domain.entity.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

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
//
//interface SearchService {
//  fun search(query: String)
//  fun onData(func: (List<Repository>) -> Unit)
//  fun onEmpty(func: () -> Unit)
//  fun onNoInternet(func: () -> Unit)
//  fun onOtherError(func: () -> Unit)
//}
//
//interface SearchView {
//  fun onSearch(func: (String) -> Unit)
//  fun onData(data: List<Repository>)
//  fun onEmpty()
//  fun onError()
//}
//
//class SearchPresenter(view: SearchView, service: SearchService) {
//  init {
//    view.onSearch(service::search)
//    service.onData(view::onData)
//    service.onEmpty(view::onEmpty)
//    service.onNoInternet(view::onError)
//    service.onOtherError(view::onError)
//  }
//}
//
//interface LoadingService {
//  fun onLoading(func: () -> Unit)
//  fun stopLoading(func: () -> Unit)
//  fun hideLoading(func: () -> Unit)
//}
//
//interface LoadingView {
//  fun showLoading()
//  fun hideLoading()
//  fun stopLoading()
//}
//
//class LoadingPresenter(view: LoadingView, service: LoadingService) {
//  init {
//    service.onLoading(view::showLoading)
//    service.stopLoading(view::stopLoading)
//    service.hideLoading(view::hideLoading)
//  }
//}
//
//class SearchRepository {
//  fun search(query: String): Observable<List<Repository>> = Observable.empty()
//}
//
//class SearchServiceImpl(private val repository: SearchRepository) : SearchService, LoadingService {
//
//  val uniquePb = PublishSubject.create<SearchEvent>()
//
//  override fun search(query: String) {
//    uniquePb.onNext(SearchEvent.LOADING_EVENT)
//    repository.search(query).map {
//      if (it.isEmpty()) {
//        uniquePb.onNext(SearchEvent.EMPTY_DATA_EVENT)
//      } else {
//        uniquePb.onNext(SearchEvent.NEW_DATA_EVENT)
//      }
//      uniquePb.onNext(SearchEvent.STOP_LOADING)
//    }
//      .onErrorReturn {
//        if (it is UnknownHostException) {
//          uniquePb.onNext(SearchEvent.NO_INTERNET_ERROR)
//        } else {
//          uniquePb.onNext(SearchEvent.GENERIC_ERROR)
//        }
//        uniquePb.onNext(SearchEvent.STOP_LOADING)
//      }
//      .subscribeOn(Schedulers.io())
//      .observeOn(AndroidSchedulers.mainThread())
//      .subscribe()
//  }
//
//  override fun onData(func: (List<Repository>) -> Unit) {
//    uniquePb.filter { it == SearchEvent.NEW_DATA_EVENT }.subscribe { func(it) }
//  }
//
//  override fun onEmpty(func: () -> Unit) {
//    uniquePb.filter { it == SearchEvent.EMPTY_DATA_EVENT }.subscribe { func() }
//  }
//
//  override fun onNoInternet(func: () -> Unit) {
//    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//  }
//
//  override fun onOtherError(func: () -> Unit) {
//    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//  }
//
//  override fun onLoading(func: () -> Unit) {
//    uniquePb.filter { it == SearchEvent.LOADING_EVENT }.subscribe { func() }
//  }
//
//  override fun stopLoading(func: () -> Unit) {
//    uniquePb.filter { it == SearchEvent.STOP_LOADING }.subscribe { func() }
//  }
//
//  override fun hideLoading(func: () -> Unit) {
//    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//  }
//
//  enum class SearchEvent {
//    LOADING_EVENT,
//    NEW_DATA_EVENT,
//    EMPTY_DATA_EVENT,
//    NO_INTERNET_ERROR,
//    GENERIC_ERROR,
//    STOP_LOADING
//  }
//}
