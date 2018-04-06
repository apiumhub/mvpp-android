package com.apiumhub.github.domain.entity

data class RepositorySearchDto(
        val total_count: Int?,
        val incomplete_results: Boolean?,
        val items: List<Repository>?)