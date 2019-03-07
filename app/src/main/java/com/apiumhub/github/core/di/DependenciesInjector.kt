package com.apiumhub.github.core.di

import android.app.Application
import org.koin.android.ext.android.startKoin

fun Application.startInjector() = startKoin(
  this,
  listOf(
    presenterModule,
    inputModule,
    orchestratorModule,
    servicesModule
  )
)