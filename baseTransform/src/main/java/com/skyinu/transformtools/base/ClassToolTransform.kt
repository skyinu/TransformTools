package com.skyinu.transformtools.base

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.QualifiedContent.ContentType
import com.android.build.api.transform.QualifiedContent.Scope
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.ClassPool
import javassist.CtClass
import org.gradle.api.Project
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class ClassToolTransform(
    var mProject: Project,
    var namePrefix: String
) : Transform() {
    private lateinit var mClassPool: ClassPool
    private var mJarInputs = mutableListOf<JarInput>()
    private var mDirInputs = mutableListOf<File>()
    private var mClassHandler: ClassHandler? = null

    override fun getName() = "${namePrefix}classTool"

    override fun getInputTypes(): Set<ContentType> = TransformManager.CONTENT_CLASS

    override fun isIncremental() = false

    override fun getScopes(): MutableSet<in Scope> = TransformManager.SCOPE_FULL_PROJECT

    override fun transform(transformInvocation: TransformInvocation) {
        super.transform(transformInvocation)
        val outputProvider = transformInvocation.outputProvider
        outputProvider.deleteAll()
        initClassPool()
        initClassPath(transformInvocation)
        handleClass(outputProvider)
    }

    private fun handleClass(outputProvider: TransformOutputProvider) {
        mDirInputs.forEach { targetDir ->
            val fileTree = targetDir.walk()
            fileTree
                .filter {
                    it.isFile && it.name.endsWith(Utils.SUFFIX_CLASS)
                }
                .forEach {
                    val fileItem = it
                    val rawClass =
                        Utils.safeGetCtClass(
                            mClassPool,
                            Utils.retrieveClassName(fileItem, targetDir)
                        )
                    rawClass?.let {
                        if (rawClass.isFrozen) {
                            rawClass.defrost()
                        }
                        if (mClassHandler?.travelClass(rawClass) == true) {
                            rawClass.writeFile(targetDir.absolutePath)
                        }
                    }
                }
        }
        mDirInputs.clear()
        mJarInputs.forEach {
            val targetFile = outputProvider.getContentLocation(
                it.name, it.contentTypes,
                it.scopes, Format.JAR
            )
            handleJar(it.file, targetFile)
        }
    }

    private fun handleJar(
        originFile: File,
        targetFile: File
    ) {
        val buffer = ByteArray(8096)
        val originFileIns = ZipInputStream(FileInputStream(originFile))
        val targetFileOutS = ZipOutputStream(FileOutputStream(targetFile))
        try {
            var entry = originFileIns.nextEntry
            while (entry != null) {
                val entryName = entry.name
                targetFileOutS.putNextEntry(ZipEntry(entryName))
                var rawClass: CtClass? = null
                if (entryName.endsWith(Utils.SUFFIX_CLASS)) {
                    val className = Utils.retrieveClassNameForJarClass(entryName)
                    rawClass = Utils.safeGetCtClass(mClassPool, className)
                    if (rawClass != null) {
                        if (rawClass.isFrozen) {
                            rawClass.defrost()
                        }
                    }
                }
                if (rawClass != null && mClassHandler?.travelClass(rawClass) == true) {
                    targetFileOutS.write(rawClass.toBytecode())
                } else {
                    var len = originFileIns.read(buffer)
                    while (len > 0) {
                        targetFileOutS.write(buffer, 0, len)
                        len = originFileIns.read(buffer)
                    }
                }
                entry = originFileIns.nextEntry
            }
        } finally {
            originFileIns.close()
            targetFileOutS.close()
        }
    }

    private fun initClassPool() {
        mClassPool = ClassPool()
        mClassPool.appendSystemPath()
        val appPlugin = mProject.plugins.findPlugin(AppPlugin::class.java)
        appPlugin?.let {
            mClassPool.appendClassPath(it.getExtension().bootClasspath[0].path)
        }
    }

    private fun initClassPath(transformInvocation: TransformInvocation) {
        travelInput(transformInvocation) {
            when (it) {
                is JarInput -> {
                    mJarInputs.add(it)
                    mClassPool.insertClassPath(it.file.path)
                }
                is DirectoryInput -> {
                    val out = transformInvocation.outputProvider.getContentLocation(
                        it.name, it.contentTypes,
                        it.scopes, Format.DIRECTORY
                    )
                    FileUtils.copyDirectory(it.file, out)
                    mDirInputs.add(out)
                    mClassPool.insertClassPath(out.path)
                }
            }
        }
    }

    private fun travelInput(
        transformInvocation: TransformInvocation,
        action: (inputItem: QualifiedContent) -> Unit
    ) {
        transformInvocation.inputs.forEach { inputItem ->
            inputItem.jarInputs.forEach {
                val jarItem = it
                action(jarItem)
            }
            inputItem.directoryInputs.forEach {
                val dirItem = it
                action(dirItem)
            }
        }
    }

    fun regiesterClassHandler(classHandler: ClassHandler) {
        this.mClassHandler = classHandler
    }
}