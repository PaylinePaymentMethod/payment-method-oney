package com.payline.payment.oney.bean.common.enums;

public enum StayType {

    HOTEL(1),
    RENTAL(2),
    TOUR_TRIP(3),
    OTHER(4);

    private int code;

    StayType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static StayType fromCode(int code){
        for (StayType value : StayType.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return null;
    }
}
