package com.payline.payment.oney.bean.common.enums;

public enum MeanOfTransport {

    PLANE(1),
    TRAIN(2),
    ROAD(3),
    BOAT_FERRY(4);

    private int code;

    MeanOfTransport(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static MeanOfTransport fromCode(int code){
        for (MeanOfTransport value : MeanOfTransport.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return null;
    }
}
