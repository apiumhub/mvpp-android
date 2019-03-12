package com.apiumhub.github.core.di

import android.app.Application
import com.rcdespanyol.core.di.presenterModule
import com.rcdespanyol.core.di.repositoriesModule
import com.rcdespanyol.core.di.servicesModule
import org.koin.android.ext.android.startKoin

fun Application.startInjector() = startKoin(
  this,
  listOf(
    presenterModule,
    servicesModule,
    repositoriesModule
  )
)