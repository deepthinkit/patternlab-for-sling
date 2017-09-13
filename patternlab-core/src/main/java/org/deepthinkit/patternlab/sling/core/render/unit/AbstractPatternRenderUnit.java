package org.deepthinkit.patternlab.sling.core.render.unit;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.scripting.sightly.java.compiler.RenderUnit;
import org.apache.sling.scripting.sightly.render.RenderContext;

import javax.script.Bindings;
import java.io.PrintWriter;

/**
 * Abstract class for Pattern RenderUnit, applies proper markup to pattern depending on PatternConfiguration
 */
public abstract class AbstractPatternRenderUnit extends RenderUnit {

    private static final String NEW_LINE = "\n";

    private String startTag;

    private String endTag;

    AbstractPatternRenderUnit(PatternConfiguration patternConfiguration) {
        if (patternConfiguration != null) {
            this.startTag = patternConfiguration.getStartTag();
            this.endTag = patternConfiguration.getEndTag();
        }
    }

    @Override
    protected final void render(PrintWriter out, Bindings bindings, Bindings arguments, RenderContext renderContext) {
        if (StringUtils.isNotBlank(startTag)) {
            out.write(startTag + NEW_LINE);
        }
        render(out, renderContext);
        if (StringUtils.isNotBlank(endTag)) {
            out.write(NEW_LINE + endTag + NEW_LINE);
        }
    }

    protected abstract void render(PrintWriter out, RenderContext renderContext);
}
