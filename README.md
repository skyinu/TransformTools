# PrintCatch

## Description

PrintCatch目标是向project中所有的source code、jar中的try catch代码块中的catch代码块插入log,用于解决代码执行时
错误意外被try catch导致问题难以定位的问题,插入log后可以方便观察运行时的exception情况

再次基础上增加了想指定类特定行插入代码的功能

## Usage

add classpath

```groovy
compile 'com.skyinu:printexception-plugin:0.1.0'
```

add plugin dependency

```groovy
apply plugin: 'com.skyinu.printexception'
```

configuration

```groovy
wardHere{
    dumpAble true //whether dump the modified class file
    dumpDir "${project.buildDir}${File.separator}dumpDir" //the directory to dump the modified class file
    exceptionTag  "error" //the log tag use to print exception log
    plain true
    injectJar true//whether inject log to jar dependencies
    injectCode {
        classPath = "com.skyinu.wardhere.MainActivity"
        lineNumber = 25
        code =  "android.util.Log.e(\"tag\", \"error \" + \"tag\");"
      }

      injectCode {
        classPath = "com.skyinu.wardhere.MainActivity"
        lineNumber = 37
        code =  "android.util.Log.e(\"tag\", \"37 \" + \"tag\");"
      }
}
```

+ plain:

    为true表示只插入一条简单的log,形如

    ```java
        try {
            Object object = null;
            return object.toString();
        } catch (Exception var3) {
            Log.e("error", "MainActivity.java->catchTest->" + var3.getMessage());
            return null;
        }
    ```

    为false表示输出完整log 并 可自定义log形式,此时需要添加一些配置

        添加依赖

        ```groovy
        compile 'com.skyinu:printexception:0.1.1'
        ```

        这样就可以支持打印完整log了, 如果想自定义,调用方法`com.skyinu.printexception.LogPrinter.setEventListenter`
        传入自己的event listener即可

## publish

./gradlew clean build bintrayUpload -PbintrayUser=BINTRAY_USERNAME -PbintrayKey=BINTRAY_KEY -PdryRun=false

## debug

gradlew :app:assembleDebug -Dorg.gradle.debug=true  --no-daemon

## extra

if you occur exception when clean project, you can run `gradlew --stop` then retry