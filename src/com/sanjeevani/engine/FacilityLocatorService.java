package com.sanjeevani.engine;

import com.sanjeevani.models.HealthFacility;
import com.sanjeevani.models.PatientProfile;
import com.sanjeevani.data.HospitalDatabase;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Geo-spatial routing and facility matching engine.
 * Computes Haversine great-circle distances, travel time estimates,
 * and matches optimal care tiers based on clinical triage urgency.
 */
public class FacilityLocatorService {
    private static final double EARTH_RADIUS_KM = 6371.0;
    private HospitalDatabase db;

    public FacilityLocatorService() {
        this.db = HospitalDatabase.getInstance();
    }

    public List<HealthFacility> findNearbyFacilities(double userLat, double userLng, 
                                                     String facilityTypeFilter, 
                                                     boolean emergencyOnly, 
                                                     String triageTier, 
                                                     int maxResults) {
        List<HealthFacility> all = db.getAllFacilities();
        List<HealthFacility> resultList = new ArrayList<>();

        for (HealthFacility src : all) {
            // Check filters
            if (facilityTypeFilter != null && !facilityTypeFilter.isEmpty() && !"ALL".equalsIgnoreCase(facilityTypeFilter)) {
                if (!src.getFacilityType().equalsIgnoreCase(facilityTypeFilter)) {
                    continue;
                }
            }

            if (emergencyOnly && !src.is24x7Emergency()) {
                continue;
            }

            // Compute distance
            double dist = calculateHaversineDistance(userLat, userLng, src.getLatitude(), src.getLongitude());
            
            // Create a clone/wrapper with dynamic distance
            HealthFacility copy = new HealthFacility(
                    src.getId(), src.getName(), src.getFacilityType(), src.getTypeDisplayName(),
                    src.getAddress(), src.getDistrict(), src.getState(), src.getPincode(),
                    src.getLatitude(), src.getLongitude(), src.getContactNumber(),
                    src.getEmergencyHelpline(), src.is24x7Emergency(), src.hasIcu(),
                    src.hasMaternityWard(), src.hasBloodBank(), src.hasFreePharmacy(),
                    src.isAyushmanEmpaneled(), src.getTotalBeds(), src.getAvailableBeds(),
                    src.getAvailableSpecialties(), src.getOpdTimings(), src.getEstimatedWaitMinutes()
            );
            
            copy.setDistanceKm(dist);
            // Average road transit estimate (approx 35 km/h urban/rural mix + 5 min dispatch overhead)
            int travelMin = (int) Math.round((dist / 35.0) * 60.0) + 5;
            copy.setEstimatedTravelMinutes(Math.max(5, travelMin));

            resultList.add(copy);
        }

        // Sort based on distance & clinical tier appropriateness
        resultList.sort((a, b) -> {
            if ("EMERGENCY".equalsIgnoreCase(triageTier)) {
                // If emergency, prioritize 24x7 emergency and ICU capability first, then distance
                int emergencyComp = Boolean.compare(b.is24x7Emergency(), a.is24x7Emergency());
                if (emergencyComp != 0) return emergencyComp;
                int icuComp = Boolean.compare(b.hasIcu(), a.hasIcu());
                if (icuComp != 0) return icuComp;
            }
            return Double.compare(a.getDistanceKm(), b.getDistanceKm());
        });

        if (resultList.size() > maxResults) {
            return resultList.subList(0, maxResults);
        }
        return resultList;
    }

    public HealthFacility getRecommendedFacility(double userLat, double userLng, String triageTier) {
        List<HealthFacility> nearby = findNearbyFacilities(userLat, userLng, null, false, triageTier, 5);
        if (nearby.isEmpty()) {
            return db.getAllFacilities().get(0);
        }
        return nearby.get(0);
    }

    public static double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}
