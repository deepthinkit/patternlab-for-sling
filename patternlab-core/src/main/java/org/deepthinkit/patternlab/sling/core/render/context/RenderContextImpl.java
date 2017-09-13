package org.deepthinkit.patternlab.sling.core.render.context;

import org.apache.sling.api.scripting.SlingScriptHelper;
import org.apache.sling.scripting.sightly.SightlyException;
import org.apache.sling.scripting.sightly.extension.RuntimeExtension;
import org.apache.sling.scripting.sightly.render.AbstractRuntimeObjectModel;
import org.apache.sling.scripting.sightly.render.RenderContext;
import org.apache.sling.scripting.sightly.render.RuntimeObjectModel;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import java.util.Map;

/**
 * This is copy of org.apache.sling.scripting.sightly.impl.engine.runtime.RenderContextImpls class
 * Original class cannot be used as it is not exposed by org.apache.sling.scripting.sightly bundle
 * <p>
 * Rendering context for HTL rendering units.
 */
public class RenderContextImpl implements RenderContext {

    private static final AbstractRuntimeObjectModel OBJECT_MODEL = new SlingRuntimeObjectModel();

    private final Bindings bindings;
    private final ExtensionRegistryService extensionRegistryService;

    private RenderContextImpl(ScriptContext scriptContext, SlingScriptHelper helper) {
        bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
        extensionRegistryService = helper.getService(ExtensionRegistryService.class);
    }

    @Override
    public RuntimeObjectModel getObjectModel() {
        return OBJECT_MODEL;
    }

    /**
     * Provide the bindings for this script
     *
     * @return - the list of global bindings available to the script
     */
    @Override
    public Bindings getBindings() {
        return bindings;
    }

    @Override
    public Object call(String functionName, Object... arguments) {
        Map<String, RuntimeExtension> extensions = extensionRegistryService.extensions();
        RuntimeExtension extension = extensions.get(functionName);
        if (extension == null) {
            throw new SightlyException("Runtime extension is not available: " + functionName);
        }
        return extension.call(this, arguments);
    }

    public static RenderContext createFrom(Bindings bindings, SlingScriptHelper slingScriptHelper) {
        final ScriptContext scriptContext = new SimpleScriptContext();
        scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        final RenderContext renderContext = new RenderContextImpl(scriptContext, slingScriptHelper);
        renderContext.getBindings().putAll(bindings);
        return renderContext;
    }

}
