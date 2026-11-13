package com.sanjeevani.models;

import java.util.List;
import java.util.ArrayList;

/**
 * Model representing a Government Health Facility (Sub-Centre, PHC, CHC,
 * Sub-District Hospital, District Hospital, Medical College / AIIMS).
 */
public class HealthFacility {
    private String id;
    private String name;
    private String facilityType; // SUB_CENTRE, PHC, CHC, SUB_DISTRICT_HOSPITAL, DISTRICT_HOSPITAL, TERTIARY_AIIMS
    private String typeDisplayName; // e.g. "Primary Health Centre (PHC)"
    private String address;
    private String district;
    private String state;
    private String pincode;
    private double latitude;
    private double longitude;
    private String contactNumber;
    private String emergencyHelpline; // 108 / 102 / direct desk
    private boolean is24x7Emergency;
    private boolean hasIcu;
    private boolean hasMaternityWard;
    private boolean hasBloodBank;
    private boolean hasFreePharmacy; // Jan Aushadhi / NHM Free Drug Service
    private boolean isAyushmanEmpaneled; // AB-PMJAY
    private int totalBeds;
    private int availableBeds;
    private List<String> availableSpecialties;
    private String opdTimings;
    private int estimatedWaitMinutes;
    private double distanceKm; // Computed dynamically relative to patient
    private int estimatedTravelMinutes; // Computed dynamically

    public HealthFacility(String id, String name, String facilityType, String typeDisplayName,
                          String address, String district, String state, String pincode,
                          double latitude, double longitude, String contactNumber, 
                          String emergencyHelpline, boolean is24x7Emergency, boolean hasIcu,
                          boolean hasMaternityWard, boolean hasBloodBank, boolean hasFreePharmacy,
                          boolean isAyushmanEmpaneled, int totalBeds, int availableBeds,
                          List<String> availableSpecialties, String opdTimings, int estimatedWaitMinutes) {
        this.id = id;
        this.name = name;
        this.facilityType = facilityType;
        this.typeDisplayName = typeDisplayName;
        this.address = address;
        this.district = district;
        this.state = state;
        this.pincode = pincode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.contactNumber = contactNumber;
        this.emergencyHelpline = emergencyHelpline;
        this.is24x7Emergency = is24x7Emergency;
        this.hasIcu = hasIcu;
        this.hasMaternityWard = hasMaternityWard;
        this.hasBloodBank = hasBloodBank;
        this.hasFreePharmacy = hasFreePharmacy;
        this.isAyushmanEmpaneled = isAyushmanEmpaneled;
        this.totalBeds = totalBeds;
        this.availableBeds = availableBeds;
        this.availableSpecialties = availableSpecialties != null ? availableSpecialties : new ArrayList<>();
        this.opdTimings = opdTimings;
        this.estimatedWaitMinutes = estimatedWaitMinutes;
        this.distanceKm = 0.0;
        this.estimatedTravelMinutes = 0;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getFacilityType() { return facilityType; }
    public String getTypeDisplayName() { return typeDisplayName; }
    public String getAddress() { return address; }
    public String getDistrict() { return district; }
    public String getState() { return state; }
    public String getPincode() { return pincode; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getContactNumber() { return contactNumber; }
    public String getEmergencyHelpline() { return emergencyHelpline; }
    public boolean is24x7Emergency() { return is24x7Emergency; }
    public boolean hasIcu() { return hasIcu; }
    public boolean hasMaternityWard() { return hasMaternityWard; }
    public boolean hasBloodBank() { return hasBloodBank; }
    public boolean hasFreePharmacy() { return hasFreePharmacy; }
    public boolean isAyushmanEmpaneled() { return isAyushmanEmpaneled; }
    public int getTotalBeds() { return totalBeds; }
    public int getAvailableBeds() { return availableBeds; }
    public List<String> getAvailableSpecialties() { return availableSpecialties; }
    public String getOpdTimings() { return opdTimings; }
    public int getEstimatedWaitMinutes() { return estimatedWaitMinutes; }
    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    public int getEstimatedTravelMinutes() { return estimatedTravelMinutes; }
    public void setEstimatedTravelMinutes(int estimatedTravelMinutes) { this.estimatedTravelMinutes = estimatedTravelMinutes; }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":\"").append(escapeJson(id)).append("\",");
        sb.append("\"name\":\"").append(escapeJson(name)).append("\",");
        sb.append("\"facilityType\":\"").append(escapeJson(facilityType)).append("\",");
        sb.append("\"typeDisplayName\":\"").append(escapeJson(typeDisplayName)).append("\",");
        sb.append("\"address\":\"").append(escapeJson(address)).append("\",");
        sb.append("\"district\":\"").append(escapeJson(district)).append("\",");
        sb.append("\"state\":\"").append(escapeJson(state)).append("\",");
        sb.append("\"pincode\":\"").append(escapeJson(pincode)).append("\",");
        sb.append("\"latitude\":").append(latitude).append(",");
        sb.append("\"longitude\":").append(longitude).append(",");
        sb.append("\"contactNumber\":\"").append(escapeJson(contactNumber)).append("\",");
        sb.append("\"emergencyHelpline\":\"").append(escapeJson(emergencyHelpline)).append("\",");
        sb.append("\"is24x7Emergency\":").append(is24x7Emergency).append(",");
        sb.append("\"hasIcu\":").append(hasIcu).append(",");
        sb.append("\"hasMaternityWard\":").append(hasMaternityWard).append(",");
        sb.append("\"hasBloodBank\":").append(hasBloodBank).append(",");
        sb.append("\"hasFreePharmacy\":").append(hasFreePharmacy).append(",");
        sb.append("\"isAyushmanEmpaneled\":").append(isAyushmanEmpaneled).append(",");
        sb.append("\"totalBeds\":").append(totalBeds).append(",");
        sb.append("\"availableBeds\":").append(availableBeds).append(",");
        sb.append("\"opdTimings\":\"").append(escapeJson(opdTimings)).append("\",");
        sb.append("\"estimatedWaitMinutes\":").append(estimatedWaitMinutes).append(",");
        sb.append("\"distanceKm\":").append(String.format(java.util.Locale.US, "%.2f", distanceKm)).append(",");
        sb.append("\"estimatedTravelMinutes\":").append(estimatedTravelMinutes).append(",");
        sb.append("\"availableSpecialties\":[");
        for (int i = 0; i < availableSpecialties.size(); i++) {
            sb.append("\"").append(escapeJson(availableSpecialties.get(i))).append("\"");
            if (i < availableSpecialties.size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
