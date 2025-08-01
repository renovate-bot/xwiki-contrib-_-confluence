/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.confluence.filter.internal.macros;

import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.confluence.filter.input.ConfluenceInputContext;
import org.xwiki.contrib.confluence.resolvers.ConfluenceResolverException;
import org.xwiki.contrib.confluence.resolvers.ConfluenceScrollTranslationResolver;

/**
 * Convert the sv-translation macro. If translation support is enabled, the macro will be dropped, and the content
 * will only be kept if the current language matches the language specified in the language parameter. If translation
 * support is disabled, the macro will be converted to the contentTranslation macro, implemented in book versions.
 *
 * @version $Id$
 * @since 9.75.0
 */
@Singleton
@Component
@Named("sv-translation")
public class TranslationMacroConverter extends AbstractTranslationMacroConverter
{
    private static final String MACRO_ID = "contentTranslation";

    private static final String MACRO_PARAMETER_LANGUAGE = "language";

    @Inject
    private ConfluenceInputContext confluenceInputContext;

    @Inject
    private ConfluenceScrollTranslationResolver confluenceScrollTranslationResolver;

    @Override
    public String toXWikiId(String confluenceId, Map<String, String> confluenceParameters, String confluenceContent,
        boolean inline)
    {
        return MACRO_ID;
    }

    @Override
    public InlineSupport supportsInlineMode(String id, Map<String, String> parameters, String content)
    {
        return InlineSupport.YES;
    }

    @Override
    protected Map<String, String> toXWikiParameters(String confluenceId, Map<String, String> confluenceParameters,
        String content)
    {
        Map<String, String> parameters;
        Long currentPageId = confluenceInputContext.getCurrentPage();

        String language = confluenceParameters.get(MACRO_PARAMETER_LANGUAGE);
        if (language == null) {
            throw new RuntimeException(String.format("Could not get the language from macro [%s]", confluenceId), null);
        }

        try {
            parameters = confluenceScrollTranslationResolver.getMacroParameters(currentPageId, language);
        } catch (ConfluenceResolverException e) {
            throw new RuntimeException(String.format(
                "Could not get the language parameters from macro [%s] with language [%s]", confluenceId, language), e);
        }

        parameters.put(MACRO_PARAMETER_LANGUAGE, language);

        return parameters;
    }

    @Override
    public Locale getLanguage(String id, Map<String, String> parameters, String content, boolean inline)
    {
        String l = parameters.get(MACRO_PARAMETER_LANGUAGE);
        if (StringUtils.isEmpty(l)) {
            return null;
        }

        try {
            return LocaleUtils.toLocale(l.split("_")[0]);
        } catch (IllegalArgumentException e) {
            // Should never happen
            throw new RuntimeException("Failed to compute a Locale for the sv-translation macro with language " + l, e);
        }
    }
}
