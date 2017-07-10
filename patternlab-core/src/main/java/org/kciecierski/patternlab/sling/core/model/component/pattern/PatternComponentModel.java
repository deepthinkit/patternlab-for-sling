package org.kciecierski.patternlab.sling.core.model.component.pattern;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.*;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Via;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.codehaus.jackson.map.ObjectMapper;
import org.kciecierski.patternlab.sling.core.utils.PatternLabConstants;
import org.kciecierski.patternlab.sling.core.utils.PatternLabUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.kciecierski.patternlab.sling.core.utils.PatternLabConstants.*;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PatternComponentModel {

    @Self
    private SlingHttpServletRequest request;

    @OSGiService
    private ResourceResolverFactory resourceResolverFactory;

    @Inject
    @Via("resource")
    private String name;

    @Inject
    @Via("resource")
    private String template;

    @Inject
    @Via("resource")
    private String path;

    @Inject
    @Via("resource")
    private String data;

    private String id;

    private String templatePath;

    private Map<String, Object> patternData;

    private String currentPagePath;

    private boolean rawMode;

    public boolean isRawMode() {
        return rawMode;
    }

    public boolean isTemplate() {
        return isInclude() && StringUtils.isNotBlank(template);
    }

    public boolean isInclude() {
        return StringUtils.endsWith(path, ".html");
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public Map<String, Object> getPatternData() {
        return patternData;
    }

    public String getCurrentPagePath() {
        return currentPagePath;
    }

    @PostConstruct
    private void constructPatternComponentModel() {
        id = request.getResource().getName();
        currentPagePath = request.getResource().getParent().getPath();
        rawMode = isRawSelectorPresent();
        ResourceResolver adminResourceResolver = null;
        try {
            String jsonData = StringUtils.isNotBlank(data) ?
                    PatternLabUtils.getDataFromFile(data, request.getResourceResolver()) : StringUtils.EMPTY;

            if (isInclude()) {
                adminResourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
                constructPattern(jsonData, adminResourceResolver);
            }
        } catch (IOException | LoginException e) {
            e.printStackTrace();
        } finally {
            if (adminResourceResolver != null && adminResourceResolver.isLive()) {
                adminResourceResolver.close();
            }
        }
    }

    private void constructPattern(String jsonData, ResourceResolver adminResourceResolver) throws IOException {
        final Resource templateResource = getOrCreatePatternResource(adminResourceResolver);
        templatePath = templateResource.getPath();
        final Resource templateContentResource = adminResourceResolver.getResource(templateResource, PatternLabConstants.JCR_CONTENT);
        final ModifiableValueMap templateProperties = templateContentResource.adaptTo(ModifiableValueMap.class);
        patternData = StringUtils.isNotBlank(jsonData) ? new ObjectMapper().readValue(jsonData, HashMap.class) : Maps.newHashMap();
        final String patternCall = StringUtils.isNotBlank(template) ? generateTemplateCall(patternData) : generateIncludeCall();
        templateProperties.put(JCR_DATA_PROPERTY, IOUtils.toInputStream(patternCall));
        final ModifiableValueMap patternProperties = adminResourceResolver.getResource(request.getResource().getPath()).adaptTo(ModifiableValueMap.class);
        patternProperties.putAll(patternData);
        adminResourceResolver.commit();
    }

    private Resource getOrCreatePatternResource(ResourceResolver adminResourceResolver) throws PersistenceException {
        final String resourceTypePath = request.getResource().getResourceType();
        final String[] patternPathPartials = StringUtils.split(path, PatternLabConstants.SLASH);
        final String patternProjectSlingPrefix = patternPathPartials[0];
        final String patternProjectName = patternPathPartials[1];
        final String patternTypeName = patternPathPartials[2];
        final String patternPath = buildPatternPath(patternProjectSlingPrefix, patternProjectName, patternTypeName);
        Resource patternResource = adminResourceResolver.getResource(patternPath);
        if (patternResource == null) {
            patternResource = createPatternResource(adminResourceResolver, resourceTypePath, patternProjectSlingPrefix, patternProjectName, patternTypeName);
        }
        return patternResource;
    }

    private Resource createPatternResource(ResourceResolver adminResourceResolver, String resourceTypePath, String slingPrefix, String patternProjectName, String patternTypeName) throws PersistenceException {
        final Resource templatesFolder = adminResourceResolver.getResource(resourceTypePath + PatternLabConstants.SLASH + PatternLabConstants.TEMPLATES);
        final Resource slingPrefixFolderResource = getOrCreateFolder(adminResourceResolver, slingPrefix, templatesFolder);
        final Resource patternProjectFolderResource = getOrCreateFolder(adminResourceResolver, patternProjectName, slingPrefixFolderResource);
        final Resource patternTypeFolderResource = getOrCreateFolder(adminResourceResolver, patternTypeName, patternProjectFolderResource);
        final Resource patternResource = adminResourceResolver.create(patternTypeFolderResource, id + PatternLabConstants.HTML,
                ImmutableMap.of(PatternLabConstants.JCR_PRIMARY_TYPE, PatternLabConstants.NT_FILE));
        adminResourceResolver.create(patternResource, PatternLabConstants.JCR_CONTENT,
                ImmutableMap.of(PatternLabConstants.JCR_PRIMARY_TYPE, PatternLabConstants.NT_RESOURCE, JCR_MIME_TYPE, TEXT_HTML, JCR_DATA, StringUtils.EMPTY));
        adminResourceResolver.commit();
        return patternResource;
    }

    private Resource getOrCreateFolder(ResourceResolver adminResourceResolver, String slingPrefix, Resource templatesFolder) throws PersistenceException {
        Resource slingPrefixFolderResource = templatesFolder.getChild(slingPrefix);
        if (slingPrefixFolderResource == null) {
            slingPrefixFolderResource = adminResourceResolver.create(templatesFolder, slingPrefix,
                    ImmutableMap.of(PatternLabConstants.JCR_PRIMARY_TYPE, PatternLabConstants.SLING_FOLDER));
        }
        return slingPrefixFolderResource;
    }

    private String buildPatternPath(String slingPrefix, String patternProjectName, String patternTypeName) {
        return request.getResource().getResourceType()
                + PatternLabConstants.SLASH + PatternLabConstants.TEMPLATES + PatternLabConstants.SLASH
                + slingPrefix + PatternLabConstants.SLASH + patternProjectName + PatternLabConstants.SLASH
                + patternTypeName + PatternLabConstants.SLASH + id + PatternLabConstants.HTML;
    }

    private String generateIncludeCall() {
        return String.format(INCLUDE_PATTERN, path);
    }

    private String generateTemplateCall(Map<String, Object> jsonData) {
        String parameters = StringUtils.EMPTY;
        final Iterator<Map.Entry<String, Object>> iterator = jsonData.entrySet().iterator();
        while (iterator.hasNext()) {
            final String parameter = iterator.next().getKey();
            parameters += parameter + PROPERTIES + parameter;
            if (iterator.hasNext()) {
                parameters += COMMA;
            }
        }
        if (StringUtils.isNotBlank(parameters)) {
            parameters = PARAMETERS_PREFIX + parameters;
        }
        return String.format(TEMPLATE_CALL_PATTERN, path, template, parameters);
    }

    private boolean isRawSelectorPresent() {
        final String[] selectors = request.getRequestPathInfo().getSelectors();
        if (selectors != null) {
            for (String selector : selectors)
                if (StringUtils.equalsIgnoreCase(selector, RAW_SELECTOR)) {
                    return true;
                }
        }
        return false;
    }

}
