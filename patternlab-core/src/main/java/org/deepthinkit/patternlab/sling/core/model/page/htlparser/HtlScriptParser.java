package org.deepthinkit.patternlab.sling.core.model.page.htlparser;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.deepthinkit.patternlab.sling.core.utils.PatternLabConstants;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtlScriptParser {

    private static final String JAVA_WHITESPACE_REGEX = "\r\n|\r|\n";

    private static final String SINGLE_WHITESPACE = " ";

    private static final String FILESYSTEM_BACK_PATH = "..";

    public static String extractNoHtlTemplateCode(String patternCode) {
        StringBuilder noTemplateCode = new StringBuilder(StringUtils.EMPTY);
        final Matcher matcher = PatternLabConstants.DATA_SLY_TEMPLATE_TAG_PATTERN.matcher(patternCode);
        int index = 0;
        while (matcher.find()) {
            noTemplateCode.append(StringUtils.substring(patternCode, index, matcher.start()));
            index = matcher.end();
        }
        return noTemplateCode.toString();
    }

    public static String extractHtlTemplateCode(String template, String patternCode) {
        final Matcher matcher = PatternLabConstants.DATA_SLY_TEMPLATE_TAG_PATTERN.matcher(patternCode.replaceAll(JAVA_WHITESPACE_REGEX, SINGLE_WHITESPACE));
        while (matcher.find()) {
            final String matchedTemplate = matcher.group(2);
            if (StringUtils.equals(matchedTemplate, template)) {
                return matcher.group(0);
            }
        }

        return StringUtils.EMPTY;
    }

    public static Set<HtlScriptModel> getHtlTemplateCallPaths(String patternCode, String projectSlingPrefix, String projectName, String currentPatternPath) {
        final Set<HtlScriptModel> htlTemplateUsePaths = Sets.newHashSet();
        final Matcher matcher = PatternLabConstants.DATA_SLY_CALL_TAG_PATTERN.matcher(patternCode);
        while (matcher.find()) {
            final String templatePathUseName = matcher.group(1);
            final String templateName = matcher.group(2);
            final String templateUsePath = getUsePathForTemplate(StringUtils.substring(patternCode, 0, matcher.start()), templatePathUseName);
            final String path = resolveAbsolutePathToPattern(templateUsePath, projectSlingPrefix, projectName, currentPatternPath);
            htlTemplateUsePaths.add(new HtlScriptModel(path, templateName));
        }

        return htlTemplateUsePaths;
    }

    public static String getUsePathForTemplate(String patternCode, String templateName) {
        String path = StringUtils.EMPTY;
        Pattern pattern = Pattern.compile(String.format(PatternLabConstants.DATA_SLY_USE_TEMPLATE_PATTERN, templateName));
        final Matcher matcher = pattern.matcher(patternCode);
        while (matcher.find()) {
            path = matcher.group(1);
        }
        return path;
    }

    public static Set<HtlScriptModel> getHtlIncludePaths(String patternCode, String projectSlingPrefix, String projectName, String currentPatternPath) {
        final Set<HtlScriptModel> htlIncludesForPatterns = Sets.newHashSet();
        final Matcher matcher = PatternLabConstants.DATA_SLY_INCLUDE_PATTERN.matcher(patternCode);
        while (matcher.find()) {
            final String path = resolveAbsolutePathToPattern(matcher.group(1), projectSlingPrefix, projectName, currentPatternPath);
            htlIncludesForPatterns.add(new HtlScriptModel(path));
        }

        return htlIncludesForPatterns;
    }

    private static String resolveAbsolutePathToPattern(String path, String projectSlingPrefix, String projectName, String currentPatternPath) {
        if (StringUtils.startsWith(path, projectSlingPrefix + PatternLabConstants.SLASH + projectName)
                || StringUtils.startsWith(path, projectSlingPrefix + PatternLabConstants.SLASH + projectName)) {
            return path;
        } else if (StringUtils.startsWith(path, projectName)) {
            return PatternLabConstants.SLASH + projectSlingPrefix + PatternLabConstants.SLASH + path;
        }
        String[] pathElements = StringUtils.split(path, PatternLabConstants.SLASH);
        StringBuilder absolutePath = new StringBuilder(StringUtils.substringBeforeLast(currentPatternPath, PatternLabConstants.SLASH));
        for (String pathElement : pathElements) {
            if (StringUtils.equals(pathElement, FILESYSTEM_BACK_PATH)) {
                absolutePath = new StringBuilder(StringUtils.substringBeforeLast(currentPatternPath, PatternLabConstants.SLASH));
            } else {
                absolutePath.append(PatternLabConstants.SLASH).append(pathElement);
            }
        }

        return absolutePath.toString();
    }
}
