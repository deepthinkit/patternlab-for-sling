package org.deepthinkit.patternlab.sling.core.utils;

import java.util.regex.Pattern;

/**
 * Class with contants used across project
 */
public final class PatternLabConstants {

    public static final String SLASH = "/";

    public static final String RAW_SELECTOR = "raw";

    public static final String PATTERN_SELECTOR = "pattern";

    public static final String NAME_PROPERTY = "name";

    public static final String PATH_PROPERTY = "path";

    public static final String TEMPLATE_PROPERTY = "template";

    public static final String DATA_PROPERTY = "data";

    public static final String PATTERN_ID_REPLACEMENT = "-";

    public static final String PATTERN_RESOURCE_TYPE = "/apps/patternlab/components/pattern";

    public static final String JCR_PRIMARY_TYPE = "jcr:primaryType";

    public static final String NT_FILE = "nt:file";

    public static final String HTML_EXT = ".html";

    public static final String DATA_EXT = ".json";

    public static final String DESCRIPTION_EXT = ".md";

    public static final String SELECTOR = ".";

    public static final String NT_FOLDER = "nt:folder";

    public static final String SLING_FOLDER = "sling:Folder";

    public static final String SLING_ORDERED_FOLDER = "sling:OrderedFolder";

    public static final String SLING_RESOURCE_TYPE = "sling:resourceType";

    public static final String JCR_TITLE = "jcr:title";

    public static final String JCR_DATA = "jcr:data";

    public static final String JCR_CONTENT = "jcr:content";

    public static final String DATA_SLY_USE_TEMPLATE_PATTERN = "data-sly-use.%s=\"([^\"]*)\"";

    public static final Pattern DATA_SLY_TEMPLATE_PATTERN = Pattern.compile("data-sly-template.([^ =>]*)([^>]*)>");

    public static final Pattern DATA_SLY_TEMPLATE_TAG_PATTERN = Pattern.compile("<template([^>]*)data-sly-template.([^ =>]*)([^>]*)>(?:(?!</template>).)*</template>");

    public static final Pattern DATA_SLY_INCLUDE_PATTERN = Pattern.compile("data-sly-include=\"([^\"]*)\"");

    public static final Pattern DATA_SLY_CALL_TAG_PATTERN = Pattern.compile("data-sly-call=\"\\$\\{([^\\.]*)\\.([^ ]*)([^\\}]*)\\}\"");

    private PatternLabConstants() {
        //constants class
    }
}
