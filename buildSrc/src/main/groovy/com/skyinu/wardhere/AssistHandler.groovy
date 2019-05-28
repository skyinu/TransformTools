package com.skyinu.wardhere

import javassist.expr.Cast
import javassist.expr.ConstructorCall
import javassist.expr.FieldAccess
import javassist.expr.Instanceof
import javassist.expr.MethodCall
import javassist.expr.NewArray
import javassist.expr.NewExpr
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import javassist.CannotCompileException
import javassist.ClassPath
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.NotFoundException
import javassist.expr.ExprEditor
import javassist.expr.Handler
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

public class AssistHandler {
  private Project project
  private ClassPool classPool
  private List<ClassPath> classPaths
  private WardHereExtension exceptionExtension

  public AssistHandler(Project project) {
    this.project = project
    this.classPool = new ClassPool(true)
    this.classPaths = new ArrayList<>()
  }

  public void init(WardHereExtension exceptionExtension) {
    this.exceptionExtension = exceptionExtension
  }

  public void insertClassPath(String classPath) {
    try {
      classPaths.add(classPool.insertClassPath(classPath))
    } catch (NotFoundException e) {
      e.printStackTrace()
    }
  }

  public void handleJarInput(File input, File dest) {
    ZipFile inputJar = new ZipFile(input)
    Enumeration<ZipEntry> zipEntries = inputJar.entries()
    if (!exceptionExtension.injectJar || zipEntries == null || !zipEntries.hasMoreElements()) {
      FileUtils.copyFile(input, dest)
      return
    }
    ZipOutputStream zipOutputJar = new ZipOutputStream(new FileOutputStream(dest))
    zipOutputJar.setComment(inputJar.comment)
    while (zipEntries.hasMoreElements()) {
      ZipEntry inputJarEntry = zipEntries.nextElement()
      String entryName = inputJarEntry.getName()
      String className = Utils.retrieveClassNameForJarClass(entryName)
      CtClass ctClass = classPool.getOrNull(className)
      zipOutputJar.putNextEntry(new ZipEntry(inputJarEntry.name))
      if (ctClass == null) {
        writeEntryData(inputJar, inputJarEntry, zipOutputJar)
        zipOutputJar.closeEntry()
        continue
      }
      if (ctClass.isFrozen()) {
        ctClass.defrost()
      }
      CtMethod[] ctMethods = ctClass.getDeclaredMethods()
      if (ctMethods == null || ctMethods.length <= 0) {
        writeEntryData(inputJar, inputJarEntry, zipOutputJar)
        zipOutputJar.closeEntry()
        continue
      }
      ctMethods.each {
        injectCode(it)
      }
      byte[] data = ctClass.toBytecode()
      zipOutputJar.write(data)
      zipOutputJar.closeEntry()
    }
    inputJar.close()
    zipOutputJar.close()
  }

  private void writeEntryData(ZipFile inputJar, ZipEntry inputJarEntry,
      ZipOutputStream outputStream) {
    try {
      InputStream jarIns = inputJar.getInputStream(inputJarEntry)
      byte[] buffer = new byte[1024]
      int len
      while ((len = jarIns.read(buffer)) != -1) {
        outputStream.write(buffer, 0, len)
      }
    } catch (IOException ex) {
      project.logger.error(inputJarEntry.name + " => error: " + ex)
    }
  }

  public void handleDirectory(File input) {
    project.logger.error("input directory = " + input.absolutePath)
    input.eachFileRecurse {
      handleFile(input, it)
    }
  }

  public void handleFile(File directory, File input) {
    if (!input.absolutePath.endsWith(Const.SUFFIX_CLASS)) {
      return
    }
    def className = Utils.retrieveClassName(input, directory)
    CtClass ctClass = classPool.getOrNull(className)
    if (ctClass == null) {
      File parent = input.parentFile
      while (parent != null && parent.exists()) {
        className = Utils.retrieveClassName(input, parent)
        ctClass = classPool.getOrNull(className)
        if (ctClass != null) {
          break
        }
        parent = parent.parentFile
      }
      if (ctClass == null) {
        return
      }
    }
    if (ctClass.isFrozen()) {
      ctClass.defrost()
    }
    CtMethod[] ctMethods = ctClass.getDeclaredMethods()
    if (ctMethods == null || ctMethods.length <= 0) {
      return
    }
    ctMethods.each {
      injectCode(it)
    }
    ctClass.writeFile(directory.absolutePath)
  }

  private void injectCode(CtMethod ctMethod) {
    ctMethod.instrument(new ExprEditor() {
      @Override
      void edit(Handler handler) throws CannotCompileException {
        super.edit(handler)
        try {
          handler.insertBefore(buildExceptionLog(handler))
          injectCodeAtLine(ctMethod, handler.lineNumber)
        } catch (Exception ex) {
          if (ex.message != null) {
            project.logger.error(handler.fileName + " inject error = " + ex.message)
          }
        }
      }

      @Override
      void edit(MethodCall m) throws CannotCompileException {
        super.edit(m)
        injectCodeAtLine(ctMethod, m.lineNumber)
      }

      @Override
      void edit(NewArray a) throws CannotCompileException {
        super.edit(a)
        injectCodeAtLine(ctMethod, a.lineNumber)
      }

      @Override
      void edit(FieldAccess f) throws CannotCompileException {
        super.edit(f)
        injectCodeAtLine(ctMethod, f.lineNumber)
      }

      @Override
      void edit(Instanceof i) throws CannotCompileException {
        super.edit(i)
        injectCodeAtLine(ctMethod, i.lineNumber)
      }

      @Override
      void edit(Cast c) throws CannotCompileException {
        super.edit(c)
        injectCodeAtLine(ctMethod, c.lineNumber)
      }

      @Override
      void edit(NewExpr e) throws CannotCompileException {
        super.edit(e)
        injectCodeAtLine(ctMethod, e.lineNumber)
      }

      @Override
      void edit(ConstructorCall c) throws CannotCompileException {
        super.edit(c)
        injectCodeAtLine(ctMethod, c.lineNumber)
      }
    })
  }

  private void injectCodeAtLine(CtMethod ctMethod, int lineNumber) {
    try {
      CtClass declaringClass = ctMethod.getDeclaringClass()
      String parentClassName = declaringClass.getName()
      int innerPos = parentClassName.lastIndexOf("\$")
      if (innerPos > 0) {
        parentClassName = parentClassName.substring(0, innerPos)
      }
      List<CodeModel> codeModels = exceptionExtension.injectCodeRules.get(parentClassName)
      if (codeModels == null) {
        return
      }
      for (CodeModel codeModel in codeModels) {
        if (lineNumber == codeModel.lineNumber) {
          ctMethod.insertAt(codeModel.lineNumber, codeModel.code)
          codeModels.remove(codeModel)
          break
        }
      }
    } catch (Exception ex) {
      if (ex.message != null) {
        project.logger.error(handler.fileName + " inject error = " + ex.message)
      }
    }
  }

  private String buildExceptionLog(Handler handler) {
    StringBuilder builder
    if (exceptionExtension.plain) {
      builder = new StringBuilder(Const.LOG_CLASS)
      builder.append(".e")
          .append("(")
          .append("\"")
          .append(exceptionExtension.exceptionTag)
          .append("\"")
          .append(",")
          .append("\"")
          .append(handler.fileName)
          .append("->")
          .append(handler.where().name)
          .append("->")
          .append("\"+")
          .append("\$1.getMessage()")
          .append(");")
    } else {
      builder = new StringBuilder(Const.LIB_LOG_CLASS)
      builder.append(".onCatchEvent")
          .append("(")
          .append("\"")
          .append(exceptionExtension.exceptionTag)
          .append("\"")
          .append(",")
          .append("\$1")
          .append(");")
    }
    return builder.toString()
  }

  public void clear() {
    classPaths?.each {
      classPool.removeClassPath(it)
    }
    classPaths?.clear()
  }
}
