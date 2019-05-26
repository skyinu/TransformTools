package com.skyinu.wardhere

import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.AppPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

public class PrintExceptionPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        if (!(project.plugins.hasPlugin(LibraryPlugin) || project.plugins.hasPlugin(AppPlugin))) {
            throw new IllegalStateException('plugin can only be applied to android projects')
        }
        project.extensions.create(PrintExceptionExtension.DSL_DOMAIN_NAME, PrintExceptionExtension)
        project.android.registerTransform(new PrintExceptionTransform(project))
    }
}
