package com.skyinu.transformtools.trace

import com.skyinu.transformtools.base.ClassHandler
import javassist.CtClass
import javassist.CtMethod
import javassist.bytecode.AccessFlag
import javassist.expr.Cast
import javassist.expr.Instanceof
import javassist.expr.ConstructorCall
import javassist.expr.ExprEditor
import javassist.expr.Handler
import javassist.expr.MethodCall
import javassist.expr.NewArray

class TraceAssist : ClassHandler {
  companion object {
    const val TRACE_START = "android.os.Trace.beginSection(\"\\s\");\n"
    const val TRACE_END = "android.os.Trace.endSection();\n"
  }

  override fun travelClass(ctClass: CtClass): Boolean {
    val methods = ctClass.declaredMethods
    var handled = false
    methods.filter {
      it.modifiers.and(AccessFlag.SYNTHETIC) != 0
          || it.modifiers.and(AccessFlag.ABSTRACT) != 0
          || it.modifiers.and(AccessFlag.NATIVE) != 0
          || it.modifiers.and(AccessFlag.INTERFACE) != 0
          || !isMethodWithExpression(it)
    }
        .forEach {
          it.insertBefore(TRACE_START.format("trace_${it.name}"))
          it.insertAfter(TRACE_END)
          handled = true
        }
    return handled
  }

  private fun isMethodWithExpression(ctMethod: CtMethod?): Boolean {
    var isCallMethod = false
    if (ctMethod == null) {
      return false
    }

    ctMethod.instrument(object : ExprEditor() {
      override fun edit(a: NewArray) {
        super.edit(a)
        isCallMethod = true
      }

      override fun edit(c: ConstructorCall) {
        super.edit(c)
        isCallMethod = true
      }

      override fun edit(c: Cast) {
        super.edit(c)
        isCallMethod = true
      }

      override fun edit(h: Handler) {
        super.edit(h)
        isCallMethod = true
      }

      override fun edit(i: Instanceof) {
        super.edit(i)
        isCallMethod = true
      }

      override fun edit(m: MethodCall) {
        super.edit(m)
        isCallMethod = true
      }
    })
    return isCallMethod
  }
}