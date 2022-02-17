package com.skyinu.wardhere;

import androidx.annotation.Keep;
import android.util.Log;

@Keep
public class LogPrinter {
    private static EventListenerAdapter eventListenter;

    public static void setEventListenter(EventListenerAdapter eventListenter) {
        LogPrinter.eventListenter = eventListenter;
    }

    @Keep
    public static void onCatchEvent(String tag, Throwable throwable) {
        if (eventListenter != null) {
            eventListenter.onCatch(tag, throwable);
            return;
        }
        Log.d(tag, buildLog(throwable));
    }

    public static String buildLog(Throwable throwable) {
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < stackTraceElements.length; index++) {
            builder.append(stackTraceElements[index].toString())
                    .append("\n");
        }
        return builder.toString();
    }
}
