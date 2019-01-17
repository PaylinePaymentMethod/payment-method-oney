package com.payline.payment.oney.bean.response;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.OneyBean;

public class OneyFailureResponse extends OneyBean {


    @SerializedName("code")
    private Integer code;

    @SerializedName("message")
    private String message;
    @SerializedName("content")
    private String content;

    @SerializedName("Payment_Error_Response")
    private PaymentErrorResponse paymentErrorContent;


    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getContent() {
        return content;
    }

    public PaymentErrorResponse getPaymentErrorContent() {
        return paymentErrorContent;
    }

    public OneyFailureResponse(int code, String message, String content, PaymentErrorResponse responseError) {
        this.code = code;
        this.message = message;
        this.content = content;
        this.paymentErrorContent = responseError;

    }


    public static OneyFailureResponse fromJson(String json) {
        Gson parser = new Gson();
        return parser.fromJson(json, OneyFailureResponse.class);
    }


}


