package com.apiumhub.github.domain.entity

import com.apiumhub.github.AllOpen

@AllOpen
class RepositorySearchDto(
        val total_count: Int?,
        val incomplete_results: Boolean?,
        val items: List<Repository>?)