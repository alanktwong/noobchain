package com.pb.noobchain.repository;

import java.util.Map;

import com.pb.noobchain.domain.TransactionInput;
import com.pb.noobchain.domain.TransactionOutput;

public interface TransactionRepository
{
    Map<String, TransactionOutput> getUnspentTxnOutputs();

    TransactionOutput getUnspentTransactionOutput(final String transactionOutputId);

    TransactionOutput addTransactionOutput(final TransactionOutput txnOutput);

    TransactionOutput removeTransactionOutput(final TransactionInput txnInput);
}
