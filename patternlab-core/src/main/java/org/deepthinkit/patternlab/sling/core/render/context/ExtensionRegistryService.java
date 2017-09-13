package org.deepthinkit.patternlab.sling.core.render.context;

import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.scripting.sightly.extension.RuntimeExtension;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This is copy of org.apache.sling.scripting.sightly.impl.engine.ExtensionRegistryService class
 * Original class cannot be used as it is not exposed by org.apache.sling.scripting.sightly bundle
 * <p>
 * Aggregator for all runtime extensions.
 */
@Component(service = org.deepthinkit.patternlab.sling.core.render.context.ExtensionRegistryService.class)
public class ExtensionRegistryService {

    private volatile Map<String, RuntimeExtension> mapping = new HashMap<>();
    private Map<String, Integer> mappingPriorities = new HashMap<>(10, 0.9f);

    Map<String, RuntimeExtension> extensions() {
        return mapping;
    }

    @Reference(
            policy = ReferencePolicy.DYNAMIC,
            service = RuntimeExtension.class,
            cardinality = ReferenceCardinality.MULTIPLE
    )
    @SuppressWarnings("unused")
    protected synchronized void bindExtensionService(RuntimeExtension extension, Map<String, Object> properties) {
        Integer newPriority = PropertiesUtil.toInteger(properties.get(Constants.SERVICE_RANKING), 0);
        String extensionName = PropertiesUtil.toString(properties.get(RuntimeExtension.NAME), "");
        Integer priority = PropertiesUtil.toInteger(mappingPriorities.get(extensionName), 0);
        if (newPriority > priority) {
            mapping = Collections.unmodifiableMap(add(mapping, extension, extensionName));
            mappingPriorities.put(extensionName, newPriority);
        } else {
            if (!mapping.containsKey(extensionName)) {
                mapping = Collections.unmodifiableMap(add(mapping, extension, extensionName));
                mappingPriorities.put(extensionName, newPriority);
            }
        }

    }

    @SuppressWarnings("unused")
    protected synchronized void unbindExtensionService(RuntimeExtension extension, Map<String, Object> properties) {
        String extensionName = PropertiesUtil.toString(properties.get(RuntimeExtension.NAME), "");
        mappingPriorities.remove(extensionName);
        mapping = Collections.unmodifiableMap(remove(mapping, extensionName));
    }

    private Map<String, RuntimeExtension> add(Map<String, RuntimeExtension> oldMap, RuntimeExtension extension, String extensionName) {
        HashMap<String, RuntimeExtension> newMap = new HashMap<>(oldMap);
        newMap.put(extensionName, extension);
        return newMap;
    }

    private Map<String, RuntimeExtension> remove(Map<String, RuntimeExtension> oldMap, String extensionName) {
        HashMap<String, RuntimeExtension> newMap = new HashMap<>(oldMap);
        newMap.remove(extensionName);
        return newMap;
    }
}
