package com.jscs.settings;

import com.jscs.cli.JscsFinder;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.jscs.cli.JscsSettings;
import com.wix.utils.Strings;
import org.jetbrains.annotations.Nullable;

@State(name = "JscsProjectComponent",
        storages = {
                @Storage(id = "default", file = StoragePathMacros.PROJECT_FILE),
                @Storage(id = "dir", file = StoragePathMacros.PROJECT_CONFIG_DIR + "/jscsPlugin.xml", scheme = StorageScheme.DIRECTORY_BASED)})
public class Settings implements PersistentStateComponent<Settings> {
    public String jscsrcFile = JscsFinder.JSCSRC;
    public String preset = "";
    public String jscsExecutable = "";
    public String nodeInterpreter;
    public boolean treatAllIssuesAsWarnings;
    public boolean pluginEnabled;
    public boolean esnext;
    public String esprima;

    public boolean isEqualTo(Settings settings) {
        return settings != null && esnext == settings.esnext && pluginEnabled == settings.pluginEnabled &&
                treatAllIssuesAsWarnings == settings.treatAllIssuesAsWarnings &&
                Strings.areEqual(jscsExecutable, settings.jscsExecutable) &&
                Strings.areEqual(esprima, settings.esprima) &&
                Strings.areEqual(jscsrcFile, settings.jscsrcFile) &&
                Strings.areEqual(nodeInterpreter, settings.nodeInterpreter) &&
                Strings.areEqual(preset, settings.preset);
    }

    public JscsSettings toJscsSettings() {
        JscsSettings settings = new JscsSettings();
        settings.node = nodeInterpreter;
        settings.jscsExecutablePath = jscsExecutable;
        settings.preset = preset;
        settings.config = jscsrcFile;
        settings.esnext = esnext;
        settings.esprima = esprima;
        return settings;
    }

    protected Project project;

    public static Settings getInstance(Project project) {
        Settings settings = ServiceManager.getService(project, Settings.class);
        settings.project = project;
        return settings;
    }

    @Nullable
    @Override
    public Settings getState() {
        return this;
    }

    @Override
    public void loadState(Settings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    public String getVersion() {
        return nodeInterpreter + jscsExecutable + jscsrcFile + preset + esnext + esprima;
    }
}
