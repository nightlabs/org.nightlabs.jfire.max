package org.nightlabs.jfire.dunning;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.PrimaryKey;

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
public abstract class DunningLetterNotifier 
{
	@PrimaryKey
	@Column(length=100)
	private String organisationID;
	
	@Deprecated
	protected DunningLetterNotifier() { }
	
	public DunningLetterNotifier(String organisationID) {
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
	}
}