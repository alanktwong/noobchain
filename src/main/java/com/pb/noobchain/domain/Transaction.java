package com.pb.noobchain.domain;

import java.security.*;
import java.util.*;

import com.google.common.collect.Lists;
import com.pb.noobchain.service.HashUtil;

public class Transaction {
    // this is also the hash of the transaction.
    private String transactionId;

    // senders address/public key.
    private PublicKey sender;

    // Recipients address/public key.
    private PublicKey recipient;

    public float value;

    // this is to prevent anybody else from spending funds in our wallet.
    private byte[] signature;

    private List<TransactionInput> inputs = Lists.newArrayList();

    private List<TransactionOutput> outputs = Lists.newArrayList();

    // Constructor:
    public Transaction(PublicKey from, PublicKey to, float value,  List<TransactionInput> inputs) {
        this.setSender(from);
        this.setRecipient(to);
        this.setValue(value);
        this.setInputs(inputs);
    }

    private String getData(Transaction txn)
    {
        return HashUtil.getStringFromKey(txn.getSender()) + HashUtil.getStringFromKey(txn.getRecipient()) + Float.toString(txn.getValue());
    }

    //Signs all the data we dont wish to be tampered with.
    public void generateSignature(PrivateKey privateKey) {
        String data = getData(this);
        signature = HashUtil.applyECDSASig(privateKey, data);
    }

    //Verifies the data we signed hasnt been tampered with
    public boolean verifySignature() {
        String data = getData(this);
        return HashUtil.verifyECDSASig(getSender(), data, getSignature());
    }

    //returns sum of inputs(UTXOs) values
    public float getInputsValue() {
        float total = 0;
        for (TransactionInput input : getInputs()) {
            //if Transaction can't be found skip it
            if (input.getUnspentTransactionOutput() == null) {
                continue;
            }
            total += input.getUnspentTransactionOutput().getValue();
        }
        return total;
    }

    //returns sum of outputs:
    public float getOutputsValue() {
        float total = 0;
        for (TransactionOutput output : getOutputs()) {
            total += output.getValue();
        }
        return total;
    }

    public String getTransactionId()
    {
        return transactionId;
    }

    public void setTransactionId(final String transactionId)
    {
        this.transactionId = transactionId;
    }

    public PublicKey getSender()
    {
        return sender;
    }

    public void setSender(final PublicKey sender)
    {
        this.sender = sender;
    }

    public PublicKey getRecipient()
    {
        return recipient;
    }

    public void setRecipient(final PublicKey recipient)
    {
        this.recipient = recipient;
    }

    public float getValue()
    {
        return value;
    }

    public void setValue(final float value)
    {
        this.value = value;
    }

    public byte[] getSignature()
    {
        return signature;
    }

    public void setSignature(final byte[] signature)
    {
        this.signature = signature;
    }

    public List<TransactionInput> getInputs()
    {
        return inputs;
    }

    public void setInputs(final List<TransactionInput> inputs)
    {
        this.inputs = inputs;
    }

    public List<TransactionOutput> getOutputs()
    {
        return outputs;
    }

    public void setOutputs(final List<TransactionOutput> outputs)
    {
        this.outputs = outputs;
    }
}
