package com.pb.noobchain.exceptions;

public class UnequalCurrentHashException extends IllegalArgumentException
{
    public UnequalCurrentHashException()
    {
        super("Current Hashes not equal");
    }
}
