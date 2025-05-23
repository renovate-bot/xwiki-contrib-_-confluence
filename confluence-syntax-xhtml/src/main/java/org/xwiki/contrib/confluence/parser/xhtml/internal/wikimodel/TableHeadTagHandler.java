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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.rendering.internal.parser.xhtml.wikimodel.XWikiTableDataTagHandler;
import org.xwiki.rendering.wikimodel.xhtml.impl.TagContext;

import static org.xwiki.contrib.confluence.parser.xhtml.internal.wikimodel.TableCellTagHandler.beginCell;

/**
 * Make sure to produce something that won't break xwiki/2.x table. See https://jira.xwiki.org/browse/XRENDERING-488
 * 
 * @version $Id$
 * @since 9.1.5
 */
public class TableHeadTagHandler extends XWikiTableDataTagHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TableHeadTagHandler.class);

    @Override
    protected void begin(TagContext context)
    {
        beginCell(context);
        beginDocument(context);
    }

    @Override
    protected void end(TagContext context)
    {
        endDocument(context);
        TableCellTagHandler.advanceCurrentCol(context, LOGGER);
        super.end(context);
    }
}
