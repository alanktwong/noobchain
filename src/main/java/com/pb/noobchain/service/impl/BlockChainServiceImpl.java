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
import com.pb.noobchain.service.BlockChainService;

import com.google.gson.GsonBuilder;
import com.pb.noobchain.service.HashUtil;

public class BlockChainServiceImpl implements BlockChainService
{
    private static final Logger log = LoggerFactory.getLogger(BlockChainServiceImpl.class);

    private int difficulty = 5;

    public void setDifficulty(final int difficulty)
    {
        this.difficulty = difficulty;
    }

    @Override
    public List<Block> myFirstChain() {
        final List<Block> blockChain = Lists.newLinkedList();
        Block genesisBlock = createGenesis(this.difficulty);
        blockChain.add(genesisBlock);

        Block block2 = addBlock(blockChain, "2", genesisBlock, this.difficulty);
        addBlock(blockChain, "3", block2, this.difficulty);

        return blockChain;
    }

    private Block createGenesis(int difficulty) {
        Block genesisBlock = new Block("genesis", HashUtil.PREVIOUS_HASH_OF_GENESIS);
        log.info("Hash for genesis block : {}", genesisBlock.getHash());
        genesisBlock.mine(difficulty);
        return genesisBlock;
    }

    private Block addBlock(List<Block> blockChain, String id, Block previous, int difficulty) {
        Block block = new Block(id, previous.getHash());
        log.info("Hash for block {} : {}", block.getId(), block.getHash());
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
    public boolean validateChain(final List<Block> blockChain) {
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
            if (!currentBlock.getHash().substring(0, this.difficulty).equals(hashTarget)) {
                UnminedChainException ex = new UnminedChainException();
                log.error(ex.getMessage());
                throw ex;
            }
        }
        return true;
    }


    @Override
    public List<Block> tryMining(final List<Block> blockChain) {
        for (Block block : blockChain) {
            log.info("Trying to mine block: {} ...", block.getId());
            block.mine(this.difficulty);
        }
        return blockChain;
    }

    @Override
    public boolean mineBlockAndAddToChain(final Block newBlock, final List<Block> blockChain) {
        boolean result = newBlock.mine(difficulty);
        if (result) {
            result &= blockChain.add(newBlock);
        }
        return result;
    }
}
