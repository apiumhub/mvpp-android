package com.apiumhub.github.details

import android.os.Bundle
import android.view.View
import com.apiumhub.github.R
import com.apiumhub.github.core.domain.entity.Repository
import com.apiumhub.github.core.domain.entity.RepositoryDetailsDto
import com.apiumhub.github.core.presentation.base.BaseFragment
import com.apiumhub.github.core.presentation.base.Event
import com.apiumhub.github.core.presentation.base.EventView
import kotlinx.android.synthetic.main.repository_details.*
import org.koin.android.ext.android.get
import org.koin.core.parameter.ParameterList

interface RepositoryDetailsView : EventView {
  fun bindLoadDetails(func: (String, String) -> Unit)

  fun repositoryInformationLoaded(details: RepositoryDetailsDto)
  fun readmeLoaded(readme: String)
}

class RepositoryDetailsFragment : BaseFragment(),
  RepositoryDetailsView {
  override fun getLayoutId(): Int = R.layout.repository_details

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    get<RepositoryDetailsPresenter> { ParameterList(this as RepositoryDetailsView) }

    arguments?.getParcelable<Repository>(REPOSITORY_KEY)?.let {
      tvAuthor.text = it.name
      tvDescription.text = it.description
      subject.onNext(Event.Single(Pair(it.owner?.login!!, it.name!!)))
    }
  }

  //region -- Actions --
  override fun bindLoadDetails(func: (String, String) -> Unit) = bindPair(func)
  //endregion

  override fun repositoryInformationLoaded(details: RepositoryDetailsDto) {
    tvBranches.text = details.branchesCount.toString()
    tvCommits.text = details.commitCount.toString()
  }

  override fun readmeLoaded(readme: String) {
    wvReadme.loadData(readme, "text/html", "UTF-8")
  }

  companion object {
    const val REPOSITORY_KEY = "RepositoryUrlKey"

    fun newInstance(repository: Repository): RepositoryDetailsFragment = RepositoryDetailsFragment().apply {
      arguments = Bundle().apply {
        putParcelable(REPOSITORY_KEY, repository)
      }
    }
  }
}
