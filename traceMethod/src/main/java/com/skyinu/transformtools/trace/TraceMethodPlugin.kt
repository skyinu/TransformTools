package com.skyinu.transformtools.trace

import com.android.build.gradle.AppPlugin
import com.skyinu.transformtools.base.ClassToolTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

class TraceMethodPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    val appPlugin = project.plugins.findPlugin(AppPlugin::class.java)
    appPlugin?.let {
      val transform = ClassToolTransform(project, "trace")
      transform.regiesterClassHandler(TraceAssist())
      it.extension.registerTransform(transform)
    }
  }
}