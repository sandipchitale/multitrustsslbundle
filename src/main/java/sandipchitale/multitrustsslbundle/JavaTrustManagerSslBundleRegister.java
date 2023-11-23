package sandipchitale.multitrustsslbundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.ssl.SslBundleRegistrar;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundleRegistry;
import org.springframework.boot.ssl.SslStoreBundle;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Component
public class JavaTrustManagerSslBundleRegister implements SslBundleRegistrar {
    private static final Logger LOG = LoggerFactory.getLogger(JavaTrustManagerSslBundleRegister.class);

    public static final String JAVA_CACERTS_BUNDLE_NAME = "JAVA_CACERTS_BUNDLE";

    private static SslBundle jdkSslBundle;

    JavaTrustManagerSslBundleRegister() {
        try (InputStream is = Files.newInputStream(Path.of(System.getProperty("java.home"), "lib", "security", "cacerts"))) {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            char[] password = "changeit".toCharArray();
            trustStore.load(is, password);
            SslStoreBundle sslStoreBundle = SslStoreBundle.of(null, null, trustStore);
            jdkSslBundle = SslBundle.of(sslStoreBundle);
        } catch (NoSuchAlgorithmException | KeyStoreException | IOException | CertificateException e) {
            LOG.warn("Unable to initialize TrustManagerFactory and load default trustStore", e);
        }
    }

    @Override
    public void registerBundles(SslBundleRegistry registry) {
        if (jdkSslBundle != null) {
            registry.registerBundle(JAVA_CACERTS_BUNDLE_NAME, jdkSslBundle);
        }
    }

    @SuppressWarnings("unused")
    public static SslBundle getJdkSslBundle() {
        return jdkSslBundle;
    }
}