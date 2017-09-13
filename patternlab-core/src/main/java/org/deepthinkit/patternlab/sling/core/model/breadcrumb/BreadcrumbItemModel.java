package org.deepthinkit.patternlab.sling.core.model.breadcrumb;

/**
 * Simple class for representing Pattern Lab Page Breadcrumb elements
 */
public class BreadcrumbItemModel {

    private final String id;

    private final String name;

    public BreadcrumbItemModel(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
