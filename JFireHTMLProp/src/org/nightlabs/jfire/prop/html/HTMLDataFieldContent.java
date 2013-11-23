/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/
package org.nightlabs.jfire.prop.html;

import java.util.List;
import java.util.Map;

import org.nightlabs.htmlcontent.IFCKEditorContentFile;
import org.nightlabs.jfire.prop.PropertySet;

/**
 * Helper class wrapping properties of a certain {@link HTMLDataField} instance. It is used for inheritance purposes in the case the contents
 * of an {@link HTMLDataField} are inherited between {@link PropertySet}s.<p>
 * Every time the contents of an {@link HTMLDataField} are inherited a new instance of this class is created wrapping the data that is associated
 * with the {@link HTMLDataField} to be inherited (see {@link HTMLDataField#getData()}. This instance is then utilised to set the data (see {@link HTMLDataField#setData(Object)}
 * of the corresponding {@link HTMLDataField} that is part of the target (child) {@link PropertySet}, i.e. the {@link PropertySet} that will inherit all (or perhaps only a certain
 * part of the) data from the source (mother) {@link PropertySet}.
 * @author Frederik Loeser <!-- frederik [AT] nightlabs [DOT] de -->
 */
public class HTMLDataFieldContent {

	/** The map mapping language IDs to corresponding texts. */
	private Map<String, String> texts;
	/** The list of {@link IFCKEditorContentFile}s. */
	private List<IFCKEditorContentFile> files;

	/**
	 * Initialises a new {@link HTMLDataFieldContent} instance.
	 * @param texts The map mapping language IDs to corresponding texts.
	 * @param files The list of {@link IFCKEditorContentFile}s.
	 */
	HTMLDataFieldContent(final Map<String, String> texts, final List<IFCKEditorContentFile> files) {
		this.texts = texts;
		this.files = files;
	}

	/**
	 * @return the map mapping language IDs to corresponding texts.
	 */
	public Map<String, String> getTexts() {
		return texts;
	}
	/**
	 * @return the list of {@link IFCKEditorContentFile}s.
	 */
	public List<IFCKEditorContentFile> getFiles() {
		return files;
	}
}
