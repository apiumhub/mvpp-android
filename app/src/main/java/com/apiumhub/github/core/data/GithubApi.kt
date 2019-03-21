package com.apiumhub.github.core.data

import com.apiumhub.github.BuildConfig
import com.apiumhub.github.core.domain.entity.BranchDto
import com.apiumhub.github.core.domain.entity.CommitsDto
import com.apiumhub.github.core.domain.entity.Repository
import com.apiumhub.github.core.domain.entity.RepositorySearchDto
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
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
  fun findAllRepositories(): Observable<List<Repository>>

  @GET("/search/repositories")
  fun searchRepositories(@Query("q") query: String): Observable<RepositorySearchDto>

  @GET("/repos/{owner}/{repository}/stats/commit_activity")
  fun getCommitsForRepository(@Path("owner") user: String, @Path("repository") repository: String): Observable<Response<List<CommitsDto>>>

  @GET("/repos/{owner}/{repository}/branches")
  fun getBranchesForRepository(@Path("owner") user: String, @Path("repository") repository: String): Observable<List<BranchDto>>

  @GET("repos/{owner}/{repository}/readme")
  @Headers("Accept:application/vnd.github.v3.html")
  fun getReadmeForRepository(@Path("owner") user: String, @Path("repository") repository: String): Observable<String>

  companion object {

    fun create(baseUrl: String = BuildConfig.BASE_URL): GithubApi {
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
        .addConverterFactory(ScalarsConverterFactory.create())//Needed to handle HTML String responses
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .baseUrl(baseUrl)
        .build()

      return retrofit.create(GithubApi::class.java)
    }
  }
}