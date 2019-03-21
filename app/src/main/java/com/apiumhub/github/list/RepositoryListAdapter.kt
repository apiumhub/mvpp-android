package com.apiumhub.github.list

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.apiumhub.github.databinding.RepositoryRowBinding
import com.apiumhub.github.core.domain.entity.Repository
import com.jakewharton.rxbinding2.view.RxView
import com.squareup.picasso.Picasso
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

class RepoListAdapter(
  private val disposeBag: CompositeDisposable,
  private val repositoryClicked: (repository: Repository) -> Unit
) : RecyclerView.Adapter<RepoItemViewHolder>() {

  private var repos: List<Repository> = emptyList()
  private val unsubscribe: PublishSubject<Any> = PublishSubject.create()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoItemViewHolder =
    RepoItemViewHolder(RepositoryRowBinding.inflate(LayoutInflater.from(parent.context)))

  override fun getItemCount(): Int = repos.size

  override fun onBindViewHolder(holder: RepoItemViewHolder, position: Int) {
    val repo = repos[position]
    holder.binding.repositoryRowRepositoryName.text = repo.name
    holder.binding.repositoryRowDescription.text = repo.description
    disposeBag.add(RxView.clicks(holder.binding.root).takeUntil(unsubscribe).subscribe { repositoryClicked(repo) })
    Picasso.with(holder.binding.root.context).load(repo.owner?.avatar_url).into(holder.binding.repositoryRowImage)
  }

  fun setItems(items: List<Repository>) {
    this.repos = items
  }

  override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
    super.onDetachedFromRecyclerView(recyclerView)
    unsubscribe.onNext(Any())
  }
}

class RepoItemViewHolder(val binding: RepositoryRowBinding) : RecyclerView.ViewHolder(binding.root)