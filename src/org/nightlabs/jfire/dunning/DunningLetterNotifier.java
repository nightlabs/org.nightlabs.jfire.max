package org.nightlabs.jfire.dunning;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.jfire.dunning.id.DunningLetterNotifierID;
import org.nightlabs.jfire.organisation.Organisation;


/**
 * An abstract handler to notify someone about a DunningLetter. 
 * We'll later implement notifiers that send e-mails to employees 
 * (to tell them that a not-yet-finalized DunningLetter was automatically 
 * created and they should process it) or directly send a PDF of a DunningLetter 
 * to a customer via Fax or more.<br>
 * 
 * <br>Each DunningLetterNotifier implementation either brings its own configuration 
 * (e.g. which employee to notify) or finds out itself all needed information from 
 * the DunningLetter and its related object graph.
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		objectIdClass=DunningLetterNotifierID.class,
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_DunningLetterNotifier"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public abstract class DunningLetterNotifier 
{
	@PrimaryKey
	@Column(length=100)
	private String organisationID;
	
	@PrimaryKey
	@Column(length=100)
	private String dunningLetterNotifierID;
	
	@Persistent(
			loadFetchGroup="all",
			persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningConfig dunningConfig;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DunningLetter dunningLetter;
	
	@Deprecated
	protected DunningLetterNotifier() { }
	
	public DunningLetterNotifier(String organisationID, String dunningLetterNotifierID, DunningConfig dunningConfig, DunningLetter dunningLetter) {
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		this.dunningLetterNotifierID = dunningLetterNotifierID;
		this.dunningConfig = dunningConfig;
		this.dunningLetter = dunningLetter;
	}
	
	public abstract void triggerNotifier();
}