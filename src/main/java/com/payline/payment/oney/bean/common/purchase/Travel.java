package com.payline.payment.oney.bean.common.purchase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.payline.payment.oney.bean.common.OneyBean;

import java.util.Date;
import java.util.List;

/**
 * Tour operator data
 */
public class Travel extends OneyBean {

    @SerializedName("main_traveler_firstname")
    private String mainTravelerFirstname;
    @SerializedName("main_traveler_surname")
    private String mainTravelerSurname;
    @SerializedName("main_traveler_birthdate")
    private Date mainTravelerBirthdate;

    private List<Journey> journey;
    private List<Stay> stay;

    Travel( TravelBuilder builder ) {
        this.mainTravelerFirstname = builder.mainTravelerFirstname;
        this.mainTravelerSurname = builder.mainTravelerSurname;
        this.mainTravelerBirthdate = builder.mainTravelerBirthdate;
        this.journey = builder.journey;
        this.stay = builder.stay;
    }

    public String getMainTravelerFirstname() {
        return mainTravelerFirstname;
    }

    public String getMainTravelerSurname() {
        return mainTravelerSurname;
    }

    public Date getMainTravelerBirthdate() {
        return mainTravelerBirthdate;
    }

    public List<Journey> getJourney() {
        return journey;
    }

    public List<Stay> getStay() {
        return stay;
    }

    public static class TravelBuilder {
        private String mainTravelerFirstname;
        private String mainTravelerSurname;
        private Date mainTravelerBirthdate;
        private List<Journey> journey;
        private List<Stay> stay;

        public TravelBuilder withMainTravelerFirstname(String mainTravelerFirstname) {
            this.mainTravelerFirstname = mainTravelerFirstname;
            return this;
        }

        public TravelBuilder withMainTravelerSurname(String mainTravelerSurname) {
            this.mainTravelerSurname = mainTravelerSurname;
            return this;
        }

        public TravelBuilder withMainTravelerBirthdate(Date mainTravelerBirthdate) {
            this.mainTravelerBirthdate = mainTravelerBirthdate;
            return this;
        }

        public TravelBuilder withJourney(List<Journey> journey) {
            this.journey = journey;
            return this;
        }

        public TravelBuilder withStay(List<Stay> stay) {
            this.stay = stay;
            return this;
        }

        public Travel build(){
            return new Travel( this );
        }
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setDateFormat("yyy-MM-dd").create();
        return gson.toJson( this );
    }
}
