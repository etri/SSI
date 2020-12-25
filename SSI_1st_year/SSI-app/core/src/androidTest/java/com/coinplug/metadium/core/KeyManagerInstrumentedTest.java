package com.coinplug.metadium.core;

import android.content.Context;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iitp.core.crypto.DuplicatedKeyException;
import com.iitp.core.crypto.KeyManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.web3j.crypto.Keys;
import org.web3j.crypto.MnemonicUtils;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class KeyManagerInstrumentedTest {
    private final byte[] MESSAGE = "test message".getBytes();

    private void assertGeneral(Context appContext, String address) throws Exception {
        KeyManager keyManager = KeyManager.getInstance();
        assertNotNull(address);
        assertNotNull(keyManager.getPrivateKey(appContext, address));
        assertTrue(keyManager.hasPrivateKey(appContext, address));
        assertEquals(address, Numeric.prependHexPrefix(Keys.getAddress(Sign.signedMessageToKey(MESSAGE, KeyManager.stringToSignatureData(keyManager.signMessage(appContext, address, MESSAGE))))));
        assertEquals(address, Numeric.prependHexPrefix(Keys.getAddress(Sign.signedPrefixedMessageToKey(MESSAGE, KeyManager.stringToSignatureData(keyManager.signPrefixMessage(appContext, address, MESSAGE))))));

        byte[] cipherText = keyManager.encryptECIES(appContext, address, MESSAGE);
        assertArrayEquals(MESSAGE, keyManager.decryptECIES(appContext, address, cipherText));
    }

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        KeyManager keyManager = KeyManager.getInstance();
        keyManager.clear(appContext);

        // generate key
        String address0 = keyManager.generatePrivateKey(appContext);
        assertGeneral(appContext, address0);
        assertNotNull(keyManager.getMnemonic(appContext, address0));
        assertTrue(keyManager.withMnemonic(appContext, address0));

        // import private key
        String addressName1 = "name1";
        String address1 = keyManager.addPrivateKey(appContext, Keys.createEcKeyPair().getPrivateKey(), addressName1);
        assertGeneral(appContext, address1);
        assertNotNull(address1);
        assertNull(keyManager.getMnemonic(appContext, address1));
        assertFalse(keyManager.withMnemonic(appContext, address1));
        assertEquals(addressName1, keyManager.getName(appContext, address1));

        SecureRandom secureRandom = new SecureRandom();
        String mnemonic;
        byte[] entropy = new byte[32];

        // import mnemonic bip44 ether, metamask etc..
        secureRandom.nextBytes(entropy);
        mnemonic = MnemonicUtils.generateMnemonic(entropy);
        String address2 = keyManager.addPrivateKey(appContext, mnemonic, KeyManager.getInstance().getEtherPath(), null);
        assertGeneral(appContext, address2);
        assertNull(keyManager.getMnemonic(appContext, address2));
        assertFalse(keyManager.withMnemonic(appContext, address2));

        // import mnemonic bip44 meta
        mnemonic = "fruit wave dwarf banana earth journey tattoo true farm silk olive fence";
        String address3 = keyManager.addPrivateKey(appContext, mnemonic, KeyManager.getInstance().getMetaPath(), null);
        assertEquals(address3, "0xb04c93f8c3445ec19c79c086fa8dc66b817f5610".toLowerCase());
        assertEquals(Numeric.toHexStringWithPrefix(keyManager.getPrivateKey(appContext, address3)), "0x7044c63e2a2a28857d922a3542a30f1de9358c987e35ea459b36803b260556fc");
        assertGeneral(appContext, address3);
        assertNotNull(keyManager.getMnemonic(appContext, address3));
        assertTrue(keyManager.withMnemonic(appContext, address3));


        // import mnemonic bip44 ledger
        secureRandom.nextBytes(entropy);
        mnemonic = MnemonicUtils.generateMnemonic(entropy);
        String address4 = keyManager.addPrivateKey(appContext, mnemonic, KeyManager.getInstance().getLedgerPath(), null);
        assertGeneral(appContext, address4);
        assertNull(keyManager.getMnemonic(appContext, address4));
        assertFalse(keyManager.withMnemonic(appContext, address4));

        // import mnemonic bip44 testnet
        secureRandom.nextBytes(entropy);
        mnemonic = MnemonicUtils.generateMnemonic(entropy);
        String address5 = keyManager.addPrivateKey(appContext, mnemonic, KeyManager.getInstance().getTestnetPath(), null);
        assertGeneral(appContext, address5);
        assertNull(keyManager.getMnemonic(appContext, address5));
        assertFalse(keyManager.withMnemonic(appContext, address5));

        // import keystore
        final String password = "07079595Ff!";
        WalletFile walletFile = Wallet.createLight(password, Keys.createEcKeyPair());
        String keystoreJson = new ObjectMapper().writeValueAsString(walletFile);
        String address6 = keyManager.addPrivateKey(appContext, keystoreJson, password, null);
        assertGeneral(appContext, address6);
        assertNull(keyManager.getMnemonic(appContext, address6));
        assertFalse(keyManager.withMnemonic(appContext, address6));

        // key list
        List<String> keyList = keyManager.getAddressList(appContext);
        assertEquals(7, keyList.size());

        // duplicate key
        try {
            keyManager.addPrivateKey(appContext, mnemonic, KeyManager.getInstance().getTestnetPath(), null);
            assertTrue(false);
        }
        catch (Exception e) {
            assertTrue(e instanceof DuplicatedKeyException);
        }

        // remove key
        keyManager.removePrivateKey(appContext, address6);
        assertFalse(keyManager.hasPrivateKey(appContext, address6));
        assertNull(keyManager.getPrivateKey(appContext, address6));
        assertNull(keyManager.signMessage(appContext, address6, MESSAGE));
        assertNull(keyManager.signPrefixMessage(appContext, address6, MESSAGE));
        assertNull(keyManager.signTransaction(appContext, address6, RawTransaction.createTransaction(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, address6, null)));
        assertNull(keyManager.encryptECIES(appContext, address6, "test".getBytes()));
        assertNull(keyManager.decryptECIES(appContext, address6, "dfadf".getBytes()));
        assertEquals(6, keyManager.getAddressList(appContext).size());

        // clear
        keyManager.clear(appContext);
        assertEquals(0, keyManager.getAddressList(appContext).size());
        assertNull(keyManager.getPrivateKey(appContext, address0));
        assertFalse(keyManager.hasPrivateKey(appContext, address0));
        assertNull(keyManager.getMnemonic(appContext, address0));
        assertFalse(keyManager.withMnemonic(appContext, address0));
    }


}
