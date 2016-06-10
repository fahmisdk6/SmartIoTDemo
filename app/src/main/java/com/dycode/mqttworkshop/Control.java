package com.dycode.mqttworkshop;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by fahmi on 11/06/2016.
 */
public class Control {
    @SerializedName("ledControl")
    @Expose
    private int ledControl;

    public Control(int ledControl) {
        this.ledControl = ledControl;
    }

    public int getLedControl() {
        return ledControl;
    }
}
