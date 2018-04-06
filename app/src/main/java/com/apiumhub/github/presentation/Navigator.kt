package com.apiumhub.github.presentation

import android.support.v4.app.FragmentManager
import com.apiumhub.github.R
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.presentation.details.IRepositoryDetailsView

object Navigator {

    fun openRepositoryDetails(fragmentManager: FragmentManager, repository: Repository) {
        fragmentManager.beginTransaction().apply {
            replace(R.id.main_container, IRepositoryDetailsView.create(repository))
            addToBackStack("RepositoryDetails")
            commit()
        }
    }

}