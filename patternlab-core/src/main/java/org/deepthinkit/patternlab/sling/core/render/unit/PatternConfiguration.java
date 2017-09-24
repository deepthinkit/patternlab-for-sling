package org.deepthinkit.patternlab.sling.core.render.unit;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PatternConfiguration retrieved from Pattern Json, which can be used for additional modification of Pattern markup
 */
public final class PatternConfiguration {

    private static final String PATTERN_CONFIG_PREFIX = "pattern-";

    private static final String TAG = "tag";

    private static final Pattern START_TAG_PATTERN = Pattern.compile("<([^\\s>]+)[^>]*>");

    private static final String END_TAG = "</%s>";

    private final String startTag;

    private final String endTag;

    public PatternConfiguration(Map<String, Object> patternLabConfig) {
        this.startTag = (String) patternLabConfig.get(TAG);
        this.endTag = extractEndTag(startTag);
    }

    String getStartTag() {
        return startTag;
    }

    String getEndTag() {
        return endTag;
    }

    @SuppressWarnings("unchecked")
    public static PatternConfiguration extractFromPatternData(Map<String, Object> patternData) {
        final Map<String, Object> patternConfig = Maps.newHashMap();
        for (String key : patternData.keySet()) {
            if (StringUtils.startsWith(key, PATTERN_CONFIG_PREFIX)) {
                patternConfig.put(StringUtils.substringAfter(key, PATTERN_CONFIG_PREFIX), patternData.get(key));
            }
        }
        for (String key : patternConfig.keySet()) {
            patternData.remove(PATTERN_CONFIG_PREFIX + key);
        }
        return new PatternConfiguration(patternConfig);
    }

    private String extractEndTag(String startTag) {
        if (StringUtils.isNotBlank(startTag)) {
            Matcher matcher = START_TAG_PATTERN.matcher(startTag);
            if (matcher.find()) {
                return String.format(END_TAG, matcher.group(1));
            }
        }
        return null;
    }

}
