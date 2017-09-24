package org.deepthinkit.patternlab.sling.core.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import java.io.IOException;
import java.io.InputStream;

import static org.deepthinkit.patternlab.sling.core.utils.PatternLabConstants.*;

/**
 * Util class with short methods used across project
 */
public final class PatternLabUtils {

    public static String constructPatternId(String patternResourcePath, String patternsPath, String templateName, String jsonDataFileName) {
        final String patternPath = StringUtils.substringAfter(patternResourcePath, patternsPath + SLASH);
        final StringBuilder patternIdBuilder = new StringBuilder();
        if (StringUtils.endsWith(patternPath, HTML_EXT)) {
            patternIdBuilder.append(StringUtils.substringBeforeLast(patternPath, HTML_EXT));
        } else {
            patternIdBuilder.append(patternPath);
        }
        if (StringUtils.isNotBlank(templateName)) {
            patternIdBuilder.append(SLASH).append(templateName);
        }
        if (StringUtils.isNotBlank(jsonDataFileName)) {
            final String patternFileName = StringUtils.substringAfterLast(patternPath, SLASH);
            final String fileNameWithoutExt = StringUtils.substringBeforeLast(patternFileName, HTML_EXT);
            final String dataFileSuffix = StringUtils.substringBetween(jsonDataFileName, fileNameWithoutExt
                    + SELECTOR, DATA_EXT);
            patternIdBuilder.append(SLASH).append(StringUtils.defaultString(dataFileSuffix, fileNameWithoutExt));
        }
        return StringUtils.replace(patternIdBuilder.toString(), SLASH, PATTERN_ID_REPLACEMENT);
    }

    public static String constructPatternId(String patternResourcePath, String patternsPath, String templateName) {
        return constructPatternId(patternResourcePath, patternsPath, templateName, StringUtils.EMPTY);
    }

    public static String constructPatternId(String patternResourcePath, String patternsPath) {
        return constructPatternId(patternResourcePath, patternsPath, StringUtils.EMPTY, StringUtils.EMPTY);
    }

    public static String getResourceTitleOrName(final Resource resource) {
        final String title = resource.getValueMap().get(JCR_TITLE, String.class);
        return StringUtils.defaultString(title, resource.getName());
    }

    public static String getDataFromFile(String filePath, ResourceResolver resourceResolver) throws IOException {
        if (StringUtils.isBlank(filePath)) {
            return null;
        }
        final Resource dataContentResource = resourceResolver.getResource(filePath + SLASH + JCR_CONTENT);
        if (dataContentResource == null) {
            return null;
        }
        final ValueMap dataProperties = dataContentResource.adaptTo(ValueMap.class);
        final InputStream inputStream = dataProperties.get(JCR_DATA, InputStream.class);
        return IOUtils.toString(inputStream);
    }

    public static String getDataFromFile(Resource resource) throws IOException {
        return getDataFromFile(resource.getPath(), resource.getResourceResolver());
    }

    public static boolean isRawSelectorPresent(SlingHttpServletRequest request) {
        final String[] selectors = request.getRequestPathInfo().getSelectors();
        if (selectors != null) {
            for (String selector : selectors)
                if (StringUtils.equalsIgnoreCase(selector, RAW_SELECTOR)) {
                    return true;
                }
        }
        return false;
    }

    public static String getPatternIdFromSelector(SlingHttpServletRequest request) {
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

    private PatternLabUtils() {
        //util class
    }
}
