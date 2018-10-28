package com.pb.noobchain.service;

import java.security.PublicKey;

import com.pb.noobchain.domain.Block;
import com.pb.noobchain.domain.Transaction;
import com.pb.noobchain.domain.TransactionInput;
import com.pb.noobchain.domain.TransactionOutput;
import com.pb.noobchain.domain.Wallet;

public interface TransactionDomainService
{
    TransactionOutput getUnspentTransactionOutput(final String transactionOutputId);

    TransactionOutput addTransactionOutput(final TransactionOutput txnOutput);

    TransactionOutput removeTransactionOutput(final TransactionInput txnInput);

    boolean processTransaction(Transaction transaction);

    Transaction sendFundsFromWallet(Wallet wallet, PublicKey _recipient, float value);

    boolean addTransactionToBlock(Transaction transaction, Block block);
}
