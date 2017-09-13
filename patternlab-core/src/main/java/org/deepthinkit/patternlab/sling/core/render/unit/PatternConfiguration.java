package org.deepthinkit.patternlab.sling.core.render.unit;

import java.util.Map;

/**
 * PatternConfiguration retrieved from Pattern Json, which can be used for additional modification of Pattern markup
 */
public final class PatternConfiguration {

    private static final String PATTERN_CONFIG = "pattern-config";

    private static final String START_TAG = "startTag";

    private static final String END_TAG = "endTag";

    private final String startTag;

    private final String endTag;

    public PatternConfiguration(Map<String, Object> patternLabConfig) {
        this.startTag = (String) patternLabConfig.get(START_TAG);
        this.endTag = (String) patternLabConfig.get(END_TAG);
    }

    String getStartTag() {
        return startTag;
    }

    String getEndTag() {
        return endTag;
    }

    @SuppressWarnings("unchecked")
    public static PatternConfiguration extractFromPatternData(Map<String, Object> patternData) {
        final Object patternLabConfig = patternData.get(PATTERN_CONFIG);
        if (patternLabConfig != null && patternLabConfig instanceof Map) {
            patternData.remove(PATTERN_CONFIG);
            return new PatternConfiguration((Map<String, Object>) patternLabConfig);
        }
        return null;
    }

}
