package com.apiumhub.github.data

import com.apiumhub.github.domain.entity.BranchDto
import com.apiumhub.github.domain.entity.CommitsDto
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.entity.RepositorySearchDto
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubApi {

  @GET("/repositories")
  fun findAllRepositories(): Deferred<List<Repository>>

  @GET("/search/repositories")
  fun searchRepositories(@Query("q") query: String): Deferred<RepositorySearchDto>

  @GET("/repos/{owner}/{repository}/stats/commit_activity")
  fun getCommitsForRepository(@Path("owner") user: String, @Path("repository") repository: String): Deferred<List<CommitsDto>>

  @GET("/repos/{owner}/{repository}/branches")
  fun getBranchesForRepository(@Path("owner") user: String, @Path("repository") repository: String): Deferred<List<BranchDto>>

  @GET("repos/{owner}/{repository}/readme")
  @Headers("Accept:application/vnd.github.v3.html")
  fun getReadmeForRepository(@Path("owner") user: String, @Path("repository") repository: String): Deferred<String>

  companion object {

    fun create(baseUrl: String = "https://api.github.com"): GithubApi {
      val client = OkHttpClient
        .Builder()
        .addInterceptor(
          HttpLoggingInterceptor()
            .apply {
              level = HttpLoggingInterceptor.Level.BODY
            })
        .build()

      val retrofit = Retrofit.Builder()
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .addConverterFactory(ScalarsConverterFactory.create())//Needed to handle HTML String responses
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .baseUrl(baseUrl)
        .build()

      return retrofit.create(GithubApi::class.java)
    }
  }
}