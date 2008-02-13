package org.nightlabs.jfire.jbpm;

import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jbpm.JbpmConfiguration;
import org.nightlabs.jfire.jbpm.internal.JbpmConfigurationCarrier;
import org.nightlabs.jfire.security.SecurityReflector;

public class JbpmLookup
{
	protected JbpmLookup() {}

	protected static String getEhCacheConfigFileName(String organisationID)
	{
		return "ehcache-"+organisationID+"-cfg.xml";
	}

	protected static String getJbpmConfigFileName(String organisationID)
	{
		return "jbpm-"+organisationID+"-cfg.xml";
	}

	protected static String getHibernateConfigFileName(String organisationID, HibernateEnvironmentMode hibernateEnvironmentMode)
	{
		return "hibernate-" + organisationID + '-' + hibernateEnvironmentMode + "-cfg.xml";
	}

	protected static final String JNDI_PREFIX = "java:/jfire/jbpmConfiguration";

	protected static String getJbpmConfigurationJndiName(String organisationID)
	{
		return JNDI_PREFIX + '/' + organisationID;
	}

	protected static void bindJbpmConfiguration(String organisationID, JbpmConfiguration jbpmConfiguration)
	{
		Logger.getLogger(JbpmLookup.class).info("bindJbpmConfiguration: organisationID="+organisationID);
		String jndiName = getJbpmConfigurationJndiName(organisationID);
		try {
			InitialContext initialContext = new InitialContext();
			try {
				try {
					initialContext.createSubcontext(JNDI_PREFIX);
				} catch (NameAlreadyBoundException e) {
					// ignore
				}

				initialContext.bind(jndiName, new JbpmConfigurationCarrier(jbpmConfiguration));
			} finally {
				initialContext.close();
			}
		} catch (NamingException x) {
			throw new RuntimeException("Could not bind JbpmConfiguration \""+jndiName+"\" into JNDI!", x);
		}
	}

	public static JbpmConfiguration getJbpmConfiguration()
	{
		String organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
		String jndiName = getJbpmConfigurationJndiName(organisationID);
		try {
			InitialContext initialContext = new InitialContext();
			try {
				JbpmConfigurationCarrier carrier = (JbpmConfigurationCarrier) initialContext.lookup(jndiName);
				JbpmConfiguration res = carrier.getJbpmConfiguration();
				if (res == null)
					throw new IllegalStateException("JbpmConfigurationCarrier.jbpmConfiguration is null!");
				return res;
			} finally {
				initialContext.close();
			}
		} catch (NamingException x) {
			throw new RuntimeException("Could not obtain JbpmConfiguration \""+jndiName+"\" from JNDI!", x);
		}
	}
}
