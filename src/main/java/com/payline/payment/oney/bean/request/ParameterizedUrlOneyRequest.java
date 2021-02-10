package com.payline.payment.oney.bean.request;

import com.payline.payment.oney.utils.PluginUtils;
import com.payline.pmapi.bean.payment.Order;

public abstract class ParameterizedUrlOneyRequest extends OneyRequest {

    protected String purchaseReference;

    public String getPurchaseReference() {
        return purchaseReference;
    }

    public static class Builder {
        protected String purchaseReference;

        public Builder withPurchaseReferenceFromOrder( Order order ){
            if( order != null ){
                this.purchaseReference = PluginUtils.fullPurchaseReference( order.getReference() );
            }
            return this;
        }
    }

}
