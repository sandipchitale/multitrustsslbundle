package sandipchitale.multitrustsslbundle;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
import java.util.Random;

@SpringBootApplication
public class MultitrustsslbundleApplication {

	private final Random random = new Random();

	@Bean
	public CommandLineRunner server1CLR (SslBundles sslBundles,
										 RestTemplateBuilder restTemplateBuilder,
										 RestClient.Builder restClientBuilder) {
	    return (args) -> {

			// Note: We are using a single RestTemplate to access and trust three different servers with each
			// having their own certificate
			RestTemplate restTemplate = restTemplateBuilder.build();

			// BLOCK
			// Ideally a single composite SslBundle should do the below compositing
			// of trust material
			// See: https://github.com/spring-projects/spring-boot/issues/38387
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
			trustManagers.addAll(Arrays.asList(sslBundles.getBundle("cacerts")
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
			// BLOCK END

			// With RestTemplate
			restTemplate.setRequestFactory(requestFactory);
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
				// Retrieve a random todo
				int id = Math.abs(1+ random.nextInt(200));
				System.out.println("Trying with RestTemplate https://jsonplaceholder.typicode.com/todos/" + id);
				System.out.println("Response: " + restTemplate.getForObject("https://jsonplaceholder.typicode.com/todos/" + id, String.class));
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
				// Retrieve a random todo
				int id = Math.abs(1+ random.nextInt(200));
				System.out.println("Trying with RestClient https://jsonplaceholder.typicode.com/todos/" + id);
				System.out.println("Response: " + restClient.get().uri("https://jsonplaceholder.typicode.com/todos/" + id).retrieve().body(String.class));
			} catch (Exception e) {
				System.out.println("Failed: " + e.getMessage());
			}
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(MultitrustsslbundleApplication.class, args);
	}

}
