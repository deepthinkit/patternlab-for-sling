package org.patternlab.sling.core.model.pattern;

import com.google.common.collect.Lists;
import org.patternlab.sling.core.model.breadcrumb.BreadcrumbItemModel;
import org.patternlab.sling.core.utils.PatternLabUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;

import java.util.List;

public class PatternModel {

    private final String id;

    private final String name;

    private final String path;

    private final String template;

    private final String data;

    private final boolean displayed;

    private final List<BreadcrumbItemModel> breadcrumb;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isDisplayed() {
        return displayed;
    }

    public String getTemplate() {
        return template;
    }

    public String getPath() {
        return path;
    }

    public String getData() {
        return data;
    }

    public List<BreadcrumbItemModel> getBreadcrumb() {
        return breadcrumb;
    }

    public PatternModel(Resource resource, String appsPath, String patternId) {
        this.id = PatternLabUtils.constructPatternId(resource, appsPath);
        this.name = StringUtils.lowerCase(PatternLabUtils.getResourceTitleOrName(resource));
        this.template = StringUtils.EMPTY;
        this.path = resource.getPath();
        this.data = StringUtils.EMPTY;
        this.displayed = StringUtils.isBlank(patternId) || StringUtils.startsWith(getId(), patternId);
        this.breadcrumb = Lists.newArrayList(new BreadcrumbItemModel(id, name));
    }

    public PatternModel(Resource resource, String appsPath, String patternId, String jsonDataFile) {
        this.id = PatternLabUtils.constructPatternId(resource, appsPath, jsonDataFile);
        this.name = StringUtils.lowerCase(jsonDataFile);
        this.template = StringUtils.EMPTY;
        this.path = resource.getPath();
        this.data = StringUtils.isNotBlank(jsonDataFile) ? resource.getParent().getPath() + "/" + jsonDataFile : StringUtils.EMPTY;
        this.displayed = StringUtils.isBlank(patternId) || StringUtils.startsWith(getId(), patternId);
        this.breadcrumb = Lists.newArrayList(new BreadcrumbItemModel(PatternLabUtils.constructPatternId(resource, appsPath), PatternLabUtils.getResourceTitleOrName(resource)));
        this.breadcrumb.add(new BreadcrumbItemModel(id, name));
    }

    public PatternModel(Resource resource, String appsPath, String patternId, String jsonDataFile, String templateName) {
        this.id = PatternLabUtils.constructPatternId(resource, appsPath, jsonDataFile, templateName);
        this.name = StringUtils.lowerCase(StringUtils.isBlank(jsonDataFile) ? templateName : jsonDataFile);
        this.template = templateName;
        this.path = resource.getPath();
        this.data = StringUtils.isNotBlank(jsonDataFile) ? resource.getParent().getPath() + "/" + jsonDataFile : StringUtils.EMPTY;
        this.displayed = StringUtils.isBlank(patternId) || StringUtils.startsWith(getId(), patternId);
        this.breadcrumb = Lists.newArrayList(new BreadcrumbItemModel(PatternLabUtils.constructPatternId(resource, appsPath), PatternLabUtils.getResourceTitleOrName(resource)));
        if (StringUtils.isNotBlank(jsonDataFile)) {
            this.breadcrumb.add(new BreadcrumbItemModel(PatternLabUtils.constructPatternId(resource, appsPath, StringUtils.EMPTY, templateName), templateName));
        }
        this.breadcrumb.add(new BreadcrumbItemModel(id, name));
    }


}