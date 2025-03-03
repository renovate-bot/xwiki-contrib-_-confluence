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
package org.xwiki.contrib.confluence.urlmapping.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xwiki.contrib.confluence.resolvers.ConfluencePageIdResolver;
import org.xwiki.contrib.confluence.resolvers.ConfluencePageTitleResolver;
import org.xwiki.contrib.confluence.resolvers.ConfluenceResolverException;
import org.xwiki.contrib.confluence.resolvers.ConfluenceSpaceKeyResolver;
import org.xwiki.contrib.urlmapping.URLMappingResult;
import org.xwiki.contrib.urlmapping.suggestions.URLMappingSuggestionUtils;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.resource.entity.EntityResourceAction;
import org.xwiki.resource.entity.EntityResourceReference;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.xwiki.contrib.confluence.urlmapping.internal.UrlMappingTestTools.assertFailedConversion;

@ComponentTest
@ComponentList({
    ConfluenceAttachmentURLMapper.class,
    ConfluenceHTMLURLMapper.class,
    ConfluencePageDisplayURLMapper.class,
    ConfluenceSpaceDisplayURLMapper.class,
    ConfluenceViewPageURLMapper.class,
    ConfluenceTinyLinkURLMapper.class
})
class ConfluenceURLMappingTest
{
    private static final String MY_SPACE = "MySpace";
    private static final String WEB_HOME = "WebHome";
    private static final String XWIKI = "xwiki";
    private static final String MIGRATION_ROOT = "MigrationRoot";
    private static final String PERSONAL_SPACE_KEY = "~712020e1bd3d553ddc454fb4b02dde7bea2b85";

    private static final DocumentReference MY_DOC_REF = new DocumentReference(
        XWIKI,
        List.of(MIGRATION_ROOT, MY_SPACE, "MyDoc"),
        WEB_HOME
    );

    private static final EntityResourceReference DOC_RR = new EntityResourceReference(
        MY_DOC_REF, EntityResourceAction.VIEW);

    private static final DocumentReference SPACE_HOME_REF = new DocumentReference(
        XWIKI,
            List.of(MIGRATION_ROOT, MY_SPACE),
        WEB_HOME
    );

    private static final DocumentReference PERSONAL_SPACE_REF = new DocumentReference(
        XWIKI,
        List.of(MIGRATION_ROOT, PERSONAL_SPACE_KEY),
        WEB_HOME
    );

    private static final EntityResourceReference PERSONAL_SPACE = new EntityResourceReference(PERSONAL_SPACE_REF,
        EntityResourceAction.VIEW);

    private static final EntityResourceReference SPACE_HOME = new EntityResourceReference(SPACE_HOME_REF,
        EntityResourceAction.VIEW);

    private static final String GET = "get";

    @MockComponent
    private ConfluencePageTitleResolver confluencePageTitleResolver;

    @MockComponent
    private ConfluenceSpaceKeyResolver confluenceSpaceKeyResolver;

    @MockComponent
    private ConfluencePageIdResolver confluencePageIdResolver;

    @MockComponent
    private URLMappingSuggestionUtils suggestionUtils;

    @InjectMockComponents
    private ConfluenceURLMappingPrefixHandler handler;

    @BeforeComponent
    void setup() throws ConfluenceResolverException
    {
        when(suggestionUtils.getSuggestionsFromDocumentReference(any())).thenAnswer(i -> {
            if (((LocalDocumentReference) (i.getArgument(0))).getRoot().getName().equals("WithoutSuggestionSpace")) {
                return null;
            }
            return new WordBlock("mysuggestion");
        });
        when(confluencePageIdResolver.getDocumentById(42)).thenReturn(MY_DOC_REF);
        when(confluencePageTitleResolver.getDocumentByTitle(ConfluenceURLMappingTest.MY_SPACE, "My Doc"))
            .thenReturn(MY_DOC_REF);

        // For tiny link tests
        when(confluencePageIdResolver.getDocumentById(139414)).thenReturn(
            new EntityReference("OK.liAC", EntityType.DOCUMENT));
        when(confluencePageIdResolver.getDocumentById(724765494)).thenReturn(
            new EntityReference("OK.NgszKw", EntityType.DOCUMENT));
        when(confluencePageIdResolver.getDocumentById(138313)).thenReturn(
            new EntityReference("OK.SRwC", EntityType.DOCUMENT));
        when(confluencePageIdResolver.getDocumentById(139483)).thenReturn(
            new EntityReference("OK.2yAC", EntityType.DOCUMENT));
        when(confluencePageIdResolver.getDocumentById(956713432)).thenReturn(
            new EntityReference("OK.2EkGOQ", EntityType.DOCUMENT));
        when(confluencePageIdResolver.getDocumentById(139521)).thenReturn(
            new EntityReference("OK.ASEC", EntityType.DOCUMENT));
        when(confluencePageIdResolver.getDocumentById(1255346114)).thenReturn(
            new EntityReference("OK.wg-TSg", EntityType.DOCUMENT));
        when(confluenceSpaceKeyResolver.getSpaceByKey(ConfluenceURLMappingTest.MY_SPACE)).thenReturn(SPACE_HOME_REF);
        when(confluenceSpaceKeyResolver.getSpaceByKey(PERSONAL_SPACE_KEY)).thenReturn(PERSONAL_SPACE_REF);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "display/MySpace/My+Doc?param=thatwedontcareabout",
        "display/MySpace/My+Doc"
    })
    void convertPageDisplayURL(String path)
    {
        URLMappingResult converted = handler.convert(path, GET, null);
        assertEquals(DOC_RR, converted.getResourceReference());
        assertEquals("", converted.getURL());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "display/MySpace/?param=thatwedontcareabout",
        "display/MySpace?param=thatwedontcareabout",
        "spaces/viewspace.action?key=MySpace",
        "spaces/viewspace.action?param=thatwedontcareabout&key=MySpace",
        "display/MySpace/",
        "display/MySpace"
    })
    void convertSpaceDisplayURL(String path)
    {
        URLMappingResult converted = handler.convert(path, GET, null);
        assertEquals(SPACE_HOME, converted.getResourceReference());
        assertEquals("", converted.getURL());
    }

    @Test
    void convertPersonalSpaceDisplayURL()
    {
        URLMappingResult converted = handler.convert("display/%7E712020e1bd3d553ddc454fb4b02dde7bea2b85", GET, null);
        assertEquals(PERSONAL_SPACE, converted.getResourceReference());
        assertEquals("", converted.getURL());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "spaces/MySpace/pages/42/my-page-title&param=thatwedontcareabout",
        "spaces/MySpace/pages/42/my-page-title",
        "MySpace/my-page-title-42.html",
        "pages/viewpage.action?spaceKey=MySpace&title=My+Doc",
        "pages/viewpage.action?pageId=42&param=thatwedontcareabout",
        "pages/viewpage.action?pageId=42"
    })
    void convertViewPageActionURL(String path)
    {
        URLMappingResult converted = handler.convert(path, GET, null);
        assertEquals(DOC_RR, converted.getResourceReference());
        assertEquals("", converted.getURL());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "pages/editpage.action?spaceKey=MySpace&title=My+Doc",
        "pages/editpage.action?pageId=42&param=thatwedontcareabout",
        "pages/editpage.action?pageId=42"
    })
    void convertEditPageActionURL(String path)
    {
        URLMappingResult converted = handler.convert(path, GET, null);
        EntityResourceReference edit = new EntityResourceReference(MY_DOC_REF, new EntityResourceAction("edit"));
        assertEquals(edit, converted.getResourceReference());
        assertEquals("", converted.getURL());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "download/thumbnails/42/hello+world.txt",
        "download/thumbnails/42/hello+world.txt?version=1&modificationDate=1551265055986&api=v2",
        "download/attachments/42/hello+world.txt?param=thatwedontcareabout",
        "download/attachments/42/hello+world.txt"
    })
    void convertAttachmentURL(String path)
    {
        URLMappingResult converted = handler.convert(path, GET, null);
        assertEquals(
            new EntityResourceReference(
                new AttachmentReference("hello world.txt", MY_DOC_REF),
                new EntityResourceAction("download")),
            converted.getResourceReference());
        assertEquals("", converted.getURL());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "hello/pages/viewpage.action?pageId=42",
        "hello/download/attachments/42/hello+world.txt",
        "hello/download/thumbnails/42/hello+world.txt",
        "hello/display/MySpace/My+Doc?param=thatwedontcareabout",
        "pages/viewpages.action?pageId=42",
        "pages/pages/viewpages.action?pageId=42",
        "download/attachment/42/hello+world.txt",
        "display/public/MySpace/My+Doc?param=thatwedontcareabout",
        "display/public/MySpace/My+Doc",
        "display/display/MySpace/My+Doc?param=thatwedontcareabout",
        "displays/MySpace/My+Doc?param=thatwedontcareabout"
    })
    void dontConvertWrongURL(String path)
    {
        URLMappingResult converted = handler.convert(path, GET, null);
        assertFailedConversion(converted);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "pages/viewpage.action?pageId=1337",
        "pages/viewpage.action?spaceKey=WithoutSuggestionSpace&title=unknowntitle",
        "WithoutSuggestionSpace/my-not-found-page-title-1337.html",
        "display/WithoutSuggestionSpace",
        "display/WithoutSuggestionSpace/My+Doc",
        "download/thumbnails/43/hello+world.txt",
        "download/attachments/43/hello+world.txt"
    })
    void handleNotFoundCorrectURLs(String path)
    {
        URLMappingResult converted = handler.convert(path, GET, null);
        assertFailedConversion(converted);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "spaces/MyUnknownSpace/pages/1337/my-not-found-page-title",
        "pages/viewpage.action?spaceKey=MyUnknownSpace&title=my-not-found-page-title",
        "MyUnknownSpace/my-not-found-page-title-1337.html",
        "display/MyUnknownSpace",
        "display/MyUnknownSpace/My+Doc",
        "display/MySpace/My+Unknown+Doc"
    })
    void notFoundCorrectWithSuggestions(String path)
    {
        URLMappingResult converted = handler.convert(path, GET, null);
        assertInstanceOf(Block.class, converted.getSuggestions());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "x/wg-TSg",
        "x/liAC",
        "x/NgszKw",
        "x/SRwC",
        "x/2yAC",
        "x/2EkGOQ",
        "x/ASEC"
    })
    void handleTinyLinks(String path) throws ConfluenceResolverException
    {
        URLMappingResult converted = handler.convert(path, GET, null);
        assertEquals(
            new EntityResourceReference(
                new EntityReference("OK." + path.substring(2), EntityType.DOCUMENT),
                EntityResourceAction.VIEW),
            converted.getResourceReference());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "x/OQUAAA",
        "x/=[@{]"
    })
    void tinyLinksBrokenOrDocIdNotFound()
    {
        URLMappingResult converted = handler.convert("/x/abab", GET, null);
        assertFailedConversion(converted);
    }
}
