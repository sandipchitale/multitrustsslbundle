package sandipchitale.multitrustsslbundle;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@SpringBootApplication
public class MultitrustsslbundleApplication {

	@Bean
	public CommandLineRunner server1CLR (SslBundles sslBundles, RestTemplateBuilder restTemplateBuilder) {
	    return (args) -> {
			SslBundle javaSslBundle = sslBundles.getBundle(JavaTrustManagerSslBundleRegister.JAVA_CACERTS_BUNDLE_NAME);
			List<TrustManager> trustManagers = new LinkedList<>();

			trustManagers.addAll(Arrays.asList(sslBundles.getBundle("server1")
					.getManagers().getTrustManagers()));

			trustManagers.addAll(Arrays.asList(sslBundles.getBundle("server2")
					.getManagers().getTrustManagers()));

			trustManagers.addAll(Arrays.asList(sslBundles.getBundle(JavaTrustManagerSslBundleRegister.JAVA_CACERTS_BUNDLE_NAME)
					.getManagers().getTrustManagers()));

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, new TrustManager[]{new CompositeX509TrustManager(trustManagers)}, null);
			SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext);

			final HttpClientConnectionManager httpClientConnectionManager = PoolingHttpClientConnectionManagerBuilder
					.create()
					.setSSLSocketFactory(sslConnectionSocketFactory)
					.build();

			CloseableHttpClient closeableHttpClient = HttpClients.custom()
					.setConnectionManager(httpClientConnectionManager)
					.evictExpiredConnections()
					.build();

			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			requestFactory.setHttpClient(closeableHttpClient);
			RestTemplate restTemplate = restTemplateBuilder.build();
			restTemplate.setRequestFactory(requestFactory);

			try {
				System.out.println("Trying https://server1:8081");
				System.out.println("Response: " + restTemplate.getForObject("https://server1:8081", String.class));
			} catch (Exception e) {
				System.out.println("Failed: " + e.getMessage());
			}
			try {
				System.out.println("Trying https://server2:8082");
				System.out.println("Response: " + restTemplate.getForObject("https://server2:8082", String.class));
			} catch (Exception e) {
				System.out.println("Failed: " + e.getMessage());
			}
			try {
				System.out.println("Trying https://jsonplaceholder.typicode.com/todos/1");
				System.out.println("Response: " + restTemplate.getForObject("https://jsonplaceholder.typicode.com/todos/1", String.class));
			} catch (Exception e) {
				System.out.println("Failed: " + e.getMessage());
			}
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(MultitrustsslbundleApplication.class, args);
	}

}
