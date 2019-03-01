package com.skyinu.printexception;

import java.io.File;

public class Utils {
    public static String retrieveClassName(File targetFile, File parentDir) {
        return targetFile.getAbsolutePath().substring(parentDir.getAbsolutePath().length() + 1)
                .replace(File.separator, ".").replace(Const.SUFFIX_CLASS, "");
    }

    public static String retrieveClassNameForJarClass(String jarClass) {
        String pre = jarClass.replace(File.separator, ".").replace(Const.SUFFIX_CLASS, "");
        return pre.replaceAll("/", ".");
    }
}
