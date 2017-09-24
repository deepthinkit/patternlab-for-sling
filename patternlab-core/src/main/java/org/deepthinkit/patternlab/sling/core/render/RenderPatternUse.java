package org.deepthinkit.patternlab.sling.core.render;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.commons.classloader.DynamicClassLoaderManager;
import org.apache.sling.scripting.sightly.SightlyException;
import org.apache.sling.scripting.sightly.java.compiler.RenderUnit;
import org.apache.sling.scripting.sightly.pojo.Use;
import org.apache.sling.scripting.sightly.render.RenderContext;
import org.codehaus.jackson.map.ObjectMapper;
import org.deepthinkit.patternlab.sling.core.render.context.RenderContextImpl;
import org.deepthinkit.patternlab.sling.core.render.unit.IncludePatternRenderUnit;
import org.deepthinkit.patternlab.sling.core.render.unit.PatternConfiguration;
import org.deepthinkit.patternlab.sling.core.render.unit.TemplateCallRenderUnit;
import org.deepthinkit.patternlab.sling.core.utils.PatternLabConstants;
import org.deepthinkit.patternlab.sling.core.utils.PatternLabUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Bindings;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for reading Pattern data saved in node and rendering include or template call properly
 */
public class RenderPatternUse implements Use {

    private static final Logger LOGGER = LoggerFactory.getLogger(RenderPatternUse.class);

    private static final String OUT_BINDING = "out";

    private static final String REQUEST_BINDING = "request";

    private static final String SLING_BINDING = "sling";

    @Override
    public void init(Bindings bindings) {
        final PrintWriter out = (PrintWriter) bindings.get(OUT_BINDING);
        final SlingHttpServletRequest request = (SlingHttpServletRequest) bindings.get(REQUEST_BINDING);
        final String template = (String) bindings.get(PatternLabConstants.TEMPLATE_PROPERTY);
        final String path = (String) bindings.get(PatternLabConstants.PATH_PROPERTY);
        final String data = (String) bindings.get(PatternLabConstants.DATA_PROPERTY);
        final SlingScriptHelper slingScriptHelper = (SlingScriptHelper) bindings.get(SLING_BINDING);
        final Map<String, Object> patternData = retrievePatternData(request, data);
        renderPattern(template, path, patternData, bindings, slingScriptHelper, out);
    }

    private Map<String, Object> retrievePatternData(SlingHttpServletRequest request, String data) {
        try {
            final String jsonData = StringUtils.isNotBlank(data) ?
                    PatternLabUtils.getDataFromFile(data, request.getResourceResolver()) : StringUtils.EMPTY;
            return StringUtils.isNotBlank(jsonData)
                    ? new ObjectMapper().readValue(jsonData, HashMap.class) : Maps.newHashMap();
        } catch (IOException e) {
            LOGGER.error("Error during retrieving pattern data:", e);
        }
        return Maps.newHashMap();
    }

    private void renderPattern(String template, String path, Map<String, Object> patternData, Bindings bindings, SlingScriptHelper slingScriptHelper,
                               PrintWriter out) {
        final RenderContext renderContext = RenderContextImpl.createFrom(bindings, slingScriptHelper);
        final PatternConfiguration patternConfiguration = PatternConfiguration.extractFromPatternData(patternData);
        final RenderUnit renderUnit = StringUtils.isNotBlank(template) ?
                new TemplateCallRenderUnit(path, template, patternData, patternConfiguration) :
                new IncludePatternRenderUnit(path, patternConfiguration);
        renderUnit.render(out, renderContext, bindings);
    }

}
