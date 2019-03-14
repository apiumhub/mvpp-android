package com.apiumhub.github.domain.repository.common



interface EventService {
  fun cancel()

  fun onStart(func: () -> Unit)
  fun onStop(func: () -> Unit)
  fun onEmpty(func: () -> Unit)
  fun onErrorNullList(func: () -> Unit)
  fun onErrorNoInternet(func: () -> Unit)
  fun onErrorOther(func: () -> Unit)
}