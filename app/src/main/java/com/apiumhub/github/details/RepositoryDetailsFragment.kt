package com.apiumhub.github.details

import android.os.Bundle
import android.view.View
import com.apiumhub.github.R
import com.apiumhub.github.core.domain.entity.Repository
import com.apiumhub.github.core.domain.entity.RepositoryDetailsDto
import com.apiumhub.github.core.presentation.base.BaseFragment
import com.apiumhub.github.core.presentation.base.Event
import com.apiumhub.github.core.presentation.base.EventView
import com.apiumhub.github.databinding.RepositoryDetailsBinding
import org.koin.android.ext.android.get
import org.koin.core.parameter.ParameterList

interface RepositoryDetailsView : EventView {
  fun bindLoadDetails(func: (String, String) -> Unit)

  fun repositoryInformationLoaded(details: RepositoryDetailsDto)
  fun readmeLoaded(readme: String)
}

class RepositoryDetailsFragment : BaseFragment<RepositoryDetailsBinding>(),
  RepositoryDetailsView {
  override fun getLayoutId(): Int = R.layout.repository_details

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    get<RepositoryDetailsPresenter> { ParameterList(this as RepositoryDetailsView) }

    arguments?.getParcelable<Repository>(REPOSITORY_KEY)?.let {
      binding.repositoryDetailsAuthor.text = it.name
      binding.repositoryDetailsDescription.text = it.description
      subject.onNext(Event.Single(Pair(it.owner?.login!!, it.name!!)))
    }
  }

  //region -- Actions --
  override fun bindLoadDetails(func: (String, String) -> Unit) = bindPair(func)
  //endregion

  override fun repositoryInformationLoaded(details: RepositoryDetailsDto) {
    binding.repositoryDetailsBranches.text = details.branchesCount.toString()
    binding.repositoryDetailsCommits.text = details.commitCount.toString()
  }

  override fun readmeLoaded(readme: String) {
    binding.repositoryDetailsReadmeWebview.loadData(readme, "text/html", "UTF-8")
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
