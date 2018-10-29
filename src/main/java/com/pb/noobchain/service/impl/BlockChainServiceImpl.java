package com.pb.noobchain.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.google.common.base.Preconditions;
import com.pb.noobchain.domain.Block;
import com.pb.noobchain.exceptions.BrokenChainException;
import com.pb.noobchain.exceptions.UnequalCurrentHashException;
import com.pb.noobchain.exceptions.UnminedChainException;
import com.pb.noobchain.service.BlockChainService;

import com.google.gson.GsonBuilder;

public class BlockChainServiceImpl implements BlockChainService
{
    private static final Logger log = LoggerFactory.getLogger(BlockChainServiceImpl.class);

    private int difficulty = 5;

    public void setDifficulty(final int difficulty)
    {
        this.difficulty = difficulty;
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
        Preconditions.checkArgument(blockChain.size() >= 1, "Can only validate chains that have more than the genesis block");

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
