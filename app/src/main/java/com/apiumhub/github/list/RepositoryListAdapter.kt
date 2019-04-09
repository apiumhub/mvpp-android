package com.apiumhub.github.list

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.apiumhub.github.R
import com.apiumhub.github.core.domain.entity.Repository
import com.jakewharton.rxbinding2.view.RxView
import com.squareup.picasso.Picasso
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.repository_row.view.*

class RepositoryListAdapter(
  private val disposeBag: CompositeDisposable,
  private val repositoryClicked: (repository: Repository) -> Unit
) : RecyclerView.Adapter<RepositoryListAdapter.RepoItemViewHolder>() {

  private var repos: List<Repository> = emptyList()
  private val unsubscribe: PublishSubject<Any> = PublishSubject.create()

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepoItemViewHolder =
    RepoItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.repository_row, parent, false))

  override fun getItemCount(): Int = repos.size

  override fun onBindViewHolder(holder: RepoItemViewHolder, position: Int) {
    val repo = repos[position]
    holder.itemView.tvName.text = repo.name
    holder.itemView.tvDescription.text = repo.description
    disposeBag.add(RxView.clicks(holder.itemView).takeUntil(unsubscribe).subscribe { repositoryClicked(repo) })
    Picasso.with(holder.itemView.context).load(repo.owner?.avatar_url).into(holder.itemView.ivImage)
  }

  fun setItems(items: List<Repository>) {
    this.repos = items
  }

  override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
    super.onDetachedFromRecyclerView(recyclerView)
    unsubscribe.onNext(Any())
  }

  inner class RepoItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

