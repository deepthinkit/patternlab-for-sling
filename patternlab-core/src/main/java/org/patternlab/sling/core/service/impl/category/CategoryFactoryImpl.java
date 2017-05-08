package org.patternlab.sling.core.service.impl.category;

import com.google.common.collect.Lists;
import org.patternlab.sling.core.model.category.CategoryModel;
import org.patternlab.sling.core.model.pattern.PatternModel;
import org.patternlab.sling.core.service.api.category.CategoryFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Kamil Ciecierski on 4/24/2017.
 */
public class CategoryFactoryImpl implements CategoryFactory {

    private static final String JCR_PRIMARY_TYPE = "jcr:primaryType";

    private static final String NT_FILE = "nt:file";

    private static final String HTML = ".html";

    private static final String JCR_CONTENT = "jcr:content";

    private static final String JCR_DATA = "jcr:data";

    private static final Pattern DATA_SLY_TEMPLATE_PATTERN = Pattern.compile("data-sly-template.([^ =>]*)([^>]*)>");

    private static final String DATA_EXTENSION = ".js";

    private static final String NT_FOLDER = "nt:folder";

    private static final String SLING_FOLDER = "sling:Folder";

    private static final String SLING_ORDERED_FOLDER = "sling:OrderedFolder";


    public CategoryModel createCategory(Resource resource, String appsPath, String patternId) {
        return createCategory(resource, appsPath, patternId, null);
    }

    public CategoryModel createCategory(Resource resource, String appsPath, String patternId, CategoryModel parentCategory) {
        if (isFolder(resource)) {
            List<CategoryModel> subCategories = Lists.newArrayList();
            List<PatternModel> patterns = Lists.newArrayList();
            CategoryModel currentCategory = new CategoryModel(resource, appsPath, subCategories, patterns, patternId, parentCategory);

            final Iterator<Resource> childResources = resource.listChildren();
            while (childResources.hasNext()) {
                updateSubCategoriesAndPatterns(childResources.next(), subCategories, patterns, currentCategory, appsPath, patternId);
            }
            return currentCategory;
        }
        return null;
    }

    private void updateSubCategoriesAndPatterns(Resource resource, List<CategoryModel> subCategories, List<PatternModel> patterns, CategoryModel parentCategory, String appsPath, String patternId) {
        if (isHtlFile(resource)) {
            final List<String> templateNames = extractTemplatesFromFile(resource);
            if (CollectionUtils.isEmpty(templateNames)) {
                patterns.add(new PatternModel(resource, appsPath, patternId));
            } else {
                patterns.addAll(getTemplatesPatterns(resource, appsPath, patternId, parentCategory, templateNames));
            }
        } else if (isFolder(resource)) {
            final CategoryModel category = createCategory(resource, appsPath, patternId, parentCategory);
            if (category != null && category.isValid()) {
                subCategories.add(category);
            }
        }
    }

    private List<PatternModel> getTemplatesPatterns(Resource resource, String appsPath, String patternId, CategoryModel currentCategory, List<String> templateNames) {
        List<PatternModel> templatesPatterns = Lists.newArrayList();
        final Resource folderResource = resource.getParent();
        final List<String> jsonDataFiles = getJsonDataFiles(folderResource, StringUtils.substringBefore(resource.getName(), ".html"));
        for (String templateName : templateNames) {
            final List<String> templateDedicatedDataFiles = getTemplateDedicatedFiles(templateName, jsonDataFiles, templateNames);
            if (CollectionUtils.isEmpty(templateDedicatedDataFiles)) {
                templatesPatterns.add(new PatternModel(resource, appsPath, patternId, StringUtils.EMPTY, templateName));
            } else {
                for (String jsonDataFileName : templateDedicatedDataFiles) {
                    templatesPatterns.add(new PatternModel(resource, appsPath, patternId, jsonDataFileName, templateName));
                }
            }
        }
        return templatesPatterns;
    }

    private List<String> getTemplateDedicatedFiles(String templateName, List<String> jsonDataFiles, List<String> templateNames) {
        final List<String> templateDedicatedDataFiles = Lists.newArrayList();
        for (String jsonDataFile : jsonDataFiles) {
            final String[] jsonDataIds = StringUtils.substringsBetween(jsonDataFile, ".", ".");
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
        return StringUtils.equals(getPrimaryType(resource), NT_FILE) && resource.getName().endsWith(HTML);
    }

    private boolean isFolder(Resource resource) {
        final String primaryType = getPrimaryType(resource);
        return StringUtils.equals(primaryType, NT_FOLDER) || StringUtils.equals(primaryType, SLING_FOLDER) || StringUtils.endsWith(primaryType, SLING_ORDERED_FOLDER);
    }

    private List<String> getJsonDataFiles(Resource filesParentResource, String name) {
        final List<String> componentJsonDataFiles = Lists.newArrayList();
        final Iterator<Resource> children = filesParentResource.listChildren();
        while (children.hasNext()) {
            final Resource childResource = children.next();
            final String childResourceName = childResource.getName();
            if (StringUtils.startsWith(childResourceName, name) && StringUtils.endsWith(childResourceName, DATA_EXTENSION)) {
                componentJsonDataFiles.add(childResourceName);
            }
        }
        return componentJsonDataFiles;
    }


    private List<String> extractTemplatesFromFile(Resource resource) {
        final Resource fileContentResource = resource.getChild(JCR_CONTENT);
        if (fileContentResource != null) {
            final ValueMap fileContentProperties = fileContentResource.adaptTo(ValueMap.class);
            final InputStream jcrData = fileContentProperties.get(JCR_DATA, InputStream.class);
            try {
                final String fileContent = IOUtils.toString(jcrData);
                return extractTemplatesFromFileContent(fileContent);
            } catch (IOException e) {
            }
        }
        return null;
    }

    private List<String> extractTemplatesFromFileContent(String fileContent) {
        final List<String> templates = Lists.newArrayList();

        final Matcher matcher = DATA_SLY_TEMPLATE_PATTERN.matcher(fileContent);
        while (matcher.find()) {
            templates.add(matcher.group(1));
        }
        return templates;
    }


    private String getPrimaryType(Resource resource) {
        final ValueMap resourceProperties = resource.adaptTo(ValueMap.class);
        return resourceProperties.get(JCR_PRIMARY_TYPE, String.class);
    }

}
