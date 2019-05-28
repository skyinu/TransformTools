package com.skyinu.wardhere

import org.gradle.api.Project

public class WardHereExtension {
  public static final String DSL_DOMAIN_NAME = "wardHere"
  boolean dumpAble = false
  String dumpDir
  String exceptionTag = "Exception"
  boolean plain = true
  boolean injectJar = true
  Map<String, List<CodeModel>> injectCodeRules = new HashMap<>()
  private Project project

  WardHereExtension(Project project) {
    this.project = project
  }

  void injectCode(Closure closure) {
    CodeModel codeModel = new CodeModel()
    project.configure(codeModel, closure)
    List<CodeModel> codeModelList = injectCodeRules.getOrDefault(codeModel.classPath,
        new ArrayList<>())
    injectCodeRules.put(codeModel.classPath, codeModelList)
    codeModelList.add(codeModel)
  }
}