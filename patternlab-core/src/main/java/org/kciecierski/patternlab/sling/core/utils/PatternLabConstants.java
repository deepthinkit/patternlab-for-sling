package org.kciecierski.patternlab.sling.core.utils;

import java.util.regex.Pattern;

/**
 * Created by Kamil Ciecierski on 5/18/2017.
 */
public final class PatternLabConstants {

    public static final String SLASH = "/";

    public static final String TEMPLATES = "templates";

    public static final String RAW_SELECTOR = "raw";

    public static final String PATTERN_SELECTOR = "pattern";

    public static final String NAME_PROPERTY = "name";

    public static final String PATH_PROPERTY = "path";

    public static final String TEMPLATE_PROPERTY = "template";

    public static final String DATA_PROPERTY = "data";

    public static final String PATTERN_COMPONENT_RESOURCE_TYPE = "/apps/patternlab/components/pattern";

    public static final String TEMPLATE_CALL_PATTERN = "<template data-sly-template.template=\"${@ data}\"><sly data-sly-use.template=\"%s\" data-sly-call=\"${template.%s %s}\"></sly><template>";

    public static final String INCLUDE_PATTERN = "<sly data-sly-include=\"%s\"></sly>";

    public static final String JCR_DATA_PROPERTY = "jcr:data";

    public static final String PROPERTIES = "=data.";

    public static final String PARAMETERS_PREFIX = " @";

    public static final String COMMA = ", ";

    public static final String JCR_PRIMARY_TYPE = "jcr:primaryType";

    public static final String NT_FILE = "nt:file";

    public static final String HTML = ".html";

    public static final String JCR_CONTENT = "jcr:content";

    public static final String JCR_DATA = "jcr:data";

    public static final String DATA_EXT = ".json";

    public static final String DESCRIPTION_EXT = ".md";

    public static final String SELECTOR = ".";

    public static final String NT_FOLDER = "nt:folder";

    public static final String NT_RESOURCE = "nt:resource";

    public static final String SLING_FOLDER = "sling:Folder";

    public static final String SLING_ORDERED_FOLDER = "sling:OrderedFolder";

    public static final String JCR_MIME_TYPE = "jcr:mimeType";

    public static final String TEXT_HTML = "text/html";

    public static final Pattern DATA_SLY_TEMPLATE_PATTERN = Pattern.compile("data-sly-template.([^ =>]*)([^>]*)>");

    public static final Pattern DATA_SLY_TEMPLATE_TAG_PATTERN = Pattern.compile("<template([^>]*)data-sly-template.([^ =>]*)([^>]*)>(?:(?!</template>).)*</template>");

    public static final Pattern DATA_SLY_INCLUDE_PATTERN = Pattern.compile("data-sly-include=\"([^\"]*)\"");

    public static final Pattern DATA_SLY_CALL_TAG_PATTERN = Pattern.compile("data-sly-call=\"\\$\\{([^\\.]*)\\.([^ ]*)([^\\}]*)\\}\"");

    public static final String DATA_SLY_USE_TEMPLATE_PATTERN =  "data-sly-use.%s=\"([^\"]*)\"";


    private PatternLabConstants() {
        //constants class
    }
}
