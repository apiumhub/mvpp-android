package com.apiumhub.github

import com.apiumhub.github.domain.entity.CommitsDto
import com.apiumhub.github.domain.entity.Repository
import com.google.gson.Gson
import java.io.File

object DataProvider {

  val repositories = getEntityFromJson<List<Repository>>("repositories.json")
  val commitsDto = getEntityFromJson<List<CommitsDto>>("commits_activity.json")

  private inline fun <reified T> getEntityFromJson(json: String): T =
    Gson().fromJson(getResource(json).readText(), T::class.java)

  private fun getResource(fileName: String): File {
    val loader = ClassLoader.getSystemClassLoader()
    val resource = loader.getResource(fileName)
    return File(resource.path)
  }
}