package com.apiumhub.github.data.common

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

abstract class NetworkRepository(private val errorsStream: PublishSubject<Throwable>) {
  fun <T> executeRequest(request: Observable<T>, returnOnError: T? = null): Observable<T> {
    return request.onErrorReturn {
      errorsStream.onNext(it)
      returnOnError
    }
  }
}