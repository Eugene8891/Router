package com.midrive.voice.router_compiler.utils;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

/**
 * Print log when compiling.
 */
public class Logger {
    private static final String TAG = "::compiler";
    private Messager msg;

    public Logger(Messager messager) {
        msg = messager;
    }

    /**
     * Print info log.
     */
    public void info(CharSequence info) {
        msg.printMessage(Diagnostic.Kind.NOTE, TAG + info);
    }

    public void error(CharSequence error) {
        msg.printMessage(Diagnostic.Kind.ERROR, TAG + "An exception is encountered, [" + error + "]");
    }

    public void error(Throwable error) {
        msg.printMessage(Diagnostic.Kind.ERROR, TAG + "An exception is encountered, [" + error.getMessage() + "]" + "\n" + formatStackTrace(error.getStackTrace()));
    }

    public void warning(CharSequence warning) {
        msg.printMessage(Diagnostic.Kind.WARNING, TAG + warning);
    }

    private String formatStackTrace(StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : stackTrace) {
            sb.append("    at ").append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

}
