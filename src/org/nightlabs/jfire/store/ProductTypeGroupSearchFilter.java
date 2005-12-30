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

import org.nightlabs.jdo.search.SearchFilter;

/**
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class ProductTypeGroupSearchFilter extends SearchFilter {

	public ProductTypeGroupSearchFilter(int conjunction) {
		super(conjunction);
	}

	/**
	 * @see org.nightlabs.jdo.search.SearchFilter#getExtendClass()
	 */
	protected Class getExtendClass() {
		Class productTypeGroupClass = getProductTypeGroupClass();
		if (!ProductTypeGroup.class.isAssignableFrom(productTypeGroupClass))
			throw new IllegalArgumentException("getProductTypeGroupClass must return a subclass of ProductTypeGroup");
		return productTypeGroupClass;
	}
	
	/**
	 * Return the extend-class for this filter. Must return an
	 * subclass of {@link ProductTypeGroup}.
	 */
	protected abstract Class getProductTypeGroupClass();
}
