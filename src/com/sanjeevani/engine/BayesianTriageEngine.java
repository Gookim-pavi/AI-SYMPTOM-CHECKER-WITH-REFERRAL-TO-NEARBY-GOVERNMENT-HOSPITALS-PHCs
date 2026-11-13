package com.sanjeevani.engine;

import com.sanjeevani.models.Symptom;
import com.sanjeevani.models.DiseaseProfile;
import com.sanjeevani.models.PatientProfile;
import com.sanjeevani.models.TriageResult;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Advanced Bayesian & Multi-Factor Clinical Triage Engine.
 * Integrates symptom likelihood vectors, red-flag triggers, demographic multipliers,
 * and vital signs scoring (NEWS2/qSOFA inspired).
 */
public class BayesianTriageEngine {
    private SymptomKnowledgeBase kb;

    public BayesianTriageEngine() {
        this.kb = SymptomKnowledgeBase.getInstance();
    }

    public TriageResult evaluate(List<String> symptomIds, PatientProfile patient, Map<String, Integer> symptomSeverities) {
        if (symptomIds == null || symptomIds.isEmpty()) {
            TriageResult emptyResult = new TriageResult(
                    TriageResult.RISK_LOW, 10,
                    "No symptoms selected",
                    "Please select or describe your symptoms for an AI clinical assessment.",
                    "PHC"
            );
            emptyResult.addActionStep("Select one or more symptoms from the anatomical body map or enter your symptoms in the search bar.");
            return emptyResult;
        }

        // 1. Detect Red Flags
        List<String> redFlags = new ArrayList<>();
        int maxSymptomSeverity = 1;
        
        for (String sId : symptomIds) {
            Symptom sym = kb.getSymptom(sId);
            if (sym != null) {
                int userSev = (symptomSeverities != null && symptomSeverities.containsKey(sId)) 
                        ? symptomSeverities.get(sId) : sym.getDefaultSeverity();
                if (userSev > maxSymptomSeverity) maxSymptomSeverity = userSev;

                if (sym.isRedFlag() || userSev >= 9) {
                    String msg = sym.getRedFlagMessage();
                    if (msg == null || msg.isEmpty()) {
                        msg = "Critical symptom detected: " + sym.getName();
                    }
                    redFlags.add(msg);
                }
            }
        }

        // 2. Vital Signs Scoring (NEWS2 / qSOFA Assessment)
        double vitalAlertScore = 0.0;
        List<String> vitalWarnings = new ArrayList<>();
        if (patient != null) {
            if (patient.getSpo2Percent() != null) {
                int spo2 = patient.getSpo2Percent();
                if (spo2 < 90) {
                    vitalAlertScore += 3.0;
                    redFlags.add("Critical Oxygen Saturation (SpO2: " + spo2 + "% < 90%). Emergency supplemental oxygen required.");
                } else if (spo2 <= 93) {
                    vitalAlertScore += 2.0;
                    vitalWarnings.add("Low Oxygen Saturation (SpO2: " + spo2 + "%).");
                }
            }

            if (patient.getSystolicBp() != null) {
                int sbp = patient.getSystolicBp();
                if (sbp < 90) {
                    vitalAlertScore += 3.0;
                    redFlags.add("Severe Hypotension / Shock (Systolic BP: " + sbp + " mmHg).");
                } else if (sbp > 180) {
                    vitalAlertScore += 2.5;
                    vitalWarnings.add("Hypertensive Urgency (Systolic BP: " + sbp + " mmHg).");
                }
            }

            if (patient.getBodyTempF() != null) {
                double temp = patient.getBodyTempF();
                if (temp >= 104.0) {
                    vitalAlertScore += 2.0;
                    vitalWarnings.add("Hyperpyrexia (High Fever: " + temp + "°F). Risk of febrile convulsions.");
                } else if (temp >= 101.5) {
                    vitalAlertScore += 1.0;
                }
            }

            if (patient.getRespiratoryRate() != null) {
                int rr = patient.getRespiratoryRate();
                if (rr > 25) {
                    vitalAlertScore += 2.5;
                    vitalWarnings.add("Tachypnea / Fast Breathing (" + rr + " breaths/min).");
                }
            }

            if (patient.isPregnant() && symptomIds.contains("sym_pelvic_pain_severe_pregnancy")) {
                redFlags.add("High-Risk Obstetric Emergency in Pregnancy. Immediate transfer to CHC/FRU with labor room & blood bank.");
            }
        }

        // 3. Bayesian Posterior Disease Calculation
        Map<String, DiseaseProfile> allProfiles = kb.getAllDiseaseProfiles();
        Map<String, Double> unnormalizedPosteriors = new HashMap<>();
        double totalScoreSum = 0.0;

        for (Map.Entry<String, DiseaseProfile> entry : allProfiles.entrySet()) {
            DiseaseProfile dp = entry.getValue();
            double prior = dp.getBasePrevalence();
            
            // Apply comorbidity / demographic multipliers
            if (patient != null) {
                if (patient.getAge() >= 60 && "Cardiovascular".equals(dp.getCategory())) prior *= 1.8;
                if (patient.getAge() <= 10 && "Respiratory".equals(dp.getCategory())) prior *= 1.4;
                if (patient.isPregnant() && "Obstetric".equals(dp.getCategory())) prior *= 3.0;
                if (patient.getComorbidities() != null) {
                    for (String comorb : patient.getComorbidities()) {
                        if ("Diabetes".equalsIgnoreCase(comorb) && dp.getId().equals("dis_vector_fever")) prior *= 1.3;
                        if ("Cardiac History".equalsIgnoreCase(comorb) && dp.getId().equals("dis_mi")) prior *= 2.5;
                        if ("Asthma/COPD".equalsIgnoreCase(comorb) && dp.getId().equals("dis_pneumonia")) prior *= 2.0;
                    }
                }
            }

            // Likelihood calculation: P(Symptoms | Disease)
            double likelihoodProduct = 1.0;
            int matchedSymptomCount = 0;

            for (String sId : symptomIds) {
                if (dp.getSymptomWeights().containsKey(sId)) {
                    double weight = dp.getSymptomWeights().get(sId);
                    int sev = (symptomSeverities != null && symptomSeverities.containsKey(sId))
                            ? symptomSeverities.get(sId) : 5;
                    double sevMultiplier = 0.8 + (sev / 10.0) * 0.4;
                    likelihoodProduct *= (weight * sevMultiplier);
                    matchedSymptomCount++;
                } else {
                    likelihoodProduct *= 0.15; // Penalty for absent expected symptoms
                }
            }

            if (matchedSymptomCount > 0) {
                double posterior = prior * likelihoodProduct * Math.pow(2.0, matchedSymptomCount);
                unnormalizedPosteriors.put(dp.getId(), posterior);
                totalScoreSum += posterior;
            }
        }

        // Rank differentials
        List<Map.Entry<String, Double>> sortedList = new ArrayList<>(unnormalizedPosteriors.entrySet());
        sortedList.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // 4. Urgency Classification
        String finalRiskLevel = TriageResult.RISK_LOW;
        int urgencyScore = 20;
        String primaryFacility = "PHC";
        String primaryRecommendation = "Consult General Medical Officer at Primary Health Centre (PHC).";
        String clinicalSummary = "Preliminary screening suggests mild/moderate symptoms suitable for primary care evaluation.";

        if (!redFlags.isEmpty() || vitalAlertScore >= 3.0 || maxSymptomSeverity >= 9) {
            finalRiskLevel = TriageResult.RISK_EMERGENCY;
            urgencyScore = Math.min(100, 85 + (int)(vitalAlertScore * 4) + (redFlags.size() * 3));
            primaryFacility = "DISTRICT_HOSPITAL";
            primaryRecommendation = "IMMEDIATE EMERGENCY ATTENTION REQUIRED. Call 108 Ambulance or go to the nearest 24/7 District Hospital / Emergency Trauma Centre.";
            clinicalSummary = "CRITICAL: Severe clinical red flags or physiological instability detected requiring emergency stabilization.";
        } else if (maxSymptomSeverity >= 7 || vitalAlertScore >= 1.5 || (sortedList.size() > 0 && "HIGH".equals(kb.getDiseaseProfile(sortedList.get(0).getKey()).getRiskTier()))) {
            finalRiskLevel = TriageResult.RISK_HIGH;
            urgencyScore = 65 + (int)(vitalAlertScore * 5);
            primaryFacility = "CHC";
            primaryRecommendation = "Urgent medical assessment needed within 6 hours. Visit nearest Community Health Centre (CHC) or Sub-District Hospital.";
            clinicalSummary = "High urgency symptoms identified with potential for rapid progression. Medical workup and diagnostic evaluation indicated.";
        } else if (maxSymptomSeverity >= 4 || symptomIds.size() >= 3) {
            finalRiskLevel = TriageResult.RISK_MODERATE;
            urgencyScore = 45;
            primaryFacility = "PHC";
            primaryRecommendation = "Schedule a visit to your nearest Government Primary Health Centre (PHC) within 24 hours.";
            clinicalSummary = "Moderate clinical presentation manageable through routine outpatient consultation and primary diagnostics.";
        } else {
            finalRiskLevel = TriageResult.RISK_LOW;
            urgencyScore = 25;
            primaryFacility = "PHC";
            primaryRecommendation = "Primary care consultation or Ayushman Arogya Mandir (Sub-Centre) / Self-Care with monitoring.";
            clinicalSummary = "Mild self-limiting or early-stage symptoms. Supportive care, hydration, and primary health monitoring advised.";
        }

        TriageResult result = new TriageResult(finalRiskLevel, urgencyScore, primaryRecommendation, clinicalSummary, primaryFacility);
        result.setVitalAlertScore(vitalAlertScore);

        for (String rf : redFlags) {
            result.addRedFlag(rf);
        }
        for (String vw : vitalWarnings) {
            result.addRedFlag("[Warning] " + vw);
        }

        // Add top differentials
        int count = 0;
        for (Map.Entry<String, Double> entry : sortedList) {
            if (count >= 3) break;
            DiseaseProfile dp = kb.getDiseaseProfile(entry.getKey());
            if (dp != null) {
                double pct = totalScoreSum > 0 ? (entry.getValue() / totalScoreSum) * 100.0 : 50.0;
                if (pct < 10.0 && count > 0) pct = 10.0 + (3 - count) * 5;
                result.addDifferential(dp.getName(), dp.getIcd11Code(), Math.min(96.0, pct), dp.getClinicalSummary());
                
                // Add disease tips
                for (String tip : dp.getHomeCareTips()) {
                    if (!result.getImmediateActionSteps().contains(tip) && result.getImmediateActionSteps().size() < 4) {
                        result.addActionStep(tip);
                    }
                }
                for (String prec : dp.getPrecautions()) {
                    if (!result.getWhatNotToDo().contains(prec) && result.getWhatNotToDo().size() < 3) {
                        result.addWhatNotToDo(prec);
                    }
                }
            }
            count++;
        }

        if (result.getImmediateActionSteps().isEmpty()) {
            result.addActionStep("Maintain adequate oral hydration (boiled water / ORS).");
            result.addActionStep("Carry your Government Referral Slip to the OPD desk for priority verification.");
            result.addActionStep("Keep track of your temperature and symptom progression.");
        }
        if (result.getWhatNotToDo().isEmpty()) {
            result.addWhatNotToDo("Do not consume unprescribed strong antibiotics or painkillers without medical consultation.");
            result.addWhatNotToDo("Do not ignore escalating breathing distress, chest pain, or confusion.");
        }

        // Adaptive follow-up questions
        if (symptomIds.contains("sym_fever_high_grade") || symptomIds.contains("sym_fever_mild_low_grade")) {
            result.addFollowUpQuestion("How many days has the fever been present? (e.g. <3 days vs >1 week)");
            result.addFollowUpQuestion("Have you observed any skin rashes, bleeding gums, or severe pain behind the eyes?");
        }
        if (symptomIds.contains("sym_cough_dry") || symptomIds.contains("sym_cough_wet_phlegm")) {
            result.addFollowUpQuestion("Is there any history of night sweats, significant weight loss, or contact with TB patients?");
        }
        if (symptomIds.contains("sym_abdominal_cramping_mild") || symptomIds.contains("sym_diarrhea_watery")) {
            result.addFollowUpQuestion("Are you able to drink water and fluids without immediate vomiting?");
        }

        return result;
    }
}
