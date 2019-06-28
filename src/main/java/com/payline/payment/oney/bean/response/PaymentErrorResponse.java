package com.payline.payment.oney.bean.response;

import com.google.gson.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.payline.payment.oney.bean.common.OneyBean;
import com.payline.payment.oney.bean.common.OneyError;
import com.payline.payment.oney.exception.MalformedJsonException;
import com.payline.pmapi.logger.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PaymentErrorResponse extends OneyBean {

    private static final Logger LOGGER = LogManager.getLogger(PaymentErrorResponse.class);

    private static final String PAYMENTS_ERROR_RESPONSE = "Payments_Error_Response";
    private static final String ERROR_LIST = "error_list";
    private static final String ERROR_LIST_1 = "error_list ";

    @SerializedName("error_list")
    private List<OneyError> errorList;

    public List<OneyError> getErrorList() {
        return errorList;
    }


    public PaymentErrorResponse(List<OneyError> oneyErrors) {
        this.errorList = oneyErrors;
    }

    public static PaymentErrorResponse paymentErrorResponseFromJson(String json)
            throws MalformedJsonException {

        //Specifier le type renvoye
        Type errorListType = new TypeToken<ArrayList<OneyError>>() {
        }.getType();
        Gson gson = new Gson();
        JsonObject jsonObject;
        try {
            jsonObject = gson.fromJson(json, JsonObject.class);
        }
        catch( JsonSyntaxException e ){
            LOGGER.error("Unable to parse JSON content", e);
            throw new MalformedJsonException( e );
        }

        LOGGER.debug("Oney error message : {}", json);

        JsonElement jsonElementError = jsonObject.get(PAYMENTS_ERROR_RESPONSE);
        if (jsonElementError != null) {
            //On essaie de recuperer "error_list " et "error_list" on parse ensuite la cle contenant des valeurs
            JsonArray errorListWithKey1 = (JsonArray) ((JsonObject) jsonElementError).get(ERROR_LIST_1);
            JsonArray errorListWithKey2 = (JsonArray) ((JsonObject) jsonElementError).get(ERROR_LIST);

            if (errorListWithKey1 != null) {
                return new PaymentErrorResponse(gson.fromJson(errorListWithKey1, errorListType));
            } else {

                return new PaymentErrorResponse(gson.fromJson(errorListWithKey2, errorListType));
            }
        }

        return null;
    }


}


