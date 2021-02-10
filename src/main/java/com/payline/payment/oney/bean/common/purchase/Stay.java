package com.payline.payment.oney.bean.common.purchase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.OneyBean;
import com.payline.payment.oney.bean.common.enums.StayType;

import java.util.Date;

/**
 * Stay data
 */
public class Stay extends OneyBean {

    @SerializedName("number_of_travelers")
    private Integer numberOfTravelers;
    @SerializedName("place_of_residence")
    private String placeOfResidence;
    @SerializedName("number_of_rooms")
    private Integer numberOfRooms;
    @SerializedName("arrival_date")
    private Date arrivalDate;
    @SerializedName("departure_date")
    private Date departureDate;
    /**
     * 1: if a car is rented
     */
    @SerializedName("vehicle_rental_flag")
    private Integer vehicleRentalFlag;
    /**
     * 1: if an insurance is subscribed
     */
    @SerializedName("stay_insurance_flag")
    private Integer stayInsuranceFlag;
    /**
     * 1: hotel, 2: rental, 3: tour/trip, 4: other
     */
    @SerializedName("stay_type_code")
    private Integer stayTypeCode;

    Stay( StayBuilder builder ){
        this.numberOfTravelers = builder.numberOfTravelers;
        this.placeOfResidence = builder.placeOfResidence;
        this.numberOfRooms = builder.numberOfRooms;
        this.arrivalDate = builder.arrivalDate;
        this.departureDate = builder.departureDate;
        this.vehicleRentalFlag = builder.vehicleRentalFlag;
        this.stayInsuranceFlag = builder.stayInsuranceFlag;
        this.stayTypeCode = builder.stayTypeCode;
    }

    public Integer getNumberOfTravelers() {
        return numberOfTravelers;
    }

    public String getPlaceOfResidence() {
        return placeOfResidence;
    }

    public Integer getNumberOfRooms() {
        return numberOfRooms;
    }

    public Date getArrivalDate() {
        return arrivalDate;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public Integer getVehicleRentalFlag() {
        return vehicleRentalFlag;
    }

    public Integer getStayInsuranceFlag() {
        return stayInsuranceFlag;
    }

    public Integer getStayTypeCode() {
        return stayTypeCode;
    }

    public static class StayBuilder {
        private Integer numberOfTravelers;
        private String placeOfResidence;
        private Integer numberOfRooms;
        private Date arrivalDate;
        private Date departureDate;
        private Integer vehicleRentalFlag;
        private Integer stayInsuranceFlag;
        private Integer stayTypeCode;

        public StayBuilder withNumberOfTravelers(int numberOfTravelers) {
            this.numberOfTravelers = numberOfTravelers;
            return this;
        }

        public StayBuilder withPlaceOfResidence(String placeOfResidence) {
            this.placeOfResidence = placeOfResidence;
            return this;
        }

        public StayBuilder withNumberOfRooms(int numberOfRooms) {
            this.numberOfRooms = numberOfRooms;
            return this;
        }

        public StayBuilder withArrivalDate(Date arrivalDate) {
            this.arrivalDate = arrivalDate;
            return this;
        }

        public StayBuilder withDepartureDate(Date departureDate) {
            this.departureDate = departureDate;
            return this;
        }

        public StayBuilder withVehicleRentalFlag(boolean vehicleRentalFlag) {
            this.vehicleRentalFlag = vehicleRentalFlag ? 1 : 0;
            return this;
        }

        public StayBuilder withStayInsuranceFlag(boolean stayInsuranceFlag) {
            this.stayInsuranceFlag = stayInsuranceFlag ? 1 : 0;
            return this;
        }

        public StayBuilder withStayType(StayType stayType) {
            this.stayTypeCode = stayType.getCode();
            return this;
        }

        public Stay build(){
            return new Stay(this);
        }
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setDateFormat("yyy-MM-dd").create();
        return gson.toJson( this );
    }
}
