package com.apiumhub.github.presentation.details

import android.os.Bundle
import android.view.View
import com.apiumhub.github.R
import com.apiumhub.github.databinding.RepositoryDetailsBinding
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.entity.RepositoryDetailsDto
import com.apiumhub.github.presentation.base.BaseFragment
import io.reactivex.subjects.PublishSubject

open class RepositoryDetailsFragmentInternal : BaseFragment<RepositoryDetailsBinding>(), IRepositoryDetailsView {

    companion object {
        const val REPOSITORY_KEY = "RepositoryUrlKey"
    }

    override fun getLayoutId(): Int = R.layout.repository_details

    private lateinit var repository: Repository

    private val loadDetailsSubject = PublishSubject.create<Any>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = arguments?.getParcelable(REPOSITORY_KEY) ?: throw IllegalArgumentException("A valid repository must be provided")
        binding.repositoryDetailsAuthor.text = repository.name
        binding.repositoryDetailsDescription.text = repository.description
        loadDetailsSubject.onNext(Any())
    }

    override fun loadRepositoryDetails(func: (user: String, repository: String) -> Unit) {
        loadDetailsSubject.subscribe { func(repository.owner?.login!!, repository.name!!) }
    }

    override fun repositoryInformationLoaded(details: RepositoryDetailsDto) {
        binding.repositoryDetailsBranches.text = details.branchesCount.toString()
        binding.repositoryDetailsCommits.text = details.commitCount.toString()
    }

    override fun readmeLoaded(readme: String) {
        binding.repositoryDetailsReadmeWebview.loadData(readme, "text/html", "UTF-8")
    }
}

class RepositoryDetailsFragment : RepositoryDetailsFragmentInternal() {

    companion object {
        fun newInstance(repository: Repository): RepositoryDetailsFragment = RepositoryDetailsFragment().apply {
            arguments = Bundle().apply {
                putParcelable(REPOSITORY_KEY, repository)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        RepositoryDetailsPresenter(this, IRepositoryDetailsService.create())
        super.onViewCreated(view, savedInstanceState)
    }
}
