package com.sanjeevani.data;

import com.sanjeevani.models.HealthFacility;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Repository containing curated Government Healthcare Facilities (PHCs, CHCs,
 * Sub-Divisional Hospitals, District Hospitals, and Medical Colleges/AIIMS)
 * with coordinates, emergency capabilities, bed availability, and contact lines.
 */
public class HospitalDatabase {
    private static HospitalDatabase instance;
    private List<HealthFacility> facilities = new ArrayList<>();

    private HospitalDatabase() {
        initFacilities();
    }

    public static synchronized HospitalDatabase getInstance() {
        if (instance == null) {
            instance = new HospitalDatabase();
        }
        return instance;
    }

    private void add(String id, String name, String type, String typeDisplay, String address,
                     String district, String state, String pincode, double lat, double lng,
                     String contact, String helpline, boolean is24x7, boolean icu, boolean mat,
                     boolean bloodBank, boolean freePharm, boolean ayushman, int totalBeds,
                     int availBeds, List<String> specialties, String opdHours, int waitMin) {
        facilities.add(new HealthFacility(id, name, type, typeDisplay, address, district, state,
                pincode, lat, lng, contact, helpline, is24x7, icu, mat, bloodBank, freePharm,
                ayushman, totalBeds, availBeds, specialties, opdHours, waitMin));
    }

    private void initFacilities() {
        // Karnataka / Bengaluru Region Facilities (Urban & Rural)
        add("FAC-KA-001", "Government Primary Health Centre (PHC) - Domlur", "PHC", "Primary Health Centre",
                "Old Airport Road, Domlur Village", "Bengaluru Urban", "Karnataka", "560071",
                12.9610, 77.6387, "+91 80 2297 5601", "108 / 104", false, false, true, false, true, true,
                10, 4, List.of("General Medicine", "Immunization", "Maternal Health", "NCD Screening"), "08:00 AM - 04:00 PM", 25);

        add("FAC-KA-002", "Government Primary Health Centre (PHC) - Ulsoor", "PHC", "Primary Health Centre",
                "Near Ulsoor Lake, Kensington Road", "Bengaluru Urban", "Karnataka", "560008",
                12.9817, 77.6200, "+91 80 2297 5602", "108 / 104", false, false, true, false, true, true,
                12, 6, List.of("General Medicine", "Pediatrics Screening", "Ayush Clinic"), "08:00 AM - 04:00 PM", 20);

        add("FAC-KA-003", "Community Health Centre (CHC) - K.R. Puram", "CHC", "Community Health Centre / FRU",
                "Old Madras Road, KR Puram", "Bengaluru Urban", "Karnataka", "560036",
                13.0034, 77.6960, "+91 80 2297 5800", "108 Emergency Desk", true, false, true, true, true, true,
                40, 14, List.of("General Surgery", "Obstetrics & Gynecology", "Pediatrics", "Emergency Triage", "X-Ray / Lab"), "24x7 Emergency / OPD: 09:00 AM - 01:00 PM", 35);

        add("FAC-KA-004", "Bowring & Lady Curzon District Teaching Hospital", "DISTRICT_HOSPITAL", "District Government Hospital",
                "Lady Curzon Road, Tasker Town, Shivaji Nagar", "Bengaluru Urban", "Karnataka", "560001",
                12.9822, 77.6045, "+91 80 2559 1325", "108 / 080-25591325", true, true, true, true, true, true,
                680, 112, List.of("24x7 Trauma & Emergency", "Cardiology", "Neurology", "General Surgery", "Orthopedics", "NICU/ICU", "Dialysis"), "24x7 Emergency / OPD: 08:30 AM - 01:30 PM", 45);

        add("FAC-KA-005", "Victoria Hospital - Bangalore Medical College & Research Institute", "TERTIARY_AIIMS", "Apex Medical College & Trauma Centre",
                "Fort Road, Near City Market, Kalasipalya", "Bengaluru Urban", "Karnataka", "560002",
                12.9634, 77.5753, "+91 80 2670 1150", "108 / 080-26701150", true, true, true, true, true, true,
                1200, 185, List.of("Trauma Centre Level 1", "Cardiothoracic Surgery", "Neurosurgery", "Burn Care Centre", "Medical Oncology", "Super-Specialty ICU"), "24x7 Emergency", 60);

        add("FAC-KA-006", "Government General Hospital - Jayanagar", "SUB_DISTRICT_HOSPITAL", "Sub-Divisional Hospital",
                "32nd Cross, 4th Block, Jayanagar", "Bengaluru Urban", "Karnataka", "560011",
                12.9250, 77.5838, "+91 80 2663 3220", "108 / 102", true, true, true, true, true, true,
                150, 38, List.of("General Medicine", "Pediatrics", "Obstetrics", "ENT", "Ophthalmology", "24x7 Casualty"), "24x7 Emergency / OPD: 09:00 AM - 02:00 PM", 30);

        add("FAC-KA-007", "Ayushman Arogya Mandir (Sub-Centre) - Anekal Rural", "SUB_CENTRE", "Ayushman Arogya Mandir (Sub-Centre)",
                "Main Road, Jigani Hobli, Anekal", "Bengaluru Rural", "Karnataka", "562106",
                12.7100, 77.6900, "+91 80 2784 1234", "108", false, false, false, false, true, true,
                4, 2, List.of("Primary Health Screening", "Maternal Care", "First Aid", "NCD Followup"), "09:00 AM - 03:00 PM", 15);

        add("FAC-KA-008", "Government Primary Health Centre (PHC) - Devanahalli", "PHC", "Primary Health Centre",
                "B.B. Road, Near Bus Stand, Devanahalli", "Bengaluru Rural", "Karnataka", "562110",
                13.2483, 77.7126, "+91 80 2768 2233", "108 / 104", true, false, true, false, true, true,
                20, 8, List.of("General Medicine", "Labor Room", "Immunization", "Basic Emergency"), "24x7 Emergency / OPD: 09:00 AM - 04:00 PM", 20);

        // Delhi NCR Facilities
        add("FAC-DL-001", "All India Institute of Medical Sciences (AIIMS Delhi)", "TERTIARY_AIIMS", "National Apex Medical Institute",
                "Sri Aurobindo Marg, Ansari Nagar", "New Delhi", "Delhi", "110029",
                28.5672, 77.2100, "+91 11 2658 8500", "108 / 011-26588700", true, true, true, true, true, true,
                2478, 310, List.of("Apex Trauma Centre", "Cardiology", "Neurosurgery", "Pediatric Surgery", "Oncology", "Organ Transplant", "Advanced ICU"), "24x7 Emergency", 75);

        add("FAC-DL-002", "Safdarjung Hospital & Vardhman Mahavir Medical College", "DISTRICT_HOSPITAL", "Central Government Teaching Hospital",
                "Opposite AIIMS, Ring Road", "New Delhi", "Delhi", "110029",
                28.5694, 77.2078, "+91 11 2616 5060", "108 / 102", true, true, true, true, true, true,
                1530, 190, List.of("24x7 Emergency & Trauma", "Burn Unit", "Orthopedics", "Cardiology", "Pediatrics", "Gynecology"), "24x7 Emergency", 65);

        add("FAC-DL-003", "Delhi Government Aam Aadmi Mohalla Clinic - Malviya Nagar", "PHC", "Primary Urban Health Centre",
                "Block C, Malviya Nagar", "South Delhi", "Delhi", "110017",
                28.5355, 77.2060, "+91 11 2230 7140", "108", false, false, false, false, true, true,
                0, 0, List.of("Free General Consultation", "212 Free Diagnostic Tests", "Free Essential Medicines"), "08:00 AM - 02:00 PM", 15);

        add("FAC-DL-004", "Dr. Ram Manohar Lohia (RML) Hospital", "DISTRICT_HOSPITAL", "Central Government Hospital",
                "Baba Kharak Singh Marg, Connaught Place", "Central Delhi", "Delhi", "110001",
                28.6247, 77.2023, "+91 11 2336 5525", "108 / 011-23404286", true, true, true, true, true, true,
                1210, 145, List.of("24x7 Emergency", "Cardiology", "Neurology", "Nephrology", "Critical Care", "Blood Bank"), "24x7 Emergency", 50);

        add("FAC-DL-005", "Community Health Centre (CHC) - Najafgarh", "CHC", "Rural Community Health Centre",
                "Rural Health Training Centre, Najafgarh", "South West Delhi", "Delhi", "110043",
                28.6092, 76.9798, "+91 11 2801 2244", "108 / 102", true, false, true, true, true, true,
                60, 18, List.of("General Medicine", "Labor & Delivery Room", "Pediatrics", "Minor OT", "X-Ray & Lab"), "24x7 Emergency / OPD: 09:00 AM - 01:00 PM", 25);

        // Maharashtra / Mumbai & Rural Region
        add("FAC-MH-001", "King Edward Memorial (KEM) Hospital & Seth GS Medical College", "TERTIARY_AIIMS", "Municipal Apex Teaching Hospital",
                "Acharya Donde Marg, Parel", "Mumbai", "Maharashtra", "400012",
                19.0022, 72.8423, "+91 22 2410 7000", "108 / 022-24136051", true, true, true, true, true, true,
                1800, 220, List.of("Trauma Centre", "Cardiovascular Surgery", "Neurosurgery", "Pediatric Intensive Care", "Dialysis"), "24x7 Emergency", 70);

        add("FAC-MH-002", "Government Primary Health Centre (PHC) - Manchar", "PHC", "Primary Health Centre",
                "Pune-Nashik Highway, Ambegaon", "Pune", "Maharashtra", "410503",
                19.0010, 73.9400, "+91 2133 223344", "108 / 104", true, false, true, false, true, true,
                20, 9, List.of("Primary Care", "Maternity Delivery", "National Vector Disease Control", "Free Pharmacy"), "24x7 Emergency / OPD: 09:00 AM - 04:00 PM", 20);

        add("FAC-MH-003", "District Civil Hospital - Thane", "DISTRICT_HOSPITAL", "District Government Hospital",
                "Court Naka, Near Collector Office, Thane West", "Thane", "Maharashtra", "400601",
                19.1983, 72.9781, "+91 22 2534 4455", "108 / 022-25344456", true, true, true, true, true, true,
                350, 62, List.of("24x7 Emergency", "ICU", "Orthopedics", "General Surgery", "Pediatrics", "Maternity FRU"), "24x7 Emergency", 40);

        // Tamil Nadu / Chennai & Rural Region
        add("FAC-TN-001", "Rajiv Gandhi Government General Hospital (RGGGH) & Madras Medical College", "TERTIARY_AIIMS", "Apex State Government Hospital",
                "EVR Periyar Salai, Park Town", "Chennai", "Tamil Nadu", "600003",
                13.0805, 80.2785, "+91 44 2530 5000", "108 / 044-25305100", true, true, true, true, true, true,
                2722, 340, List.of("24x7 Emergency & Trauma", "Comprehensive Stroke Unit", "Cardiology & Cath Lab", "Neurosurgery", "Toxicology Centre"), "24x7 Emergency", 55);

        add("FAC-TN-002", "Government Primary Health Centre (PHC) - Medavakkam", "PHC", "Primary Health Centre / 24x7 Delivery Centre",
                "Velachery Main Road, Medavakkam", "Chengalpattu", "Tamil Nadu", "600100",
                12.9200, 80.1900, "+91 44 2277 1234", "108 / 104", true, false, true, false, true, true,
                30, 11, List.of("24x7 Normal Delivery", "General Medicine", "Dental Clinic", "Free Lab & Pharmacy"), "24x7 Emergency / OPD: 08:00 AM - 03:00 PM", 20);

        // Uttar Pradesh / Lucknow & Rural PHCs
        add("FAC-UP-001", "King George's Medical University (KGMU) & Trauma Centre", "TERTIARY_AIIMS", "State Medical University & Trauma Centre",
                "Shah Mina Road, Chowk", "Lucknow", "Uttar Pradesh", "226003",
                26.8687, 80.9167, "+91 522 225 7450", "108 / 0522-2257451", true, true, true, true, true, true,
                3500, 420, List.of("Level-1 Trauma Centre", "Cardiology", "Neurology", "Plastic Surgery", "Pediatrics", "Critical Care"), "24x7 Emergency", 80);

        add("FAC-UP-002", "Community Health Centre (CHC) - Mohanlalganj", "CHC", "First Referral Unit (CHC/FRU)",
                "Raebareli Road, Mohanlalganj", "Lucknow", "Uttar Pradesh", "226301",
                26.6800, 80.9800, "+91 522 284 1222", "108 / 102", true, false, true, true, true, true,
                50, 16, List.of("Emergency Care", "Obstetrics & Cesarean Delivery", "Pediatrics", "Pathology Lab", "Free Drug Dispensary"), "24x7 Emergency", 30);

        add("FAC-UP-003", "Primary Health Centre (PHC) - Gosainganj", "PHC", "Primary Health Centre",
                "Sultanpur Road, Gosainganj", "Lucknow", "Uttar Pradesh", "226501",
                26.7700, 81.1200, "+91 522 281 3344", "108 / 104", false, false, true, false, true, true,
                15, 6, List.of("General OPD", "Immunization", "Antenatal Care", "Malaria Screening"), "08:00 AM - 04:00 PM", 20);
    }

    public List<HealthFacility> getAllFacilities() {
        return Collections.unmodifiableList(facilities);
    }

    public HealthFacility getFacilityById(String id) {
        for (HealthFacility f : facilities) {
            if (f.getId().equalsIgnoreCase(id)) {
                return f;
            }
        }
        return null;
    }
}
