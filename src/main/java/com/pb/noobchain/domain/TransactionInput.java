package com.pb.noobchain.domain;

public class TransactionInput
{
    //Reference to TransactionOutputs -> transactionId
    private String transactionOutputId;

    //Contains the Unspent transaction output
    private TransactionOutput unspentTransactionOutput;

    public TransactionInput(String transactionOutputId) {
        this.transactionOutputId = transactionOutputId;
    }

    public String getTransactionOutputId()
    {
        return transactionOutputId;
    }

    public void setTransactionOutputId(final String transactionOutputId)
    {
        this.transactionOutputId = transactionOutputId;
    }

    public TransactionOutput getUnspentTransactionOutput()
    {
        return unspentTransactionOutput;
    }

    public void setUnspentTransactionOutput(final TransactionOutput unspentTransactionOutput)
    {
        this.unspentTransactionOutput = unspentTransactionOutput;
    }
}
