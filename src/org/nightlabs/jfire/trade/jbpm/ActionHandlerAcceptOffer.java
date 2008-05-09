package org.nightlabs.jfire.trade.jbpm;

import java.util.HashSet;

import javax.jdo.PersistenceManager;

import org.jbpm.graph.def.Action;
import org.jbpm.graph.def.Event;
import org.jbpm.graph.def.Node;
import org.jbpm.graph.exe.ExecutionContext;
import org.jbpm.instantiation.Delegation;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.math.Base62Coder;

public class ActionHandlerAcceptOffer
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;

	public static void register(org.jbpm.graph.def.ProcessDefinition jbpmProcessDefinition)
	{
		Action action = new Action(new Delegation(ActionHandlerAcceptOffer.class.getName()));
		action.setName(ActionHandlerAcceptOffer.class.getName());

		Event event = new Event("node-enter");
		event.addAction(action);

		Node finalized = jbpmProcessDefinition.getNode(JbpmConstantsOffer.Vendor.NODE_NAME_ACCEPTED);
		if (finalized == null)
			throw new IllegalArgumentException("The node \""+ JbpmConstantsOffer.Vendor.NODE_NAME_ACCEPTED +"\" does not exist in the ProcessDefinition \"" + jbpmProcessDefinition.getName() + "\"!");

		finalized.addEvent(event);
	}

	@Override
	@Implement
	protected void doExecute(ExecutionContext executionContext)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		Offer offer = (Offer) getStatable();

		User user = SecurityReflector.getUserDescriptor().getUser(pm);
		Trader.getTrader(pm).onAcceptOffer(user, offer);
	}

	protected static String truncateOld(String identifier, int length)
    {
        if (identifier.length() > length)
        {
            // Truncation is necessary so cut down to "maxlength-2" and add 2 char hashcode
            int tailIndex = length - 2;
            int tailHash = identifier.hashCode();

            // Scale the hash code down to the range 0 - 1295
            if (tailHash < 0)
            {
                tailHash = tailHash % HASH_RANGE_OLD + (HASH_RANGE_OLD - 1);
            }
            else
            {
                tailHash = tailHash % HASH_RANGE_OLD + HASH_RANGE_OLD;
            }

            String suffix = "0" + Integer.toString(tailHash, Character.MAX_RADIX);

            return identifier.substring(0, tailIndex) + suffix.substring(suffix.length() - 2);
        }
        else
        {
            return identifier;
        }
    }

	protected static String truncate(String identifier, int length)
    {
        if (identifier.length() > length)
        {
            // Truncation is necessary so cut down to "maxlength-HASH_LENGTH" and add HASH_LENGTH chars hashcode
            int tailIndex = length - HASH_LENGTH;
            int tailHash = identifier.hashCode();

            if (tailHash < 0)
            	tailHash *= -1;

            // Scale the hash code down to the range 0 ... (HASH_RANGE - 1)
            tailHash %= HASH_RANGE;

            String suffix = Integer.toString(tailHash, Character.MAX_RADIX);
            if (suffix.length() > HASH_LENGTH)
            	throw new IllegalStateException("Calculated hash has more characters than defined by HASH_LENGTH! This should never happen!");

            if (suffix.length() < HASH_LENGTH) {
            	StringBuilder sb = new StringBuilder(HASH_LENGTH);
            	sb.append(suffix);
            	while (sb.length() < HASH_LENGTH)
            		sb.insert(0, '0');
            	suffix = sb.toString();
            }

            return identifier.substring(0, tailIndex) + suffix;
        }
        else
        {
            return identifier;
        }
    }

	private static final int HASH_LENGTH = 4;
    private static final int HASH_RANGE = calculateHashMax();
    private static final int calculateHashMax()
    {
        int hm = 1;
        for (int i = 0; i < HASH_LENGTH; ++i)
            hm *= Character.MAX_RADIX;

        return hm;
    }

	private static final int HASH_RANGE_OLD = Character.MAX_RADIX * Character.MAX_RADIX / 2;

	public static void main(String[] args) {
		Base62Coder base62Coder = Base62Coder.sharedInstance();
		String prefix = base62Coder.encode((long) (Long.MAX_VALUE * Math.random()), 10);
		int identifierLength = 12;
		int iterationCounter = 0;
		HashSet<String> truncateOldResults = new HashSet<String>();
		HashSet<String> truncateNewResults = new HashSet<String>();
		while (iterationCounter < 10000000) {
			++iterationCounter;
			String suffix = base62Coder.encode((long) (Long.MAX_VALUE * Math.random()), 10);
			String identifier = prefix + suffix;
			truncateOldResults.add(truncateOld(identifier, identifierLength));
			truncateNewResults.add(truncate(identifier, identifierLength));
		}
		System.out.println("Number of iterations: " + iterationCounter);
		System.out.println("Number of unique identifiers with OLD algo: " + truncateOldResults.size());
		System.out.println("Number of unique identifiers with NEW algo: " + truncateNewResults.size());
	}
}
