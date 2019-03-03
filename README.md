# PrintCatch

## Description

PrintCatch目标是向project中所有的source code、jar中的try catch代码块中的catch代码块插入log,用于解决代码执行时
错误意外被try catch导致问题难以定位的问题,插入log后可以方便观察运行时的exception情况

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
printException{
    dumpAble true //whether dump the modified class file
    dumpDir "${project.buildDir}${File.separator}dumpDir" //the directory to dump the modified class file
    exceptionTag  "error" //the log tag use to print exception log
    plain true
    injectJar true//whether inject log to jar dependencies
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
## publish

./gradlew clean build bintrayUpload -PbintrayUser=BINTRAY_USERNAME -PbintrayKey=BINTRAY_KEY -PdryRun=false