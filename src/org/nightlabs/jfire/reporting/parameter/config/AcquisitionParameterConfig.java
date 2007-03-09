/**
 * 
 */
package org.nightlabs.jfire.reporting.parameter.config;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class AcquisitionParameterConfig implements ValueConsumer {
	private String organisationID;
	private long valueAcquisitionSetupID;
	
	private String parameterID;
	private String parameterType;
	
	// Maybe need to add x,y for GEF editor
	
	protected AcquisitionParameterConfig() {}
	
	public AcquisitionParameterConfig(String organisationID, long valueAcquisitionSetupID) {
		this.organisationID = organisationID;
		this.valueAcquisitionSetupID = valueAcquisitionSetupID;		
	}

	public AcquisitionParameterConfig(String organisationID, long valueAcquisitionSetupID, String parameterID, String parameterType) {
		this.organisationID = organisationID;
		this.valueAcquisitionSetupID = valueAcquisitionSetupID;
		this.parameterID = parameterID;
		this.parameterType = parameterType;
	}
	
	/**
	 * @return the parameterID
	 */
	public String getParameterID() {
		return parameterID;
	}

	/**
	 * @param parameterID the parameterID to set
	 */
	public void setParameterID(String parameterID) {
		this.parameterID = parameterID;
	}

	/**
	 * @return the parameterType
	 */
	public String getParameterType() {
		return parameterType;
	}

	/**
	 * @param parameterType the parameterType to set
	 */
	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
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
		return organisationID + "/" + parameterID;
	}
	
	
}
