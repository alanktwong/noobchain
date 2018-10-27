package com.pb.noobchain.domain;

import java.util.Date;

import com.pb.noobchain.service.HashUtil;

public class Block
{
    private String hash;

    private String previousHash;

    private String data; //our data will be a simple message BUT should be generified

    private long timeStamp; //as number of milliseconds since 1/1/1970.

    //Block Constructor.
    public Block(String data, String previousHash) {
        this.data = data;
        this.previousHash = previousHash;
        this.timeStamp = new Date().getTime();
        this.hash = calculateHash();
    }


    private String calculateHashImpl(String previousHash, long timeStamp, String data) {
        return HashUtil.applySha256(
            previousHash +
                Long.toString(timeStamp) +
                data
        );
    }

    public String calculateHash() {
        return calculateHashImpl(getPreviousHash(), getTimeStamp(), getData());
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
}
