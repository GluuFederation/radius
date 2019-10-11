package org.gluu.radius.service;

import java.io.File;
import java.io.FileWriter;
import java.security.PrivateKey;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.log4j.Logger;
import org.bouncycastle.openssl.PEMEncryptor;
import org.bouncycastle.openssl.jcajce.JcaMiscPEMGenerator;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;
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

    private static final Logger log = Logger.getLogger(CryptoService.class);

    private static final String dnName = "CN=Gluu Radius CA Certificates";
    private static final String PRIVATE_KEY_ENC_ALGORITHM = "AES-256-CBC";
    private static final String PRIVATE_KEY_FILENAME = "/gluu-radius.private-key.pem";
    private BootstrapConfigService bcService;
    private JSONObject serverKeyset;
    private OxAuthCryptoProvider cryptoProvider;
    private List<Algorithm> signAlgorithms;
    private int expiration;
    private int expiration_hours;
    private String authSigningKeyId;
    private ReadWriteLock cryptoLock;

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
        SignatureAlgorithm signalgo = bcService.getJwtAuthSignAlgo();
        Algorithm algo = Algorithm.fromString(signalgo.getName());
        this.authSigningKeyId = cryptoProvider.getAliasByAlgorithmForDeletion(algo,"",Use.SIGNATURE);
        log.info(String.format("Auth signing keyId: %s",this.authSigningKeyId));
        this.cryptoLock = new ReentrantReadWriteLock();
    }

    public JSONObject getServerKeyset() {

        return this.serverKeyset;
    }

    public void setServerKeyset(JSONObject serverKeyset) {
        this.serverKeyset = serverKeyset;
    }

    public AbstractCryptoProvider getCryptoProvider() {

        synchronized(cryptoProvider) {
            return this.cryptoProvider;
        }
    }

    @Override
    public AbstractCryptoProvider newCryptoProvider() {

        return getCryptoProvider();
    }
    
    public String getAuthSigningKeyId() {
        
        return this.authSigningKeyId;
    }

    public final void beginReadOpts() {

        this.cryptoLock.readLock().lock();
    }

    public final void endReadOpts() {

        this.cryptoLock.readLock().unlock();
    }

    public final void beginWriteOpts() {

        this.cryptoLock.writeLock().lock();
    }

    public final void endWriteOpts() {

        this.cryptoLock.writeLock().unlock();
    }

    public PrivateKey getAuthenticationPrivateKey() throws Exception {

        return cryptoProvider.getPrivateKey(this.authSigningKeyId);
    }

    public JSONWebKeySet generateKeys() throws Exception {

        JSONWebKeySet keyset = new JSONWebKeySet();

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
            String oldkey = cryptoProvider.getAliasByAlgorithmForDeletion(algorithm,key.getKid(),Use.SIGNATURE);
            if(oldkey != null) {
                cryptoProvider.deleteKey(oldkey);
            }

        }

        return keyset;
    }

    public void exportAuthPrivateKeyToPem() throws Exception {

        File keystorefile = new File(bcService.getJwtKeyStoreFile());
        String parentdir = keystorefile.getParent();
        if(parentdir != null) {
            File pkeyfile  = new File(parentdir+PRIVATE_KEY_FILENAME);
            exportAuthPrivateKeyToPem(pkeyfile);
        }
    }

    public void exportAuthPrivateKeyToPem(File outfile) throws Exception {
        exportAuthPrivateKeyToPem(outfile,bcService.getJwtKeyStorePin());
    }

    public void exportAuthPrivateKeyToPem(File outfile,String passphrase) throws Exception {

        FileWriter filewriter = null;
        JcaPEMWriter pemwriter = null;
        try {
            if(!outfile.canWrite()) {
                log.warn(String.format("The private key file %s is not writable.",outfile.getAbsolutePath()));
                return;   
            }
            final PEMEncryptor encryptor = new JcePEMEncryptorBuilder(PRIVATE_KEY_ENC_ALGORITHM)
                .build(passphrase.toCharArray());
            PrivateKey privatekey = cryptoProvider.getPrivateKey(authSigningKeyId);
            final JcaMiscPEMGenerator pemgenerator = new JcaMiscPEMGenerator(privatekey,encryptor);
            filewriter = new FileWriter(outfile);
            pemwriter = new JcaPEMWriter(filewriter);
            pemwriter.writeObject(pemgenerator);
        }finally {
            if(pemwriter != null)
                pemwriter.close();
            if(filewriter != null)
                filewriter.close();
        }
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