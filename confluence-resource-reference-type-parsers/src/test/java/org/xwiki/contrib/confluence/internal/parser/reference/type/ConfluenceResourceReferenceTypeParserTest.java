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
package org.xwiki.contrib.confluence.internal.parser.reference.type;

import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.contrib.confluence.resolvers.ConfluencePageIdResolver;
import org.xwiki.contrib.confluence.resolvers.ConfluencePageTitleResolver;
import org.xwiki.contrib.confluence.resolvers.ConfluenceResolverException;
import org.xwiki.contrib.confluence.resolvers.ConfluenceSpaceKeyResolver;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.model.reference.WikiReference;
import org.xwiki.rendering.listener.reference.AttachmentResourceReference;
import org.xwiki.rendering.listener.reference.DocumentResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.listener.reference.SpaceResourceReference;
import org.xwiki.test.annotation.AfterComponent;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import com.xpn.xwiki.test.reference.ReferenceComponentList;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ComponentTest
@ReferenceComponentList
class ConfluenceResourceReferenceTypeParserTest
{
    private static final String DEMO = "DEMO";
    private static final EntityReference DEMO_SPACE = new EntityReference(
        DEMO, EntityType.SPACE, new WikiReference("xwiki"));
    public static final EntityReference FORTYTWODOCREF = new EntityReference("WebHome", EntityType.DOCUMENT,
        new EntityReference("Test", EntityType.SPACE, DEMO_SPACE));
    private static final String XWIKI_DEMO = "xwiki:DEMO";
    private static final String MYSECTION = "mysection";
    private static final DocumentResourceReference DEMO_WEBHOME_REF =
        new DocumentResourceReference("xwiki:DEMO.WebHome");
    private static final String FORTYTWOREF = "xwiki:DEMO.Test.WebHome";

    @InjectMockComponents
    private ConfluenceAttachResourceReferenceTypeParser attachReferenceTypeParser;

    @InjectMockComponents
    private ConfluencePageResourceReferenceTypeParser pageReferenceTypeParser;

    @InjectMockComponents
    private ConfluenceSpaceResourceReferenceTypeParser spaceReferenceTypeParser;

    @MockComponent
    private ConfluenceSpaceKeyResolver spaceKeyResolver;

    @MockComponent
    private ConfluencePageIdResolver pageIdResolver;

    @MockComponent
    private ConfluencePageTitleResolver pageTitleResolver;

    private EntityReferenceSerializer<String> referenceSerializer;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @BeforeComponent
    void setup() throws ConfluenceResolverException
    {
        when(spaceKeyResolver.getSpaceByKey(DEMO)).thenReturn(DEMO_SPACE);
        when(pageIdResolver.getDocumentById(42)).thenReturn(FORTYTWODOCREF);
        when(pageTitleResolver.getDocumentByTitle(DEMO, "Hello @ \\World")).thenReturn(FORTYTWODOCREF);
    }

    @AfterComponent
    void setupComponent() throws ComponentLookupException
    {
        referenceSerializer = componentManager.getInstance(
            new DefaultParameterizedType(null, EntityReferenceSerializer.class, String.class));
    }

    @Test
    void space()
    {
        assertEquals(new SpaceResourceReference(XWIKI_DEMO), spaceReferenceTypeParser.parse(DEMO));
    }

    @Test
    void spaceAnchor()
    {
        SpaceResourceReference expected = new SpaceResourceReference(XWIKI_DEMO);
        expected.setAnchor(MYSECTION);
        assertEquals(expected, spaceReferenceTypeParser.parse("DEMO#mysection"));
    }

    @Test
    void spaceHome()
    {
        assertEquals(DEMO_WEBHOME_REF, pageReferenceTypeParser.parse("spaceHome:DEMO"));
        assertEquals(DEMO_WEBHOME_REF, pageReferenceTypeParser.parse("spaceHome:DEMO#"));
    }

    @Test
    void spaceHomeAnchor()
    {
        DocumentResourceReference expected = DEMO_WEBHOME_REF;
        expected.setAnchor(MYSECTION);
        assertEquals(expected, pageReferenceTypeParser.parse("spaceHome:DEMO#mysection"));
    }

    @Test
    void spaceHomeFile()
    {
        AttachmentResourceReference expected = new AttachmentResourceReference("xwiki:DEMO.WebHome@file.csv");
        assertEquals(expected, attachReferenceTypeParser.parse("spaceHome:DEMO@file.csv"));
        assertEquals(expected, attachReferenceTypeParser.parse("spaceHome:DEMO@file.csv#"));
    }

    @Test
    void spaceHomeFileAnchor()
    {
        AttachmentResourceReference expected = new AttachmentResourceReference("xwiki:DEMO.WebHome@file.txt");
        expected.setAnchor(MYSECTION);
        assertEquals(expected, attachReferenceTypeParser.parse("spaceHome:DEMO@file.txt#mysection"));
    }

    @Test
    void pageId()
    {
        DocumentResourceReference expected = new DocumentResourceReference(FORTYTWOREF);
        assertEquals(expected, pageReferenceTypeParser.parse("id:42"));
        assertEquals(expected, pageReferenceTypeParser.parse("id:42#"));
    }

    @Test
    void pageIdAnchor()
    {
        DocumentResourceReference expected = new DocumentResourceReference(FORTYTWOREF);
        expected.setAnchor(MYSECTION);
        assertEquals(expected, pageReferenceTypeParser.parse("id:42#mysection"));
    }

    @Test
    void pageIdFile()
    {
        AttachmentResourceReference expected = new AttachmentResourceReference("xwiki:DEMO.Test.WebHome@file.txt");
        assertEquals(expected, attachReferenceTypeParser.parse("id:42@file.txt"));
        assertEquals(expected, attachReferenceTypeParser.parse("id:42@file.txt#"));
    }

    @Test
    void pageIdFileAnchor()
    {
        AttachmentResourceReference expected = new AttachmentResourceReference(
            referenceSerializer.serialize(new EntityReference("fi\\le.csv", EntityType.ATTACHMENT, FORTYTWODOCREF)));
        expected.setAnchor(MYSECTION);
        assertEquals(expected, attachReferenceTypeParser.parse("id:42@fi\\\\le.csv#mysection wordtobeignored"));
    }

    @Test
    void pageTitle()
    {
        DocumentResourceReference expected = new DocumentResourceReference(FORTYTWOREF);
        assertEquals(expected, pageReferenceTypeParser.parse("page:DEMO.Hello \\@ \\\\World"));
        assertEquals(expected, pageReferenceTypeParser.parse("page:DEMO.Hello \\@ \\\\World#"));
    }

    @Test
    void pageTitleAnchor()
    {
        DocumentResourceReference expected = new DocumentResourceReference(FORTYTWOREF);
        expected.setAnchor(MYSECTION);
        assertEquals(expected, pageReferenceTypeParser.parse("page:DEMO.Hello \\@ \\\\World#mysection"));
    }

    @Test
    void pageTitleFile()
    {
        AttachmentResourceReference expected = new AttachmentResourceReference("xwiki:DEMO.Test.WebHome@fi#le.txt");
        assertEquals(expected, attachReferenceTypeParser.parse("page:DEMO.Hello \\@ \\\\World@fi\\#le.txt#"));
    }

    @Test
    void pageTitleFileAnchor()
    {
        AttachmentResourceReference expected = new AttachmentResourceReference(
            referenceSerializer.serialize(new EntityReference("fi\\le.txt", EntityType.ATTACHMENT, FORTYTWODOCREF)));
        expected.setAnchor(MYSECTION);
        assertEquals(expected, attachReferenceTypeParser.parse("page:DEMO.Hello \\@ \\\\World@fi\\\\le.txt#mysection"));
    }

    @Test
    void notFound()
    {
        assertNull(pageReferenceTypeParser.parse("page:NOTFOUND.Not Found"));
        assertNull(pageReferenceTypeParser.parse("page:NOTFOUND.Not Found#anchor"));
        assertNull(attachReferenceTypeParser.parse("page:NOTFOUND.Not Found@file"));
        assertNull(attachReferenceTypeParser.parse("page:NOTFOUND.Not Found@file#anchor"));
        assertNull(pageReferenceTypeParser.parse("spaceHome:NOTFOUND"));
        assertNull(pageReferenceTypeParser.parse("spaceHome:NOTFOUND#anchor"));
        assertNull(attachReferenceTypeParser.parse("spaceHome:NOTFOUND@file"));
        assertNull(attachReferenceTypeParser.parse("spaceHome:NOTFOUND@file#anchor"));
        assertNull(pageReferenceTypeParser.parse("id:4242"));
        assertNull(pageReferenceTypeParser.parse("id:4242#anchor"));
        assertNull(attachReferenceTypeParser.parse("id:4242@file"));
        assertNull(attachReferenceTypeParser.parse("id:4242@file#anchor"));
        assertNull(spaceReferenceTypeParser.parse("space:NOTFOUND"));
        assertNull(spaceReferenceTypeParser.parse("space:NOTFOUND#anchor"));
    }

    @Test
    void notCorrect()
    {
        // unexpected prefix
        assertNull(pageReferenceTypeParser.parse("unexpectedprefix:blablabla"));

        // missing attachment
        assertNull(attachReferenceTypeParser.parse("pageid:42"));
        assertNull(attachReferenceTypeParser.parse("pageid:42@"));

        // dangling escaping slash
        assertNull(pageReferenceTypeParser.parse("page:DEMO.Hello\\"));
        assertNull(attachReferenceTypeParser.parse("pageid:42@file.csv\\"));
    }

    @Test
    void pageTitleEmptySpace()
    {
        assertNull(pageReferenceTypeParser.parse("page:Hello \\. World"));
    }

    @Test
    void correctResourceTypes()
    {
        assertEquals(ResourceType.SPACE, spaceReferenceTypeParser.getType());
        assertEquals(ResourceType.DOCUMENT, pageReferenceTypeParser.getType());
        assertEquals(ResourceType.ATTACHMENT, attachReferenceTypeParser.getType());
    }
}
