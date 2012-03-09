package org.nightlabs.jfire.dunning;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PrimaryKey;

import org.nightlabs.clone.CloneContext;
import org.nightlabs.clone.CloneableWithContext;
import org.nightlabs.jfire.dunning.id.DunningLetterNotifierID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.util.Util;
import org.nightlabs.util.reflect.ReflectUtil;


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
		table="JFireDunning_LetterNotifier"
)
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
public abstract class DunningLetterNotifier
	implements CloneableWithContext
{
	@PrimaryKey
	@Column(length=100)
	private String organisationID;
	
	@PrimaryKey
	private long dunningLetterNotifierID;
	
	private int dunningLevel;
	
	@Deprecated
	protected DunningLetterNotifier() { }
	
	public DunningLetterNotifier(String organisationID, int dunningLevel)
	{
		this(organisationID, IDGenerator.nextID(DunningLetterNotifier.class), dunningLevel);
	}
	
	public DunningLetterNotifier(String organisationID, long dunningLetterNotifierID, int dunningLevel)
	{
		Organisation.assertValidOrganisationID(organisationID);
		this.organisationID = organisationID;
		this.dunningLetterNotifierID = dunningLetterNotifierID;
		this.dunningLevel = dunningLevel;
	}
	
	/**
	 * @return the organisationID
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

	/**
	 * @return the dunningLetterNotifierID
	 */
	public long getDunningLetterNotifierID()
	{
		return dunningLetterNotifierID;
	}
	
	public int getDunningLevel()
	{
		return dunningLevel;
	}
	
	public void setDunningLevel(int dunningLevel)
	{
		this.dunningLevel = dunningLevel;
	}

	public abstract void triggerNotifier(DunningLetter newLetter);

	@Override
	public CloneableWithContext clone(CloneContext context, boolean cloneReferences)
	{
//	WORKAROUND - JDO does not support cloning a detached object and consider it transient! ( http://www.jpox.org/servlet/forum/viewthread_thread,1865 )
//	DunningMoneyFlowConfig clone = (DunningMoneyFlowConfig) super.clone();
		DunningLetterNotifier clone = ReflectUtil.newInstanceRuntimeException(getClass());
		clone.organisationID = organisationID;
//	END OF WORKAROUND
		
		clone.dunningLetterNotifierID = IDGenerator.nextID(DunningLetterNotifier.class);
		return clone;
	}
	
	@Override
	public abstract void updateReferencesOfClone(CloneableWithContext clone, CloneContext context);
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		result = prime * result + (int) (dunningLetterNotifierID ^ (dunningLetterNotifierID >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		
		DunningLetterNotifier other = (DunningLetterNotifier) obj;
		if (Util.equals(organisationID, other.organisationID) &&
				Util.equals(dunningLetterNotifierID, other.dunningLetterNotifierID))
			return true;
		
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "DunningLetterNotifier [organisationID=" + organisationID + ", dunningLetterNotifierID="
				+ dunningLetterNotifierID + ", dunningLevel=" + dunningLevel + "]";
	}

}