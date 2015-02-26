package com.jscs.settings;

import com.jscs.JscsProjectComponent;
import com.jscs.utils.JscsFinder;
import com.jscs.utils.JscsRunner;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.execution.ExecutionException;
import com.intellij.javascript.nodejs.NodeDetectionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.TextFieldWithHistory;
import com.intellij.ui.TextFieldWithHistoryWithBrowseButton;
import com.intellij.util.NotNullProducer;
import com.intellij.util.ui.UIUtil;
import com.intellij.webcore.ui.SwingHelper;
import com.jscs.utils.JscsSettings;
import com.wix.settings.ValidationInfo;
import com.wix.ui.PackagesNotificationPanel;
import com.wix.utils.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JscsSettingsPage implements Configurable {
    public static final String FIX_IT = "Fix it";
    public static final String HOW_TO_USE_JSCS = "How to Use JSCS";
    public static final String HOW_TO_USE_LINK = "https://github.com/idok/jscs-plugin";
    protected Project project;

    private JCheckBox pluginEnabledCheckbox;
    private JTextField presetField;
    private JPanel panel;
    private JPanel errorPanel;
    private TextFieldWithHistoryWithBrowseButton jscsBinField;
    private TextFieldWithHistoryWithBrowseButton nodeInterpreterField;
    private TextFieldWithHistoryWithBrowseButton jscsrcFile;
    private JRadioButton searchForJscsrcInRadioButton;
    private JRadioButton useProjectJscsrcRadioButton;
    private HyperlinkLabel usageLink;
    private JLabel jscsConfigFilePathLabel;
    private JLabel rulesDirectoryLabel;
    private JLabel pathToJscsBinLabel;
    private JLabel nodeInterpreterLabel;
    private JCheckBox treatAllIssuesCheckBox;
    private JLabel versionLabel;
    private JCheckBox esnextCheckBox;
    private JTextField esprimaField;
    private final PackagesNotificationPanel packagesNotificationPanel;

    public JscsSettingsPage(@NotNull final Project project) {
        this.project = project;
        configBinField();
        configJscsRcField();
        configNodeField();
        useProjectJscsrcRadioButton.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                jscsrcFile.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        pluginEnabledCheckbox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                boolean enabled = e.getStateChange() == ItemEvent.SELECTED;
                setEnabledState(enabled);
            }
        });

        this.packagesNotificationPanel = new PackagesNotificationPanel(project);
        errorPanel.add(this.packagesNotificationPanel.getComponent(), BorderLayout.CENTER);

        DocumentAdapter docAdp = new DocumentAdapter() {
            protected void textChanged(DocumentEvent e) {
                updateLaterInEDT();
            }
        };
        jscsBinField.getChildComponent().getTextEditor().getDocument().addDocumentListener(docAdp);
        jscsrcFile.getChildComponent().getTextEditor().getDocument().addDocumentListener(docAdp);
        nodeInterpreterField.getChildComponent().getTextEditor().getDocument().addDocumentListener(docAdp);
//        rulesPathField.getChildComponent().getTextEditor().getDocument().addDocumentListener(docAdp);
        presetField.getDocument().addDocumentListener(docAdp);
    }

    private void addDocumentListenerToComp(TextFieldWithHistoryWithBrowseButton field, DocumentAdapter docAdp) {
        field.getChildComponent().getTextEditor().getDocument().addDocumentListener(docAdp);
    }

    private File getProjectPath() {
        return new File(project.getBaseDir().getPath());
    }

    private void updateLaterInEDT() {
        UIUtil.invokeLaterIfNeeded(new Runnable() {
            public void run() {
                JscsSettingsPage.this.update();
            }
        });
    }

    private void update() {
        ApplicationManager.getApplication().assertIsDispatchThread();
        validate();
    }

    private void setEnabledState(boolean enabled) {
        jscsrcFile.setEnabled(enabled);
        presetField.setEnabled(enabled);
        searchForJscsrcInRadioButton.setEnabled(enabled);
        useProjectJscsrcRadioButton.setEnabled(enabled);
        jscsBinField.setEnabled(enabled);
        nodeInterpreterField.setEnabled(enabled);
        jscsConfigFilePathLabel.setEnabled(enabled);
        rulesDirectoryLabel.setEnabled(enabled);
        pathToJscsBinLabel.setEnabled(enabled);
        nodeInterpreterLabel.setEnabled(enabled);
        treatAllIssuesCheckBox.setEnabled(enabled);
        esnextCheckBox.setEnabled(enabled);
        esprimaField.setEnabled(enabled);
    }

    private void validateField(List<ValidationInfo> errors, TextFieldWithHistoryWithBrowseButton field, boolean allowEmpty, String message) {
        if (!validatePath(field.getChildComponent().getText(), allowEmpty)) {
            ValidationInfo error = new ValidationInfo(field.getChildComponent().getTextEditor(), message, FIX_IT);
            errors.add(error);
        }
    }

    private void validate() {
        if (!pluginEnabledCheckbox.isSelected()) {
            return;
        }
        List<ValidationInfo> errors = new ArrayList<ValidationInfo>();
        validateField(errors, jscsBinField, false, "Path to jscs is invalid {{LINK}}");
        validateField(errors, jscsrcFile, true, "Path to jscsrc is invalid {{LINK}}"); //Please correct path to
        validateField(errors, nodeInterpreterField, false, "Path to node interpreter is invalid {{LINK}}");
        if (errors.isEmpty()) {
            try {
                packagesNotificationPanel.removeAllLinkHandlers();
            } catch (Exception e) {
                e.printStackTrace();
            }
            packagesNotificationPanel.hide();
            getVersion();
        } else {
            packagesNotificationPanel.showErrors(errors);
        }
    }

    private JscsSettings settings;

    private void getVersion() {
        if (settings != null &&
            areEqual(nodeInterpreterField, settings.node) &&
            areEqual(jscsBinField, settings.jscsExecutablePath) &&
            settings.cwd.equals(project.getBasePath())
                ) {
            return;
        }
        settings = new JscsSettings();
        settings.node = nodeInterpreterField.getChildComponent().getText();
        settings.jscsExecutablePath = jscsBinField.getChildComponent().getText();
        settings.cwd = project.getBasePath();
        try {
            String version = JscsRunner.version(settings);
            versionLabel.setText(version.trim());
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    private boolean validatePath(String path, boolean allowEmpty) {
        if (StringUtils.isEmpty(path)) {
            return allowEmpty;
        }
        File filePath = new File(path);
        if (filePath.isAbsolute()) {
            if (!filePath.exists() || !filePath.isFile()) {
                return false;
            }
        } else {
            VirtualFile child = project.getBaseDir().findFileByRelativePath(path);
            if (child == null || !child.exists() || child.isDirectory()) {
                return false;
            }
        }
        return true;
    }

    private boolean validateDirectory(String path, boolean allowEmpty) {
        if (StringUtils.isEmpty(path)) {
            return allowEmpty;
        }
        File filePath = new File(path);
        if (filePath.isAbsolute()) {
            if (!filePath.exists() || !filePath.isDirectory()) {
                return false;
            }
        } else {
            VirtualFile child = project.getBaseDir().findFileByRelativePath(path);
            if (child == null || !child.exists() || !child.isDirectory()) {
                return false;
            }
        }
        return true;
    }

    private static TextFieldWithHistory configWithDefaults(TextFieldWithHistoryWithBrowseButton field) {
        TextFieldWithHistory textFieldWithHistory = field.getChildComponent();
        textFieldWithHistory.setHistorySize(-1);
        textFieldWithHistory.setMinimumAndPreferredWidth(0);
        return textFieldWithHistory;
    }

    private void configBinField() {
        configWithDefaults(jscsBinField);
        SwingHelper.addHistoryOnExpansion(jscsBinField.getChildComponent(), new NotNullProducer<List<String>>() {
            @NotNull
            public List<String> produce() {
                List<File> newFiles = JscsFinder.searchForJscsBin(getProjectPath());
                return FileUtils.toAbsolutePath(newFiles);
            }
        });
        SwingHelper.installFileCompletionAndBrowseDialog(project, jscsBinField, "Select jscs.js cli", FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
    }

    private void configJscsRcField() {
        TextFieldWithHistory textFieldWithHistory = configWithDefaults(jscsrcFile);
        SwingHelper.addHistoryOnExpansion(textFieldWithHistory, new NotNullProducer<List<String>>() {
            @NotNull
            public List<String> produce() {
                return JscsFinder.searchForJscsRCFiles(getProjectPath());
            }
        });
        SwingHelper.installFileCompletionAndBrowseDialog(project, jscsrcFile, "Select JSCS config", FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
    }

    private void configNodeField() {
        TextFieldWithHistory textFieldWithHistory = configWithDefaults(nodeInterpreterField);
        SwingHelper.addHistoryOnExpansion(textFieldWithHistory, new NotNullProducer<List<String>>() {
            @NotNull
            public List<String> produce() {
                List<File> newFiles = NodeDetectionUtil.listAllPossibleNodeInterpreters();
                return FileUtils.toAbsolutePath(newFiles);
            }
        });
        SwingHelper.installFileCompletionAndBrowseDialog(project, nodeInterpreterField, "Select Node interpreter", FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor());
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "JSCS";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        loadSettings();
        return panel;
    }

    private static boolean areEqual(TextFieldWithHistoryWithBrowseButton field, String value) {
        return field.getChildComponent().getText().equals(value);
    }

    @Override
    public boolean isModified() {
        Settings s = getSettings();
        Settings uiSettings = toSettings();
        return !s.isEqualTo(uiSettings);
    }

    private String getJscsRCFile() {
        return useProjectJscsrcRadioButton.isSelected() ? jscsrcFile.getChildComponent().getText() : "";
    }

    @Override
    public void apply() throws ConfigurationException {
        saveSettings();
        PsiManager.getInstance(project).dropResolveCaches();
    }

    protected void saveSettings() {
        Settings settings = getSettings();
        copyTo(settings);
        project.getComponent(JscsProjectComponent.class).validateSettings();
        DaemonCodeAnalyzer.getInstance(project).restart();
    }

    public Settings toSettings() {
        Settings settings = new Settings();
        copyTo(settings);
        return settings;
    }

    public void copyTo(Settings settings) {
        settings.pluginEnabled = pluginEnabledCheckbox.isSelected();
        settings.jscsExecutable = jscsBinField.getChildComponent().getText();
        settings.nodeInterpreter = nodeInterpreterField.getChildComponent().getText();
        settings.jscsrcFile = getJscsRCFile();
        settings.preset = presetField.getText();
        settings.esprima = esprimaField.getText();
        settings.esnext = esnextCheckBox.isSelected();
        settings.treatAllIssuesAsWarnings = treatAllIssuesCheckBox.isSelected();
    }

    protected void loadSettings() {
        Settings settings = getSettings();
        pluginEnabledCheckbox.setSelected(settings.pluginEnabled);
        jscsBinField.getChildComponent().setText(settings.jscsExecutable);
        jscsrcFile.getChildComponent().setText(settings.jscsrcFile);
        nodeInterpreterField.getChildComponent().setText(settings.nodeInterpreter);
        presetField.setText(settings.preset);
        esprimaField.setText(settings.esprima);
        useProjectJscsrcRadioButton.setSelected(StringUtils.isNotEmpty(settings.jscsrcFile));
        searchForJscsrcInRadioButton.setSelected(StringUtils.isEmpty(settings.jscsrcFile));
        jscsrcFile.setEnabled(useProjectJscsrcRadioButton.isSelected());
        treatAllIssuesCheckBox.setSelected(settings.treatAllIssuesAsWarnings);
        esnextCheckBox.setSelected(settings.esnext);
        setEnabledState(settings.pluginEnabled);
    }

    @Override
    public void reset() {
        loadSettings();
    }

    @Override
    public void disposeUIResources() {
    }

    protected Settings getSettings() {
        return Settings.getInstance(project);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        usageLink = SwingHelper.createWebHyperlink(HOW_TO_USE_JSCS, HOW_TO_USE_LINK);
    }
}
