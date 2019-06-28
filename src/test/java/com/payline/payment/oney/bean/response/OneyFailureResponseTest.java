package com.payline.payment.oney.bean.response;

import com.payline.payment.oney.exception.MalformedJsonException;
import com.payline.payment.oney.utils.OneyErrorHandler;
import com.payline.payment.oney.utils.http.StringResponse;
import com.payline.pmapi.bean.common.FailureCause;
import com.payline.pmapi.bean.payment.response.impl.PaymentResponseFailure;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.payline.payment.oney.bean.response.PaymentErrorResponse.paymentErrorResponseFromJson;
import static com.payline.payment.oney.utils.OneyErrorHandler.handleOneyFailureResponse;
import static com.payline.payment.oney.utils.TestUtils.createStringResponse;

public class OneyFailureResponseTest {


    private static final String SEP = " - ";

    @Test
    public void testOneyFailureResponse() throws MalformedJsonException {
        StringResponse stringResponse = createStringResponse(400, "Bad request", "{\"Payments_Error_Response\":{\"error_list \":[{\"field\":\"purchase.delivery.delivery_address.country_code\",\"error_code\":\"ERR_02\",\"error_label\":\"Size of the field should be equal to [3] characters\"},{\"field\":\"purchase.item_list.category_code\",\"error_code\":\"ERR_04\",\"error_label\":\"Value of the field is invalid [{Integer}]\"},{\"field\":\"purchase.item_list.category_code\",\"error_code\":\"ERR_04\",\"error_label\":\"Value of the field is invalid [{Integer}]\"},{\"field\":\"customer.customer_address.country_code\",\"error_code\":\"ERR_02\",\"error_label\":\"Size of the field should be equal to [3] characters\"},{\"field\":\"payment.payment_type\",\"error_code\":\"ERR_03\",\"error_label\":\"Format of the field is invalid [{Integer}]\"}]}}");

        OneyFailureResponse failureCause = new OneyFailureResponse(stringResponse.getCode(), stringResponse.getMessage(), stringResponse.getContent(), paymentErrorResponseFromJson(stringResponse.getContent()));

        Assertions.assertEquals((Integer) 400, failureCause.getCode());
        Assertions.assertEquals("Bad request", failureCause.getMessage());
        Assertions.assertNotNull(failureCause.getContent());
        Assertions.assertNotNull(failureCause.getPaymentErrorContent());

    }


    @Test
    public void tesHandleOneyFailureResponseFromCause2() throws MalformedJsonException {
        String json = "{\"Payments_Error_Response\":{\"error_list \":[{\"field\":\"payment.business_transaction.code\",\"error_code\":\"ERR_02\",\"error_label\":\"Size of the field should be less than or equal to [5] characters\"}]}}";

        StringResponse stringResponse = createStringResponse(400, "Bad request", json);

        OneyFailureResponse failureCause = new OneyFailureResponse(stringResponse.getCode(), stringResponse.getMessage(), stringResponse.getContent(), paymentErrorResponseFromJson(stringResponse.getContent()));

        FailureCause failureResponse = OneyErrorHandler.handleOneyFailureResponseFromCause(failureCause);
        Assertions.assertEquals(FailureCause.INVALID_FIELD_FORMAT, failureResponse);

        PaymentResponseFailure paymentResponseFailure = PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                .withFailureCause(handleOneyFailureResponse(failureCause))
                .withErrorCode(failureCause.toPaylineErrorCode())
                .build();

        Assertions.assertNotNull(paymentResponseFailure.getErrorCode());
        Assertions.assertTrue(paymentResponseFailure.getErrorCode().contains(SEP));
        String[] split = paymentResponseFailure.getErrorCode().split(SEP);
        Assertions.assertEquals("400", split[0]);
        Assertions.assertEquals("ERR_02", split[1]);
        Assertions.assertEquals("payment.business_transaction.code", split[2]);
    }


    @Test
    public void paylineFailureResponse_PAYLAPMEXT_98() throws MalformedJsonException {
        String json = "{\"Payments_Error_Response\":{\"error_list\":[{\"error\":{\"field\":\"language_code\",\"error_code\":\"ERR_04\",\"error_label\":\"Value of the field is invalid [{String}]\"}},{\"error\":{\"field\":\"Brand_language_code\",\"error_code\":\"ERR_04\",\"error_label\":\"Value of the field is invalid [{String}]\"}},{\"error\":{\"field\":\"customer.language_code\",\"error_code\":\"ERR_04\",\"error_label\":\"Value of the field is invalid [{String}]\"}}]}}";

        StringResponse stringResponse = createStringResponse(400, "Bad request", json);

        OneyFailureResponse failureCause = new OneyFailureResponse(stringResponse.getCode(), stringResponse.getMessage(), stringResponse.getContent(), paymentErrorResponseFromJson(stringResponse.getContent()));

        FailureCause paylineFailureResponse = OneyErrorHandler.handleOneyFailureResponseFromCause(failureCause);
        Assertions.assertEquals(FailureCause.INVALID_DATA, paylineFailureResponse);


        PaymentResponseFailure paymentResponseFailure = PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                .withFailureCause(handleOneyFailureResponse(failureCause))
                .withErrorCode(failureCause.toPaylineErrorCode())
                .build();

        Assertions.assertNotNull(paymentResponseFailure.getErrorCode());
        Assertions.assertTrue(paymentResponseFailure.getErrorCode().contains(SEP));
        String[] split = paymentResponseFailure.getErrorCode().split(SEP);
        Assertions.assertEquals("400", split[0]);
        Assertions.assertEquals("ERR_04", split[1]);
        Assertions.assertEquals("language_code", split[2]);

    }


    @Test
    public void paylineFailureResponse_truncate() throws MalformedJsonException {
        String json = "{\"Payments_Error_Response\":{\"error_list\":[{\"error\":{\"field\":\"language_code_language_code_language_code_language_code\",\"error_code\":\"ERR_04\",\"error_label\":\"Value of the field is invalid [{String}]\"}},{\"error\":{\"field\":\"Brand_language_code\",\"error_code\":\"ERR_04\",\"error_label\":\"Value of the field is invalid [{String}]\"}},{\"error\":{\"field\":\"customer.language_code\",\"error_code\":\"ERR_04\",\"error_label\":\"Value of the field is invalid [{String}]\"}}]}}";

        StringResponse stringResponse = createStringResponse(400, "Bad request", json);

        OneyFailureResponse failureCause = new OneyFailureResponse(stringResponse.getCode(), stringResponse.getMessage(), stringResponse.getContent(), paymentErrorResponseFromJson(stringResponse.getContent()));

        FailureCause paylineFailureResponse = OneyErrorHandler.handleOneyFailureResponseFromCause(failureCause);
        Assertions.assertEquals(FailureCause.INVALID_DATA, paylineFailureResponse);


        PaymentResponseFailure paymentResponseFailure = PaymentResponseFailure.PaymentResponseFailureBuilder.aPaymentResponseFailure()
                .withFailureCause(handleOneyFailureResponse(failureCause))
                .withErrorCode(failureCause.toPaylineErrorCode())
                .build();

        Assertions.assertNotNull(paymentResponseFailure.getErrorCode());
        Assertions.assertTrue(paymentResponseFailure.getErrorCode().contains(SEP));
        Assertions.assertTrue(paymentResponseFailure.getErrorCode().length() == 50);
        String[] split = paymentResponseFailure.getErrorCode().split(SEP);
        Assertions.assertEquals("400", split[0]);
        Assertions.assertEquals("ERR_04", split[1]);
        Assertions.assertTrue(split[2].contains("language_code"));

    }
}
