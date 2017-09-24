package org.deepthinkit.patternlab.sling.core.model.page.htlparser;

/**
 * Simple class for representing Htl Script
 */
public class HtlScriptModel {

    private final String path;

    private final String template;

    public HtlScriptModel(final String path) {
        this(path, null);
    }

    public HtlScriptModel(final String path, final String template) {
        this.path = path;
        this.template = template;
    }

    public String getPath() {
        return path;
    }

    public String getTemplate() {
        return template;
    }

}
