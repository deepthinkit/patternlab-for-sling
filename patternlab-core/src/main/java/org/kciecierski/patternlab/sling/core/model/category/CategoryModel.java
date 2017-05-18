package org.kciecierski.patternlab.sling.core.model.category;

import com.google.common.collect.Lists;
import org.kciecierski.patternlab.sling.core.model.pattern.PatternModel;
import org.kciecierski.patternlab.sling.core.utils.PatternLabUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;

import java.util.List;

public class CategoryModel {

    private final List<CategoryModel> subCategories;

    private final List<PatternModel> patterns;

    private final List<CategoryModel> breadcrumb;

    private final String name;

    private final String id;

    private final CategoryModel parentCategory;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public CategoryModel(Resource resource, String appsPath, List<CategoryModel> subCategories, List<PatternModel> patterns, String patternId, CategoryModel parentCategory) {
        this.subCategories = subCategories;
        this.id = PatternLabUtils.constructPatternId(resource, appsPath);
        this.name = StringUtils.lowerCase(PatternLabUtils.getResourceTitleOrName(resource));
        this.parentCategory = parentCategory;
        this.breadcrumb = constructBreadcrumb();
        this.patterns = patterns;
    }

    public List<CategoryModel> getSubCategories() {
        return subCategories;
    }

    public boolean isValid() {
        return StringUtils.isNotBlank(name) && StringUtils.isNotBlank(id) && (CollectionUtils.isNotEmpty(patterns) || anySubCategoryDisplayed());
    }

    public boolean isDisplayed() {
        return patterns.stream().anyMatch(PatternModel::isDisplayed) || subCategories.stream().anyMatch(CategoryModel::isDisplayed);
    }

    public CategoryModel getParentCategory() {
        return parentCategory;
    }

    private boolean anySubCategoryDisplayed() {
        for (CategoryModel subCategory : subCategories) {
            if (subCategory.isValid()) {
                return true;
            }
        }
        return false;
    }

    private List<CategoryModel> constructBreadcrumb() {
        List<CategoryModel> patternBreadcrumb = Lists.newArrayList(this);
        CategoryModel patternParentCategory = getParentCategory();
        while (patternParentCategory != null) {
            patternBreadcrumb.add(patternParentCategory);
            patternParentCategory = patternParentCategory.getParentCategory();
        }
        return Lists.reverse(patternBreadcrumb);
    }


    public List<PatternModel> getPatterns() {
        return patterns;
    }

    public List<CategoryModel> getBreadcrumb() {
        return breadcrumb;
    }
}