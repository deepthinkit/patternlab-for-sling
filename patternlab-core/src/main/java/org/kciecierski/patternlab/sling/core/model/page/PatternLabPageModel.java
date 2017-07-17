package org.kciecierski.patternlab.sling.core.model.page;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.*;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Via;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.codehaus.jackson.map.ObjectMapper;
import org.kciecierski.patternlab.sling.core.model.category.CategoryModel;
import org.kciecierski.patternlab.sling.core.model.pattern.PatternModel;
import org.kciecierski.patternlab.sling.core.service.api.category.CategoryFactory;
import org.kciecierski.patternlab.sling.core.service.impl.category.CategoryFactoryImpl;
import org.kciecierski.patternlab.sling.core.utils.PatternLabConstants;
import org.kciecierski.patternlab.sling.core.utils.PatternLabUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.kciecierski.patternlab.sling.core.utils.PatternLabConstants.*;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PatternLabPageModel {

    private static final String BACK_PATH = "..";
    public static final String JAVA_WHITESPACE_REGEX = "\r\n|\r|\n";

    @Inject
    @Via("resource")
    private String appsPath;

    private String patternId;

    private String currentPagePath;

    private String searchPatternResults;

    private boolean rawMode;

    private List<CategoryModel> categories;

    @Self
    private SlingHttpServletRequest request;

    @OSGiService
    private ResourceResolverFactory resourceResolverFactory;

    private CategoryFactory categoryFactory;

    public String getAppsPath() {
        return appsPath;
    }

    public String getCurrentPagePath() {
        return currentPagePath;
    }

    public String getPatternId() {
        return patternId;
    }

    public String getSearchPatternResults() {
        return searchPatternResults;
    }

    public boolean isRawMode() {
        return rawMode;
    }

    public List<CategoryModel> getCategories() {
        return categories;
    }

    @PostConstruct
    private void constructPatternLabPageModel() {
        rawMode = getRawSelector();
        patternId = getPatternIdSelector();

        ResourceResolver adminResourceResolver = null;
        try {
            adminResourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
            categoryFactory = new CategoryFactoryImpl(adminResourceResolver);
            final Resource pageContentResource = request.getResource();
            currentPagePath = pageContentResource.getPath();
            constructCategories(pageContentResource, getPatternId());
            createOrUpdatePatternComponents(pageContentResource, getPatternId(), adminResourceResolver);
            constructSearchPatternResults();
            constructPatternsReferences();
        } catch (IOException | LoginException e) {
            e.printStackTrace();
        } finally {
            if (adminResourceResolver != null && adminResourceResolver.isLive()) {
                adminResourceResolver.close();
            }
        }
    }

    private void constructPatternsReferences() {
        final List<PatternModel> patternModels = categories.stream().flatMap(category ->
                getAllCategoryPatterns(category).stream()).collect(Collectors.toList());
        for (PatternModel currentPattern : patternModels) {
            final Set<PatternReferenceModel> embeddedPatterns = getEmbeddedPatternPaths(currentPattern);
            for (PatternReferenceModel embeddedPattern : embeddedPatterns) {
                addEmbeddedPattern(patternModels, currentPattern, embeddedPattern);
            }
        }
    }

    private void addEmbeddedPattern(List<PatternModel> patternModels, PatternModel currentPattern, PatternReferenceModel embeddedPattern) {
        for (PatternModel comparedPattern : patternModels) {
            if (StringUtils.equals(embeddedPattern.getPath(), comparedPattern.getPath())
                    && (StringUtils.isBlank(embeddedPattern.getTemplate()) ||
                    StringUtils.equals(embeddedPattern.getTemplate(), comparedPattern.getTemplate()))) {
                comparedPattern.getIncludingPatterns().add(currentPattern.getId());
                currentPattern.getEmbeddedPatterns().add(comparedPattern.getId());
            }
        }
    }

    private Set<PatternReferenceModel> getEmbeddedPatternPaths(PatternModel pattern) {
        final Set<PatternReferenceModel> embeddedPatternPaths = Sets.newHashSet();
        final String[] currentPatternPathElements = StringUtils.split(pattern.getPath(), PatternLabConstants.SLASH);
        final String projectSlingPrefix = currentPatternPathElements[0];
        final String projectName = currentPatternPathElements[1];

        if (StringUtils.isNotBlank(pattern.getTemplate())) {
            final String templateCode = extractTemplateCode(pattern.getTemplate(), pattern.getCode());
            embeddedPatternPaths.addAll(getHtlIncludePaths(templateCode, projectSlingPrefix, projectName, pattern.getPath()));
            embeddedPatternPaths.addAll(getHtlTemplateCallPaths(templateCode, projectSlingPrefix, projectName, pattern.getPath()));
            final String noTemplateCode = extractNoTemplateCode(pattern.getCode());
            embeddedPatternPaths.addAll(getHtlIncludePaths(noTemplateCode, projectSlingPrefix, projectName, pattern.getPath()));
            embeddedPatternPaths.addAll(getHtlTemplateCallPaths(noTemplateCode, projectSlingPrefix, projectName, pattern.getPath()));
        } else {
            embeddedPatternPaths.addAll(getHtlIncludePaths(pattern.getCode(), projectSlingPrefix, projectName, pattern.getPath()));
            embeddedPatternPaths.addAll(getHtlTemplateCallPaths(pattern.getCode(), projectSlingPrefix, projectName, pattern.getPath()));
        }

        return embeddedPatternPaths;
    }

    private String extractNoTemplateCode(String patternCode) {
        String noTemplateCode = StringUtils.EMPTY;
        final Matcher matcher = DATA_SLY_TEMPLATE_TAG_PATTERN.matcher(patternCode);
        int index = 0;
        while (matcher.find()) {
            noTemplateCode += StringUtils.substring(patternCode, index, matcher.start());
            index = matcher.end();
        }
        return noTemplateCode;
    }

    private String extractTemplateCode(String template, String patternCode) {
        final Matcher matcher = PatternLabConstants.DATA_SLY_TEMPLATE_TAG_PATTERN.matcher(patternCode.replaceAll(JAVA_WHITESPACE_REGEX, " "));
        while (matcher.find()) {
            final String matchedTemplate = matcher.group(2);
            if (StringUtils.equals(matchedTemplate, template)) {
                return matcher.group(0);
            }
        }

        return StringUtils.EMPTY;
    }

    private Set<PatternReferenceModel> getHtlTemplateCallPaths(String patternCode, String projectSlingPrefix, String projectName, String currentPatternPath) {
        final Set<PatternReferenceModel> htlTemplateUsePaths = Sets.newHashSet();
        final Matcher matcher = DATA_SLY_CALL_TAG_PATTERN.matcher(patternCode);
        while (matcher.find()) {
            final String templatePathUseName = matcher.group(1);
            final String templateName = matcher.group(2);
            final String templateUsePath = getUsePathForTemplate(StringUtils.substring(patternCode, 0, matcher.start()), templatePathUseName);
            final String path = resolveAbsolutePathToPattern(templateUsePath, projectSlingPrefix, projectName, currentPatternPath);
            htlTemplateUsePaths.add(new PatternReferenceModel(path, templateName));
        }

        return htlTemplateUsePaths;
    }

    private String getUsePathForTemplate(String patternCode, String templateName) {
        String path = StringUtils.EMPTY;
        Pattern pattern = Pattern.compile(String.format(PatternLabConstants.DATA_SLY_USE_TEMPLATE_PATTERN, templateName));
        final Matcher matcher = pattern.matcher(patternCode);
        while (matcher.find()) {
            path = matcher.group(1);
        }
        return path;
    }

    private Set<PatternReferenceModel> getHtlIncludePaths(String patternCode, String projectSlingPrefix, String projectName, String currentPatternPath) {
        final Set<PatternReferenceModel> htlIncludesForPatterns = Sets.newHashSet();
        final Matcher matcher = DATA_SLY_INCLUDE_PATTERN.matcher(patternCode);
        while (matcher.find()) {
            final String path = resolveAbsolutePathToPattern(matcher.group(1), projectSlingPrefix, projectName, currentPatternPath);
            htlIncludesForPatterns.add(new PatternReferenceModel(path));
        }

        return htlIncludesForPatterns;
    }

    private String resolveAbsolutePathToPattern(String path, String projectSlingPrefix, String projectName, String currentPatternPath) {
        if (StringUtils.startsWith(path, projectSlingPrefix + PatternLabConstants.SLASH + projectName)
                || StringUtils.startsWith(path, projectSlingPrefix + PatternLabConstants.SLASH + projectName)) {
            return path;
        } else if (StringUtils.startsWith(path, projectName)) {
            return PatternLabConstants.SLASH + projectSlingPrefix + PatternLabConstants.SLASH + path;
        }
        String[] pathElements = StringUtils.split(path, PatternLabConstants.SLASH);
        String absolutePath = StringUtils.substringBeforeLast(currentPatternPath, PatternLabConstants.SLASH);
        for (String pathElement : pathElements) {
            if (StringUtils.equals(pathElement, BACK_PATH)) {
                absolutePath = StringUtils.substringBeforeLast(currentPatternPath, PatternLabConstants.SLASH);
            } else {
                absolutePath += PatternLabConstants.SLASH + pathElement;
            }
        }

        return absolutePath;
    }

    private List<PatternModel> getAllCategoryPatterns(CategoryModel category) {
        if (category.getSubCategories().size() > 0) {
            List<PatternModel> subCategoryPatterns = category.getSubCategories().stream().flatMap(subCategory ->
                    getAllCategoryPatterns(subCategory).stream()).collect(Collectors.toList());
            subCategoryPatterns.addAll(category.getPatterns());
            return subCategoryPatterns;
        } else {
            return category.getPatterns();
        }
    }

    private void constructSearchPatternResults() throws IOException {
        List<String> patternIds = categories.stream().flatMap(category -> constructSearchPatternsResult(category).stream()).collect(Collectors.toList());
        searchPatternResults = new ObjectMapper().writeValueAsString(patternIds);
    }

    private List<String> constructSearchPatternsResult(CategoryModel categoryModel) {
        List<String> categoryResults = Lists.newArrayList();
        categoryResults.add(categoryModel.getId());
        categoryResults.addAll(categoryModel.getPatterns().stream().map(PatternModel::getId).collect(Collectors.toList()));
        categoryModel.getSubCategories().stream().forEach(category -> categoryResults.addAll(constructSearchPatternsResult(category)));
        return categoryResults;
    }

    private void createOrUpdatePatternComponents(Resource pageContentResource, String patternId, ResourceResolver adminResourceResolver) throws IOException {
        for (CategoryModel category : categories) {
            createOrUpdatePatternComponents(category, pageContentResource, patternId, adminResourceResolver);
        }
    }

    private void createOrUpdatePatternComponents(CategoryModel category, Resource pageContentResource, String patternId, ResourceResolver adminResourceResolver) throws IOException {
        for (PatternModel pattern : category.getPatterns()) {
            if (StringUtils.isBlank(patternId) || StringUtils.startsWith(pattern.getId(), patternId)) {
                createOrUpdatePatternComponent(pattern, pageContentResource, adminResourceResolver);
            }
        }
        for (CategoryModel subCategory : category.getSubCategories()) {
            createOrUpdatePatternComponents(subCategory, pageContentResource, patternId, adminResourceResolver);
        }

    }

    private void createOrUpdatePatternComponent(PatternModel pattern, Resource pageContentResource, ResourceResolver adminResourceResolver) throws IOException {
        final String componentName = pattern.getId();
        if (pageContentResource.getChild(componentName) == null) {
            adminResourceResolver.create(pageContentResource, componentName, Maps.newHashMap());
            adminResourceResolver.commit();
        }
        final Resource patternResource = adminResourceResolver.getResource(pageContentResource, componentName);
        final ModifiableValueMap patternProperties = patternResource.adaptTo(ModifiableValueMap.class);
        patternProperties.put(PatternLabUtils.SLING_RESOURCE_TYPE, PATTERN_COMPONENT_RESOURCE_TYPE);
        patternProperties.put(NAME_PROPERTY, pattern.getName());
        patternProperties.put(TEMPLATE_PROPERTY, pattern.getTemplate());
        patternProperties.put(DATA_PROPERTY, pattern.getDataPath());
        patternProperties.put(PATH_PROPERTY, pattern.getPath());
        adminResourceResolver.commit();
    }

    private boolean getRawSelector() {
        final String[] selectors = request.getRequestPathInfo().getSelectors();
        if (selectors != null) {
            for (String selector : selectors) {
                if (StringUtils.equalsIgnoreCase(selector, RAW_SELECTOR)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getPatternIdSelector() {
        final String[] selectors = request.getRequestPathInfo().getSelectors();
        if (selectors != null) {
            for (int i = 0; i < selectors.length; ++i) {
                if (StringUtils.equalsIgnoreCase(selectors[i], PATTERN_SELECTOR) && i + 1 < selectors.length) {
                    return selectors[i + 1];
                }
            }
        }
        return null;
    }

    private void constructCategories(Resource pageContentResource, String patternId) throws IOException {
        categories = Lists.newArrayList();
        final ResourceResolver resourceResolver = pageContentResource.getResourceResolver();
        final Resource appsPathResource = resourceResolver.getResource(appsPath);
        final Iterator<Resource> appsIterator = appsPathResource.listChildren();
        while (appsIterator.hasNext()) {
            final CategoryModel category = categoryFactory.createCategory(appsIterator.next(), appsPath, patternId);
            if (category != null && category.isValid()) {
                categories.add(category);
            }
        }
    }

}
