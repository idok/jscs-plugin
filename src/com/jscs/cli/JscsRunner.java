package com.jscs.cli;

import com.google.common.base.Charsets;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.jscs.cli.data.JscsLint;
import com.jscs.cli.data.LintResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.TimeUnit;

public final class JscsRunner {
    private JscsRunner() {
    }

    private static final Logger LOG = Logger.getInstance(JscsRunner.class);

    private static final int TIME_OUT = (int) TimeUnit.SECONDS.toMillis(120L);
    private static final String FIX = "--fix";
    private static final String VERSION = "--fix";


//    @NotNull
//    private static ProcessOutput runLint(@NotNull JscsSettings settings) throws ExecutionException {
//        GeneralCommandLine commandLine = createCommandLineLint(settings);
//        return execute(commandLine, TIME_OUT);
//    }

    public static LintResult lint(@NotNull String cwd, @NotNull String path, @NotNull String nodeInterpreter, @NotNull String jscsBin, @Nullable String jscsrc, @Nullable String preset, boolean esnext, String esprima) {
        JscsSettings settings = JscsSettings.build(cwd, path, nodeInterpreter, jscsBin, jscsrc, preset, esprima, esnext);
        return lint(settings);
    }

    public static LintResult lint(@NotNull JscsSettings settings) {
        LintResult result = new LintResult();
        try {
            GeneralCommandLine commandLine = createCommandLineLint(settings);
            ProcessOutput out = execute(commandLine, TIME_OUT);
//        if (out.getExitCode() == 0) {
//        } else {
            result.errorOutput = out.getStderr();
            try {
                result.jscsLint = JscsLint.read(out.getStdout());
            } catch (Exception e) {
                //result.errorOutput = out.getStdout();
            }
//        }
        } catch (Exception e) {
            e.printStackTrace();
            result.errorOutput = e.toString();
        }
        return result;
    }

    public static LintResult fix(@NotNull String cwd, @NotNull String path, @NotNull String nodeInterpreter, @NotNull String jscsBin, @Nullable String jscsrc, @Nullable String preset, boolean esnext, String esprima) {
        JscsSettings settings = JscsSettings.build(cwd, path, nodeInterpreter, jscsBin, jscsrc, preset, esprima, esnext);
        return fix(settings);
    }

    public static LintResult fix(@NotNull JscsSettings settings) {
        LintResult result = new LintResult();
        try {
            GeneralCommandLine commandLine = createCommandLineLint(settings);
            commandLine.addParameter("--fix");
            ProcessOutput out = execute(commandLine, TIME_OUT);
            result.errorOutput = out.getStderr();
            try {
                result.jscsLint = JscsLint.read(out.getStdout());
            } catch (Exception e) {
                LOG.error(e);
                //result.errorOutput = out.getStdout();
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.errorOutput = e.toString();
        }
        return result;
    }

    @NotNull
    private static ProcessOutput runVersion(@NotNull JscsSettings settings) throws ExecutionException {
        GeneralCommandLine commandLine = createCommandLine(settings);
        commandLine.addParameter("--version");
        return execute(commandLine, TIME_OUT);
    }

    @NotNull
    public static String version(@NotNull JscsSettings settings) throws ExecutionException {
        if (!new File(settings.jscsExecutablePath).exists()) {
            LOG.warn("Calling version with invalid jscs exe " + settings.jscsExecutablePath);
            return "";
        }
        ProcessOutput out = runVersion(settings);
        if (out.getExitCode() == 0) {
            return out.getStdout().trim();
        }
        return "";
    }

    @NotNull
    private static GeneralCommandLine createCommandLine(@NotNull JscsSettings settings) {
        GeneralCommandLine commandLine = new GeneralCommandLine();
        commandLine.setWorkDirectory(settings.cwd);
        if (SystemInfo.isWindows) {
            commandLine.setExePath(settings.jscsExecutablePath);
        } else {
            commandLine.setExePath(settings.node);
            commandLine.addParameter(settings.jscsExecutablePath);
        }
        return commandLine;
    }

    @NotNull
    private static GeneralCommandLine createCommandLineLint(@NotNull JscsSettings settings) {
        GeneralCommandLine commandLine = createCommandLine(settings);
        // TODO validate arguments (file exist etc)
        commandLine.addParameter(settings.targetFile);
        addParamIfExist(commandLine, "config", settings.config);
        addParam(commandLine, "reporter", "checkstyle");
        commandLine.addParameter("-v");
        addParamIfExist(commandLine, "preset", settings.preset);
        addParamIfExist(commandLine, "esprima", settings.esprima);
        if (settings.esnext) {
            commandLine.addParameter("--esnext");
        }
        return commandLine;
    }

    private static void addParam(GeneralCommandLine commandLine, String name, String value) {
        commandLine.addParameter("--" + name + '=' + value);
    }

    private static void addParamIfExist(GeneralCommandLine commandLine, String name, String value) {
        if (StringUtil.isNotEmpty(value)) {
            addParam(commandLine, name, value);
        }
    }

    @NotNull
    private static ProcessOutput execute(@NotNull GeneralCommandLine commandLine, int timeoutInMilliseconds) throws ExecutionException {
        LOG.info("Running jscs command: " + commandLine.getCommandLineString());
        Process process = commandLine.createProcess();
        OSProcessHandler processHandler = new ColoredProcessHandler(process, commandLine.getCommandLineString(), Charsets.UTF_8);
        final ProcessOutput output = new ProcessOutput();
        processHandler.addProcessListener(new ProcessAdapter() {
            public void onTextAvailable(ProcessEvent event, Key outputType) {
                if (outputType.equals(ProcessOutputTypes.STDERR)) {
                    output.appendStderr(event.getText());
                } else if (!outputType.equals(ProcessOutputTypes.SYSTEM)) {
                    output.appendStdout(event.getText());
                }
            }
        });
        processHandler.startNotify();
        if (processHandler.waitFor(timeoutInMilliseconds)) {
            output.setExitCode(process.exitValue());
        } else {
            processHandler.destroyProcess();
            output.setTimeout();
        }
        if (output.isTimeout()) {
            throw new ExecutionException("Command '" + commandLine.getCommandLineString() + "' is timed out.");
        }
        return output;
    }
}