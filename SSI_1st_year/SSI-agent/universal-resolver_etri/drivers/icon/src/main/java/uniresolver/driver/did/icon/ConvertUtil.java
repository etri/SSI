package uniresolver.driver.did.icon;

import did.Authentication;
import did.PublicKey;
import foundation.icon.did.core.Algorithm;
import foundation.icon.did.core.AlgorithmProvider;
import foundation.icon.did.document.AuthenticationProperty;
import foundation.icon.did.document.EncodeType;
import foundation.icon.did.document.PublicKeyProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConvertUtil {

    private static Logger logger = LoggerFactory.getLogger(ConvertUtil.class);

    /**
     * Convert ICON's Authentication List to UR's Authentication List.
     * @param iconAuth
     * @return
     */
    public static List<Authentication> convertIconAuthenticationToUR(List<AuthenticationProperty> iconAuth) {
        List<Authentication> list = iconAuth.stream().map(auth -> convertIconAuthenticationToUR(auth)).collect(Collectors.toList());
        return list;
    }

    public static Authentication convertIconAuthenticationToUR(AuthenticationProperty iconAuth) {
        String[] types = null;
        if(iconAuth.getType() != null) {
            types = new String[] {iconAuth.getType()};
        }
        String publicKey = iconAuth.getPublicKey();
        return Authentication.build(null, types, publicKey);
    }

    /**
     * Convert ICON's PublicKeyProperties to UR's PublicKey list.
     * @param iconKeys
     * @return
     */
    public static List<PublicKey> convertIconKeyToUR(Map<String, PublicKeyProperty> iconKeys) {
        List<PublicKey> list = new ArrayList<PublicKey>(iconKeys.size());
        for(Map.Entry<String, PublicKeyProperty> entry : iconKeys.entrySet()) {
            list.add(convertIconKeyToUR(entry.getValue()));
        }
        return list;
    }

    /**
     * Convert ICON's PublicKeyProperty to UR's PublicKey
     * @param iconKey
     * @return
     */
    public static PublicKey convertIconKeyToUR(PublicKeyProperty iconKey) {
        String id = null;
        String[] types = null;
        String publicKeyBase64 = null;
        String publicKeyBase58 = null;
        String publicKeyHex = null;
        String publicKeyPem = null;

        long created = iconKey.getCreated();
        long revoked = iconKey.getRevoked();

        //logger.info("convertIconKeyToUR() created=" + created);

        id = iconKey.getId();
        List<String> pTypes = iconKey.getType();
        types = pTypes.stream().toArray(String[]::new);
        if(iconKey.getEncodeType() == EncodeType.BASE64) {
            publicKeyBase64 = publicKeyToString(iconKey);
        }
        else if(iconKey.getEncodeType() == EncodeType.HEX) {
            publicKeyHex = publicKeyToString(iconKey);
        }
        PublicKey pKey =  PublicKey.build(id, types, publicKeyBase64, publicKeyBase58, publicKeyHex, publicKeyPem);
        pKey.setJsonLdObjectKeyValue("created", Long.valueOf(created));
        pKey.setJsonLdObjectKeyValue("revoked", Long.valueOf(revoked));
        return pKey;
    }

    /**
     * ICON public key to string.
     * @param iconKey
     * @return
     */
    private static String publicKeyToString(PublicKeyProperty iconKey) {
        Algorithm algorithm = AlgorithmProvider.create(iconKey.getAlgorithmType());
        String pub = iconKey.getEncodeType().encode(algorithm.publicKeyToByte(iconKey.getPublicKey()));
        return pub;
    }
}
