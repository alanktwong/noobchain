package com.pb.noobchain.service;

import java.util.List;

import com.pb.noobchain.domain.Block;

public interface BlockchainService
{
    List<Block> myFirstChain(int difficulty);

    String serialize(final List<Block> blockchain);

    boolean isChainValid(final List<Block> blockchain, final int difficulty);

    List<Block> tryMining(final List<Block> blockchain, final int difficulty);

}
