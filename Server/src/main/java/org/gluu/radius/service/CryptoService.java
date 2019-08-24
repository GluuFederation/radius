package org.gluu.radius.service;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.gluu.oxauth.client.supergluu.impl.ICryptoProviderFactory;
import org.gluu.oxauth.model.crypto.AbstractCryptoProvider;
import org.gluu.oxauth.model.crypto.OxAuthCryptoProvider;
import org.gluu.oxauth.model.crypto.encryption.KeyEncryptionAlgorithm;
import org.gluu.oxauth.model.crypto.signature.SignatureAlgorithm;
import org.gluu.oxauth.model.jwk.Algorithm;
import org.gluu.oxauth.model.jwk.JSONWebKey;
import org.gluu.oxauth.model.jwk.JSONWebKeySet;
import org.gluu.oxauth.model.jwk.KeyType;
import org.gluu.oxauth.model.jwk.Use;
import org.gluu.oxauth.model.util.StringUtils;

import static org.gluu.oxauth.model.jwk.JWKParameter.*;


public class CryptoService implements ICryptoProviderFactory{

    private static final String dnName = "CN=Gluu Radius CA Certificates";
    private BootstrapConfigService bcService;
    private JSONObject serverKeyset;
    private OxAuthCryptoProvider cryptoProvider;
    private List<Algorithm> signAlgorithms;
    private int expiration;
    private int expiration_hours;
    private String authSigningKeyId;

    public CryptoService(BootstrapConfigService bcService, List<Algorithm> signAlgorithms,
         int expiration, int expiration_hours) throws Exception {

        this.bcService = bcService;
        this.signAlgorithms = signAlgorithms;
        this.expiration = expiration;
        this.expiration_hours = expiration_hours;
        this.serverKeyset = new JSONObject();
        this.serverKeyset.put("keys", new JSONArray());

        String keyStoreFile = bcService.getJwtKeyStoreFile();
        String keyStorePin = bcService.getJwtKeyStorePin();
        this.authSigningKeyId = bcService.getJwtAuthKeyId();
        this.cryptoProvider = new OxAuthCryptoProvider(keyStoreFile,keyStorePin,dnName);
        SignatureAlgorithm algo = bcService.getJwtAuthSignAlgo();
        this.authSigningKeyId = cryptoProvider.getAliasByAlgorithmForDeletion(algo,"",Use.SIGNATURE);
    }

    public JSONObject getServerKeyset() {

        return this.serverKeyset;
    }

    public void setServerKeyset(JSONObject serverKeyset) {
        this.serverKeyset = serverKeyset;
    }

    public AbstractCryptoProvider getCryptoProvider() {

        return this.cryptoProvider;
    }

    @Override
    public AbstractCryptoProvider newCryptoProvider() {

        return this.cryptoProvider;
    }
    
    public String getAuthSigningKeyId() {
        
        return this.authSigningKeyId;
    }

    public JSONWebKeySet generateKeys() throws Exception {

        JSONWebKeySet keyset = new JSONWebKeySet();

        synchronized(cryptoProvider) {
            Calendar calendar =  new GregorianCalendar();
            calendar.add(Calendar.DATE,expiration);
            calendar.add(Calendar.HOUR,expiration_hours);

            for(Algorithm algorithm : signAlgorithms) {
               JSONWebKey key = generateKey(calendar, algorithm,Use.SIGNATURE);
               SignatureAlgorithm signalgo = SignatureAlgorithm.fromString(algorithm.name());
               if(bcService.getJwtAuthSignAlgo() == signalgo) {
                   this.authSigningKeyId = key.getKid();
               }
               keyset.getKeys().add(key);
               String oldkey = cryptoProvider.getAliasByAlgorithmForDeletion(signalgo,key.getKid(),Use.SIGNATURE);
               if(oldkey != null) {
                   cryptoProvider.deleteKey(oldkey);
               }

            }

        }

        return keyset;
    }

    private final JSONWebKey generateKey(Calendar expirytime,Algorithm algo,Use use) throws Exception {

        JSONObject result = cryptoProvider.generateKey(algo,expirytime.getTimeInMillis(),use);
        JSONWebKey key = new JSONWebKey();
        key.setKid(result.getString(KEY_ID));
        key.setUse(use);
        key.setAlg(algo);
        if(use == Use.SIGNATURE) {
            SignatureAlgorithm signalgo = SignatureAlgorithm.fromString(algo.name());
            key.setKty(KeyType.fromString(signalgo.getFamily().toString()));
            key.setCrv(signalgo.getCurve());
        }else if(use == Use.ENCRYPTION) {
            KeyEncryptionAlgorithm encalgo = KeyEncryptionAlgorithm.fromName(algo.name());
            key.setKty(KeyType.fromString(encalgo.getFamily()));
        }
        key.setExp(result.optLong(EXPIRATION_TIME));
        key.setN(result.optString(MODULUS));
        key.setE(result.optString(EXPONENT));
        key.setX(result.optString(X));
        key.setY(result.optString(Y));

        JSONArray x5c = result.optJSONArray(CERTIFICATE_CHAIN);
        key.setX5c(StringUtils.toList(x5c));

        return key;
    }

}