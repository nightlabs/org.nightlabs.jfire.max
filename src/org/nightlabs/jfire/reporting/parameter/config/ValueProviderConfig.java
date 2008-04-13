package org.nightlabs.jfire.reporting.parameter.config;

import java.io.Serializable;

import javax.jdo.listener.DeleteCallback;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.reporting.parameter.ValueProvider;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderID;

/**
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * 
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.parameter.config.id.ValueProviderConfigID"
 *		detachable = "true"
 *		table="JFireReporting_ValueProviderConfig"
 *
 * @jdo.create-objectid-class
 * 		field-order="organisationID, valueAcquisitionSetupID, valueProviderOrganisationID, valueProviderCategoryID, valueProviderID, valueProviderConfigID"
 * 		include-imports="id/ValueProviderConfig.imports.inc"
 *		include-body="id/ValueProviderConfig.body.inc"
 *	
 * @jdo.fetch-group name="ValueProviderConfig.message" fetch-groups="default" fields="message"
 *
 */
public class ValueProviderConfig
implements ValueConsumer, Serializable, IGraphicalInfoProvider, DeleteCallback
{

	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_MESSAGE = "ValueProviderConfig.message";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private long valueAcquisitionSetupID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String valueProviderOrganisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String valueProviderCategoryID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String valueProviderID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private long valueProviderConfigID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int pageIndex;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int pageRow;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int pageColumn;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ValueAcquisitionSetup setup;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean allowNullOutputValue;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean showMessageInHeader;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean growVertically;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int x;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int y;
	
	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="valueProviderConfig"
	 */
	private ValueProviderConfigMessage message;
	
	
	protected ValueProviderConfig() {}
	
	public ValueProviderConfig(ValueAcquisitionSetup setup, long valueProviderConfigID)  {
		this.organisationID = setup.getOrganisationID();
		this.valueAcquisitionSetupID = setup.getValueAcquisitionSetupID();
		this.setup = setup;
		this.message = new ValueProviderConfigMessage(this);
		this.valueProviderConfigID = valueProviderConfigID;
		// default values
		showMessageInHeader = true;
		growVertically = true;
	}

	/**
	 * @return the valueProviderID
	 */
	public String getValueProviderID() {
		return valueProviderID;
	}

	/**
	 * @param valueProviderID the valueProviderID to set
	 */
	public void setValueProviderID(String valueProviderID) {
		this.valueProviderID = valueProviderID;
	}

	/**
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return the valueAcquisitionSetupID
	 */
	public long getValueAcquisitionSetupID() {
		return valueAcquisitionSetupID;
	}

	public String getConsumerKey() {
		return valueProviderOrganisationID + "/" + valueProviderCategoryID + "/" + valueProviderID + "/" + ObjectIDUtil.longObjectIDFieldToString(valueProviderConfigID);
	}

	/**
	 * The pageIndex is the number of the page
	 * the value provider referenced is displayed.
	 * 
	 * @return the pageIndex
	 */
	public int getPageIndex() {
		return pageIndex;
	}

	/**
	 * @param pageIndex the pageIndex to set
	 */
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	/**
	 * The pageRow is defines the vertical order
	 * of valueProviders within one page.
	 * 
	 * @return the pageRow
	 */
	public int getPageRow() {
		return pageRow;
	}

	/**
	 * @param pageRow the pageRow to set
	 */
	public void setPageRow(int pageOrder) {
		this.pageRow = pageOrder;
	}

	/**
	 * The pageColumn defines the order
	 * of valueProviders within one row.
	 * 
	 * @return the pageColumn
	 */
	public int getPageColumn() {
		return pageColumn;
	}

	/**
	 * @param pageRow the pageColumn to set
	 */
	public void setPageColumn(int pageColumn) {
		this.pageColumn = pageColumn;
	}
	
	/**
	 * @return the setup
	 */
	public ValueAcquisitionSetup getSetup() {
		return setup;
	}

	/**
	 * @return the valueProviderCategoryID
	 */
	public String getValueProviderCategoryID() {
		return valueProviderCategoryID;
	}

	/**
	 * @param valueProviderCategoryID the valueProviderCategoryID to set
	 */
	public void setValueProviderCategoryID(String valueProviderCategoryID) {
		this.valueProviderCategoryID = valueProviderCategoryID;
	}
	
	/**
	 * @return the valueProviderOrganisationID
	 */
	public String getValueProviderOrganisationID() {
		return valueProviderOrganisationID;
	}

	/**
	 * @param valueProviderOrganisationID the valueProviderOrganisationID to set
	 */
	public void setValueProviderOrganisationID(String valueProviderOrganisationID) {
		this.valueProviderOrganisationID = valueProviderOrganisationID;
	}
	
	public ValueProviderID getConfigValueProviderID() {
		return ValueProviderID.create(
				getValueProviderOrganisationID(),
				getValueProviderCategoryID(),
				getValueProviderID()
			);
	}

	public void setValueProvider(ValueProvider valueProvider) {
		this.valueProviderOrganisationID = valueProvider.getOrganisationID();
		this.valueProviderCategoryID = valueProvider.getValueProviderCategoryID();
		this.valueProviderID = valueProvider.getValueProviderID();
		this.message.validatePrimaryKeyFields(this);
	}

	/**
	 * returns the x coordinate
	 * @return the x coordinate
	 */
	public int getX() {
		return x;
	}

	/**
	 * returns the y coordinate
	 * @return the y coordinate
	 */
	public int getY() {
		return y;
	}

	/**
	 * sets the x coordinate
	 * @param x the x coordinate to set
	 */
	public void setX(int x) {
		this.x = x;
	}

	/**
	 * sets the y coordinate
	 * @param y the y coordinate to set
	 */
	public void setY(int y) {
		this.y = y;
	}
	
	/**
	 * @return The message that should be shown when this config is used
	 */
	public ValueProviderConfigMessage getMessage() {
		return message;
	}

	/**
	 * @return the valueProviderConfigID
	 */
	public long getValueProviderConfigID() {
		return valueProviderConfigID;
	}

	/**
	 * @return the allowNullOutputValue
	 */
	public boolean isAllowNullOutputValue() {
		return allowNullOutputValue;
	}

	/**
	 * @param allowNullOutputValue the allowNullOutputValue to set
	 */
	public void setAllowNullOutputValue(boolean allowNullOutputValue) {
		this.allowNullOutputValue = allowNullOutputValue;
	}

	/**
	 * @return the showMessageInHeader
	 */
	public boolean isShowMessageInHeader() {
		return showMessageInHeader;
	}

	/**
	 * @param showMessageInHeader the showMessageInHeader to set
	 */
	public void setShowMessageInHeader(boolean showMessageInHeader) {
		this.showMessageInHeader = showMessageInHeader;
	}

	/**
	 * Defines whether the cell that renders the UI linked to this value provider config
	 * should be arranged in the layou so the UI grabs vertically available space
	 * and grows as the cell grows.
	 * @return Whether the rendering cell should grow vertically 
	 */
	public boolean isGrowVertically() {
		return growVertically;
	}
	
	/**
	 * See {@link #isGrowVertically()}.
	 * 
	 * @param growVertically The growVertically
	 */
	public void setGrowVertically(boolean growVertically) {
		this.growVertically = growVertically;
	}
	
	/** {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime
				* result
				+ (int) (valueAcquisitionSetupID ^ (valueAcquisitionSetupID >>> 32));
		result = prime
				* result
				+ ((valueProviderCategoryID == null) ? 0
						: valueProviderCategoryID.hashCode());
		result = prime
				* result
				+ (int) (valueProviderConfigID ^ (valueProviderConfigID >>> 32));
		result = prime * result
				+ ((valueProviderID == null) ? 0 : valueProviderID.hashCode());
		result = prime
				* result
				+ ((valueProviderOrganisationID == null) ? 0
						: valueProviderOrganisationID.hashCode());
		return result;
	}

	/** {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ValueProviderConfig other = (ValueProviderConfig) obj;
		if (organisationID == null) {
			if (other.organisationID != null)
				return false;
		} else if (!organisationID.equals(other.organisationID))
			return false;
		if (valueAcquisitionSetupID != other.valueAcquisitionSetupID)
			return false;
		if (valueProviderCategoryID == null) {
			if (other.valueProviderCategoryID != null)
				return false;
		} else if (!valueProviderCategoryID
				.equals(other.valueProviderCategoryID))
			return false;
		if (valueProviderConfigID != other.valueProviderConfigID)
			return false;
		if (valueProviderID == null) {
			if (other.valueProviderID != null)
				return false;
		} else if (!valueProviderID.equals(other.valueProviderID))
			return false;
		if (valueProviderOrganisationID == null) {
			if (other.valueProviderOrganisationID != null)
				return false;
		} else if (!valueProviderOrganisationID
				.equals(other.valueProviderOrganisationID))
			return false;
		return true;
	}

	public void jdoPreDelete() {
		setup.removeOrphanedBindings(this);
	}
}
