package org.deepthinkit.patternlab.sling.core.model.category.factory;

import org.apache.sling.api.resource.Resource;
import org.deepthinkit.patternlab.sling.core.model.category.PatternCategoryModel;

import java.io.IOException;

/**
 * Responsible for creating hierarchy of Pattern Categories based on repository structure
 */
public interface PatternCategoryFactory {

    PatternCategoryModel createCategory(Resource resource, String appsPath, String patternDisplayed) throws IOException;

    PatternCategoryModel createCategory(Resource resource, String appsPath, String patternDisplayed, PatternCategoryModel parentCategory) throws IOException;
}

