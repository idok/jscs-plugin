package com.jscs.utils.data;

import com.thoughtworks.xstream.XStream;

import java.util.ArrayList;
import java.util.List;

public class JscsLint {
    public File file;
    public String version;

    public static JscsLint read(String xml) {
        XStream xstream = new XStream();
        xstream.alias("checkstyle", JscsLint.class);
        xstream.useAttributeFor(JscsLint.class, "version");
        xstream.alias("file", File.class);
        xstream.alias("error", Issue.class);
        xstream.addImplicitCollection(File.class, "errors");
        xstream.useAttributeFor(File.class, "name");
        xstream.useAttributeFor(Issue.class, "source");
        xstream.useAttributeFor(Issue.class, "line");
        xstream.useAttributeFor(Issue.class, "column");
        xstream.useAttributeFor(Issue.class, "severity");
        xstream.useAttributeFor(Issue.class, "message");
        return (JscsLint) xstream.fromXML(xml);
    }

    public static class File {
        public String name;
        public List<Issue> errors = new ArrayList<Issue>();
    }

    public static class Issue {
        public String source;
        public int line;
        public int column;
        public String severity;
        public String message;
    }
}

