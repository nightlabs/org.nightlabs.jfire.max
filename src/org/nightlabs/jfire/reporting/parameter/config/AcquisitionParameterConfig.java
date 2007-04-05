/**
 * 
 */
package org.nightlabs.jfire.reporting.parameter.config;

import java.io.Serializable;

import org.nightlabs.jfire.reporting.parameter.ValueProvider;

/**
 * This is used to represent a BIRT parameter within a {@link ValueAcquisitionSetup}.
 * These objects are always the end of the {@link ValueProvider} chain when
 * quering parameters from the user.
 *  
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 * 
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.parameter.config.id.AcquisitionParameterConfigID"
 *		detachable = "true"
 *		table="JFireReporting_AcquisitionParameterConfig"
 *
 * @jdo.create-objectid-class field-order="organisationID, valueAcquisitionSetupID, parameterID"
 * 
 * @jdo.inheritance strategy = "new-table" 
 * @jdo.inheritance-discriminator strategy="class-name"
 * 
 * @jdo.implements name="org.nightlabs.jfire.reporting.parameter.config.ValueConsumer"
 * 
 */
public class AcquisitionParameterConfig 
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
	 * @jdo.column length="100"
	 */
	private String parameterID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String parameterType;
	
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
	
	protected AcquisitionParameterConfig() {}
	
	public AcquisitionParameterConfig(ValueAcquisitionSetup setup) {
		this.organisationID = setup.getOrganisationID();
		this.valueAcquisitionSetupID = setup.getValueAcquisitionSetupID();
		this.setup = setup;
	}

	public AcquisitionParameterConfig(
			ValueAcquisitionSetup setup, 
			String parameterID, String parameterType
		) 
	{
		this(setup);
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

	/**
	 * @return the setup
	 */
	public ValueAcquisitionSetup getSetup() {
		return setup;
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
