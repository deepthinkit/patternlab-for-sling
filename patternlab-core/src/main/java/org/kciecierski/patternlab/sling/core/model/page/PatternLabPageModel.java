package org.kciecierski.patternlab.sling.core.model.page;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.*;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Via;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.kciecierski.patternlab.sling.core.service.impl.category.CategoryFactoryImpl;
import org.kciecierski.patternlab.sling.core.model.category.CategoryModel;
import org.kciecierski.patternlab.sling.core.model.pattern.PatternModel;
import org.kciecierski.patternlab.sling.core.service.api.category.CategoryFactory;
import org.kciecierski.patternlab.sling.core.utils.PatternLabUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

@Model(adaptables = SlingHttpServletRequest.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PatternLabPageModel {

    public static final String NO_MENU_SELECTOR = "raw";

    private static final String PATTERN = "pattern";

    private static final String NAME = "name";

    private static final String PATH = "path";

    private static final String PATTERN_COMPONENT_RESOURCE_TYPE = "/apps/patternlab/components/pattern";

    private static final String TEMPLATE = "template";

    private static final String DATA = "data";

    private CategoryFactory categoryFactory;

    @Inject
    @Via("resource")
    private String appsPath;

    @Self
    private SlingHttpServletRequest request;

    @OSGiService
    private ResourceResolverFactory resourceResolverFactory;

    private List<CategoryModel> categories;

    private boolean noMenu;

    private String patternId;

    private String currentPagePath;

    public String getAppsPath() {
        return appsPath;
    }

    public List<CategoryModel> getCategories() {
        return categories;
    }

    public boolean isNoMenu() {
        return noMenu;
    }

    @PostConstruct
    private void constructPatternLabPageModel() {
        noMenu = getNoMenuFromSelector();
        patternId = getPatternIdFromSelector();

        ResourceResolver adminResourceResolver = null;
        try {
            adminResourceResolver = resourceResolverFactory.getAdministrativeResourceResolver(null);
            categoryFactory = new CategoryFactoryImpl(adminResourceResolver);
            final Resource pageContentResource = request.getResource();
            currentPagePath = pageContentResource.getPath();
            constructCategories(pageContentResource, getPatternId());
            createOrUpdatePatternComponents(pageContentResource, getPatternId(), adminResourceResolver);
        } catch (IOException | LoginException e) {
            e.printStackTrace();
        } finally {
            if (adminResourceResolver != null && adminResourceResolver.isLive()) {
                adminResourceResolver.close();
            }
        }
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
        patternProperties.put(NAME, pattern.getName());
        patternProperties.put(TEMPLATE, pattern.getTemplate());
        patternProperties.put(DATA, pattern.getDataPath());
        patternProperties.put(PATH, pattern.getPath());
        adminResourceResolver.commit();
    }

    private boolean getNoMenuFromSelector() {
        final String[] selectors = request.getRequestPathInfo().getSelectors();
        if (selectors != null) {
            for (int i = 0; i < selectors.length; ++i) {
                if (StringUtils.equalsIgnoreCase(selectors[i], NO_MENU_SELECTOR)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String getPatternIdFromSelector() {
        final String[] selectors = request.getRequestPathInfo().getSelectors();
        if (selectors != null) {
            for (int i = 0; i < selectors.length; ++i) {
                if (StringUtils.equalsIgnoreCase(selectors[i], PATTERN) && i + 1 < selectors.length) {
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

    public String getCurrentPagePath() {
        return currentPagePath;
    }

    public String getPatternId() {
        return patternId;
    }
}
