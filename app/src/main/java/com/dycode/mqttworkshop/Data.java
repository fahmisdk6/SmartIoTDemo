package com.dycode.mqttworkshop;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by fahmi on 11/06/2016.
 */
public class Data {
    @SerializedName("indicLED")
    @Expose
    private Integer indicLED;
    @SerializedName("roomTemp")
    @Expose
    private Double roomTemp;
    @SerializedName("roomPress")
    @Expose
    private Integer roomPress;

    public Integer getIndicLED() {
        return indicLED;
    }

    public Double getRoomTemp() {
        return roomTemp;
    }

    public Integer getRoomPress() {
        return roomPress;
    }
}
