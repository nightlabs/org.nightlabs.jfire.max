package org.nightlabs.jfire.base.security.integration.ldap.connection;


import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link SSLSocketFactory} implementation that accepts every certificate without any validation.
 *
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 */
public class DummySSLSocketFactory extends SSLSocketFactory{
	
	private static Logger logger = LoggerFactory.getLogger(DummySSLSocketFactory.class);

    /** 
     * The default instance. 
     */
    private static SSLSocketFactory instance;

    /**
     * Gets the default instance.
     * 
     * @return the default instance
     */
    public static SSLSocketFactory getDefault(){
        if (instance == null){
        	synchronized(DummySSLSocketFactory.class){
        		if(instance == null){
                    instance = new DummySSLSocketFactory();
        		}
        	}
        }
        return instance;
    }

    /** 
     * The delegate.
     */
    private SSLSocketFactory delegate;

    /**
     * Creates a new instance of DummySSLSocketFactory.
     */
    public DummySSLSocketFactory(){
        try{
            TrustManager[] tma = {
            		new X509TrustManager(){
		                public X509Certificate[] getAcceptedIssuers(){
		                    return new X509Certificate[0];
		                }
		                public void checkClientTrusted( X509Certificate[] arg0, String arg1 ) throws CertificateException{}
		                public void checkServerTrusted( X509Certificate[] arg0, String arg1 ) throws CertificateException{}
            		}};
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, tma, new SecureRandom());
            delegate = sc.getSocketFactory();
        } catch (Exception e){
        	logger.error(e.getMessage(), e);
        	throw new RuntimeException(e);
        }
    }


    /**
     * {@inheritDoc}
     */
    public String[] getDefaultCipherSuites(){
        return delegate.getDefaultCipherSuites();
    }


    /**
     * {@inheritDoc}
     */
    public String[] getSupportedCipherSuites(){
        return delegate.getSupportedCipherSuites();
    }


    /**
     * {@inheritDoc}
     */
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException{
        try{
            return delegate.createSocket(s, host, port, autoClose);
        }catch (IOException e){
        	logger.error(e.getMessage(), e);
            throw e;
        }
    }


    /**
     * {@inheritDoc}
     */
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException{
        try{
            return delegate.createSocket(host, port);
        }catch (IOException e){
        	logger.error(e.getMessage(), e);
            throw e;
        }
    }


    /**
     * {@inheritDoc}
     */
    public Socket createSocket(InetAddress host, int port) throws IOException{
        try{
            return delegate.createSocket(host, port);
        } catch (IOException e){
        	logger.error(e.getMessage(), e);
            throw e;
        }
    }


    /**
     * {@inheritDoc}
     */
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException{
        try{
            return delegate.createSocket(host, port, localHost, localPort);
        } catch (IOException e){
        	logger.error(e.getMessage(), e);
            throw e;
        }
    }


    /**
     * {@inheritDoc}
     */
    public Socket createSocket(InetAddress address, int port, InetAddress localhAddress, int localPort) throws IOException{
        try{
            return delegate.createSocket(address, port, localhAddress, localPort);
        } catch (IOException e){
        	logger.error(e.getMessage(), e);
            throw e;
        }
    }

}
