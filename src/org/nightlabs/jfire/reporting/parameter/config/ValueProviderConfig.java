package org.nightlabs.jfire.reporting.parameter.config;

public class ValueProviderConfig implements ValueConsumer {
	private String organisationID;
	private long valueAcquisitionSetupID;
	private String valueProviderID;
	
	// Maybe need to add x,y for GEF editor
	
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
	
	
}
