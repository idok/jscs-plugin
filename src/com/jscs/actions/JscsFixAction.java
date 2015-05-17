package com.jscs.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jscs.JscsExternalAnnotator;
import com.jscs.JscsProjectComponent;
import com.jscs.cli.JscsRunner;
import com.jscs.cli.data.LintResult;
import org.jetbrains.annotations.NotNull;

public class JscsFixAction extends AnAction {

    public static boolean isJscsEnabled(Project project) {
        if (project != null) {
            JscsProjectComponent conf = project.getComponent(JscsProjectComponent.class);
            return conf.isEnabled();
        }
        return false;
    }

    public static boolean isJSFile(VirtualFile file) {
        return file != null && file.getExtension() != null && file.getExtension().equals("js");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean enabled = false;
        Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());
        boolean pluginEnabled = isJscsEnabled(project);
        if (project != null) {
            final VirtualFile file = (VirtualFile) e.getDataContext().getData(DataConstants.VIRTUAL_FILE);
            enabled = pluginEnabled && isJSFile(file); // || isRtFileContext(e.getDataContext()));
            if (file != null) {
                e.getPresentation().setText("JSCS Fix '" + file.getName() + '\'');
            }
        }
        e.getPresentation().setVisible(enabled);
    }

    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) return;
        final VirtualFile file = (VirtualFile) e.getDataContext().getData(DataConstants.VIRTUAL_FILE);

        JscsProjectComponent component = project.getComponent(JscsProjectComponent.class);
//        JscsConfigFileListener.start(collectedInfo.project);
//        actualFile = ActualFile2.getOrCreateActualFile(JSCS_TEMP_FILE_KEY, file, collectedInfo.fileContent);
//        if (actualFile == null || actualFile.getActualFile() == null) {
//            return null;
//        }
//            File cwd = new File(project.getBasePath());
//            if (actualFile instanceof ActualFile2.TempActualFile) {
//                cwd = ((ActualFile2.TempActualFile) actualFile).getTempFile().file.getParentFile();
//            }
//        String relativeFile = actualFile.getActualFile().getName();
//        File cwd = actualFile.getActualFile().getParentFile();
//            String relativeFile = FileUtils.makeRelative(cwd, actualFile.getActualFile());

        String rc = JscsExternalAnnotator.getRC(project, component.jscsRcFile);
        LintResult result = JscsRunner.fix(project.getBasePath(), file.getPath(), component.nodeInterpreter, component.jscsExecutable, rc, component.preset, component.settings.esnext, component.settings.esprima);
        file.refresh(true, false);
    }
}
