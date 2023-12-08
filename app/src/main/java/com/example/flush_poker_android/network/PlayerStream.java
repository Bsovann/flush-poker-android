package com.example.flush_poker_android.network;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/** Object IO Stream for Players
 *
 * @author Kyle Chainey */
public class PlayerStream{
    private final ObjectInputStream inputStream;
    public ObjectInputStream getInputStream() {
        return inputStream;
    }
    public ObjectOutputStream getOutputStream() {
        return outputStream;
    }
    private final ObjectOutputStream outputStream;
    public PlayerStream(ObjectInputStream inputStream, ObjectOutputStream outputStream){
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }
}
