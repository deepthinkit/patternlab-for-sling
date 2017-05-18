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
import org.kciecierski.patternlab.sling.core.model.page.PatternLabPageModel;
import org.kciecierski.patternlab.sling.core.utils.PatternLabUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PatternComponentModel {

    private static final String TEMPLATE_CALL_PATTERN = "<template data-sly-template.template=\"${@ data}\"><sly data-sly-use.template=\"%s\" data-sly-call=\"${template.%s %s}\"></sly><template>";

    private static final String INCLUDE_PATTERN = "<sly data-sly-include=\"%s\"></sly>";

    private static final String JCR_DATA_PROPERTY = "jcr:data";

    private static final String PROPERTIES = "=data.";

    private static final String PARAMETERS_PREFIX = " @";

    private static final String COMMA = ", ";

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

    private boolean raw;

    public boolean isRaw() {
        return raw;
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

    @PostConstruct
    private void constructPatternComponentModel() {
        id = request.getResource().getName();
        currentPagePath = request.getResource().getParent().getPath();
        raw = getNoMenuFromSelector();
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
        final Resource templateResource = getOrCreateTemplateResource(adminResourceResolver);
        templatePath = templateResource.getPath();
        final Resource templateContentResource = adminResourceResolver.getResource(templateResource, "jcr:content");
        final ModifiableValueMap templateProperties = templateContentResource.adaptTo(ModifiableValueMap.class);
        patternData = StringUtils.isNotBlank(jsonData) ? new ObjectMapper().readValue(jsonData, HashMap.class) : Maps.newHashMap();
        final String templateCall = StringUtils.isNotBlank(template) ? generateTemplateCall(patternData) : generateIncludeCall();
        templateProperties.put(JCR_DATA_PROPERTY, IOUtils.toInputStream(templateCall));
        final ModifiableValueMap patternProperties = adminResourceResolver.getResource(request.getResource().getPath()).adaptTo(ModifiableValueMap.class);
        patternProperties.putAll(patternData);
        adminResourceResolver.commit();

    }

    private Resource getOrCreateTemplateResource(ResourceResolver adminResourceResolver) throws PersistenceException {
        final String resourceTypePath = request.getResource().getResourceType();
        Resource resource = adminResourceResolver.getResource(request.getResource().getResourceType() + "/templates/" + id + ".html");
        if (resource == null) {
            final Resource templatesFolder = adminResourceResolver.getResource(resourceTypePath + "/templates");
            resource = adminResourceResolver.create(templatesFolder, id + ".html", ImmutableMap.of("jcr:primaryType", "nt:file"));
            adminResourceResolver.create(resource, "jcr:content", ImmutableMap.of("jcr:primaryType", "nt:resource", "jcr:mimeType", "text/html", "jcr:data", ""));
            adminResourceResolver.commit();
        }
        return resource;
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

    private boolean getNoMenuFromSelector() {
        final String[] selectors = request.getRequestPathInfo().getSelectors();
        if (selectors != null) {
            for (int i = 0; i < selectors.length; ++i) {
                if (StringUtils.equalsIgnoreCase(selectors[i], PatternLabPageModel.RAW_SELECTOR)) {
                    return true;
                }
            }
        }
        return false;
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
}
