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

package org.nightlabs.jfire.reporting.layout;

import java.io.Serializable;

import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.NotWritableException;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.util.Util;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.Column;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemFieldMetaDataID;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Discriminator;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemFieldMetaDataID"
 *		detachable="true"
 *		table="JFireReporting_ReportRegistryItemFieldMetaData"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, reportRegistryItemType, reportRegistryItemID, fieldName"
 *
 * @jdo.fetch-group name="ReportRegistryItem.fieldMetaDataMap" fields="reportRegistryItem" fetch-groups="default"
 */
@PersistenceCapable(
	objectIdClass=ReportRegistryItemFieldMetaDataID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireReporting_ReportRegistryItemFieldMetaData")
@FetchGroups(
	@FetchGroup(
		fetchGroups={"default"},
		name="ReportRegistryItem.fieldMetaDataMap",
		members=@Persistent(name="reportRegistryItem"))
)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ReportRegistryItemFieldMetaData
implements org.nightlabs.inheritance.FieldMetaData, Serializable
{
	private static final long serialVersionUID = 20081212L;

	private static final Logger logger = Logger.getLogger(ReportRegistryItemFieldMetaData.class);

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String reportRegistryItemType;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String reportRegistryItemID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String fieldName;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ReportRegistryItem reportRegistryItem;

	/**
	 * Whether or not the field may be changed by children.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private byte writableByChildren = FieldMetaData.WRITABLEBYCHILDREN_YES;

	/**
	 * writable is set to false if the mother has writableByChildren
	 * set to false.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean writable = true;

	/**
	 * If true, the value of the child is automatically updated if the
	 * mother's field is changed.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean valueInherited = true;

	/**
	 * @deprecated For JDO only.
	 */
	@Deprecated
	protected ReportRegistryItemFieldMetaData() {
	}

	public ReportRegistryItemFieldMetaData(ReportRegistryItem reportRegistryItem, String fieldName)
	{
		setProductType(reportRegistryItem);
		setFieldName(fieldName);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

	/**
	 * @return Returns the fieldName.
	 */
	public String getFieldName()
	{
		return fieldName;
	}
	/**
	 * @param fieldName The fieldName to set.
	 */
	protected void setFieldName(String fieldName)
	{
		this.fieldName = fieldName;
	}

	/**
	 * @param reportRegistryItem The {@link ReportRegistryItem} to set.
	 */
	protected void setProductType(ReportRegistryItem reportRegistryItem)
	{
		if (reportRegistryItem == null)
			throw new NullPointerException("reportRegistryItem must not be null!");
		if (reportRegistryItem.getOrganisationID() == null)
			throw new NullPointerException("reportRegistryItem.organisationID must not be null!");
		if (reportRegistryItem.getReportRegistryItemType() == null)
			throw new NullPointerException("reportRegistryItem.registryItemType must not be null!");
		if (reportRegistryItem.getReportRegistryItemID() == null)
			throw new NullPointerException("reportRegistryItem.registryItemID must not be null!");
		this.organisationID = reportRegistryItem.getOrganisationID();
		this.reportRegistryItemID = reportRegistryItem.getReportRegistryItemID();
		this.reportRegistryItemType = reportRegistryItem.getReportRegistryItemType();
		this.reportRegistryItem = reportRegistryItem;
	}

	/**
	 * @see org.nightlabs.inheritance.ProductInfoFieldMetaData#getWritableByChildren()
	 */
	public byte getWritableByChildren()
	{
		return writableByChildren;
	}
	/**
	 * @see org.nightlabs.inheritance.ProductInfoFieldMetaData#setWritableByChildren(byte)
	 */
	public void setWritableByChildren(byte writableByChildren)
	{
		this.writableByChildren = writableByChildren;
	}

	/**
	 * @return Returns the writable.
	 */
	public boolean isWritable()
	{
		return writable;
	}
	/**
	 * @param writable The writable to set.
	 */
	public void setWritable(boolean writable)
	{
		this.writable = writable;
	}
	/**
	 * @see org.nightlabs.inheritance.ProductInfoFieldMetaData#assertWritable()
	 */
	public void assertWritable() throws NotWritableException
	{
		if (!isWritable())
			throw new NotWritableException("Field \""+getFieldName()+"\" is not writeable!");
	}

	/**
	 * @return Returns the valueInherited.
	 */
	public boolean isValueInherited()
	{
		return valueInherited;
	}


	/**
	 * @return the reportRegistryItemID
	 */
	public String getReportRegistryItemID() {
		return reportRegistryItemID;
	}

	/**
	 * @return the reportRegistryItemType
	 */
	public String getReportRegistryItemType() {
		return reportRegistryItemType;
	}

	/**
	 * @return the reportRegistryItem
	 */
	public ReportRegistryItem getReportRegistryItem() {
		return reportRegistryItem;
	}


	/**
	 * @param valueInherited The valueInherited to set.
	 */
	public void setValueInherited(boolean valueInherited)
	{
		if (!writable && !valueInherited)
			throw new IllegalStateException("The field is not writable, thus the value must be inherited. Cannot set valueInherited to false!");

		if (logger.isTraceEnabled()) {
			logger.trace("[" + ObjectIDUtil.intObjectIDFieldToString(System.identityHashCode(this)) + "] setValueInherited: "+valueInherited+" for field "+fieldName+" and reportRegistryItem "+JDOHelper.getObjectId(reportRegistryItem));
		}

		this.valueInherited = valueInherited;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Util.hashCode(organisationID);
		result = prime * result + Util.hashCode(reportRegistryItemType);
		result = prime * result + Util.hashCode(reportRegistryItemID);
		result = prime * result + Util.hashCode(fieldName);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		final ReportRegistryItemFieldMetaData o = (ReportRegistryItemFieldMetaData) obj;
		return
				Util.equals(this.organisationID, o.organisationID) &&
				Util.equals(this.reportRegistryItemType, o.reportRegistryItemType) &&
				Util.equals(this.reportRegistryItemID, o.reportRegistryItemID) &&
				Util.equals(this.fieldName, o.fieldName);
	}

}
