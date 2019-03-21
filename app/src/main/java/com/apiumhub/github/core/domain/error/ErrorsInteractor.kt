package com.apiumhub.github.core.domain.error

import com.apiumhub.github.core.presentation.errors.IErrorsInteractor
import io.reactivex.Observable
import retrofit2.HttpException
import java.net.UnknownHostException

class UnauthorizedException : Throwable()
class ForbiddenException : Throwable()
class NoInternetConnectionException : Throwable()

class ErrorsInteractor(private val errorsStream: Observable<Throwable>) : IErrorsInteractor {

    override fun onUnauthorizedError(onError: (exception: Throwable) -> Unit) {
        errorsStream
                .filter { it is HttpException && it.code() == 401 }
                .map { UnauthorizedException() }
                .subscribe { onError(it) }
    }

    override fun onNoInternetConnectionError(onError: (exception: Throwable) -> Unit) {
        errorsStream
                .filter { it is UnknownHostException }
                .map { NoInternetConnectionException() }
                .subscribe { onError(it) }
    }

    override fun onForbiddenError(onError: (exception: Throwable) -> Unit) {
        errorsStream
                .filter { it is HttpException && it.code() == 403 }
                .map { ForbiddenException() }
                .subscribe { onError(it) }
    }
}