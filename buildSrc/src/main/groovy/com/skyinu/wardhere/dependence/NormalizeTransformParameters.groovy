package com.skyinu.wardhere.dependence;

import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.tasks.Input

interface NormalizeTransformParameters extends TransformParameters {
  @Input
  String getBuildDir()
  void setBuildDir(String dir)
}
