package com.example.yuan_wanandroid.model.ssl;

import android.os.Build;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/**
 * <pre>
 *     author : 残渊
 *     time   : 2019/04/21
 *     desc   : OkHttp在4.4及以下不支持TLS协议的解决方法。
 *              在Android 4.4.1以后，也就是API20+以后默认打开TLS 1.2 而再此之前的TLS是默认关闭的
 * </pre>
 */

public class SSLSocketCompatFactory extends SSLSocketFactory {

    private SSLSocketFactory defaultFactory;
    // Android 5.0+ (API level21) provides reasonable default settings
    // but it still allows SSLv3
    // https://developer.android.com/about/versions/android-5.0-changes.html#ssl
    static String protocols[] = null, cipherSuites[] = null;

    static {
        try {
            SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket();
            if (socket != null) {
                /* set reasonable protocol versions */
                // - enable all supported protocols (enables TLSv1.1 and TLSv1.2 on Android <5.0)
                // - remove all SSL versions (especially SSLv3) because they‘re insecure now
                List<String> protocols = new LinkedList<>();
                for (String protocol : socket.getSupportedProtocols())
                    if (!protocol.toUpperCase().contains("SSL"))
                        protocols.add(protocol);
                SSLSocketCompatFactory.protocols = protocols.toArray(new String[protocols.size()]);
                /* set up reasonable cipher suites */
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    // choose known secure cipher suites
                    List<String> allowedCiphers = Arrays.asList(
                            // TLS 1.2
                            "TLS_RSA_WITH_AES_256_GCM_SHA384",
                            "TLS_RSA_WITH_AES_128_GCM_SHA256",
                            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256",
                            "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256",
                            "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384",
                            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256",
                            "TLS_ECHDE_RSA_WITH_AES_128_GCM_SHA256",
                            // maximum interoperability
                            "TLS_RSA_WITH_3DES_EDE_CBC_SHA",
                            "TLS_RSA_WITH_AES_128_CBC_SHA",
                            // additionally
                            "TLS_RSA_WITH_AES_256_CBC_SHA",
                            "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA",
                            "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA",
                            "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA",
                            "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA");
                    List<String> availableCiphers = Arrays.asList(socket.getSupportedCipherSuites());
                    // take all allowed ciphers that are available and put them into preferredCiphers
                    HashSet<String> preferredCiphers = new HashSet<>(allowedCiphers);
                    preferredCiphers.retainAll(availableCiphers);
                    /* For maximum security, preferredCiphers should *replace* enabled ciphers (thus disabling
                     * ciphers which are enabled by default, but have become unsecure), but I guess for
                     * the security level of DAVdroid and maximum compatibility, disabling of insecure
                     * ciphers should be a server-side task */
                    // add preferred ciphers to enabled ciphers
                    HashSet<String> enabledCiphers = preferredCiphers;
                    enabledCiphers.addAll(new HashSet<>(Arrays.asList(socket.getEnabledCipherSuites())));
                    SSLSocketCompatFactory.cipherSuites = enabledCiphers.toArray(new String[enabledCiphers.size()]);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SSLSocketCompatFactory(X509TrustManager tm) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, (tm != null) ? new X509TrustManager[]{tm} : null, null);
            defaultFactory = sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new AssertionError(); // The system has no TLS. Just give up.
        }
    }

    private void upgradeTLS(SSLSocket ssl) {
        // Android 5.0+ (API level21) provides reasonable default settings
        // but it still allows SSLv3
        // https://developer.android.com/about/versions/android-5.0-changes.html#ssl
        if (protocols != null) {
            ssl.setEnabledProtocols(protocols);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && cipherSuites != null) {
            ssl.setEnabledCipherSuites(cipherSuites);
        }
    }

    @Override public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        Socket ssl = defaultFactory.createSocket(host, port);
        if (ssl instanceof SSLSocket)
            upgradeTLS((SSLSocket) ssl);
        return ssl;
    }

    @Override public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        Socket ssl = defaultFactory.createSocket(host, port, localHost, localPort);
        if (ssl instanceof SSLSocket)
            upgradeTLS((SSLSocket) ssl);
        return ssl;
    }

    @Override public Socket createSocket(InetAddress host, int port) throws IOException {
        Socket ssl = defaultFactory.createSocket(host, port);
        if (ssl instanceof SSLSocket)
            upgradeTLS((SSLSocket) ssl);
        return ssl;
    }

    @Override public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        Socket ssl = defaultFactory.createSocket(address, port, localAddress, localPort);
        if (ssl instanceof SSLSocket)
            upgradeTLS((SSLSocket) ssl);
        return ssl;
    }

    @Override public String[] getDefaultCipherSuites() {
        return cipherSuites;
    }

    @Override public String[] getSupportedCipherSuites() {
        return cipherSuites;
    }

    @Override public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        Socket ssl = defaultFactory.createSocket(s, host, port, autoClose);
        if (ssl instanceof SSLSocket)
            upgradeTLS((SSLSocket) ssl);
        return ssl;
    }

}