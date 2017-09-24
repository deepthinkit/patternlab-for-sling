package org.deepthinkit.patternlab.sling.core.model.pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.deepthinkit.patternlab.sling.core.model.breadcrumb.BreadcrumbItemModel;
import org.deepthinkit.patternlab.sling.core.utils.PatternLabConstants;
import org.deepthinkit.patternlab.sling.core.utils.PatternLabUtils;

import java.io.IOException;
import java.util.List;
import java.util.Set;


/**
 * Class for storing Pattern details displayed on page
 */
public class PatternModel {

    private final String id;

    private final String name;

    private final String path;

    private final String template;

    private final String dataPath;

    private final String code;

    private final String data;

    private final String description;

    private final boolean displayed;

    private final List<BreadcrumbItemModel> breadcrumb;

    private final Set<String> embeddedPatterns;

    private final Set<String> includingPatterns;

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

    public String getDataPath() {
        return dataPath;
    }

    public List<BreadcrumbItemModel> getBreadcrumb() {
        return breadcrumb;
    }

    public String getCode() {
        return code;
    }

    public String getData() {
        return data;
    }

    public String getDescription() {
        return description;
    }

    public Set<String> getEmbeddedPatterns() {
        return embeddedPatterns;
    }

    public Set<String> getIncludingPatterns() {
        return includingPatterns;
    }

    public PatternModel(Resource resource, String patternsPath, String patternId, ResourceResolver resourceResolver) throws IOException {
        this.id = PatternLabUtils.constructPatternId(resource.getPath(), patternsPath);
        this.name = StringUtils.lowerCase(PatternLabUtils.getResourceTitleOrName(resource));
        this.template = StringUtils.EMPTY;
        this.path = resource.getPath();
        this.dataPath = StringUtils.EMPTY;
        this.displayed = StringUtils.isBlank(patternId) || StringUtils.startsWith(getId(), patternId);
        this.breadcrumb = Lists.newArrayList(new BreadcrumbItemModel(id, name));
        this.code = PatternLabUtils.getDataFromFile(path, resourceResolver);
        this.data = null;
        final String descriptionPath = StringUtils.substringBeforeLast(path, PatternLabConstants.SELECTOR) + PatternLabConstants.DESCRIPTION_EXT;
        this.description = PatternLabUtils.getDataFromFile(descriptionPath, resourceResolver);
        this.embeddedPatterns = Sets.newHashSet();
        this.includingPatterns = Sets.newHashSet();
    }

    public PatternModel(Resource resource, String patternsPath, String patternId, String jsonDataFile, String templateName, ResourceResolver resourceResolver) throws IOException {
        this.id = PatternLabUtils.constructPatternId(resource.getPath(), patternsPath, templateName, jsonDataFile);
        this.name = StringUtils.lowerCase(StringUtils.isBlank(jsonDataFile) ? templateName : jsonDataFile);
        this.template = templateName;
        this.path = resource.getPath();
        this.dataPath = StringUtils.isNotBlank(jsonDataFile) ? resource.getParent().getPath() + PatternLabConstants.SLASH + jsonDataFile : StringUtils.EMPTY;
        this.displayed = StringUtils.isBlank(patternId) || StringUtils.startsWith(getId(), patternId);
        this.breadcrumb = Lists.newArrayList(new BreadcrumbItemModel(PatternLabUtils.constructPatternId(resource.getPath(), patternsPath), PatternLabUtils.getResourceTitleOrName(resource)));
        if (StringUtils.isNotBlank(jsonDataFile)) {
            this.breadcrumb.add(new BreadcrumbItemModel(PatternLabUtils.constructPatternId(resource.getPath(), patternsPath, templateName), templateName));
        }
        this.breadcrumb.add(new BreadcrumbItemModel(id, name));
        this.code = PatternLabUtils.getDataFromFile(path, resourceResolver);
        this.data = PatternLabUtils.getDataFromFile(dataPath, resourceResolver);
        this.description = constructDescription(jsonDataFile, templateName, resourceResolver);
        this.embeddedPatterns = Sets.newHashSet();
        this.includingPatterns = Sets.newHashSet();
    }

    private String constructDescription(String jsonDataFile, String templateName, ResourceResolver resourceResolver) throws IOException {
        String description = null;
        if (StringUtils.isNotBlank(jsonDataFile)) {
            final String descriptionPath = StringUtils.substringBeforeLast(dataPath, PatternLabConstants.SELECTOR) + PatternLabConstants.SELECTOR + templateName + PatternLabConstants.DESCRIPTION_EXT;
            description = PatternLabUtils.getDataFromFile(descriptionPath, resourceResolver);
        }
        if (StringUtils.isBlank(description)) {
            final String descriptionPath = StringUtils.substringBeforeLast(path, PatternLabConstants.SELECTOR) + PatternLabConstants.SELECTOR + templateName + PatternLabConstants.DESCRIPTION_EXT;
            description = PatternLabUtils.getDataFromFile(descriptionPath, resourceResolver);
        }
        if (StringUtils.isBlank(description)) {
            final String descriptionPath = StringUtils.substringBeforeLast(path, PatternLabConstants.SELECTOR) + PatternLabConstants.DESCRIPTION_EXT;
            description = PatternLabUtils.getDataFromFile(descriptionPath, resourceResolver);
        }
        return description;
    }

}