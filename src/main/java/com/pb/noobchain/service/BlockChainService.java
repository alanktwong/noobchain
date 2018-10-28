package com.pb.noobchain.service;

import java.util.List;

import com.pb.noobchain.domain.Block;

public interface BlockChainService
{
    String serialize(final List<Block> blockChain);

    boolean validateChain(final List<Block> blockChain);

    List<Block> tryMining(final List<Block> blockChain);

    boolean mineBlockAndAddToChain(Block newBlock, List<Block> blockChain);
}
