package com.pb.noobchain.service;

import java.security.PublicKey;

import com.pb.noobchain.domain.Block;
import com.pb.noobchain.domain.Transaction;
import com.pb.noobchain.domain.Wallet;

public interface TransactionService
{
    boolean processTransaction(Transaction transaction);

    Transaction sendFundsFromWallet(Wallet wallet, PublicKey recipient, float value);

    boolean addTransactionToBlock(Transaction transaction, Block block);
}
