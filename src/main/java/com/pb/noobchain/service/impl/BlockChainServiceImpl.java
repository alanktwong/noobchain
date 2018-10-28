package com.pb.noobchain.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
