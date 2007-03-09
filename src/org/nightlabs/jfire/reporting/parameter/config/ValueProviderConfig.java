package org.nightlabs.jfire.reporting.parameter.config;

/**
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ValueProviderConfig implements ValueConsumer {
	private String organisationID;
	private long valueAcquisitionSetupID;
	private String valueProviderID;
	
	private int pageIndex;
	private int pageOrder;
	
	// Maybe need to add x,y for GEF editor
	
	// int wizard
	
	protected ValueProviderConfig() {}
	
	public ValueProviderConfig(String organisationID, long valueAcquisitionSetupID)  {
		this.organisationID = organisationID;
		this.valueAcquisitionSetupID = valueAcquisitionSetupID;
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
		return organisationID + "/" + valueProviderID;
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
}
