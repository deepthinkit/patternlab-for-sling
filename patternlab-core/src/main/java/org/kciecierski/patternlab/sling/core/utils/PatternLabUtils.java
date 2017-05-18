package org.kciecierski.patternlab.sling.core.utils;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Kamil Ciecierski on 4/23/2017.
 */
public final class PatternLabUtils {

    public static final String SLING_RESOURCE_TYPE = "sling:resourceType";

    private static final String JCR_TITLE = "jcr:title";

    private static final String JCR_DATA = "jcr:data";

    private static final String JCR_CONTENT = "jcr:content";

    private static final String SLASH = "/";

    public static String constructPatternId(Resource resource, String appsPath, String jsonDataFileName, String templateName) {
        String patternId = StringUtils.substringAfter(resource.getPath(), appsPath + "/");
        if (StringUtils.endsWith(patternId, ".html")) {
            patternId = StringUtils.substringBeforeLast(patternId, ".html");
        }
        if (StringUtils.isNotBlank(templateName)) {
            patternId += "/" + templateName;
        }
        final String fileName = StringUtils.substringBeforeLast(resource.getName(), ".html");
        final String dataFileSuffix = StringUtils.substringBetween(jsonDataFileName, fileName + ".", ".js");
        if (StringUtils.isNotBlank(dataFileSuffix)) {
            patternId += "/" + dataFileSuffix;

        }
        return StringUtils.replace(patternId, SLASH, "-");
    }

    public static String constructPatternId(Resource resource, String appsPath) {
        return constructPatternId(resource, appsPath, StringUtils.EMPTY, StringUtils.EMPTY);
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

}
