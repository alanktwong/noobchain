package com.pb.noobchain.domain;


import java.security.*;
import java.util.Map;

import com.google.common.collect.Maps;
import com.pb.noobchain.service.HashUtil;

public class Wallet {
    private String id;

    private PrivateKey privateKey;

    private PublicKey publicKey;

    //only UTXOs owned by this wallet.
    public Map<String,TransactionOutput> unspentTransactionOutputs = Maps.newHashMap();

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
