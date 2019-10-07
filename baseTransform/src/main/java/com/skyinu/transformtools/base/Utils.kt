package com.skyinu.transformtools.base

import javassist.ClassPool
import javassist.CtClass
import java.io.File

object Utils {
  const val SUFFIX_CLASS = ".class"
  fun retrieveClassName(
    targetFile: File,
    parentDir: File
  ): String {
    return targetFile.absolutePath.substring(parentDir.absolutePath.length + 1)
        .replace(File.separator, ".")
        .replace(SUFFIX_CLASS, "")
  }

  fun retrieveClassNameForJarClass(jarClass: String): String {
    return jarClass.replace(File.separator, ".")
        .replace(SUFFIX_CLASS, "")
        .replace("/", ".")
  }

  fun safeGetCtClass(
    classPool: ClassPool,
    className: String
  ): CtClass? {
    try {
      return classPool.getOrNull(className)
    } catch (ex: Exception) {
      ex.printStackTrace()
    }
    return null
  }
}