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

package org.nightlabs.jfire.store;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.store.id.ProductTypeGroupID;
import org.nightlabs.jfire.store.id.ProductTypeID;

public class ProductTypeGroupIDSearchResult
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public static class Entry
	implements Serializable
	{
		private static final long serialVersionUID = 1L;
		private ProductTypeGroupID productTypeGroupID;
		private List<ProductTypeID> productTypeIDs = new LinkedList<ProductTypeID>();

		public Entry(ProductTypeGroupID productTypeGroupID) {
			this.productTypeGroupID = productTypeGroupID;
		}

//		public Entry(ProductTypeGroup productTypeGroup) {
//			this.productTypeGroupID = (ProductTypeGroupID)JDOHelper.getObjectId(productTypeGroup);
//		}

//		public void addProductType(ProductType productType) {
//			productTypeIDs.add((ProductTypeID)JDOHelper.getObjectId(productType));
//		}

		public void addProductType(ProductTypeID productType) {
			productTypeIDs.add(productType);
		}

		public List<ProductTypeID> getProductTypeIDs() {
			return productTypeIDs;
		}

		public ProductTypeGroupID getProductTypeGroupID() {
			return productTypeGroupID;
		}
	}

	private Map<ProductTypeGroupID, Entry> entries = new HashMap<ProductTypeGroupID, Entry>();

	public ProductTypeGroupIDSearchResult() {
		super();
	}

	public Collection<Entry> getEntries() {
		return entries.values();
	}

	public Set<ProductTypeGroupID> getProductTypesGroupIDs() {
		return entries.keySet();
	}

	public Entry getEntry(ProductTypeGroup productTypeGroup) {
		return entries.get(JDOHelper.getObjectId(productTypeGroup));
	}

	public Entry getEntry(ProductTypeGroupID productTypeGroupID) {
		return entries.get(productTypeGroupID);
	}

	public void addEntry(Entry entry) {
		entries.put(entry.productTypeGroupID, entry);
	}

	public void addEntry(ProductTypeGroupID productTypeGroupID) {
		if (!entries.containsKey(productTypeGroupID))
			entries.put(productTypeGroupID, new Entry(productTypeGroupID));
	}

//	public void addEntry(ProductTypeGroup productTypeGroup) {
//		entries.put((ProductTypeGroupID)JDOHelper.getObjectId(productTypeGroup), new Entry(productTypeGroup));
//	}

	public void addType(ProductTypeGroupID productTypeGroupID, ProductTypeID productTypeID) {
		Entry entry = getEntry(productTypeGroupID);
		if (entry != null)
			entry.addProductType(productTypeID);
	}

	public int getSize() {
		return entries.size();
	}

	// TODO: don't iterate each time but build up when corresponding add-methods are called
	public Set<ProductTypeID> getAllProductTypeIDs() {
		Set<ProductTypeID> productTypeIDs = new HashSet<ProductTypeID>();
		for (Map.Entry<ProductTypeGroupID, Entry> mapEntry : entries.entrySet()) {
			productTypeIDs.addAll(mapEntry.getValue().getProductTypeIDs());
		}
		return productTypeIDs;
	}
}
