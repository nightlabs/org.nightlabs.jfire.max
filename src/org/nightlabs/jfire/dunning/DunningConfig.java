package org.nightlabs.jfire.dunning;

import java.io.Serializable;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.apache.log4j.Logger;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningConfig"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DunningConfig
implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DunningConfig.class);

	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	@PrimaryKey
	@Column(length=100)
	private String dunningConfigID;
	
	/**
	 */
	@Persistent(
			dependent="true",
			mappedBy="dunningConfig"
	)
	private DunningConfigName name;
	/**
	 * @deprecated This constructor exists only for JDO and should never be used directly!
	 */
	@Deprecated
	protected DunningConfig() { }
	/**
	 * Create an instance of <code>DunningConfig</code>.
	 *
	 * @param organisationID first part of the primary key. The organisation which created this object.
	 * @param dunningConfigID second part of the primary key. A local identifier within the namespace of the organisation.
	 * @see #DunningConfig(boolean)
	 */
	public DunningConfig(String organisationID, String dunningConfigID) {
	}
	
	public String getOrganisationID() {
		return organisationID;
	}

	public String getDunningConfigID() {
		return dunningConfigID;
	}


	public DunningConfigName getName() {
		return name;
	}
}