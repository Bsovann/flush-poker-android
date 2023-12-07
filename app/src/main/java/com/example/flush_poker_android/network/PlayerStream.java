package com.example.flush_poker_android.network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PlayerStream{
    private ObjectInputStream inputStream;
    public ObjectInputStream getInputStream() {
        return inputStream;
    }
    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }
    private ObjectOutputStream outputStream;
    public PlayerStream(ObjectInputStream inputStream, ObjectOutputStream outputStream){
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }
}
