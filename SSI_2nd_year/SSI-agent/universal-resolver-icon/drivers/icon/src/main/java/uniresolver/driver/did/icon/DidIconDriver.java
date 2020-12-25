package uniresolver.driver.did.icon;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import did.Authentication;
import did.DIDDocument;
import did.PublicKey;
import foundation.icon.did.document.Document;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.result.ResolveResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
public class DidIconDriver implements Driver {

    private static Logger log = LoggerFactory.getLogger(DidIconDriver.class);

    public static final String DEFAULT_BLOCKSTACK_CORE_URL = "https://core.blockstack.org";
    public static final HttpClient DEFAULT_HTTP_CLIENT = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();

    private String blockstackCoreUrl = DEFAULT_BLOCKSTACK_CORE_URL;
    private HttpClient httpClient = DEFAULT_HTTP_CLIENT;

    private String uniresolver_driver_did_icon_node_url;
    private String uniresolver_driver_did_icon_score_addr;
    private String uniresolver_driver_did_icon_network_id;
    private IconDidService iconService;

    private Cache<String, String> documentCache;

    public DidIconDriver() {

        uniresolver_driver_did_icon_node_url = System.getenv("uniresolver_driver_did_icon_node_url");
        uniresolver_driver_did_icon_score_addr = System.getenv("uniresolver_driver_did_icon_score_addr");
        uniresolver_driver_did_icon_network_id = System.getenv("uniresolver_driver_did_icon_network_id");
        log.debug("#### uniresolver_driver_did_icon_node_url=" + uniresolver_driver_did_icon_node_url);
        log.debug("#### uniresolver_driver_did_icon_score_addr=" + uniresolver_driver_did_icon_score_addr);
        log.debug("#### uniresolver_driver_did_icon_network_id=" + uniresolver_driver_did_icon_network_id);
        iconService = new IconDidService(uniresolver_driver_did_icon_node_url,
                uniresolver_driver_did_icon_score_addr, uniresolver_driver_did_icon_network_id);

        documentCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }

    String readDocument(String identifier) throws Exception {
        long start = System.currentTimeMillis();
        String resolveResultStr = documentCache.getIfPresent(identifier);
        // cache return
        if (resolveResultStr != null) {
            long elapsed = System.currentTimeMillis() - start;
            log.debug("{} cache resolved. elapsed time:{} ms, size:{}", identifier, elapsed, documentCache.estimatedSize());
            return resolveResultStr;
        } else {
            ResolveResult resolveResult = resolve(identifier);
            resolveResultStr = resolveResult.toJson();
            documentCache.put(identifier, resolveResultStr);
            return resolveResultStr;
        }
    }

    @Override
    public ResolveResult resolve(String identifier) throws ResolutionException {
        //log.debug("===========================================");
        long start = System.currentTimeMillis();

        log.debug("##### resolve() identifier=" + identifier);
        log.debug("#### this : " + this + ", iconService=" + iconService);

        //IconDidService iconService = new IconDidService(uniresolver_driver_did_icon_node_url,
        //		uniresolver_driver_did_icon_score_addr, uniresolver_driver_did_icon_network_id);
        Document doc = iconService.readDocument(identifier);
        long end = System.currentTimeMillis();
        log.debug("#### 1. elapsed time:{} ms", (end - start));

        if (doc != null) {
            // public key
            List<PublicKey> publicKeyList = ConvertUtil.convertIconKeyToUR(doc.getPublicKeyProperty());
            // authentications....
            List<Authentication> authList = ConvertUtil.convertIconAuthenticationToUR(doc.getAuthentication());

            List<did.Service> services = new ArrayList<did.Service>();
            DIDDocument didDocument = DIDDocument.build(identifier, publicKeyList, authList, services);
            log.debug("#### resolver() return");

            end = System.currentTimeMillis();
            log.debug("#### 2. elapsed time:{} ms", (end - start));
            return ResolveResult.build(didDocument);
        } else {
            throw new ResolutionException("Failed to read did document");
        }
    }

    public Map<String, Object> properties() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("blockstackCoreUrl", this.getBlockstackCoreUrl());
        return properties;
    }

    /*
     * Getters and setters
     */

    public HttpClient getHttpClient() {

        return this.httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {

        this.httpClient = httpClient;
    }

    public String getBlockstackCoreUrl() {

        return this.blockstackCoreUrl;
    }

    public void setBlockstackCoreUrl(String stackUrl) {
        this.blockstackCoreUrl = stackUrl;
    }


}
