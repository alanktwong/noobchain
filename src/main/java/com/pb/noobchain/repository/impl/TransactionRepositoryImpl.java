package com.pb.noobchain.repository.impl;

import java.util.HashMap;
import java.util.Map;

import com.pb.noobchain.domain.TransactionInput;
import com.pb.noobchain.domain.TransactionOutput;
import com.pb.noobchain.repository.TransactionRepository;

public class TransactionRepositoryImpl implements TransactionRepository
{
    // a rough count of how many transactions have been generated.
    private int sequence = 0;
    private Map<String,TransactionOutput> unspentTxnOutputs = new HashMap<>();

    public void saveTransactionCount(int sequence) {
        this.sequence = sequence;
    }

    public int countTransactions() {
        return this.sequence;
    }

    @Override
    public TransactionOutput getUnspentTransactionOutput(final String transactionOutputId) {
        return unspentTxnOutputs.get(transactionOutputId);
    }

    @Override
    public TransactionOutput addTransactionOutput(final TransactionOutput txnOutput) {
        return unspentTxnOutputs.put(txnOutput.getParentTransactionId(), txnOutput);
    }

    @Override
    public TransactionOutput removeTransactionOutput(final TransactionInput txnInput) {
        final TransactionOutput unspentTransactionOutput = txnInput.getUnspentTransactionOutput();
        return unspentTxnOutputs.remove(unspentTransactionOutput.getId());
    }

    @Override
    public Map<String, TransactionOutput> getUnspentTxnOutputs()
    {
        return unspentTxnOutputs;
    }
}
