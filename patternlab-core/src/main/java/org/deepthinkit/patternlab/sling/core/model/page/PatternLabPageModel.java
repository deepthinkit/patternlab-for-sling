package org.deepthinkit.patternlab.sling.core.model.page;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Via;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.codehaus.jackson.map.ObjectMapper;
import org.deepthinkit.patternlab.sling.core.model.category.PatternCategoryModel;
import org.deepthinkit.patternlab.sling.core.utils.PatternLabConstants;
import org.deepthinkit.patternlab.sling.core.utils.PatternLabUtils;
import org.deepthinkit.patternlab.sling.core.model.category.factory.PatternCategoryFactory;
import org.deepthinkit.patternlab.sling.core.model.category.factory.impl.PatternPatternCategoryFactoryImpl;
import org.deepthinkit.patternlab.sling.core.model.pattern.PatternModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Sling Model responsible for generating, updating and rendering Pattern Lab Page
 */
@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PatternLabPageModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(PatternLabPageModel.class);

    private static final String FILESYSTEM_BACK_PATH = "..";

    private static final String JAVA_WHITESPACE_REGEX = "\r\n|\r|\n";

    private static final String SINGLE_WHITESPACE = " ";

    @Inject
    @Via("resource")
    private String appsPath;

    private String patternId;

    private String currentPagePath;

    private String searchPatternResults;

    private boolean rawMode;

    private List<PatternCategoryModel> categories;

    @Self
    private SlingHttpServletRequest request;

    private PatternCategoryFactory patternCategoryFactory;

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

    public List<PatternCategoryModel> getCategories() {
        return categories;
    }

    @PostConstruct
    private void constructPatternLabPageModel() {
        final Resource pageContentResource = request.getResource();
        rawMode = PatternLabUtils.isRawSelectorPresent(request);
        patternId = PatternLabUtils.getPatternIdFromSelector(request);
        patternCategoryFactory = new PatternPatternCategoryFactoryImpl(request.getResourceResolver());
        currentPagePath = pageContentResource.getPath();

        try {
            constructCategories(pageContentResource);
            constructSearchPatternResults();
            constructPatternsReferences();

        } catch (IOException e) {
            LOGGER.error("Error during constructing Pattern Lab page:", e);
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
        StringBuilder noTemplateCode = new StringBuilder(StringUtils.EMPTY);
        final Matcher matcher = PatternLabConstants.DATA_SLY_TEMPLATE_TAG_PATTERN.matcher(patternCode);
        int index = 0;
        while (matcher.find()) {
            noTemplateCode.append(StringUtils.substring(patternCode, index, matcher.start()));
            index = matcher.end();
        }
        return noTemplateCode.toString();
    }

    private String extractTemplateCode(String template, String patternCode) {
        final Matcher matcher = PatternLabConstants.DATA_SLY_TEMPLATE_TAG_PATTERN.matcher(patternCode.replaceAll(JAVA_WHITESPACE_REGEX, SINGLE_WHITESPACE));
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
        final Matcher matcher = PatternLabConstants.DATA_SLY_CALL_TAG_PATTERN.matcher(patternCode);
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
        final Matcher matcher = PatternLabConstants.DATA_SLY_INCLUDE_PATTERN.matcher(patternCode);
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
        StringBuilder absolutePath = new StringBuilder(StringUtils.substringBeforeLast(currentPatternPath, PatternLabConstants.SLASH));
        for (String pathElement : pathElements) {
            if (StringUtils.equals(pathElement, FILESYSTEM_BACK_PATH)) {
                absolutePath = new StringBuilder(StringUtils.substringBeforeLast(currentPatternPath, PatternLabConstants.SLASH));
            } else {
                absolutePath.append(PatternLabConstants.SLASH).append(pathElement);
            }
        }

        return absolutePath.toString();
    }

    private List<PatternModel> getAllCategoryPatterns(PatternCategoryModel category) {
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

    private List<String> constructSearchPatternsResult(PatternCategoryModel patternCategoryModel) {
        List<String> categoryResults = Lists.newArrayList();
        categoryResults.add(patternCategoryModel.getId());
        categoryResults.addAll(patternCategoryModel.getPatterns().stream().map(PatternModel::getId).collect(Collectors.toList()));
        patternCategoryModel.getSubCategories().forEach(category -> categoryResults.addAll(constructSearchPatternsResult(category)));
        return categoryResults;
    }


    private void constructCategories(Resource pageContentResource) throws IOException {
        categories = Lists.newArrayList();
        final ResourceResolver resourceResolver = pageContentResource.getResourceResolver();
        final Resource appsPathResource = resourceResolver.getResource(appsPath);
        final Iterator<Resource> appsIterator = appsPathResource.listChildren();
        while (appsIterator.hasNext()) {
            final PatternCategoryModel category = patternCategoryFactory.createCategory(appsIterator.next(), appsPath, patternId);
            if (category != null && category.isValid()) {
                categories.add(category);
            }
        }
    }

}
