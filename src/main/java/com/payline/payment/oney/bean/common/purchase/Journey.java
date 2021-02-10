package com.payline.payment.oney.bean.common.purchase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.OneyBean;
import com.payline.payment.oney.bean.common.enums.MeanOfTransport;

import java.util.Date;

/**
 * Journey data
 */
public class Journey extends OneyBean {

    @SerializedName("number_of_travelers")
    private Integer numberOfTravelers;
    /**
     * Each journey is identified as an outward, a return or a transitional journey if the trip has several places.
     * Number indicate the progress.
     * O for Outward T1, T2, Tn for Transitional R for Return
     */
    @SerializedName("journey_number")
    private String journeyNumber;
    @SerializedName("journey_date")
    private Date journeyDate;
    @SerializedName("departure_city")
    private String departureCity;
    @SerializedName("arrival_city")
    private String arrivalCity;
    @SerializedName("ticket_category")
    private String ticketCategory;
    /**
     * 1: if the ticket is exchangeable
     */
    @SerializedName("exchangeability_flag")
    private Integer exchangeabilityFlag;
    /**
     * 1: if an insurance is subscribed
     */
    @SerializedName("travel_insurance_flag")
    private Integer travelInsuranceFlag;
    /**
     * 1: plane, 2: train, 3: road, 4: boat/ferry
     */
    @SerializedName("mean_of_transport_code")
    private Integer meanOfTransportCode;

    Journey( JourneyBuilder builder ){
        this.numberOfTravelers = builder.numberOfTravelers;
        this.journeyNumber = builder.journeyNumber;
        this.journeyDate = builder.journeyDate;
        this.departureCity = builder.departureCity;
        this.arrivalCity = builder.arrivalCity;
        this.ticketCategory = builder.ticketCategory;
        this.exchangeabilityFlag = builder.exchangeabilityFlag;
        this.travelInsuranceFlag = builder.travelInsuranceFlag;
        this.meanOfTransportCode = builder.meanOfTransportCode;
    }

    public Integer getNumberOfTravelers() {
        return numberOfTravelers;
    }

    public String getJourneyNumber() {
        return journeyNumber;
    }

    public Date getJourneyDate() {
        return journeyDate;
    }

    public String getDepartureCity() {
        return departureCity;
    }

    public String getArrivalCity() {
        return arrivalCity;
    }

    public String getTicketCategory() {
        return ticketCategory;
    }

    public Integer getExchangeabilityFlag() {
        return exchangeabilityFlag;
    }

    public Integer getTravelInsuranceFlag() {
        return travelInsuranceFlag;
    }

    public Integer getMeanOfTransportCode() {
        return meanOfTransportCode;
    }

    public static class JourneyBuilder {
        private int numberOfTravelers;
        private String journeyNumber;
        private Date journeyDate;
        private String departureCity;
        private String arrivalCity;
        private String ticketCategory;
        private Integer exchangeabilityFlag;
        private Integer travelInsuranceFlag;
        private Integer meanOfTransportCode;

        public JourneyBuilder withNumberOfTravelers(int numberOfTravelers) {
            this.numberOfTravelers = numberOfTravelers;
            return this;
        }

        public JourneyBuilder withJourneyNumber(String journeyNumber) {
            this.journeyNumber = journeyNumber;
            return this;
        }

        public JourneyBuilder withJourneyDate(Date journeyDate) {
            this.journeyDate = journeyDate;
            return this;
        }

        public JourneyBuilder withDepartureCity(String departureCity) {
            this.departureCity = departureCity;
            return this;
        }

        public JourneyBuilder withArrivalCity(String arrivalCity) {
            this.arrivalCity = arrivalCity;
            return this;
        }

        public JourneyBuilder withTicketCategory(String ticketCategory) {
            this.ticketCategory = ticketCategory;
            return this;
        }

        public JourneyBuilder withExchangeabilityFlag(boolean exchangeabilityFlag) {
            this.exchangeabilityFlag = exchangeabilityFlag ? 1 : 0;
            return this;
        }

        public JourneyBuilder withTravelInsuranceFlag(boolean travelInsuranceFlag) {
            this.travelInsuranceFlag = travelInsuranceFlag ? 1 : 0;
            return this;
        }

        public JourneyBuilder withMeanOfTransport(MeanOfTransport meanOfTransport) {
            this.meanOfTransportCode = meanOfTransport.getCode();
            return this;
        }

        public Journey build(){
            return new Journey( this );
        }
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setDateFormat("yyy-MM-dd").create();
        return gson.toJson( this );
    }
}
