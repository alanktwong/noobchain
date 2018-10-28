package com.pb.noobchain.service.impl;

import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.pb.noobchain.domain.Block;
import com.pb.noobchain.domain.Transaction;
import com.pb.noobchain.domain.TransactionInput;
import com.pb.noobchain.domain.TransactionOutput;
import com.pb.noobchain.domain.Wallet;
import com.pb.noobchain.repository.TransactionRepository;
import com.pb.noobchain.service.HashUtil;
import com.pb.noobchain.service.TransactionService;

public class TransactionServiceImpl implements TransactionService
{
    // a rough count of how many transactions have been generated.
    private static int SEQUENCE = 0;

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private TransactionRepository transactionRepository;

    private Provider provider = new BouncyCastleProvider();

    private float minimumTransaction = 0.00f;

    public void setMinimumTransaction(final float minimumTransaction)
    {
        this.minimumTransaction = minimumTransaction;
    }

    public void setProvider(final Provider provider)
    {
        if (provider != null) {
            Security.addProvider(provider);
        }
        this.provider = provider;
    }

    public TransactionServiceImpl(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
        this.setProvider(provider);
    }

    @Override
    public boolean processTransaction(Transaction transaction) {

        if (!transaction.verifySignature()) {
            log.error("#Transaction Signature failed to verify");
            return false;
        }

        //gather transaction inputs (Make sure they are unspent):
        final List<TransactionInput> inputs = transaction.getInputs().stream()
            .map(input -> {
                TransactionOutput unspentTxnOutput = transactionRepository.getUnspentTransactionOutput(input.getTransactionOutputId());
                input.setUnspentTransactionOutput(unspentTxnOutput);
                return input;
            }).collect(Collectors.toList());
        transaction.setInputs(inputs);

        //check if transaction is valid:
        if (transaction.getInputsValue() < this.minimumTransaction) {
            log.error("#Transaction Inputs to small: {}", transaction.getInputsValue());
            return false;
        }

        //generate transaction outputs:

        //get value of inputs then the left over change:
        float leftOver = transaction.getInputsValue() - transaction.getValue();
        final String hash = calculateHash(transaction);
        transaction.setTransactionId(hash);

        List<TransactionOutput> outputs = transaction.getOutputs();
        sendValueToRecipient(transaction, outputs);
        sendLeftOverChangeToSender(transaction, leftOver, outputs);
        transaction.setOutputs(outputs);

        //add outputs to Unspent list
        transaction.getOutputs().forEach(transactionRepository::addTransactionOutput);

        //remove transaction inputs from UTXO lists as spent:
        transaction.getInputs().stream()
            .filter(i -> i.getUnspentTransactionOutput() != null)
            .forEach(transactionRepository::removeTransactionOutput);

        return true;
    }

    private void sendLeftOverChangeToSender(final Transaction transaction, final float leftOver, final List<TransactionOutput> outputs)
    {
        final TransactionOutput output = new TransactionOutput(transaction.getSender(), leftOver, transaction.getTransactionId());
        outputs.add(output);
    }

    private void sendValueToRecipient(final Transaction transaction, final List<TransactionOutput> outputs)
    {
        TransactionOutput output = new TransactionOutput(transaction.getRecipient(), transaction.getValue(), transaction.getTransactionId());
        outputs.add(output);
    }

    @Override
    public Transaction sendFundsFromWallet(Wallet fromWallet, PublicKey recipient, float value) {
        //gather balance and check funds.
        if (fromWallet.getBalance(transactionRepository.getUnspentTxnOutputs()) < value) {
            log.error("#Not Enough funds to send transaction. Transaction Discarded.");
            return null;
        }
        //create array list of inputs
        List<TransactionInput> inputs = Lists.newArrayList();

        float total = 0;

        final Map<String, TransactionOutput> utxosOfWallet = fromWallet.getUnspentTransactionOutputs();
        for (Map.Entry<String, TransactionOutput> item: utxosOfWallet.entrySet()){
            TransactionOutput UTXO = item.getValue();
            total += UTXO.getValue();
            inputs.add(new TransactionInput(UTXO.getId()));
            if (total > value) {
                break;
            }
        }

        Transaction newTransaction = new Transaction(fromWallet.getPublicKey(), recipient , value, inputs);
        newTransaction.generateSignature(fromWallet.getPrivateKey());

        for (TransactionInput input: inputs) {
            utxosOfWallet.remove(input.getTransactionOutputId());
        }
        fromWallet.setUnspentTransactionOutputs(utxosOfWallet);

        return newTransaction;
    }

    @Override
    public boolean addTransactionToBlock(Transaction transaction, Block block) {
        // process transaction and check if valid, unless block is genesis block then ignore.
        if (transaction == null) {
            return false;
        }
        if (!HashUtil.PREVIOUS_HASH_OF_GENESIS.equals(block.getPreviousHash())) {

            if (processTransaction(transaction)) {
                log.error("Transaction failed to process. Discarded.");
                return false;
            }
        }
        final List<Transaction> transactions = block.getTransactions();
        transactions.add(transaction);
        block.setTransactions(transactions);
        log.info("Transaction Successfully added to Block");
        return true;
    }

    // This calculates the transaction hash (which will be used as its Id)
    public String calculateHash(Transaction txn) {
        //increase the sequence to avoid 2 identical transactions having the same hash
        SEQUENCE++;
        final String input = HashUtil.getStringFromKey(txn.getSender()) +
            HashUtil.getStringFromKey(txn.getRecipient()) +
            Float.toString(txn.getValue()) +
            SEQUENCE;
        return HashUtil.applySha256(input);
    }
}
