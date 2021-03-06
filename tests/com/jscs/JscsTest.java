package com.jscs;

import com.jscs.settings.Settings;
import com.jscs.utils.JscsRunnerTest;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.fixtures.*;

public class JscsTest extends LightPlatformCodeInsightFixtureTestCase {
    @Override
    protected String getTestDataPath() {
        return TestUtils.getTestDataPath();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected boolean isWriteActionRequired() {
        return false;
    }

    protected void doTest(final String file, String rc) {
        Project project = myFixture.getProject();
        Settings settings = Settings.getInstance(project);
        settings.jscsExecutable = JscsRunnerTest.JSCS_BIN;
        settings.jscsrcFile = rc; //getTestDataPath() + "/.jscsrc";
        settings.nodeInterpreter = JscsRunnerTest.NODE_INTERPRETER;
        settings.preset = "";
        settings.pluginEnabled = true;
        myFixture.configureByFile(file);
        myFixture.enableInspections(new JscsInspection());
        myFixture.checkHighlighting(true, false, true);
    }

    protected void doTest() {
        String name = getTestName(true).replaceAll("_", "-");
        doTest("/inspections/" + name + ".js", "");
    }

    public void testRequireSpaceBeforeBinaryOperators() {
        doTest();
    }

    public void testValidateIndentation() {
        doTest();
    }
}
