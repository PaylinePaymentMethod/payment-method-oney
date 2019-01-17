package com.payline.payment.oney.bean.response;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.payline.payment.oney.bean.common.OneyBean;
import com.payline.payment.oney.bean.common.OneyError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PaymentErrorResponse extends OneyBean {

    private static final Logger LOGGER = LogManager.getLogger(PaymentErrorResponse.class);

    @SerializedName("error_list")
    private List<OneyError> errorList;

    public List<OneyError> getErrorList() {
        return errorList;
    }


    public PaymentErrorResponse(List<OneyError> oneyErrors) {
        this.errorList = oneyErrors;
    }

    public static PaymentErrorResponse paymentErrorResponseFromJson(String json) {

        //Specifier le type renvoye
        Type errorListType = new TypeToken<ArrayList<OneyError>>() {
        }.getType();
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        LOGGER.debug("Oney error message : {}", json);

        if (jsonObject.get("Payments_Error_Response") != null) {
            //On essaie de recuperer "error_list " et "error_list" on parse ensuite la cle contenant des valeurs
            JsonArray errorListWithKey1 = (JsonArray) ((JsonObject) jsonObject.get("Payments_Error_Response")).get("error_list ");
            JsonArray errorListWithKey2 = (JsonArray) ((JsonObject) jsonObject.get("Payments_Error_Response")).get("error_list");

            if (errorListWithKey1 != null) {
                return new PaymentErrorResponse(gson.fromJson(errorListWithKey1, errorListType));
            } else {

                return new PaymentErrorResponse(gson.fromJson(errorListWithKey2, errorListType));
            }
        }

        return null;
    }


}


