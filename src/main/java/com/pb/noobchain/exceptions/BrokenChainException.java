package com.pb.noobchain.exceptions;

public class BrokenChainException extends IllegalArgumentException
{

    public BrokenChainException()
    {
        super("Previous Hashes not equal");
    }

}
