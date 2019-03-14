package com.apiumhub.github

import android.app.Application
import com.apiumhub.github.core.di.startInjector

class GithubApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    startInjector()
  }
}