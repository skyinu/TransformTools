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
    var pre = targetFile.absolutePath.substring(parentDir.absolutePath.length + 1)
        .replace(File.separator, ".")
    if (pre.endsWith(SUFFIX_CLASS)) {
      pre = pre.substring(0, pre.length - SUFFIX_CLASS.length)
    }
    return pre
  }

  fun retrieveClassNameForJarClass(jarClass: String): String {
    var pre = jarClass.replace(File.separator, ".")
    if (pre.endsWith(SUFFIX_CLASS)) {
      pre = pre.substring(0, pre.length - SUFFIX_CLASS.length)
    }
    return pre.replace("/", ".")
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