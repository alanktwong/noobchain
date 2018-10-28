package com.pb.noobchain.service;

import java.util.List;

import com.pb.noobchain.domain.Block;

public interface BlockChainService extends TransactionDomainService
{
    List<Block> myFirstChain(int difficulty);

    String serialize(final List<Block> blockChain);

    boolean validateChain(final List<Block> blockChain, final int difficulty);

    List<Block> tryMining(final List<Block> blockChain, final int difficulty);

}
