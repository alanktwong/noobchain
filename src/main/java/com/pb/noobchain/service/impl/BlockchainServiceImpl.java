package com.pb.noobchain.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import com.pb.noobchain.domain.Block;
import com.pb.noobchain.exceptions.BrokenChainException;
import com.pb.noobchain.exceptions.UnequalCurrentHashException;
import com.pb.noobchain.exceptions.UnminedChainException;
import com.pb.noobchain.service.BlockchainService;

import com.google.gson.GsonBuilder;

public class BlockchainServiceImpl implements BlockchainService
{
    private static final Logger log = LoggerFactory.getLogger(BlockchainServiceImpl.class);

    @Override
    public List<Block> myFirstChain(int difficulty) {
        final List<Block> blockchain = Lists.newArrayList();
        Block genesisBlock = new Block("Hi im the first block", "0");
        blockchain.add(genesisBlock);
        log.info("Hash for block 1 : {}", genesisBlock.getHash());
        genesisBlock.mine(difficulty);

        Block block2 = new Block("Im the 2nd block", genesisBlock.getHash());
        blockchain.add(block2);
        log.info("Hash for block 2 : {}", block2.getHash());
        block2.mine(difficulty);

        Block block3 = new Block("Im the 3rd block", block2.getHash());
        blockchain.add(block3);
        log.info("Hash for block 3 : {}", block3.getHash());
        block3.mine(difficulty);

        return blockchain;
    }

    @Override
    public String serialize(final List<Block> blockchain) {
        String json = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
        log.info(json);
        return json;
    }

    @Override
    public boolean isChainValid(final List<Block> blockchain, int difficulty) {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[difficulty]).replace('\0', '0');
        Assert.isTrue(blockchain.size() >= 1);

        //loop through blockchain to check hashes:
        for (int i=1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
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
    public List<Block> tryMining(final List<Block> blockchain, final int difficulty) {
        for (Block block : blockchain) {
            log.info("Trying to mine block: {} ...", block);
            block.mine(difficulty);
        }
        return blockchain;
    }
}
