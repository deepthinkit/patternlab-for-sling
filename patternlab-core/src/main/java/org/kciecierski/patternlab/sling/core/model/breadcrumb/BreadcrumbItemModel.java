package org.kciecierski.patternlab.sling.core.model.breadcrumb;

/**
 * Created by Kamil Ciecierski on 5/6/2017.
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
