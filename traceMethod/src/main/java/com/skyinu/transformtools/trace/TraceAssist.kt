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
import org.gradle.api.Project

class TraceAssist(project: Project) : ClassHandler {
    companion object {
        const val TRACE_START = "android.os.Trace.beginSection(\"%s\");\n"
        const val TRACE_END = "android.os.Trace.endSection();\n"
        val EXCLUDE_LIST = arrayListOf("kotlin.")
        private const val CLASS_FILE_SUFFIX = ".class"
    }

    private val excludePackages = arrayListOf<String>()

    init {
        excludePackages.addAll(EXCLUDE_LIST)
        val traceExtension = project.extensions.findByType(TraceExtension::class.java)
        traceExtension?.let {
            traceExtension.excludePackages?.let {
                excludePackages.addAll(it)
            }
        }
    }

    override fun travelClass(ctClass: CtClass): Boolean {
        if (shouldFilterClass(ctClass)) {
            return false
        }
        var handled = false
        var occurError = false
        val methods = ctClass.declaredMethods
        methods.filter {
            !(it.modifiers.and(AccessFlag.SYNTHETIC) != 0
                    || it.modifiers.and(AccessFlag.ABSTRACT) != 0
                    || it.modifiers.and(AccessFlag.NATIVE) != 0
                    || it.modifiers.and(AccessFlag.INTERFACE) != 0
                    || !isMethodWithExpression(it))
        }
            .forEach {
                try {
                    it.insertBefore(TRACE_START.format("trace_${ctClass.simpleName}_${it.name}"))
                    it.insertAfter(TRACE_END, false, ctClass.isKotlin)
                    handled = true
                } catch (exception: Throwable) {
                    occurError = true
                    println("${CtClass.version} occur exception when handle ->${ctClass.name} ${it.name}")
                    println("error = ${exception.message ?: exception.cause?.message}")
                    println("----------------------")
                }
            }
        return handled && !occurError
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

    private fun shouldFilterClass(ctClass: CtClass): Boolean {
        try｛
           if (!ctClass.name.endsWith(CLASS_FILE_SUFFIX)) {
            return true
            }
        val packageName = ctClass.packageName
        excludePackages.forEach {
            if (packageName.isEmpty() || packageName.startsWith(it)) {
                return true
            }
        }
        if (ctClass.isInterface || ctClass.isAnnotation || ctClass.isEnum) {
            return true
        }
｝catch (ex: Exception) {
            println(" error $ctClass")
            return true
        }
        return false
    }
}
