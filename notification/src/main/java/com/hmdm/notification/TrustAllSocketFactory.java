package com.hmdm.notification;
import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class TrustAllSocketFactory extends SSLSocketFactory {

    private final SSLSocketFactory delegate;

    public TrustAllSocketFactory() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] xcs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] xcs, String authType) {}
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                }
        };

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trustAllCerts, new SecureRandom());
        this.delegate = ctx.getSocketFactory();
    }

    private Socket disableHostnameVerification(Socket socket) {
        if (socket instanceof SSLSocket) {
            SSLSocket sslSocket = (SSLSocket) socket;
            // 🚫 Don't reuse getSSLParameters(), use a new instance
            SSLParameters sslParams = new SSLParameters();
            sslParams.setEndpointIdentificationAlgorithm(null); // ✅ This disables hostname verification
            sslSocket.setSSLParameters(sslParams);
        }
        return socket;
    }

    @Override
    public Socket createSocket() throws IOException {
        return disableHostnameVerification(delegate.createSocket());
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return disableHostnameVerification(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return disableHostnameVerification(delegate.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return disableHostnameVerification(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return disableHostnameVerification(delegate.createSocket(address, port, localAddress, localPort));
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return disableHostnameVerification(delegate.createSocket(s, host, port, autoClose));
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }
}
