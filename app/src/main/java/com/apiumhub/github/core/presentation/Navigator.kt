package com.apiumhub.github.core.presentation

import android.support.v4.app.FragmentManager
import com.apiumhub.github.R
import com.apiumhub.github.core.domain.entity.Repository
import com.apiumhub.github.details.RepositoryDetailsFragment

object Navigator {

  fun openRepositoryDetails(fragmentManager: FragmentManager, repository: Repository) {
    fragmentManager.beginTransaction().apply {
      replace(R.id.main_container, RepositoryDetailsFragment.newInstance(repository))
      addToBackStack("RepositoryDetails")
      commit()
    }
  }

}