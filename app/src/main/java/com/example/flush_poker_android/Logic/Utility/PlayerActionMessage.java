package com.example.flush_poker_android.Logic.Utility;

public class PlayerActionMessage {

    public PlayerActionMessage(String action, String name){
        this.action = action;
        this.name = name;
    }
    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String action;
    private String name;

}
