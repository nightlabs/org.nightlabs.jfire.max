package org.nightlabs.jfire.reporting.parameter.config;

import java.io.Serializable;

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
 * @jdo.create-objectid-class field-order="organisationID, valueAcquisitionSetupID, valueProviderOrganisationID, valueProviderCategoryID, valueProviderID"
 *
 */
public class ValueProviderConfig 
implements ValueConsumer, Serializable, IGraphicalInfoProvider 
{

	private static final long serialVersionUID = 1L;

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
	 */
	private String valueProviderOrganisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private String valueProviderCategoryID;
	
	/**
	 * @jdo.field primary-key="true"
	 */
	private String valueProviderID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int pageIndex;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int pageOrder;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ValueAcquisitionSetup setup;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	
	private int x;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */	
	private int y;
	
	// Maybe need to add x,y for GEF editor
	
	protected ValueProviderConfig() {}
	
	public ValueProviderConfig(ValueAcquisitionSetup setup)  {		
		this.organisationID = setup.getOrganisationID();
		this.valueAcquisitionSetupID = setup.getValueAcquisitionSetupID();
		this.setup = setup;
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
		return valueProviderOrganisationID + "/" + valueProviderCategoryID + "/" + valueProviderID;
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
	 * The pageOrder is defines the order
	 * of valueProviders within one page.
	 * 
	 * @return the pageOrder
	 */
	public int getPageOrder() {
		return pageOrder;
	}

	/**
	 * @param pageOrder the pageOrder to set
	 */
	public void setPageOrder(int pageOrder) {
		this.pageOrder = pageOrder;
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
		
}
