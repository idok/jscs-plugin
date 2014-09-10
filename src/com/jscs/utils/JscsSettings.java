package com.jscs.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JscsSettings {
    public String node;
    public String jscsExecutablePath;
    public String rules;
    public String config;
    public String cwd;
    public String targetFile;

    public static JscsSettings build(@NotNull String cwd, @NotNull String path, @NotNull String nodeInterpreter, @NotNull String jscsBin, @Nullable String jscsrc, @Nullable String rulesdir) {
        JscsSettings settings = new JscsSettings();
        settings.cwd = cwd;
        settings.jscsExecutablePath = jscsBin;
        settings.node = nodeInterpreter;
        settings.rules = rulesdir;
        settings.config = jscsrc;
        settings.targetFile = path;
        return settings;
    }
}