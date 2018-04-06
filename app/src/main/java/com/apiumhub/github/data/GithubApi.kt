package com.apiumhub.github.data

import com.apiumhub.github.domain.entity.BranchDto
import com.apiumhub.github.domain.entity.CommitsDto
import com.apiumhub.github.domain.entity.Repository
import com.apiumhub.github.domain.entity.RepositorySearchDto
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

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
        private val BASE_URL: String
            get() = "https://api.github.com"

        fun create(): GithubApi {
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
                    .baseUrl(BASE_URL)
                    .build()

            return retrofit.create(GithubApi::class.java)
        }
    }
}