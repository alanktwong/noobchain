package com.pb.noobchain.service;

import java.util.List;

import com.pb.noobchain.domain.Block;

public interface BlockchainService
{
    List<Block> myFirstChain();

    boolean isChainValid(final List<Block> blockchain);

}
