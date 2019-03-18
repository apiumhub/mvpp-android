package com.apiumhub.github.presentation.details

import android.os.Bundle
import android.view.View
import com.apiumhub.github.R
import com.apiumhub.github.databinding.RepositoryDetailsBinding
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.entity.RepositoryDetailsDto
import com.apiumhub.github.presentation.base.BaseFragment
import com.apiumhub.github.presentation.base.EventView
import io.reactivex.subjects.PublishSubject
import org.koin.android.ext.android.get
import org.koin.core.parameter.ParameterList

interface RepositoryDetailsView : EventView {
  fun onLoadRepositoryDetails(func: (String, String) -> Unit)

  fun repositoryInformationLoaded(details: RepositoryDetailsDto)
  fun readmeLoaded(readme: String)
}


class RepositoryDetailsFragment : BaseFragment<RepositoryDetailsBinding>(), RepositoryDetailsView {

  init {
    get<RepositoryDetailsPresenter> { ParameterList(this as RepositoryDetailsView) }
  }

  private val subject: PublishSubject<Pair<String, String>> = PublishSubject.create()

  override fun getLayoutId(): Int = R.layout.repository_details

  private lateinit var repository: Repository

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    repository =
      arguments?.getParcelable(REPOSITORY_KEY) ?: throw IllegalArgumentException("A valid repository must be provided")
    binding.repositoryDetailsAuthor.text = repository.name
    binding.repositoryDetailsDescription.text = repository.description

    subject.onNext(Pair(repository.owner?.login!!, repository.name!!))
  }

  override fun repositoryInformationLoaded(details: RepositoryDetailsDto) {
    binding.repositoryDetailsBranches.text = details.branchesCount.toString()
    binding.repositoryDetailsCommits.text = details.commitCount.toString()
  }

  override fun readmeLoaded(readme: String) {
    binding.repositoryDetailsReadmeWebview.loadData(readme, "text/html", "UTF-8")
  }

  override fun onLoadRepositoryDetails(func: (String, String) -> Unit) {
    disposeBag.add(subject.subscribe { func(it.first, it.second) })
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
