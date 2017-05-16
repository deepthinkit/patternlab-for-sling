package org.kciecierski.patternlab.sling.core.service.api.category;

import org.kciecierski.patternlab.sling.core.model.category.CategoryModel;
import org.apache.sling.api.resource.Resource;

import java.io.IOException;

/**
 * Created by Kamil Ciecierski on 4/24/2017.
 */
public interface CategoryFactory {

    CategoryModel createCategory(Resource resource, String appsPath, String patternDisplayed) throws IOException;

    CategoryModel createCategory(Resource resource, String appsPath, String patternDisplayed, CategoryModel parentCategory) throws IOException;
}
