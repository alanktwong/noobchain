package com.pb.noobchain.domain;

import java.security.PublicKey;

import com.pb.noobchain.service.util.HashUtil;

public class TransactionOutput
{
    private String id;

    //also known as the new owner of these coins.
    private PublicKey recipient;

    //the amount of coins they own
    private float value;

    //the id of the transaction this output was created in
    private String parentTransactionId;

    //Constructor
    public TransactionOutput(PublicKey recipient, float value, String parentTransactionId) {
        this.setRecipient(recipient);
        this.setValue(value);
        this.setParentTransactionId(parentTransactionId);

        this.setId(createId(this));
    }

    private String createId(TransactionOutput txnOutput)
    {
        final String input = HashUtil.getStringFromKey(txnOutput.getRecipient()) + Float.toString(txnOutput.getValue()) + txnOutput.getParentTransactionId();
        return HashUtil.applySha256(input);
    }


    //Check if coin belongs to you
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == recipient);
    }

    public String getId()
    {
        return id;
    }

    public void setId(final String id)
    {
        this.id = id;
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

    public String getParentTransactionId()
    {
        return parentTransactionId;
    }

    public void setParentTransactionId(final String parentTransactionId)
    {
        this.parentTransactionId = parentTransactionId;
    }
}
