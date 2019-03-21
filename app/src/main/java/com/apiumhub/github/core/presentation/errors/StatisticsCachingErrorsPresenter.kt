package com.apiumhub.github.core.presentation.errors

import com.apiumhub.github.core.data.exception.StatsCachingException

interface IStatisticsCachingErrorInteractor {
    fun onStatisticsCachingError(func: (exception: StatsCachingException) -> Unit)
}

interface IStatisticsCachingErrorView {
    fun onStatisticsCachingError()
}

class StatisticsCachingErrorsPresenter(view: IStatisticsCachingErrorView, service: IStatisticsCachingErrorInteractor) {
    init {
        service.onStatisticsCachingError { view.onStatisticsCachingError() }
    }
}