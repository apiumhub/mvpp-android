package com.apiumhub.github.presentation

import android.support.v4.app.FragmentManager
import com.apiumhub.github.R
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.presentation.details.RepositoryDetailsFragment

object Navigator {

  fun openRepositoryDetails(fragmentManager: FragmentManager, repository: Repository) {
    fragmentManager.beginTransaction().apply {
      replace(R.id.main_container, RepositoryDetailsFragment.newInstance(repository))
      addToBackStack("RepositoryDetails")
      commit()
    }
  }

}