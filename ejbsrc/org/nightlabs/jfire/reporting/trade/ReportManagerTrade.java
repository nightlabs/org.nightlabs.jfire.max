/*
 * Generated by XDoclet - Do not edit!
 */
package org.nightlabs.jfire.reporting.trade;

/**
 * Remote interface for jfire/ejb/JFireReportingTrade/ReportManagerTrade.
 * @build-nightlabs.xml generated
 */
public interface ReportManagerTrade
   extends javax.ejb.EJBObject
{
   /**
    * This method is called by the datastore initialization mechanism.
    * @throws ModuleException
    */
   public void initializeScripting(  )
      throws org.nightlabs.ModuleException, java.rmi.RemoteException;

   /**
    * This method is called by the datastore initialization mechanism.
    * @throws ModuleException
    */
   public void initializeReporting(  )
      throws org.nightlabs.ModuleException, java.rmi.RemoteException;

}
