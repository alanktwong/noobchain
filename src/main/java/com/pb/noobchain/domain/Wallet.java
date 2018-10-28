package com.pb.noobchain.domain;


import java.security.*;
import java.util.Map;

import com.google.common.collect.Maps;
import com.pb.noobchain.service.HashUtil;

public class Wallet {
    private String id;

    // Our private key is used to sign our transactions, so that nobody can spend our noobCoins other than the owner of the private key
    private PrivateKey privateKey;

    // Public key will act as our address. It’s OK to share this public key with others to receive payment.
    private PublicKey publicKey;

    //only UTXOs owned by this wallet.
    private Map<String,TransactionOutput> unspentTransactionOutputs = Maps.newHashMap();

    public Wallet(final String id){
        this.id = id;
        HashUtil.generateKeyPair().ifPresent(keyPair -> {
            // Set the public and private keys from the keyPair
            setPrivateKey(keyPair.getPrivate());
            setPublicKey(keyPair.getPublic());
        });
    }

    public String getId()
    {
        return id;
    }

    public void setId(final String id)
    {
        this.id = id;
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
