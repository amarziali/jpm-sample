package com.example.jpm.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;

/**
 * @author Angel Conde
 */
@Configuration
public class OpenSearchRestClientConfiguration {

    @Value("${opensearch.url:https://localhost:9200}")
    private URL endpoint;

    @Value("${opensearch.user:admin}")
    private String user;

    @Value("${opensearch.password:admin}")
    private String password;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(user, password));
        //Create a client.
        RestClientBuilder builder = RestClient.builder(new HttpHost(endpoint.getHost(), endpoint.getPort(), endpoint.getProtocol()))
                .setHttpClientConfigCallback(httpClientBuilder ->
                {
                    try {
                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
                                .setSSLContext(SSLContexts
                                        .custom()
                                        .loadTrustMaterial(new TrustAllStrategy())
                                        .build());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        return new RestHighLevelClient(builder);
    }
}
