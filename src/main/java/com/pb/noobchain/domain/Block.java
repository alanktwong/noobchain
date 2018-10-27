package com.pb.noobchain.domain;

import java.util.Date;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pb.noobchain.service.HashUtil;

public class Block
{
    private static final Logger log = LoggerFactory.getLogger(Block.class);

    private String hash;

    private String previousHash;

    private String data; //our data will be a simple message BUT should be generified

    private long timeStamp; //as number of milliseconds since 1/1/1970.

    private int nonce;

    //Block Constructor.
    public Block(String data, String previousHash) {
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }

    private String calculateHashImpl(String previousHash, long timeStamp, String data) {
        final String toHash = previousHash +
            Long.toString(timeStamp) +
            Integer.toString(nonce) +
            data;
        return HashUtil.applySha256(toHash);
    }

    public String calculateHash() {
        return calculateHashImpl(getPreviousHash(), getTimeStamp(), getData());
    }

    private void mineImpl(Block block, int difficulty) {
        //Create a string with difficulty * "0"
        String target = new String(new char[difficulty]).replace('\0', '0');
        while (!block.getHash().substring( 0, difficulty).equals(target)) {
            int nonce = block.getNonce();
            nonce++;
            block.setNonce(nonce);
            block.setHash(block.calculateHash());
        }
        log.info("Block Mined!!! New hash: {}", block.getHash());
    }

    public void mine(int difficulty) {
       mineImpl(this, difficulty);
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

    public String getData()
    {
        return data;
    }

    public void setData(final String data)
    {
        this.data = data;
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

    @Override
    public String toString()
    {
        return ReflectionToStringBuilder.toString(this);
    }
}
