package org.kciecierski.patternlab.sling.core.model.page;

/**
 * Created by Kamil Ciecierski on 14-Jul-17.
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
