package com.sanjeevani.engine;

import com.sanjeevani.models.HealthFacility;
import com.sanjeevani.models.ReferralSlip;
import com.sanjeevani.models.TriageResult;
import com.sanjeevani.models.PatientProfile;
import com.sanjeevani.data.HospitalDatabase;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generator and in-memory registry of official Government Referral Slips / Passes.
 */
public class ReferralSlipGenerator {
    private static ReferralSlipGenerator instance;
    private Map<String, ReferralSlip> issuedSlips = new ConcurrentHashMap<>();
    private AtomicInteger slipSequence = new AtomicInteger(1001);

    private ReferralSlipGenerator() {}

    public static synchronized ReferralSlipGenerator getInstance() {
        if (instance == null) {
            instance = new ReferralSlipGenerator();
        }
        return instance;
    }

    public ReferralSlip generateSlip(PatientProfile patient, TriageResult triage, 
                                     List<String> reportedSymptomNames, 
                                     String facilityId, String assignedDept) {
        HospitalDatabase db = HospitalDatabase.getInstance();
        HealthFacility targetFacility = null;
        if (facilityId != null && !facilityId.isEmpty()) {
            targetFacility = db.getFacilityById(facilityId);
        }
        if (targetFacility == null) {
            FacilityLocatorService locator = new FacilityLocatorService();
            targetFacility = locator.getRecommendedFacility(patient.getLatitude(), patient.getLongitude(), triage.getRiskLevel());
        }

        String stateCode = "IN";
        if (patient.getState() != null) {
            if (patient.getState().contains("Karnataka")) stateCode = "KA";
            else if (patient.getState().contains("Delhi")) stateCode = "DL";
            else if (patient.getState().contains("Maharashtra")) stateCode = "MH";
            else if (patient.getState().contains("Tamil")) stateCode = "TN";
            else if (patient.getState().contains("Uttar")) stateCode = "UP";
        }

        String slipId = "REF-2026-" + stateCode + "-" + slipSequence.getAndIncrement();
        
        ReferralSlip slip = new ReferralSlip(
                slipId,
                patient.getName() != null && !patient.getName().isEmpty() ? patient.getName() : "Citizen Patient",
                patient.getAge() > 0 ? patient.getAge() : 35,
                patient.getGender() != null ? patient.getGender() : "Not Specified",
                triage.getRiskLevel(),
                triage.getUrgencyScore(),
                triage.getClinicalSummary(),
                reportedSymptomNames,
                triage.getRedFlagsDetected(),
                targetFacility,
                assignedDept != null ? assignedDept : determineDepartment(triage)
        );

        issuedSlips.put(slip.getSlipId(), slip);
        return slip;
    }

    private String determineDepartment(TriageResult triage) {
        if (TriageResult.RISK_EMERGENCY.equals(triage.getRiskLevel())) {
            return "24x7 Emergency Casualty & Resuscitation Unit";
        } else if (TriageResult.RISK_HIGH.equals(triage.getRiskLevel())) {
            return "Priority Medical Care / Specialist OPD";
        } else {
            return "Primary Outpatient Department (OPD)";
        }
    }

    public ReferralSlip getSlipById(String slipId) {
        return issuedSlips.get(slipId);
    }

    public List<ReferralSlip> getAllIssuedSlips() {
        return new ArrayList<>(issuedSlips.values());
    }
}
