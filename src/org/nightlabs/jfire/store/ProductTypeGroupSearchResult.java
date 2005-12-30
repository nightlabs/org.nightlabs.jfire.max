/*
 * Created 	on Sep 3, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.store;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.store.id.ProductTypeGroupID;
import org.nightlabs.jfire.store.id.ProductTypeID;

public class ProductTypeGroupSearchResult implements Serializable {
	
	public static class Entry implements Serializable {
		private ProductTypeGroupID productTypeGroup;
		private List productTypes = new LinkedList();
		
		public Entry(ProductTypeGroupID productTypeGroupID) {
			this.productTypeGroup = productTypeGroupID;			
		}
		
		public Entry(ProductTypeGroup productTypeGroup) {
			this.productTypeGroup = (ProductTypeGroupID)JDOHelper.getObjectId(productTypeGroup);			
		}

		public void addProductType(ProductType productType) {
			productTypes.add(JDOHelper.getObjectId(productType));
		}
		
		public void addProductType(ProductTypeID productType) {
			productTypes.add(productType);
		}
		
		public List getProductTypes() {
			return productTypes;
		}
		
		public ProductTypeGroupID getProductTypeGroup() {
			return productTypeGroup;
		}
	}
	
	
	private Map entries = new HashMap();

	public ProductTypeGroupSearchResult() {
		super();
	}
	
	public Collection getEntries() {
		return entries.values();
	}
	
	public Set getEventGroupIDs() {
		return entries.keySet();
	}
	
	public Entry getEntry(ProductTypeGroup productTypeGroup) {
		return (Entry)entries.get(JDOHelper.getObjectId(productTypeGroup));
	}
	
	public Entry getEntry(ProductTypeGroupID productTypeGroup) {
		return (Entry)entries.get(productTypeGroup);
	}
	
	public void addEntry(Entry entry) {
		entries.put(entry.productTypeGroup, entry);
	}
	
	public void addEntry(ProductTypeGroupID productTypeGroup) {
		entries.put(productTypeGroup, new Entry(productTypeGroup));
	}
	
	public void addEntry(ProductTypeGroup productTypeGroup) {
		entries.put(JDOHelper.getObjectId(productTypeGroup), new Entry(productTypeGroup));
	}
	
	public void addType(ProductTypeGroup productTypeGroup, ProductType productType) {
		Entry entry = getEntry(productTypeGroup);
		if (entry != null)
			entry.addProductType(productType);
	}
	
	public void addType(ProductTypeGroupID productTypeGroup, ProductTypeID productType) {
		Entry entry = getEntry(productTypeGroup);
		if (entry != null)
			entry.addProductType(productType);
	}
	
	public int getSize() {
		return entries.size();
	}
}
