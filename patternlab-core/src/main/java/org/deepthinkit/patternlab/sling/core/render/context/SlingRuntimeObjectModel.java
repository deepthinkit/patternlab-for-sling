package org.deepthinkit.patternlab.sling.core.render.context;

import org.apache.sling.api.adapter.Adaptable;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.scripting.sightly.render.AbstractRuntimeObjectModel;

/**
 * This is copy of org.apache.sling.scripting.sightly.impl.engine.runtime.SlingRuntimeObjectModel class
 * Original class cannot be used as it is not exposed by org.apache.sling.scripting.sightly bundle
 */
public class SlingRuntimeObjectModel extends AbstractRuntimeObjectModel {

    @Override
    protected Object getProperty(Object target, Object propertyObj) {
        Object result = super.getProperty(target, propertyObj);
        if (result == null && target instanceof Adaptable) {
            ValueMap valueMap = ((Adaptable) target).adaptTo(ValueMap.class);
            if (valueMap != null) {
                String property = toString(propertyObj);
                result = valueMap.get(property);
            }
        }
        return result;
    }

}
