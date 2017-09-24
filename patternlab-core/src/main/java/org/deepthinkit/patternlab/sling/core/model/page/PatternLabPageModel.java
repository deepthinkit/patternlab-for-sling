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
import org.deepthinkit.patternlab.sling.core.model.category.factory.PatternCategoryFactory;
import org.deepthinkit.patternlab.sling.core.model.category.factory.impl.PatternPatternCategoryFactoryImpl;
import org.deepthinkit.patternlab.sling.core.model.page.htlparser.HtlScriptModel;
import org.deepthinkit.patternlab.sling.core.model.page.htlparser.HtlScriptParser;
import org.deepthinkit.patternlab.sling.core.model.pattern.PatternModel;
import org.deepthinkit.patternlab.sling.core.utils.PatternLabConstants;
import org.deepthinkit.patternlab.sling.core.utils.PatternLabUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sling Model responsible for generating, updating and rendering Pattern Lab Page
 */
@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PatternLabPageModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(PatternLabPageModel.class);

    @Inject
    @Via("resource")
    private String patternsPath;

    private String patternId;

    private String currentPagePath;

    private String searchPatternResults;

    private boolean rawMode;

    private List<PatternCategoryModel> categories;

    @Self
    private SlingHttpServletRequest request;

    private PatternCategoryFactory patternCategoryFactory;

    public String getPatternsPath() {
        return patternsPath;
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

    private void constructCategories(Resource pageContentResource) throws IOException {
        categories = Lists.newArrayList();
        final ResourceResolver resourceResolver = pageContentResource.getResourceResolver();
        final Resource patternsPathResource = resourceResolver.getResource(patternsPath);
        final Iterator<Resource> appsIterator = patternsPathResource.listChildren();
        while (appsIterator.hasNext()) {
            final PatternCategoryModel category = patternCategoryFactory.createCategory(appsIterator.next(), patternsPath, patternId);
            if (category != null && category.isValid()) {
                categories.add(category);
            }
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

    private void constructPatternsReferences() {
        final List<PatternModel> patternModels = categories.stream().flatMap(category ->
                getAllCategoryPatterns(category).stream()).collect(Collectors.toList());
        for (PatternModel currentPattern : patternModels) {
            final Set<HtlScriptModel> embeddedPatterns = getEmbeddedPatternPaths(currentPattern);
            for (HtlScriptModel embeddedPattern : embeddedPatterns) {
                addEmbeddedPattern(patternModels, currentPattern, embeddedPattern);
            }
        }
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

    private Set<HtlScriptModel> getEmbeddedPatternPaths(PatternModel pattern) {
        final Set<HtlScriptModel> embeddedPatternPaths = Sets.newHashSet();
        final String[] currentPatternPathElements = StringUtils.split(pattern.getPath(), PatternLabConstants.SLASH);
        final String projectSlingPrefix = currentPatternPathElements[0];
        final String projectName = currentPatternPathElements[1];

        if (StringUtils.isNotBlank(pattern.getTemplate())) {
            final String templateCode = HtlScriptParser.extractHtlTemplateCode(pattern.getTemplate(), pattern.getCode());
            embeddedPatternPaths.addAll(HtlScriptParser.getHtlIncludePaths(templateCode, projectSlingPrefix, projectName, pattern.getPath()));
            embeddedPatternPaths.addAll(HtlScriptParser.getHtlTemplateCallPaths(templateCode, projectSlingPrefix, projectName, pattern.getPath()));
            final String noTemplateCode = HtlScriptParser.extractNoHtlTemplateCode(pattern.getCode());
            embeddedPatternPaths.addAll(HtlScriptParser.getHtlIncludePaths(noTemplateCode, projectSlingPrefix, projectName, pattern.getPath()));
            embeddedPatternPaths.addAll(HtlScriptParser.getHtlTemplateCallPaths(noTemplateCode, projectSlingPrefix, projectName, pattern.getPath()));
        } else {
            embeddedPatternPaths.addAll(HtlScriptParser.getHtlIncludePaths(pattern.getCode(), projectSlingPrefix, projectName, pattern.getPath()));
            embeddedPatternPaths.addAll(HtlScriptParser.getHtlTemplateCallPaths(pattern.getCode(), projectSlingPrefix, projectName, pattern.getPath()));
        }

        return embeddedPatternPaths;
    }

    private void addEmbeddedPattern(List<PatternModel> patternModels, PatternModel currentPattern, HtlScriptModel embeddedPattern) {
        final String comparedPatternId = PatternLabUtils.constructPatternId(embeddedPattern.getPath(), patternsPath, embeddedPattern.getTemplate());
        currentPattern.getEmbeddedPatterns().add(comparedPatternId);

        final String currentPatternId = PatternLabUtils.constructPatternId(currentPattern.getPath(), patternsPath, currentPattern.getTemplate());
        for (PatternModel comparedPattern : patternModels) {
            if (StringUtils.equals(embeddedPattern.getPath(), comparedPattern.getPath())
                    && (StringUtils.isBlank(embeddedPattern.getTemplate()) ||
                    StringUtils.equals(embeddedPattern.getTemplate(), comparedPattern.getTemplate()))) {
                comparedPattern.getIncludingPatterns().add(currentPatternId);
            }
        }
    }


}
