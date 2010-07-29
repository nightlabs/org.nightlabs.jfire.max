package org.nightlabs.jfire.dunning;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
@PersistenceCapable(
		identityType=IdentityType.APPLICATION,
		detachable="true",
		table="JFireDunning_ProcessDunningStep"
)
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ProcessDunningStep 
extends AbstractDunningStep 
{

}
