package com.pb.noobchain.service.dto;

import java.util.Date;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockDTO<T>
{
    @NonNull
    private String hash;

    @NonNull
    private String previousHash;

    @NonNull
    private T data; //our data can be a simple message BUT should become a generic

    @NonNull
    private long timeStamp = new Date().getTime(); //as number of milliseconds since 1/1/1970.

}
