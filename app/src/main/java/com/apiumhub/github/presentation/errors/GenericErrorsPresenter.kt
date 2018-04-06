package com.apiumhub.github.presentation.errors

import com.apiumhub.github.domain.error.ErrorsInteractor
import io.reactivex.Observable

interface IErrorsInteractor {
    fun onUnauthorizedError(onError: (exception: Throwable) -> Unit)
    fun onForbiddenError(onError: (exception: Throwable) -> Unit)
    fun onNoInternetConnectionError(onError: (exception: Throwable) -> Unit)

    companion object {
        fun create(errorsStream: Observable<Throwable>): IErrorsInteractor {
            return ErrorsInteractor(errorsStream)
        }
    }
}

interface IErrorsView {
    fun onNoInternetConnectionError()
    fun onUnauthorizedError()
    fun onForbiddenError()
}

class GenericErrorsPresenter(view: IErrorsView, service: IErrorsInteractor) {
    init {
        service.onUnauthorizedError { view.onUnauthorizedError() }
        service.onNoInternetConnectionError { view.onNoInternetConnectionError() }
        service.onForbiddenError { view.onForbiddenError() }
    }
}