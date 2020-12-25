package uniresolver.driver.did.icon;


import com.google.gson.JsonSyntaxException;
import foundation.icon.did.DidService;
import foundation.icon.did.document.Converters;
import foundation.icon.did.document.Document;
import foundation.icon.did.exceptions.ResolveException;
import foundation.icon.icx.IconService;
import foundation.icon.icx.data.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;

public class IconDidService {

    private static final Logger logger = LoggerFactory.getLogger(IconDidService.class);
    // 을지로..
//    private static final String SCORE_ADDR = "cxf800a0c1c351bde3efd4af8c79fcb77b585e7de5";

    // 여의도
//    private static final String SCORE_ADDR = "cxf800a0c1c351bde3efd4af8c79fcb77b585e7de5";
//    private static final String NETWORK_ID = "3";
//    private static final String NODE_URL = "https://bicon.net.solidwallet.io/api/v3";

    // 메인넷
//    private static final String SCORE_ADDR = "cxcaef4255ec5cb784594655fa5ff62ce09a4f8dfa";
//    private static final String NETWORK_ID = "1";
//    private static final String NODE_URL = "https://wallet.icon.foundation/api/v3";

    private String scoreAddr;
    private String networkId = "1";
    private String nodeUrl;
    private DidService didService;

    public IconDidService(String nodeUrl, String scoreAddr, String netId) {
        this.nodeUrl = nodeUrl;
        this.networkId = netId;
        this.scoreAddr = scoreAddr;
        Address scoreAddress = new Address(scoreAddr);
        BigInteger networkId = new BigInteger(netId);
        IconService iconService = IconServiceFactory.createInstance(nodeUrl);
        didService = new DidService(iconService, networkId, scoreAddress);
    }

    public Document readDocument(String did) {
        logger.debug("readDocument() did: " + did);

        if (did == null) {
            throw new ResolveException("did cannot be null.");
        }

        Document document = null;
        try {
            document = didService.readDocument(did);
        } catch (IOException e) {
            logger.error("Failed to readDocument. msg:{}", e.getMessage());
            throw new ResolveException(e.getMessage());
        }
        //logger.debug("Document=" + document);

        String json = document.toJson();
        try {
            return Converters.gson().fromJson(json, Document.class);
        } catch (JsonSyntaxException e) {
            throw new ResolveException("'" + json + "' parsing error.");
        }
    }
}
