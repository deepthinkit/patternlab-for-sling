package org.deepthinkit.patternlab.sling.core.model.page;

/**
 * Simple class for representing referenced Pattern
 */
public class PatternReferenceModel {

    private final String path;

    private final String template;

    public PatternReferenceModel(final String path) {
        this(path, null);
    }

    public PatternReferenceModel(final String path, final String template) {
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
