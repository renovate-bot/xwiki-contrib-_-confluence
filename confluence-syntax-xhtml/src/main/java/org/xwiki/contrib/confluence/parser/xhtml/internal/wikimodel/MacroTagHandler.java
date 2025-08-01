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
package org.xwiki.contrib.confluence.parser.xhtml.internal.wikimodel;

import org.xwiki.contrib.confluence.parser.xhtml.ConfluenceMacroSupport;
import org.xwiki.contrib.confluence.parser.xhtml.ConfluenceReferenceConverter;
import org.xwiki.rendering.wikimodel.WikiParameter;
import org.xwiki.rendering.wikimodel.WikiParameters;
import org.xwiki.rendering.wikimodel.impl.IWikiScannerContext;
import org.xwiki.rendering.wikimodel.xhtml.handler.TagHandler;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles macros.
 * <p>
 * Example:
 * <p>
 * {@code
 * <ac:macro ac:name="code">
 *   <ac:parameter ac:name="title">Bloc code title</ac:parameter>
 *   <ac:parameter ac:name="theme">theme</ac:parameter>
 *   <ac:parameter ac:name="language">language</ac:parameter>
 *   <ac:plain-text-body><![CDATA[Content of bloc code]]></ac:plain-text-body>
 * </ac:macro>
 * }
 *
 * @version $Id$
 * @since 9.0
 */
public class MacroTagHandler extends TagHandler implements ConfluenceTagHandler
{

    private static final List<String> VIEW_FILE_MACROS = List.of(
        "view-file", "viewfile", "viewdoc", "viewppt", "viewxls", "viewpdf", "excel"
    );

    private static final String SPACE = "space";
    private static final String PAGE = "page";
    private static final String AC_NAME = "ac:name";

    private final ConfluenceMacroSupport macroSupport;
    private final ConfluenceReferenceConverter converter;

    /**
     * A Confluence Macro.
     */
    public static class ConfluenceMacro
    {
        /**
         * The name of the Confluence macro.
         */
        public String name;

        /**
         * The macro parameters.
         */
        public WikiParameters parameters = new WikiParameters();

        /**
         * The macro content.
         */
        public String content;

        /**
         * The macro index.
         */
        public int index = -1;
    }

    /**
     * Default constructor.
     * @param macroSupport macro support
     * @param converter the reference converter
     */
    public MacroTagHandler(ConfluenceMacroSupport macroSupport, ConfluenceReferenceConverter converter)
    {
        super(false);
        this.macroSupport = macroSupport;
        this.converter = converter;
    }

    @Override
    protected void begin(TagContext context)
    {
        ConfluenceMacro macro = new ConfluenceMacro();

        WikiParameter macroNameParam = context.getParams().getParameter(AC_NAME);
        macro.name = macroNameParam == null ? "" : macroNameParam.getValue();

        context.getTagStack().pushStackParameter(CONFLUENCE_CONTAINER, macro);
    }

    @Override
    protected void end(TagContext context)
    {
        ConfluenceMacro macro = (ConfluenceMacro) context.getTagStack().popStackParameter(CONFLUENCE_CONTAINER);

        // We want to make sure macros in paragraphs and titles are marked as inline.
        // We observe that Confluence exports list items like this: <li><p>content</p></li> and
        // table cells like this: <td><p>...</p></td> so these two cases are covered.
        // Confluence also exports block macros as <p>{block macro}</p>. We remove the extra paragraph
        // in ConfluenceXWikiGeneratorListener.
        IWikiScannerContext s = context.getScannerContext();
        boolean isInline = supportsInlineMode(macro) && (
            s.isInHeader()
                || isInListItem(context)
                || isInParagraph(context)
                || isInSpan(context)
                || isInTaskBody(context)
                || isInLink(context)
        );

        handleViewFileQuirk(macro);
        s.onMacro(macro.name, macro.parameters, macro.content, isInline);
    }

    private void handleViewFileQuirk(ConfluenceMacro macro)
    {
        if (!VIEW_FILE_MACROS.contains(macro.name)) {
            return;
        }

        WikiParameter pageParam = macro.parameters.getParameter(PAGE);
        if (pageParam == null) {
            return;
        }

        WikiParameter spaceParam = macro.parameters.getParameter(SPACE);
        String space = "";
        if (spaceParam != null) {
            space = spaceParam.getValue();
            macro.parameters = macro.parameters.remove(SPACE);
        }

        macro.parameters = macro.parameters.setParameter(PAGE,
            converter.convertDocumentReference(space, pageParam.getValue()));
    }

    private static boolean isInTaskBody(TagContext context)
    {
        return context.getParent() != null && "ac:task-body".equals(context.getParent().getName());
    }

    private static boolean isInLink(TagContext context)
    {
        return context.getParent() != null && "a".equals(context.getParent().getName());
    }

    private static boolean isInSpan(TagContext context)
    {
        return context.getParent() != null && "span".equals(context.getParent().getName());
    }

    private static boolean isInParagraph(TagContext context)
    {
        return context.getTagStack().getStackParameter(CONFLUENCE_IN_PARAGRAPH) != null;
    }

    private boolean isInListItem(TagContext context)
    {
        return context.getParent().isTag("li");
    }

    private boolean supportsInlineMode(ConfluenceMacro macro)
    {
        if (macroSupport == null) {
            return true;
        }

        Map<String, String> parameters = new LinkedHashMap<>(macro.parameters.getSize());
        for (WikiParameter p : macro.parameters) {
            parameters.put(p.getKey(), p.getValue());
        }

        return macroSupport.supportsInlineMode(macro.name, parameters, macro.content);
    }

    static boolean inViewFile(TagContext context)
    {
        if (context == null) {
            return false;
        }

        WikiParameter nameParam = context.getParams().getParameter(AC_NAME);
        if ("ac:structured-macro".equals(context.getName())) {
            return nameParam != null && VIEW_FILE_MACROS.contains(nameParam.getValue());
        }

        return inViewFile(context.getParentContext());
    }

}
