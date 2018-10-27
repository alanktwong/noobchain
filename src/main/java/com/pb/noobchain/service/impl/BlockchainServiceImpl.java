package com.pb.noobchain.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.pb.noobchain.domain.Block;
import com.pb.noobchain.service.BlockchainService;

public class BlockchainServiceImpl implements BlockchainService
{
    private static final Logger log = LoggerFactory.getLogger(BlockchainServiceImpl.class);

    @Override
    public List<Block> myFirstChain() {
        Block genesisBlock = new Block("Hi im the first block", "0");
        log.info("Hash for block 1 : {}", genesisBlock.getHash());

        Block block2 = new Block("Im the 2nd block", genesisBlock.getHash());
        log.info("Hash for block 2 : {}", block2.getHash());

        Block block3 = new Block("Im the 3rd block", block2.getHash());
        log.info("Hash for block 3 : {}", block3.getHash());

        return Lists.newArrayList(genesisBlock);
    }

    public boolean isChainValid(final List<Block> blockchain) {
        Block currentBlock;
        Block previousBlock;

        //loop through blockchain to check hashes:
        for (int i=1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i-1);
            //compare registered hash and calculated hash:
            if (!currentBlock.getHash().equals(currentBlock.calculateHash()) ){
                System.out.println("Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())) {
                System.out.println("Previous Hashes not equal");
                return false;
            }
        }
        return true;
    }
}
