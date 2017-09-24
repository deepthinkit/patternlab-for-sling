package org.deepthinkit.patternlab.sling.core.model.category.factory.impl;

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.deepthinkit.patternlab.sling.core.model.category.PatternCategoryModel;
import org.deepthinkit.patternlab.sling.core.utils.PatternLabConstants;
import org.deepthinkit.patternlab.sling.core.utils.PatternLabUtils;
import org.deepthinkit.patternlab.sling.core.model.category.factory.PatternCategoryFactory;
import org.deepthinkit.patternlab.sling.core.model.pattern.PatternModel;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;

public class PatternPatternCategoryFactoryImpl implements PatternCategoryFactory {

    private final ResourceResolver resourceResolver;

    public PatternPatternCategoryFactoryImpl(ResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    public PatternCategoryModel createCategory(Resource resource, String patternsPath, String patternId) throws IOException {
        return createCategory(resource, patternsPath, patternId, null);
    }

    public PatternCategoryModel createCategory(Resource resource, String patternsPath, String patternId, PatternCategoryModel parentCategory) throws IOException {
        if (isFolder(resource)) {
            final List<PatternCategoryModel> subCategories = Lists.newArrayList();
            final List<PatternModel> patterns = Lists.newArrayList();
            final PatternCategoryModel currentCategory = new PatternCategoryModel(resource, patternsPath, subCategories, patterns, parentCategory);
            final Iterator<Resource> childResources = resource.listChildren();
            while (childResources.hasNext()) {
                updateSubCategoriesAndPatterns(childResources.next(), subCategories, patterns, currentCategory, patternsPath, patternId);
            }
            return currentCategory;
        }
        return null;
    }

    private void updateSubCategoriesAndPatterns(Resource resource, List<PatternCategoryModel> subCategories, List<PatternModel> patterns,
                                                PatternCategoryModel parentCategory, String patternsPath, String patternId) throws IOException {
        if (isHtlFile(resource)) {
            final List<String> templateNames = retrieveTemplatesFromFile(resource);
            if (CollectionUtils.isEmpty(templateNames)) {
                patterns.add(new PatternModel(resource, patternsPath, patternId, this.resourceResolver));
            } else {
                patterns.addAll(getTemplatesPatterns(resource, patternsPath, patternId, templateNames));
            }
        } else if (isFolder(resource)) {
            final PatternCategoryModel category = createCategory(resource, patternsPath, patternId, parentCategory);
            if (category != null && category.isValid()) {
                subCategories.add(category);
            }
        }
    }

    private List<PatternModel> getTemplatesPatterns(Resource resource, String patternsPath, String patternId,
                                                    List<String> templateNames) throws IOException {
        List<PatternModel> templatesPatterns = Lists.newArrayList();
        final Resource folderResource = resource.getParent();
        final List<String> jsonDataFiles = retrieveJsonDataFilesInFolder(folderResource, StringUtils.substringBefore(resource.getName(), PatternLabConstants.HTML_EXT));
        for (String templateName : templateNames) {
            final List<String> templateDedicatedDataFiles = getTemplateDedicatedFiles(templateName, jsonDataFiles, templateNames);
            if (CollectionUtils.isEmpty(templateDedicatedDataFiles)) {
                templatesPatterns.add(new PatternModel(resource, patternsPath, patternId, StringUtils.EMPTY, templateName, this.resourceResolver));
            } else {
                for (String jsonDataFileName : templateDedicatedDataFiles) {
                    templatesPatterns.add(new PatternModel(resource, patternsPath, patternId, jsonDataFileName, templateName, this.resourceResolver));
                }
            }
        }
        return templatesPatterns;
    }

    private List<String> getTemplateDedicatedFiles(String templateName, List<String> jsonDataFiles, List<String> templateNames) {
        final List<String> templateDedicatedDataFiles = Lists.newArrayList();
        for (String jsonDataFile : jsonDataFiles) {
            final String[] jsonDataIds = StringUtils.substringsBetween(jsonDataFile, PatternLabConstants.SELECTOR, PatternLabConstants.SELECTOR);
            if (jsonDataIds == null || jsonDataIds.length == 0) {
                templateDedicatedDataFiles.add(jsonDataFile);
            } else {
                final String jsonDataId = jsonDataIds[0];
                if (StringUtils.equalsIgnoreCase(templateName, jsonDataId) || !templateNames.contains(jsonDataId)) {
                    templateDedicatedDataFiles.add(jsonDataFile);
                }
            }
        }
        return templateDedicatedDataFiles;
    }

    private boolean isHtlFile(Resource resource) {
        return StringUtils.equals(getPrimaryType(resource), PatternLabConstants.NT_FILE) && resource.getName().endsWith(PatternLabConstants.HTML_EXT);
    }

    private boolean isFolder(Resource resource) {
        final String primaryType = getPrimaryType(resource);
        return StringUtils.equals(primaryType, PatternLabConstants.NT_FOLDER) || StringUtils.equals(primaryType, PatternLabConstants.SLING_FOLDER) || StringUtils.endsWith(primaryType, PatternLabConstants.SLING_ORDERED_FOLDER);
    }

    private List<String> retrieveJsonDataFilesInFolder(Resource filesParentResource, String name) {
        final List<String> patternJsonDataFiles = Lists.newArrayList();
        final Iterator<Resource> children = filesParentResource.listChildren();
        while (children.hasNext()) {
            final Resource childResource = children.next();
            final String childResourceName = childResource.getName();
            if (StringUtils.startsWith(childResourceName, name) && StringUtils.endsWith(childResourceName, PatternLabConstants.DATA_EXT)) {
                patternJsonDataFiles.add(childResourceName);
            }
        }
        return patternJsonDataFiles;
    }


    private List<String> retrieveTemplatesFromFile(Resource resource) throws IOException {
        final String fileContent = PatternLabUtils.getDataFromFile(resource);
        final List<String> templates = Lists.newArrayList();

        final Matcher matcher = PatternLabConstants.DATA_SLY_TEMPLATE_PATTERN.matcher(fileContent);
        while (matcher.find()) {
            templates.add(matcher.group(1));
        }
        return templates;
    }

    private String getPrimaryType(Resource resource) {
        final ValueMap resourceProperties = resource.adaptTo(ValueMap.class);
        return resourceProperties.get(PatternLabConstants.JCR_PRIMARY_TYPE, String.class);
    }

}
