package com.pb.noobchain.service.impl;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import com.pb.noobchain.domain.Block;
import com.pb.noobchain.domain.Transaction;
import com.pb.noobchain.domain.TransactionInput;
import com.pb.noobchain.domain.TransactionOutput;
import com.pb.noobchain.domain.Wallet;
import com.pb.noobchain.exceptions.BrokenChainException;
import com.pb.noobchain.exceptions.UnequalCurrentHashException;
import com.pb.noobchain.exceptions.UnminedChainException;
import com.pb.noobchain.service.BlockChainService;

import com.google.gson.GsonBuilder;

public class BlockChainServiceImpl implements BlockChainService
{
    private static final Logger log = LoggerFactory.getLogger(BlockChainServiceImpl.class);

    private Map<String,TransactionOutput> unspentTxnOutputs = new HashMap<>();

    private float minimumTransaction = 0.00f;

    public void setMinimumTransaction(final float minimumTransaction)
    {
        this.minimumTransaction = minimumTransaction;
    }

    @Override
    public List<Block> myFirstChain(int difficulty) {
        final List<Block> blockChain = Lists.newLinkedList();
        Block genesisBlock = createGenesis(difficulty);
        blockChain.add(genesisBlock);

        Block block2 = addBlock(blockChain, 2, genesisBlock, difficulty);
        addBlock(blockChain, 3, block2, difficulty);

        return blockChain;
    }

    private Block createGenesis(int difficulty) {
        Block genesisBlock = new Block("Hi im the genesis block", "0");
        log.info("Hash for genesis block : {}", genesisBlock.getHash());
        genesisBlock.mine(difficulty);
        return genesisBlock;
    }

    private Block addBlock(List<Block> blockChain, int index, Block previous, int difficulty) {
        Block block = new Block(String.format("Im the %s block", index), previous.getHash());
        log.info("Hash for block {} : {}", index, block.getHash());
        block.mine(difficulty);
        blockChain.add(block);
        return block;
    }

    @Override
    public String serialize(final List<Block> blockChain) {
        String json = new GsonBuilder().setPrettyPrinting().create().toJson(blockChain);
        log.info(json);
        return json;
    }

    @Override
    public boolean validateChain(final List<Block> blockChain, int difficulty) {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        Assert.isTrue(blockChain.size() >= 1);

        //loop through blockchain to check hashes:
        for (int i=1; i < blockChain.size(); i++) {
            currentBlock = blockChain.get(i);
            previousBlock = blockChain.get(i-1);
            //compare registered hash and calculated hash:
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())){
                UnequalCurrentHashException ex = new UnequalCurrentHashException();
                log.error(ex.getMessage());
                throw ex;
            }
            //compare previous hash and registered previous hash
            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())) {
                BrokenChainException ex = new BrokenChainException();
                log.error(ex.getMessage());
                throw ex;
            }
            //check if hash is solved
            if (!currentBlock.getHash().substring(0, difficulty).equals(hashTarget)) {
                UnminedChainException ex = new UnminedChainException();
                log.error(ex.getMessage());
                throw ex;
            }
        }
        return true;
    }


    @Override
    public List<Block> tryMining(final List<Block> blockChain, final int difficulty) {
        for (Block block : blockChain) {
            log.info("Trying to mine block: {} ...", block);
            block.mine(difficulty);
        }
        return blockChain;
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
                TransactionOutput unspentTxnOutput = getUnspentTransactionOutput(input.getTransactionOutputId());
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
        transaction.setTransactionId(transaction.calculateHash());

        List<TransactionOutput> outputs = transaction.getOutputs();
        sendValueToRecipient(transaction, outputs);
        sendLeftOverChangeToSender(transaction, leftOver, outputs);
        transaction.setOutputs(outputs);

        //add outputs to Unspent list
        transaction.getOutputs().forEach(this::addTransactionOutput);

        //remove transaction inputs from UTXO lists as spent:
        transaction.getInputs().stream()
            .filter(i -> i.getUnspentTransactionOutput() != null)
            .forEach(this::removeTransactionOutput);

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

    public Transaction sendFundsToWallet(Wallet wallet, PublicKey _recipient, float value) {
        //gather balance and check funds.
        if (wallet.getBalance(this.unspentTxnOutputs) < value) {
            log.error("#Not Enough funds to send transaction. Transaction Discarded.");
            return null;
        }
        //create array list of inputs
        List<TransactionInput> inputs = Lists.newArrayList();

        float total = 0;

        final Map<String, TransactionOutput> utxosOfWallet = wallet.getUnspentTransactionOutputs();
        for (Map.Entry<String, TransactionOutput> item: utxosOfWallet.entrySet()){
            TransactionOutput UTXO = item.getValue();
            total += UTXO.getValue();
            inputs.add(new TransactionInput(UTXO.getId()));
            if (total > value) {
                break;
            }
        }

        Transaction newTransaction = new Transaction(wallet.getPublicKey(), _recipient , value, inputs);
        newTransaction.generateSignature(wallet.getPrivateKey());

        for (TransactionInput input: inputs) {
            utxosOfWallet.remove(input.getTransactionOutputId());
        }
        wallet.setUnspentTransactionOutputs(utxosOfWallet);

        return newTransaction;
    }

    @Override
    public boolean addTransactionToBlock(Transaction transaction, Block block) {
        return true;
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
}
