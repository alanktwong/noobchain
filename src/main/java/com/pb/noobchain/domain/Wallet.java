package com.pb.noobchain.domain;


import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.Map;

import com.google.common.collect.Maps;

public class Wallet {
    private PrivateKey privateKey;

    private PublicKey publicKey;

    //only UTXOs owned by this wallet.
    public Map<String,TransactionOutput> unspentTransactionOutputs = Maps.newHashMap();

    public Wallet(){
        generateKeyPair(this);
    }

    public void generateKeyPair(Wallet wallet) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            // Initialize the key generator and generate a KeyPair
            keyGen.initialize(ecSpec, random);   //256 bytes provides an acceptable security level
            KeyPair keyPair = keyGen.generateKeyPair();
            // Set the public and private keys from the keyPair
            wallet.setPrivateKey(keyPair.getPrivate());
            wallet.setPublicKey(keyPair.getPublic());
        } catch(Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public float getBalance(Map<String,TransactionOutput> utxos) {
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item: utxos.entrySet()) {
            TransactionOutput unspentTransactionOutput = item.getValue();
            //if output belongs to me ( if coins belong to me )
            if (unspentTransactionOutput.isMine(publicKey)) {
                //add it to our list of unspent transactions.
                unspentTransactionOutputs.put(unspentTransactionOutput.getId(), unspentTransactionOutput);
                total += unspentTransactionOutput.getValue();
            }
        }
        return total;
    }

    public Map<String, TransactionOutput> getUnspentTransactionOutputs()
    {
        return unspentTransactionOutputs;
    }

    public void setUnspentTransactionOutputs(final Map<String, TransactionOutput> unspentTransactionOutputs)
    {
        this.unspentTransactionOutputs = unspentTransactionOutputs;
    }

    public PrivateKey getPrivateKey()
    {
        return privateKey;
    }

    public void setPrivateKey(final PrivateKey privateKey)
    {
        this.privateKey = privateKey;
    }

    public PublicKey getPublicKey()
    {
        return publicKey;
    }

    public void setPublicKey(final PublicKey publicKey)
    {
        this.publicKey = publicKey;
    }
}
