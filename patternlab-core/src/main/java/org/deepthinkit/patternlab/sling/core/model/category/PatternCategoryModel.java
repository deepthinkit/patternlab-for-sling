package org.deepthinkit.patternlab.sling.core.model.category;

import com.google.common.collect.Lists;
import org.deepthinkit.patternlab.sling.core.utils.PatternLabUtils;
import org.deepthinkit.patternlab.sling.core.model.pattern.PatternModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;

import java.util.List;

public class PatternCategoryModel {

    private final List<PatternCategoryModel> subCategories;

    private final List<PatternModel> patterns;

    private final List<PatternCategoryModel> breadcrumb;

    private final String name;

    private final String id;

    private final PatternCategoryModel parentCategory;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public PatternCategoryModel(Resource resource, String patternsPath, List<PatternCategoryModel> subCategories, List<PatternModel> patterns, PatternCategoryModel parentCategory) {
        this.subCategories = subCategories;
        this.id = PatternLabUtils.constructPatternId(resource.getPath(), patternsPath);
        this.name = StringUtils.lowerCase(PatternLabUtils.getResourceTitleOrName(resource));
        this.parentCategory = parentCategory;
        this.breadcrumb = constructBreadcrumb();
        this.patterns = patterns;
    }

    public List<PatternCategoryModel> getSubCategories() {
        return subCategories;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(name) && StringUtils.isNotBlank(id) && (CollectionUtils.isNotEmpty(patterns) || anySubCategoryDisplayed());
    }

    public boolean isDisplayed() {
        return patterns.stream().anyMatch(PatternModel::isDisplayed) || subCategories.stream().anyMatch(PatternCategoryModel::isDisplayed);
    }

    public PatternCategoryModel getParentCategory() {
        return parentCategory;
    }

    private boolean anySubCategoryDisplayed() {
        for (PatternCategoryModel subCategory : subCategories) {
            if (subCategory.isValid()) {
                return true;
            }
        }
        return false;
    }

    private List<PatternCategoryModel> constructBreadcrumb() {
        List<PatternCategoryModel> patternBreadcrumb = Lists.newArrayList(this);
        PatternCategoryModel patternParentCategory = getParentCategory();
        while (patternParentCategory != null) {
            patternBreadcrumb.add(patternParentCategory);
            patternParentCategory = patternParentCategory.getParentCategory();
        }
        return Lists.reverse(patternBreadcrumb);
    }


    public List<PatternModel> getPatterns() {
        return patterns;
    }

    public List<PatternCategoryModel> getBreadcrumb() {
        return breadcrumb;
    }
}