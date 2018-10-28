package com.pb.noobchain.exceptions;

public class InvalidTransactionException extends IllegalArgumentException
{
    public InvalidTransactionException(final String string)
    {
        super(string);
    }
}
