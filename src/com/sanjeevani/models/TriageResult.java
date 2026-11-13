package com.sanjeevani.models;

import java.util.List;
import java.util.ArrayList;

/**
 * Triage result produced by the Bayesian and rule-based AI clinical engine.
 */
public class TriageResult {
    public static final String RISK_EMERGENCY = "EMERGENCY"; // Red - Immediate ER / 108
    public static final String RISK_HIGH = "HIGH";           // Orange - Visit Hospital/CHC within 6 hours
    public static final String RISK_MODERATE = "MODERATE";   // Yellow - Visit PHC within 24 hours
    public static final String RISK_LOW = "LOW";             // Green - Self Care / Teleconsult / Sub-Centre

    private String riskLevel;
    private int urgencyScore; // 0 - 100
    private String primaryCareRecommendation;
    private String clinicalSummary;
    private List<String> redFlagsDetected;
    private List<DifferentialCondition> differentialPossibilities;
    private String recommendedFacilityCategory; // PHC, CHC, DISTRICT_HOSPITAL, TERTIARY_AIIMS
    private List<String> immediateActionSteps;
    private List<String> whatNotToDo;
    private List<String> suggestedFollowUpQuestions;
    private double vitalAlertScore; // NEWS2 / qSOFA inspired score
    private boolean isEmergencyHotlineNeeded;

    public static class DifferentialCondition {
        private String name;
        private String icd11;
        private double probabilityPercent;
        private String explanation;

        public DifferentialCondition(String name, String icd11, double probabilityPercent, String explanation) {
            this.name = name;
            this.icd11 = icd11;
            this.probabilityPercent = probabilityPercent;
            this.explanation = explanation;
        }

        public String getName() { return name; }
        public String getIcd11() { return icd11; }
        public double getProbabilityPercent() { return probabilityPercent; }
        public String getExplanation() { return explanation; }
    }

    public TriageResult(String riskLevel, int urgencyScore, String primaryCareRecommendation, 
                        String clinicalSummary, String recommendedFacilityCategory) {
        this.riskLevel = riskLevel;
        this.urgencyScore = urgencyScore;
        this.primaryCareRecommendation = primaryCareRecommendation;
        this.clinicalSummary = clinicalSummary;
        this.recommendedFacilityCategory = recommendedFacilityCategory;
        this.redFlagsDetected = new ArrayList<>();
        this.differentialPossibilities = new ArrayList<>();
        this.immediateActionSteps = new ArrayList<>();
        this.whatNotToDo = new ArrayList<>();
        this.suggestedFollowUpQuestions = new ArrayList<>();
        this.vitalAlertScore = 0.0;
        this.isEmergencyHotlineNeeded = RISK_EMERGENCY.equals(riskLevel);
    }

    public void addRedFlag(String redFlag) { redFlagsDetected.add(redFlag); }
    public void addDifferential(String name, String icd11, double probabilityPercent, String explanation) {
        differentialPossibilities.add(new DifferentialCondition(name, icd11, probabilityPercent, explanation));
    }
    public void addActionStep(String step) { immediateActionSteps.add(step); }
    public void addWhatNotToDo(String tip) { whatNotToDo.add(tip); }
    public void addFollowUpQuestion(String q) { suggestedFollowUpQuestions.add(q); }

    public String getRiskLevel() { return riskLevel; }
    public int getUrgencyScore() { return urgencyScore; }
    public String getPrimaryCareRecommendation() { return primaryCareRecommendation; }
    public String getClinicalSummary() { return clinicalSummary; }
    public List<String> getRedFlagsDetected() { return redFlagsDetected; }
    public List<DifferentialCondition> getDifferentialPossibilities() { return differentialPossibilities; }
    public String getRecommendedFacilityCategory() { return recommendedFacilityCategory; }
    public List<String> getImmediateActionSteps() { return immediateActionSteps; }
    public List<String> getWhatNotToDo() { return whatNotToDo; }
    public List<String> getSuggestedFollowUpQuestions() { return suggestedFollowUpQuestions; }
    public double getVitalAlertScore() { return vitalAlertScore; }
    public void setVitalAlertScore(double vitalAlertScore) { this.vitalAlertScore = vitalAlertScore; }
    public boolean isEmergencyHotlineNeeded() { return isEmergencyHotlineNeeded; }
    public void setEmergencyHotlineNeeded(boolean emergencyHotlineNeeded) { isEmergencyHotlineNeeded = emergencyHotlineNeeded; }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"riskLevel\":\"").append(escapeJson(riskLevel)).append("\",");
        sb.append("\"urgencyScore\":").append(urgencyScore).append(",");
        sb.append("\"primaryCareRecommendation\":\"").append(escapeJson(primaryCareRecommendation)).append("\",");
        sb.append("\"clinicalSummary\":\"").append(escapeJson(clinicalSummary)).append("\",");
        sb.append("\"recommendedFacilityCategory\":\"").append(escapeJson(recommendedFacilityCategory)).append("\",");
        sb.append("\"vitalAlertScore\":").append(vitalAlertScore).append(",");
        sb.append("\"isEmergencyHotlineNeeded\":").append(isEmergencyHotlineNeeded).append(",");
        
        sb.append("\"redFlagsDetected\":[");
        for (int i = 0; i < redFlagsDetected.size(); i++) {
            sb.append("\"").append(escapeJson(redFlagsDetected.get(i))).append("\"");
            if (i < redFlagsDetected.size() - 1) sb.append(",");
        }
        sb.append("],");

        sb.append("\"differentialPossibilities\":[");
        for (int i = 0; i < differentialPossibilities.size(); i++) {
            DifferentialCondition dc = differentialPossibilities.get(i);
            sb.append("{");
            sb.append("\"name\":\"").append(escapeJson(dc.getName())).append("\",");
            sb.append("\"icd11\":\"").append(escapeJson(dc.getIcd11())).append("\",");
            sb.append("\"probabilityPercent\":").append(String.format(java.util.Locale.US, "%.1f", dc.getProbabilityPercent())).append(",");
            sb.append("\"explanation\":\"").append(escapeJson(dc.getExplanation())).append("\"");
            sb.append("}");
            if (i < differentialPossibilities.size() - 1) sb.append(",");
        }
        sb.append("],");

        sb.append("\"immediateActionSteps\":[");
        for (int i = 0; i < immediateActionSteps.size(); i++) {
            sb.append("\"").append(escapeJson(immediateActionSteps.get(i))).append("\"");
            if (i < immediateActionSteps.size() - 1) sb.append(",");
        }
        sb.append("],");

        sb.append("\"whatNotToDo\":[");
        for (int i = 0; i < whatNotToDo.size(); i++) {
            sb.append("\"").append(escapeJson(whatNotToDo.get(i))).append("\"");
            if (i < whatNotToDo.size() - 1) sb.append(",");
        }
        sb.append("],");

        sb.append("\"suggestedFollowUpQuestions\":[");
        for (int i = 0; i < suggestedFollowUpQuestions.size(); i++) {
            sb.append("\"").append(escapeJson(suggestedFollowUpQuestions.get(i))).append("\"");
            if (i < suggestedFollowUpQuestions.size() - 1) sb.append(",");
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
