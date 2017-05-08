package org.patternlab.sling.core.service.api.category;

import org.patternlab.sling.core.model.category.CategoryModel;
import org.apache.sling.api.resource.Resource;

/**
 * Created by Kamil Ciecierski on 4/24/2017.
 */
public interface CategoryFactory {

    CategoryModel createCategory(Resource resource, String appsPath, String patternDisplayed);

    CategoryModel createCategory(Resource resource, String appsPath, String patternDisplayed, CategoryModel parentCategory);
}
