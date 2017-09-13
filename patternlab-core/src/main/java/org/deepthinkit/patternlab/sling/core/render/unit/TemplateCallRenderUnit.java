package org.deepthinkit.patternlab.sling.core.render.unit;

import org.apache.sling.scripting.sightly.render.RenderContext;

import java.io.PrintWriter;
import java.util.Map;

/**
 * RenderUnit class responsible for rendering data-sly-use and data-sly-call for template path, name and parameters
 */
public final class TemplateCallRenderUnit extends AbstractPatternRenderUnit {

    private static final String SIGHTLY_USE = "use";

    private final String templatePath;

    private String templateName;

    private final Map<String, Object> templateParameters;

    public TemplateCallRenderUnit(String templatePath, String templateName, Map<String, Object> templateParameters,
                                  PatternConfiguration patternConfiguration) {
        super(patternConfiguration);
        this.templatePath = templatePath;
        this.templateName = templateName;
        this.templateParameters = templateParameters;
    }

    @Override
    protected void render(PrintWriter out, RenderContext renderContext) {
        Object templateUseObject = renderContext.call(SIGHTLY_USE, templatePath, obj());
        Object templateObject = renderContext.getObjectModel().resolveProperty(templateUseObject, templateName);
        callUnit(out, renderContext, templateObject, templateParameters);
    }
}

