package com.skyinu.printexception

import org.gradle.api.Project
import javassist.CannotCompileException
import javassist.ClassPath
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.NotFoundException
import javassist.expr.ExprEditor
import javassist.expr.Handler

public class AssistHandler {
    private Project project
    private ClassPool classPool;
    private List<ClassPath> classPaths;

    public AssistHandler(Project project) {
        this.project = project
        this.classPool = new ClassPool(true)
        this.classPaths = new ArrayList<>()
    }

    public void insertClassPath(String classPath) {
        try {
            classPaths.add(classPool.insertClassPath(classPath))
        } catch (NotFoundException e) {
            e.printStackTrace()
        }
    }

    public void handleDirectory(File input) {
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
            while (parent != null && parent.exists()){
                className = Utils.retrieveClassName(input, parent)
                ctClass = classPool.getOrNull(className)
                if (ctClass != null){
                    break
                }
                parent = parent.parentFile
            }
            if(ctClass == null) {
                return
            }
        }
        if(ctClass.isFrozen()){
            ctClass.defrost()
        }
        CtMethod []ctMethods = ctClass.getDeclaredMethods()
        if(ctMethods == null || ctMethods.length <=0){
            return
        }
        ctMethods.each {
            injectLogToCatch(it)
        }
        ctClass.writeFile(directory.absolutePath)
    }

    private void injectLogToCatch(CtMethod ctMethod) {
        ctMethod.instrument(new ExprEditor() {
            @Override
            void edit(Handler handler) throws CannotCompileException {
                super.edit(handler)
                handler.insertBefore("android.util.Log.e(\"TAG1\", \$1.toString());")
            }
        })
    }

    public void clear() {
        classPaths?.each {
            classPool.removeClassPath(it)
        }
    }
}
