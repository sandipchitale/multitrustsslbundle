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
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@SpringBootApplication
public class MultitrustsslbundleApplication {

	@Bean
	public CommandLineRunner server1CLR (SslBundles sslBundles,
										 RestTemplateBuilder restTemplateBuilder,
										 RestClient.Builder restClientBuilder) {
		return (args) -> {

			// Note: We are using a single RestTemplate to access and trust three different servers with each
			// having their own certificate
			RestTemplate restTemplate = restTemplateBuilder.build();

			// Ideally a single composite SslBundle should do the below compositing
			// of trust material
			// See: https://github.com/spring-projects/spring-boot/issues/38387
			// BLOCK
			List<TrustManager> trustManagers = new LinkedList<>();

			// SslBundle representing that wraps server1-truststore.jks
			// and trusts the server server1:8081's certificate
			trustManagers.addAll(Arrays.asList(sslBundles.getBundle("server1")
					.getManagers().getTrustManagers()));

			// SslBundle representing that wraps server2-truststore.jks
			// and trusts the server server2:8082's certificate
			trustManagers.addAll(Arrays.asList(sslBundles.getBundle("server2")
					.getManagers().getTrustManagers()));

			// SslBundle representing JDK's trust store
			// and trusts any public signed certificates signed by CA Authorities
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

			restTemplate.setRequestFactory(requestFactory);
			// END BLOCK


			// With RestTemplate
			try {
				System.out.println("Trying with RestTemplate https://server1:8081");
				System.out.println("Response: " + restTemplate.getForObject("https://server1:8081", String.class));
			} catch (Exception e) {
				System.out.println("Failed: " + e.getMessage());
			}
			try {
				System.out.println("Trying with RestTemplate https://server2:8082");
				System.out.println("Response: " + restTemplate.getForObject("https://server2:8082", String.class));
			} catch (Exception e) {
				System.out.println("Failed: " + e.getMessage());
			}
			try {
				System.out.println("Trying with RestTemplate https://jsonplaceholder.typicode.com/todos/1");
				System.out.println("Response: " + restTemplate.getForObject("https://jsonplaceholder.typicode.com/todos/1", String.class));
			} catch (Exception e) {
				System.out.println("Failed: " + e.getMessage());
			}

			// With RestClient
			RestClient restClient = restClientBuilder
					.requestFactory(requestFactory)
					.build();

			try {
				System.out.println("Trying with RestClient https://server1:8081");
				System.out.println("Response: " + restClient.get().uri("https://server1:8081").retrieve().body(String.class));
			} catch (Exception e) {
				System.out.println("Failed: " + e.getMessage());
			}
			try {
				System.out.println("Trying with RestClient https://server2:8082");
				System.out.println("Response: " + restClient.get().uri("https://server2:8082").retrieve().body(String.class));
			} catch (Exception e) {
				System.out.println("Failed: " + e.getMessage());
			}
			try {
				System.out.println("Trying with RestClient https://jsonplaceholder.typicode.com/todos/1");
				System.out.println("Response: " + restClient.get().uri("https://jsonplaceholder.typicode.com/todos/1").retrieve().body(String.class));
			} catch (Exception e) {
				System.out.println("Failed: " + e.getMessage());
			}
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(MultitrustsslbundleApplication.class, args);
	}

}
