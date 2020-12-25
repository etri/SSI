package uniresolver.driver.did.icon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import did.Authentication;
import did.PublicKey;
import foundation.icon.did.document.AuthenticationProperty;
import foundation.icon.did.document.Document;
import foundation.icon.did.document.PublicKeyProperty;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import did.DIDDocument;
import did.Service;
import uniresolver.ResolutionException;
import uniresolver.driver.Driver;
import uniresolver.result.ResolveResult;

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

	public DidIconDriver() {

		//uniresolver_driver_did_icon_node_url = System.getenv("uniresolver_driver_did_icon_node_url");
		//uniresolver_driver_did_icon_score_addr = System.getenv("uniresolver_driver_did_icon_score_addr");
		//uniresolver_driver_did_icon_network_id = System.getenv("uniresolver_driver_did_icon_network_id");
		// ETRI Server information
		uniresolver_driver_did_icon_node_url = "http://192.168.0.6:9080/api/v3";
		uniresolver_driver_did_icon_score_addr = "cxbb270900bd1906acef0b9885fb14fa199f41603d";
		uniresolver_driver_did_icon_network_id = "15359981";
		log.debug("#### uniresolver_driver_did_icon_node_url=" + uniresolver_driver_did_icon_node_url);
		log.debug("#### uniresolver_driver_did_icon_score_addr=" + uniresolver_driver_did_icon_score_addr);
		log.debug("#### uniresolver_driver_did_icon_network_id=" + uniresolver_driver_did_icon_network_id);
		iconService = new IconDidService(uniresolver_driver_did_icon_node_url,
				uniresolver_driver_did_icon_score_addr, uniresolver_driver_did_icon_network_id);

	}

	@Override
	public ResolveResult resolve(String identifier) throws ResolutionException {
		//log.debug("===========================================");

		log.debug("##### resolve() identifier=" + identifier);
		log.debug("#### this : " + this + ", iconService=" + iconService);

		//IconDidService iconService = new IconDidService(uniresolver_driver_did_icon_node_url,
		//		uniresolver_driver_did_icon_score_addr, uniresolver_driver_did_icon_network_id);
		Document doc = iconService.readDocument(identifier);
		if(doc == null) {
			return null;
		}

		// public key
		Map<String, PublicKeyProperty> publicKeyProperty = doc.getPublicKeyProperty();
		List<AuthenticationProperty> iconAuth = doc.getAuthentication();
		if(publicKeyProperty == null || iconAuth == null) {
			return null;
		}

		List<PublicKey> publicKeyList = ConvertUtil.convertIconKeyToUR(publicKeyProperty);
		// authentications....
		List<Authentication> authList = ConvertUtil.convertIconAuthenticationToUR(iconAuth);

		List<Service> services = new ArrayList<Service>();
		DIDDocument didDocument = DIDDocument.build(identifier, publicKeyList, authList, services);
		log.debug("#### resolver() return");
		return ResolveResult.build(didDocument);
	}

	public Map<String, Object> properties() {
		Map<String, Object> properties = new HashMap<String, Object> ();
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
