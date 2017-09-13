package org.deepthinkit.patternlab.sling.core.render.unit;

import org.apache.sling.scripting.sightly.render.RenderContext;

import java.io.PrintWriter;

/**
 * RenderUnit class responsible for rendering data-sly-include with include path to pattern
 */
public final class IncludePatternRenderUnit extends AbstractPatternRenderUnit {

    private static final String SIGHTLY_INCLUDE = "include";

    private final String includePath;

    public IncludePatternRenderUnit(String includePath, PatternConfiguration patternConfiguration) {
        super(patternConfiguration);
        this.includePath = includePath;
    }

    @Override
    protected void render(PrintWriter out, RenderContext renderContext) {
        final Object includeObject = renderContext.call(SIGHTLY_INCLUDE, includePath, obj());
        out.write(renderContext.getObjectModel().toString(includeObject));
    }
}

