package com.example.mouseremote;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 3250827484526633390L;

    int xCoord;
    int yCoord;
    boolean click;
    boolean space;

    public Message(int xCoord, int yCoord, boolean click, boolean space) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.click = click;
        this.space = space;
    }

    public int getxCoord() {
        return xCoord;
    }

    public int getyCoord() {
        return yCoord;
    }

    public boolean isClick() {
        return click;
    }

    public boolean isSpace() {
        return space;
    }
}
