package com.pb.noobchain.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.pb.noobchain.domain.Block;
import com.pb.noobchain.domain.Transaction;
import com.pb.noobchain.domain.TransactionInput;
import com.pb.noobchain.domain.TransactionOutput;
import com.pb.noobchain.exceptions.BrokenChainException;
import com.pb.noobchain.exceptions.InvalidTransactionException;
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
        Block genesisBlock = createGenesis(blockChain);
        blockChain.add(genesisBlock);

        Block block2 = addBlock("2", genesisBlock, blockChain);
        addBlock("3", block2, blockChain);

        return blockChain;
    }

    private Block createGenesis(List<Block> blockChain) {
        Block genesisBlock = new Block("1", HashUtil.PREVIOUS_HASH_OF_GENESIS);
        log.debug("Hash for genesis block : {}", genesisBlock.getHash());
        mineBlockAndAddToChain(genesisBlock, blockChain);
        return genesisBlock;
    }

    private Block addBlock(String id, Block previous, List<Block> blockChain) {
        Block block = new Block(id, previous.getHash());
        log.debug("Hash for block {} : {}", block.getId(), block.getHash());
        mineBlockAndAddToChain(block, blockChain);
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

        Block genesisBlock = blockChain.stream()
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
        Transaction genesisTransaction = genesisBlock.getTransactions().stream()
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);

        //a temporary working list of unspent transactions at a given block state.
        Map<String,TransactionOutput> tempUTXOs = new HashMap<>();
        final List<TransactionOutput> outputs = genesisTransaction.getOutputs();
        TransactionOutput firstOutput = outputs.stream().findFirst().orElseThrow(IllegalArgumentException::new);
        tempUTXOs.put(firstOutput.getId(), firstOutput);

        //loop through blockchain to check hashes:
        for (int i = 1; i < blockChain.size(); i++) {
            currentBlock = blockChain.get(i);
            previousBlock = blockChain.get(i-1);

            //compare registered hash and calculated hash:
            if(!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                throw new UnequalCurrentHashException();
            }
            //compare previous hash and registered previous hash
            if(!previousBlock.getHash().equals(currentBlock.getPreviousHash())) {
                throw new BrokenChainException();
            }
            //check if hash is solved
            if(!hasBeenMined(currentBlock)) {
                throw new UnminedChainException();
            }

            //loop thru blockchain's transactions:
            final List<Transaction> transactions = currentBlock.getTransactions();
            for (Transaction currentTxn : transactions)
            {
                checkTransaction(currentTxn, tempUTXOs);
            }

        }
        log.info("Block chain is valid");
        return true;
    }

    private void checkTransaction(final Transaction transaction,
                                  final Map<String, TransactionOutput> tempUTXOs)
    {

        if (!transaction.verifySignature()) {
            String msg = String.format("#Signature on transaction (%s) is invalid",
                transaction.getTransactionId());
            throw new InvalidTransactionException(msg);
        }

        if (transaction.getInputsValue() != transaction.getOutputsValue()) {
            String msg = String.format("#Inputs are not equal to outputs on transaction (%s)",
                transaction.getTransactionId());
            throw new InvalidTransactionException(msg);
        }

        final List<TransactionInput> inputs = transaction.getInputs();
        for (TransactionInput input: inputs) {
            TransactionOutput tempOutput = tempUTXOs.get(input.getTransactionOutputId());

            if (tempOutput == null) {
                String msg = String.format("#Referenced input on transaction (%s) is Missing",
                    transaction.getTransactionId());
                throw new InvalidTransactionException(msg);
            }

            final TransactionOutput unspentTransactionOutput = input.getUnspentTransactionOutput();
            if (unspentTransactionOutput == null) {
                String msg = String.format("#Referenced unspent output transaction (%s) by the input is null",
                    input.getTransactionOutputId());
                throw new InvalidTransactionException(msg);
            }

            Preconditions.checkNotNull(tempOutput);
            if (unspentTransactionOutput.getValue() != tempOutput.getValue()) {
                String msg = String.format("#Referenced input transaction (%s) is invalid",
                    transaction.getTransactionId());
                throw new InvalidTransactionException(msg);
            }

            tempUTXOs.remove(input.getTransactionOutputId());
        }

        final List<TransactionOutput> outputs = transaction.getOutputs();
        for (TransactionOutput output: outputs) {
            tempUTXOs.put(output.getId(), output);
        }

        final TransactionOutput transactionOutput = outputs.stream()
                .findFirst().orElseThrow(IllegalArgumentException::new);

        if (transactionOutput.getRecipient() != transaction.getRecipient()) {
            String msg = String.format("#Transaction (%s) output recipient is not who it should be",
                transaction.getTransactionId());
            throw new InvalidTransactionException(msg);
        }

        final TransactionOutput nextOutput = outputs.get(1);
        if (nextOutput.getRecipient() != transaction.getSender()) {
            String msg = String.format("#Transaction (%s) output 'change' is not sender.",
                transaction.getTransactionId());
            throw new InvalidTransactionException(msg);
        }
    }

    private boolean hasBeenMined(final Block block)
    {
        String hashTarget = HashUtil.createDifficultyString(difficulty);
        return block.getHash().substring(0, difficulty).equals(hashTarget);
    }


    @Override
    public List<Block> tryMining(final List<Block> blockChain) {
        for (Block block : blockChain) {
            log.info("Trying to mine block: {} ...", block.getId());
            block.mine(this.difficulty);
        }
        return blockChain;
    }

    public boolean mineBlockAndAddToChain(Block newBlock, List<Block> blockChain) {
        boolean result = newBlock.mine(difficulty);
        if (result) {
            result &= blockChain.add(newBlock);
        }
        return result;
    }

}
