package com.sanjeevani.models;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.security.MessageDigest;

/**
 * Official digital government health referral voucher model
 * with token number, triage code, destination PHC/hospital, and verification hash.
 */
public class ReferralSlip {
    private String slipId;
    private String issuedAt;
    private String validUntil;
    private String abhaNumberPlaceholder;
    private String patientName;
    private int patientAge;
    private String patientGender;
    private String triageRiskLevel;
    private int urgencyScore;
    private String primaryImpression;
    private List<String> reportedSymptoms;
    private List<String> redFlags;
    private String destinationFacilityId;
    private String destinationFacilityName;
    private String destinationFacilityType;
    private String destinationAddress;
    private String destinationHelpline;
    private String assignedDepartment; // e.g. General Medicine OPD, Emergency / Trauma, Obstetrics & Gynae
    private String opdTokenNumber;
    private String priorityQueueCode; // STAT, URGENT, ROUTINE
    private String verificationHash;

    public ReferralSlip(String slipId, String patientName, int patientAge, String patientGender,
                        String triageRiskLevel, int urgencyScore, String primaryImpression,
                        List<String> reportedSymptoms, List<String> redFlags,
                        HealthFacility facility, String assignedDepartment) {
        this.slipId = slipId;
        this.patientName = patientName;
        this.patientAge = patientAge;
        this.patientGender = patientGender;
        this.triageRiskLevel = triageRiskLevel;
        this.urgencyScore = urgencyScore;
        this.primaryImpression = primaryImpression;
        this.reportedSymptoms = reportedSymptoms != null ? reportedSymptoms : new ArrayList<>();
        this.redFlags = redFlags != null ? redFlags : new ArrayList<>();
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        this.issuedAt = now.format(dtf);
        this.validUntil = now.plusHours(48).format(dtf);
        
        this.abhaNumberPlaceholder = "ABHA-" + (1000 + (int)(Math.random() * 9000)) + "-" + 
                                     (1000 + (int)(Math.random() * 9000)) + "-" + 
                                     (1000 + (int)(Math.random() * 9000));
        
        if (facility != null) {
            this.destinationFacilityId = facility.getId();
            this.destinationFacilityName = facility.getName();
            this.destinationFacilityType = facility.getTypeDisplayName();
            this.destinationAddress = facility.getAddress() + ", " + facility.getDistrict() + ", " + facility.getState();
            this.destinationHelpline = facility.getEmergencyHelpline();
        } else {
            this.destinationFacilityId = "FAC-GEN-01";
            this.destinationFacilityName = "Nearest Government Primary Health Centre";
            this.destinationFacilityType = "PHC";
            this.destinationAddress = "Local Health District";
            this.destinationHelpline = "108 / 102";
        }

        this.assignedDepartment = assignedDepartment != null ? assignedDepartment : "General OPD / Primary Care";
        
        // Generate Token
        int tokenNum = 100 + (int)(Math.random() * 899);
        if ("EMERGENCY".equals(triageRiskLevel)) {
            this.priorityQueueCode = "RED-STAT";
            this.opdTokenNumber = "EMERG-T" + tokenNum;
        } else if ("HIGH".equals(triageRiskLevel)) {
            this.priorityQueueCode = "AMBER-URGENT";
            this.opdTokenNumber = "URG-T" + tokenNum;
        } else {
            this.priorityQueueCode = "GREEN-REGULAR";
            this.opdTokenNumber = "OPD-T" + tokenNum;
        }

        this.verificationHash = generateVerificationHash(slipId + patientName + issuedAt);
    }

    private String generateVerificationHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.substring(0, 16).toUpperCase();
        } catch (Exception e) {
            return "SSS-MED-VERIFIED-" + (int)(Math.random() * 999999);
        }
    }

    public String getSlipId() { return slipId; }
    public String getIssuedAt() { return issuedAt; }
    public String getValidUntil() { return validUntil; }
    public String getAbhaNumberPlaceholder() { return abhaNumberPlaceholder; }
    public String getPatientName() { return patientName; }
    public int getPatientAge() { return patientAge; }
    public String getPatientGender() { return patientGender; }
    public String getTriageRiskLevel() { return triageRiskLevel; }
    public int getUrgencyScore() { return urgencyScore; }
    public String getPrimaryImpression() { return primaryImpression; }
    public List<String> getReportedSymptoms() { return reportedSymptoms; }
    public List<String> getRedFlags() { return redFlags; }
    public String getDestinationFacilityName() { return destinationFacilityName; }
    public String getDestinationFacilityType() { return destinationFacilityType; }
    public String getDestinationAddress() { return destinationAddress; }
    public String getDestinationHelpline() { return destinationHelpline; }
    public String getAssignedDepartment() { return assignedDepartment; }
    public String getOpdTokenNumber() { return opdTokenNumber; }
    public String getPriorityQueueCode() { return priorityQueueCode; }
    public String getVerificationHash() { return verificationHash; }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"slipId\":\"").append(escapeJson(slipId)).append("\",");
        sb.append("\"issuedAt\":\"").append(escapeJson(issuedAt)).append("\",");
        sb.append("\"validUntil\":\"").append(escapeJson(validUntil)).append("\",");
        sb.append("\"abhaNumberPlaceholder\":\"").append(escapeJson(abhaNumberPlaceholder)).append("\",");
        sb.append("\"patientName\":\"").append(escapeJson(patientName)).append("\",");
        sb.append("\"patientAge\":").append(patientAge).append(",");
        sb.append("\"patientGender\":\"").append(escapeJson(patientGender)).append("\",");
        sb.append("\"triageRiskLevel\":\"").append(escapeJson(triageRiskLevel)).append("\",");
        sb.append("\"urgencyScore\":").append(urgencyScore).append(",");
        sb.append("\"primaryImpression\":\"").append(escapeJson(primaryImpression)).append("\",");
        sb.append("\"destinationFacilityId\":\"").append(escapeJson(destinationFacilityId)).append("\",");
        sb.append("\"destinationFacilityName\":\"").append(escapeJson(destinationFacilityName)).append("\",");
        sb.append("\"destinationFacilityType\":\"").append(escapeJson(destinationFacilityType)).append("\",");
        sb.append("\"destinationAddress\":\"").append(escapeJson(destinationAddress)).append("\",");
        sb.append("\"destinationHelpline\":\"").append(escapeJson(destinationHelpline)).append("\",");
        sb.append("\"assignedDepartment\":\"").append(escapeJson(assignedDepartment)).append("\",");
        sb.append("\"opdTokenNumber\":\"").append(escapeJson(opdTokenNumber)).append("\",");
        sb.append("\"priorityQueueCode\":\"").append(escapeJson(priorityQueueCode)).append("\",");
        sb.append("\"verificationHash\":\"").append(escapeJson(verificationHash)).append("\",");

        sb.append("\"reportedSymptoms\":[");
        for (int i = 0; i < reportedSymptoms.size(); i++) {
            sb.append("\"").append(escapeJson(reportedSymptoms.get(i))).append("\"");
            if (i < reportedSymptoms.size() - 1) sb.append(",");
        }
        sb.append("],");

        sb.append("\"redFlags\":[");
        for (int i = 0; i < redFlags.size(); i++) {
            sb.append("\"").append(escapeJson(redFlags.get(i))).append("\"");
            if (i < redFlags.size() - 1) sb.append(",");
        }
        sb.append("]");

        sb.append("}");
        return sb.toString();
    }

    private static String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
