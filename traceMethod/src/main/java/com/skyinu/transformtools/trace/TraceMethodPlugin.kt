package com.skyinu.transformtools.trace

import com.android.build.gradle.AppPlugin
import com.skyinu.transformtools.base.ClassToolTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

class TraceMethodPlugin : Plugin<Project> {
  companion object {
    const val TRACE_EXTENSION = "trace"
  }

  override fun apply(project: Project) {
    try {
      project.extensions.create(TRACE_EXTENSION, TraceExtension::class.java)
    } catch (ignore: Exception) {
      ignore.printStackTrace()
    }
    val appPlugin = project.plugins.findPlugin(AppPlugin::class.java)
    appPlugin?.let {
      val transform = ClassToolTransform(project, "trace")
      transform.regiesterClassHandler(TraceAssist(project))
      it.getExtension().registerTransform(transform)
    }
  }
}