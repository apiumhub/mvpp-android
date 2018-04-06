package com.apiumhub.github.presentation.errors

import com.apiumhub.github.data.StatsCachingException

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