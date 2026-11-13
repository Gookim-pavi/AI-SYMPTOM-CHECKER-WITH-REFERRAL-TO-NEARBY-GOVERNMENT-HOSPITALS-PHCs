package com.sanjeevani.models;

import java.util.List;
import java.util.ArrayList;

/**
 * Patient demographic, comorbidity history, and vital signs model.
 */
public class PatientProfile {
    private String name;
    private int age;
    private String gender; // Male, Female, Other
    private boolean isPregnant;
    private int pregnancyTrimester; // 1, 2, 3 (0 if not pregnant)
    private List<String> comorbidities; // Diabetes, Hypertension, Asthma/COPD, Cardiac History, Renal Disease, Immunocompromised
    private double latitude;
    private double longitude;
    private String locationAddress;
    private String state;
    private String district;
    private String pincode;
    
    // Optional Vitals for advanced triage
    private Double bodyTempF;       // e.g. 102.5 F
    private Integer heartRateBpm;   // e.g. 110 bpm
    private Integer systolicBp;     // e.g. 150 mmHg
    private Integer diastolicBp;    // e.g. 95 mmHg
    private Integer spo2Percent;    // e.g. 92%
    private Integer respiratoryRate;// e.g. 24 /min

    public PatientProfile() {
        this.name = "Anonymous Patient";
        this.age = 30;
        this.gender = "Male";
        this.isPregnant = false;
        this.pregnancyTrimester = 0;
        this.comorbidities = new ArrayList<>();
        this.latitude = 12.9716;  // Default sample coords (Bangalore, India)
        this.longitude = 77.5946;
        this.locationAddress = "Current Location";
        this.state = "Karnataka";
        this.district = "Bengaluru Urban";
        this.pincode = "560001";
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public boolean isPregnant() { return isPregnant; }
    public void setPregnant(boolean pregnant) { isPregnant = pregnant; }

    public int getPregnancyTrimester() { return pregnancyTrimester; }
    public void setPregnancyTrimester(int pregnancyTrimester) { this.pregnancyTrimester = pregnancyTrimester; }

    public List<String> getComorbidities() { return comorbidities; }
    public void setComorbidities(List<String> comorbidities) { this.comorbidities = comorbidities; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getLocationAddress() { return locationAddress; }
    public void setLocationAddress(String locationAddress) { this.locationAddress = locationAddress; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public Double getBodyTempF() { return bodyTempF; }
    public void setBodyTempF(Double bodyTempF) { this.bodyTempF = bodyTempF; }

    public Integer getHeartRateBpm() { return heartRateBpm; }
    public void setHeartRateBpm(Integer heartRateBpm) { this.heartRateBpm = heartRateBpm; }

    public Integer getSystolicBp() { return systolicBp; }
    public void setSystolicBp(Integer systolicBp) { this.systolicBp = systolicBp; }

    public Integer getDiastolicBp() { return diastolicBp; }
    public void setDiastolicBp(Integer diastolicBp) { this.diastolicBp = diastolicBp; }

    public Integer getSpo2Percent() { return spo2Percent; }
    public void setSpo2Percent(Integer spo2Percent) { this.spo2Percent = spo2Percent; }

    public Integer getRespiratoryRate() { return respiratoryRate; }
    public void setRespiratoryRate(Integer respiratoryRate) { this.respiratoryRate = respiratoryRate; }
}
