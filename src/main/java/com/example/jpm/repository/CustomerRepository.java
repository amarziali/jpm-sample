package com.example.jpm.repository;

import com.example.jpm.model.Customer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentracing.Span;
import io.opentracing.util.GlobalTracer;
import org.opensearch.action.ActionListener;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CustomerRepository {

    public static final String INDEX = "customers";
    private final RestHighLevelClient restHighLevelClient;

    public CustomerRepository(final RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    private final ObjectMapper objectMapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);

    public List<Customer> getSearchResult(SearchResponse response) {

        SearchHit[] searchHit = response.getHits().getHits();

        List<Customer> profileDocuments = new ArrayList<>();

        if (searchHit.length > 0) {
            Arrays.stream(searchHit)
                    .forEach(hit -> profileDocuments
                            .add(objectMapper.convertValue(hit.getSourceAsMap(),
                                    Customer.class))
                    );
        }

        return profileDocuments;
    }

    private Mono<List<Customer>> doSearch(QueryBuilder effectiveQuery) {
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        QueryBuilder queryBuilder = QueryBuilders
                .boolQuery()
                .must(effectiveQuery);

        searchSourceBuilder.query(queryBuilder);
        searchRequest
                .source(searchSourceBuilder)
                .indices(INDEX);

        final Span conversationSpan = GlobalTracer.get().activeSpan();

        return Mono.<SearchResponse>create(monoSink -> {
            try (final var scope = GlobalTracer.get().scopeManager().activate(conversationSpan, false)) {
                restHighLevelClient.searchAsync(searchRequest, RequestOptions.DEFAULT, new ActionListener<>() {
                    @Override
                    public void onResponse(SearchResponse searchResponse) {
                        monoSink.success(searchResponse);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        monoSink.error(e);
                    }
                });
            }
        }).map(this::getSearchResult);
    }

    public Mono<List<Customer>> findAll() {
        return doSearch(QueryBuilders.matchAllQuery());

    }

    public Mono<List<Customer>> findByFirstName(String firstName) {
        return doSearch(QueryBuilders.termQuery("firstName", firstName));
    }

    public Mono<List<Customer>> findByLastName(String lastName) {
        return doSearch(QueryBuilders.termQuery("lastName", lastName));
    }

    public Mono<List<Customer>> findByFirstNameContaining(String name) {
        return doSearch(QueryBuilders.matchQuery("firstName", name));
    }

    public List<Customer> saveAll(final List<Customer> customers) {
        return customers.stream().map(this::save).collect(Collectors.toList());
    }

    public Customer save(final Customer document) {
        UUID uuid = UUID.randomUUID();
        document.setId(uuid.toString());

        Map<String, Object> documentMapper = objectMapper.convertValue(document, Map.class);

        IndexRequest indexRequest = new IndexRequest(INDEX)
                .id(document.getId())
                .source(documentMapper);

        IndexResponse indexResponse = null;
        try {
            indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return document;
    }
}
