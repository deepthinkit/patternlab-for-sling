package org.kciecierski.patternlab.sling.core.model.page;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;

import javax.inject.Inject;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class PatternLabClientlibsModel {

    @Inject
    private String[] headerCss;

    @Inject
    private String[] headerJs;

    @Inject
    private String[] footerCss;

    @Inject
    private String[] footerJs;

    public String[] getHeaderCss() {
        return headerCss;
    }

    public String[] getHeaderJs() {
        return headerJs;
    }

    public String[] getFooterCss() {
        return footerCss;
    }

    public String[] getFooterJs() {
        return footerJs;
    }
}
