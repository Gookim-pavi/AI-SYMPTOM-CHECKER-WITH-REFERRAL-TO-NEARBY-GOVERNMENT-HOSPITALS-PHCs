package com.sanjeevani.models;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a disease/condition profile in the clinical ontology with
 * symptom likelihoods, primary body system, ICD-11 code, base prevalence,
 * risk tier, recommended care level, and self-care/first-aid advice.
 */
public class DiseaseProfile {
    private String id;
    private String name;
    private String category;
    private String icd11Code;
    private String riskTier; // EMERGENCY, HIGH, MODERATE, LOW
    private String recommendedFacilityCategory; // SUB_CENTRE, PHC, CHC, DISTRICT_HOSPITAL, TERTIARY_AIIMS
    private double basePrevalence; // Prior probability P(Disease)
    private Map<String, Double> symptomWeights; // Symptom ID -> Likelihood P(Symptom|Disease)
    private List<String> redFlagSymptoms;
    private String clinicalSummary;
    private List<String> homeCareTips;
    private List<String> precautions;

    public DiseaseProfile(String id, String name, String category, String icd11Code, 
                          String riskTier, String recommendedFacilityCategory, 
                          double basePrevalence, String clinicalSummary) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.icd11Code = icd11Code;
        this.riskTier = riskTier;
        this.recommendedFacilityCategory = recommendedFacilityCategory;
        this.basePrevalence = basePrevalence;
        this.clinicalSummary = clinicalSummary;
        this.symptomWeights = new HashMap<>();
        this.redFlagSymptoms = new ArrayList<>();
        this.homeCareTips = new ArrayList<>();
        this.precautions = new ArrayList<>();
    }

    public void addSymptomWeight(String symptomId, double weight) {
        symptomWeights.put(symptomId, weight);
    }

    public void addRedFlag(String symptomId) {
        redFlagSymptoms.add(symptomId);
    }

    public void addHomeCareTip(String tip) {
        homeCareTips.add(tip);
    }

    public void addPrecaution(String precaution) {
        precautions.add(precaution);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getIcd11Code() { return icd11Code; }
    public String getRiskTier() { return riskTier; }
    public String getRecommendedFacilityCategory() { return recommendedFacilityCategory; }
    public double getBasePrevalence() { return basePrevalence; }
    public Map<String, Double> getSymptomWeights() { return symptomWeights; }
    public List<String> getRedFlagSymptoms() { return redFlagSymptoms; }
    public String getClinicalSummary() { return clinicalSummary; }
    public List<String> getHomeCareTips() { return homeCareTips; }
    public List<String> getPrecautions() { return precautions; }
}
