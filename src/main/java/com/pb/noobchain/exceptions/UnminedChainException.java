package com.pb.noobchain.exceptions;

public class UnminedChainException extends IllegalArgumentException
{
    public UnminedChainException()
    {
        super("This block hasn't been mined");
    }
}
