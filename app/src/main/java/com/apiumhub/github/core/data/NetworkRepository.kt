package com.apiumhub.github.core.data

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

abstract class NetworkRepository(private val errorsStream: PublishSubject<Throwable>) {
  protected fun <T> executeRequest(request: Observable<T>, returnOnError: T? = null): Observable<T> {
    return request.onErrorReturn { errorsStream.onNext(it)
      returnOnError
    }
  }

  protected fun retryThrowable(
    throwable: Throwable, condition: Boolean, maxRetries: Int = 3, delaySeconds: Long = 3L
  ): Observable<Long>? {
    var retryCount = 0

    return if (condition && ++retryCount < maxRetries) {
      Observable.timer(delaySeconds, TimeUnit.SECONDS)
    } else Observable.error(throwable)
  }
}