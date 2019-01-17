package com.payline.payment.oney.bean.common.purchase;

import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.OneyBean;
import com.payline.pmapi.bean.payment.Order;
import com.payline.pmapi.bean.payment.request.PaymentRequest;

import java.util.ArrayList;
import java.util.List;

import static com.payline.payment.oney.utils.OneyConstants.EXTERNAL_REFERENCE_TYPE;
import static com.payline.payment.oney.utils.PluginUtils.createFloatAmount;

public class Purchase extends OneyBean {

    @SerializedName("external_reference_type")
    private String externalReferenceType; //CMDE
    @SerializedName("external_reference")
    private String externalReference;
    @SerializedName("purchase_amount")
    private Float purchaseAmount;
    @SerializedName("currency_code")
    private String currencyCode; //ISO 4217
    @SerializedName("purchase_merchant")
    private PurchaseMerchant purchaseMerchant;
    @SerializedName("delivery")
    private Delivery delivery;
    @SerializedName("item_list")
    private List<Item> listItem;
    @SerializedName("number_of_items")
    private Integer numberOfItems;

    //getter

    public String getExternalReferenceType() {
        return externalReferenceType;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public Float getPurchaseAmount() {
        return purchaseAmount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public PurchaseMerchant getPurchaseMerchant() {
        return purchaseMerchant;
    }

    public Delivery getDelivery() {
        return delivery;
    }

    public Integer getNumberOfItems() {
        return numberOfItems;
    }

    public List<Item> getListItem() {
        return listItem;
    }


    //constructors
    private Purchase(Purchase.Builder builder) {
        this.externalReferenceType = builder.externalReferenceType;
        this.externalReference = builder.externalReference;
        this.purchaseAmount = builder.purchaseAmount;
        this.currencyCode = builder.currencyCode;
        this.purchaseMerchant = builder.purchaseMerchant;
        this.delivery = builder.delivery;
        this.listItem = builder.listItem;
        this.numberOfItems = builder.numberOfItems;
    }

    private Purchase() {
    }


    //Builder
    public static class Builder {
        private String externalReferenceType; //CMDE
        private String externalReference;
        private Float purchaseAmount;
        private String currencyCode; //ISO 4217
        private PurchaseMerchant purchaseMerchant;
        private Delivery delivery;
        private List<Item> listItem;
        private Integer numberOfItems;

        public static Purchase.Builder aPurchaseBuilder() {
            return new Purchase.Builder();
        }

        public Purchase.Builder withExternalReferenceType(String externalReferenceType) {
            this.externalReferenceType = externalReferenceType;
            return this;
        }

        public Purchase.Builder withExternalReference(String externalReference) {
            this.externalReference = externalReference;
            return this;
        }

        public Purchase.Builder withPurchaseAmount(Float purchaseAmount) {
            this.purchaseAmount = purchaseAmount;
            return this;
        }

        public Purchase.Builder withCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
            return this;
        }

        public Purchase.Builder withPurchaseMerchant(PurchaseMerchant purchaseMerchant) {
            this.purchaseMerchant = purchaseMerchant;
            return this;
        }

        public Purchase.Builder withDelivery(Delivery delivery) {
            this.delivery = delivery;
            return this;
        }

        public Purchase.Builder withListItem(List<Item> listItem) {
            this.listItem = listItem;
            return this;

        }

        public Purchase.Builder withNumberOfItems(Integer numberOfItems) {
            this.numberOfItems = numberOfItems;
            return this;
        }


        public Purchase.Builder fromPayline(PaymentRequest request) {
            this.externalReferenceType = EXTERNAL_REFERENCE_TYPE;
            this.externalReference = request.getOrder().getReference();
            this.purchaseAmount = createFloatAmount(request.getOrder().getAmount().getAmountInSmallestUnit(),request.getOrder().getAmount().getCurrency());
            this.currencyCode = request.getOrder().getAmount().getCurrency().getCurrencyCode();
            this.purchaseMerchant = PurchaseMerchant.Builder.aPurchaseMerchantBuilder()
                    .fromPayline(request)
                    .build();
            this.delivery = Delivery.Builder.aDeliveryBuilder()
                    .fromPayline(request)
                    .build();
            List<Order.OrderItem> orderItems = request.getOrder().getItems();
            this.numberOfItems = orderItems.size();
            List<Item> listItems = new ArrayList<>();

            orderItems.forEach(item ->
                    listItems.add(Item.Builder.aItemBuilder()
                            .fromPayline(item)
                            .build())
            );
            //Define the main item
            Item.defineMainItem(listItems);
            this.listItem = listItems;
            return this;
        }

        private Purchase.Builder checkIntegrity() {
            if (this.listItem == null) {
                throw new IllegalStateException("Purchase must have a listItem when built");
            }
            if (this.delivery == null) {
                throw new IllegalStateException("Purchase must have a delivery when built");
            }
            if (this.currencyCode == null) {
                throw new IllegalStateException("Purchase must have a currencyCode when built");
            }
            if (this.externalReferenceType == null) {
                throw new IllegalStateException("Purchase must have a externalReferenceType when built");
            }
            if (this.externalReference == null) {
                throw new IllegalStateException("Purchase must have a externalReference when built");
            }
            if (this.purchaseAmount == null) {
                throw new IllegalStateException("Purchase must have a purchaseAmount when built");
            }
            if (this.numberOfItems == null) {
                throw new IllegalStateException("Purchase must have a numberOfItems when built");
            }
            return this;
        }


        public Purchase build() {
            return new Purchase(this.checkIntegrity());
        }
    }
}
