package com.apiumhub.github.presentation.details

import android.os.Bundle
import android.view.View
import com.apiumhub.github.R
import com.apiumhub.github.databinding.RepositoryDetailsBinding
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.entity.RepositoryDetailsDto
import com.apiumhub.github.presentation.base.BaseFragment
import org.koin.android.ext.android.get
import org.koin.core.parameter.ParameterList

class RepositoryDetailsFragment : BaseFragment<RepositoryDetailsBinding>(), IRepositoryDetailsView {

  init {
    get<RepositoryDetailsPresenter> { ParameterList(this as IRepositoryDetailsView) }
  }

  override var onLoadRepositoryDetails: (String, String) -> Unit = { _: String, _: String -> }
  override var onDestroy: () -> Unit = {}

  override fun getLayoutId(): Int = R.layout.repository_details

  private lateinit var repository: Repository

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    repository =
      arguments?.getParcelable(REPOSITORY_KEY) ?: throw IllegalArgumentException("A valid repository must be provided")
    binding.repositoryDetailsAuthor.text = repository.name
    binding.repositoryDetailsDescription.text = repository.description

    onLoadRepositoryDetails(repository.owner?.login!!, repository.name!!)
  }

  override fun onDestroyView() {
    onDestroy()
    super.onDestroyView()
  }

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
