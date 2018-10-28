package com.pb.noobchain.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pb.noobchain.service.util.HashUtil;

public class Block
{
    private static final Logger log = LoggerFactory.getLogger(Block.class);

    private String id;

    private String hash;

    private String previousHash;

    private String merkleRoot;

    //our data will be a simple message.
    private List<Transaction> transactions = new ArrayList<>();

    //as number of milliseconds since 1/1/1970.
    private long timeStamp;

    private int nonce;

    private boolean mined;

    //Block Constructor.
    public Block(String id, String previousHash) {
        this.id = id;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    private String calculateHashImpl(String previousHash, long timeStamp, String merkleRoot) {
        final String toHash = previousHash +
            Long.toString(timeStamp) +
            Integer.toString(nonce) +
            merkleRoot;
        return HashUtil.applySha256(toHash);
    }

    public String calculateHash() {
        return calculateHashImpl(getPreviousHash(), getTimeStamp(), getMerkleRoot());
    }

    private boolean mineImpl(Block block, int difficulty) {
        if (isMined()) {
            log.info("Block {} already mined!!!", block.getId());
            return isMined();
        }
        String merkleRoot = HashUtil.getMerkleRoot(block.getTransactions());
        block.setMerkleRoot(merkleRoot);

        //Create a string with difficulty * "0"
        String target = HashUtil.createDifficultyString(difficulty);
        while (!block.getHash().substring(0, difficulty).equals(target)) {
            int nonce = block.getNonce();
            nonce++;
            block.setNonce(nonce);
            block.setHash(block.calculateHash());
        }
        this.mined = true;
        log.info("Block {} mined!!! New hash: {}", block.getId(), block.getHash());
        return mined;
    }

    public boolean mine(int difficulty) {
       return mineImpl(this, difficulty);
    }

    public String getId()
    {
        return id;
    }

    public void setId(final String id)
    {
        this.id = id;
    }

    public String getHash()
    {
        return hash;
    }

    public void setHash(final String hash)
    {
        this.hash = hash;
    }

    public String getPreviousHash()
    {
        return previousHash;
    }

    public void setPreviousHash(final String previousHash)
    {
        this.previousHash = previousHash;
    }

    public long getTimeStamp()
    {
        return timeStamp;
    }

    public void setTimeStamp(final long timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public int getNonce()
    {
        return nonce;
    }

    public void setNonce(final int nonce)
    {
        this.nonce = nonce;
    }

    public String getMerkleRoot()
    {
        return merkleRoot;
    }

    public void setMerkleRoot(final String merkleRoot)
    {
        this.merkleRoot = merkleRoot;
    }

    public List<Transaction> getTransactions()
    {
        return transactions;
    }

    public void setTransactions(final List<Transaction> transactions)
    {
        this.transactions = transactions;
    }

    public boolean isMined()
    {
        return mined;
    }

    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.toString(this);
    }
}
