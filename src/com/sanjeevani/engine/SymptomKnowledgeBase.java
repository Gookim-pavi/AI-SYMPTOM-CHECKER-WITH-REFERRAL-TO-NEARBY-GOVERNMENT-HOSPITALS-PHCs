package com.sanjeevani.engine;

import com.sanjeevani.models.Symptom;
import com.sanjeevani.models.DiseaseProfile;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Clinical Knowledge Base storing symptoms, ICD-11 & SNOMED mappings,
 * disease profiles with Bayesian conditional probabilities, and acute red flag triggers.
 */
public class SymptomKnowledgeBase {
    private static SymptomKnowledgeBase instance;
    
    private Map<String, Symptom> symptomsById = new HashMap<>();
    private List<Symptom> allSymptoms = new ArrayList<>();
    private Map<String, DiseaseProfile> diseaseProfiles = new HashMap<>();

    private SymptomKnowledgeBase() {
        initSymptoms();
        initDiseaseProfiles();
    }

    public static synchronized SymptomKnowledgeBase getInstance() {
        if (instance == null) {
            instance = new SymptomKnowledgeBase();
        }
        return instance;
    }

    private void addSymptom(String id, String name, String system, int severity, 
                            String snomed, String icd11, boolean isRedFlag, 
                            String redFlagMsg, List<String> synonyms, String desc) {
        Symptom s = new Symptom(id, name, system, severity, snomed, icd11, isRedFlag, redFlagMsg, synonyms, desc);
        symptomsById.put(id, s);
        allSymptoms.add(s);
    }

    private void initSymptoms() {
        // Head & Neurological
        addSymptom("sym_headache_mild", "Tension Headache / Dull Head Pain", "Head", 3, "25064002", "8A80", false, null,
                Arrays.asList("headache", "head pain", "dull headache", "tension headache", "forehead ache", "sir dard"), "Mild to moderate aching pain across head or forehead");
        addSymptom("sym_headache_severe_thunderclap", "Sudden Severe 'Thunderclap' Headache", "Head", 10, "442491000124103", "8A8Z", true,
                "Sudden explosive worst headache of life can indicate Subarachnoid Hemorrhage or intracranial emergency.",
                Arrays.asList("worst headache of life", "thunderclap headache", "sudden intense headache", "explosive head pain"), "Sudden peak intensity head pain reaching maximum in seconds");
        addSymptom("sym_dizziness_vertigo", "Dizziness / Spinning Sensation (Vertigo)", "Head", 4, "40917007", "AB31", false, null,
                Arrays.asList("dizziness", "spinning", "lightheaded", "chakkar", "unsteady", "giddiness"), "Feeling of room spinning or lightheaded instability");
        addSymptom("sym_confusion_altered_mental", "Altered Consciousness / Severe Confusion", "Head", 9, "40917007", "MB21", true,
                "Acute confusion or unresponsiveness can signal stroke, encephalitis, or severe sepsis.",
                Arrays.asList("confusion", "disorientation", "drowsiness", "slurred speech", "loss of consciousness", "behosh"), "Inability to think clearly or altered alertness");
        addSymptom("sym_seizures_convulsions", "Seizures / Fits / Involuntary Shaking", "Head", 10, "91175000", "8A68", true,
                "Seizures require immediate medical stabilization and airway protection.",
                Arrays.asList("seizure", "convulsions", "fits", "shaking", "epilepsy fit", "daura"), "Violent involuntary muscle contractions and loss of awareness");
        addSymptom("sym_facial_droop_weakness", "Facial Drooping / One-Sided Weakness (FAST Sign)", "Head", 10, "275305007", "MB40", true,
                "Sudden one-sided facial paralysis or limb weakness is a medical emergency (Stroke FAST Protocol).",
                Arrays.asList("facial drooping", "face droop", "arm weakness", "hemiplegia", "stroke signs", "lakwa"), "Sudden asymmetry of face or weakness in one side of body");

        // Eyes & ENT
        addSymptom("sym_eye_redness_discharge", "Eye Redness / Itching / Conjunctivitis", "ENT", 3, "193899003", "9A60", false, null,
                Arrays.asList("red eyes", "pink eye", "eye discharge", "conjunctivitis", "aankh laal"), "Irritation, redness, and watery or purulent eye discharge");
        addSymptom("sym_sore_throat", "Sore Throat / Painful Swallowing", "ENT", 3, "162397003", "CA02", false, null,
                Arrays.asList("sore throat", "throat pain", "pharyngitis", "gala kharab", "gale me dard"), "Scratchiness, pain, or difficulty swallowing");
        addSymptom("sym_ear_pain_discharge", "Ear Pain / Ear Drainage", "ENT", 4, "271807003", "AA30", false, null,
                Arrays.asList("ear pain", "ear ache", "ear discharge", "kaan dard"), "Throbbing or sharp pain in the ear with or without fluid discharge");
        addSymptom("sym_nasal_congestion_runny", "Runny Nose / Nasal Congestion / Sneezing", "ENT", 2, "68235000", "CA08", false, null,
                Arrays.asList("runny nose", "blocked nose", "sneezing", "cold", "rhinitis", "zukam", "naak behna"), "Mucus discharge, blockage of nasal passages");
        addSymptom("sym_loss_of_smell_taste", "Loss of Smell or Taste", "ENT", 3, "44169009", "MB45", false, null,
                Arrays.asList("loss of smell", "anosmia", "loss of taste", "ageusia"), "Partial or total loss of olfactory/gustatory perception");

        // Chest & Cardiovascular
        addSymptom("sym_chest_pain_pressure", "Crushing Chest Pain / Pressure Radiating to Arm/Jaw", "Chest", 10, "29857009", "BA41", true,
                "Crushing retrosternal chest pain radiating to left arm/jaw is a hallmark of Acute Myocardial Infarction (Heart Attack). Call 108 immediately.",
                Arrays.asList("chest pain", "heart pain", "chest tightness", "chest pressure", "radiating pain to arm", "seene me dard"), "Heavy pressure, squeezing, or severe pain in central chest");
        addSymptom("sym_palpitations_rapid_pulse", "Rapid / Irregular Heartbeats (Palpitations)", "Chest", 6, "80313002", "MC80", false, null,
                Arrays.asList("palpitations", "racing heart", "fluttering heart", "dil ki dhadkan", "arrhythmia"), "Awareness of abnormally fast, pounding, or skipped heartbeats");
        addSymptom("sym_swelling_legs_pedal_edema", "Bilateral Leg / Ankle Swelling (Pedal Edema)", "Chest", 5, "267038008", "EE40", false, null,
                Arrays.asList("leg swelling", "swollen feet", "swollen ankles", "edema", "pair me sujan"), "Accumulation of fluid causing swelling in lower extremities");

        // Respiratory & Lungs
        addSymptom("sym_shortness_of_breath_severe", "Severe Shortness of Breath / Gasping for Air", "Respiratory", 10, "267036007", "MD11", true,
                "Severe respiratory distress or inability to speak in sentences warrants immediate hospital ER referral.",
                Arrays.asList("shortness of breath", "difficulty breathing", "breathlessness", "gasping", "saans lene me takleef", "asthma attack"), "Struggling to inhale sufficient oxygen or severe chest indrawing");
        addSymptom("sym_cough_dry", "Persistent Dry Cough", "Respiratory", 3, "11833005", "MD30", false, null,
                Arrays.asList("dry cough", "tickly cough", "khasi", "sukhi khasi"), "Continuous cough without mucus production");
        addSymptom("sym_cough_wet_phlegm", "Productive Cough with Yellow/Green Phlegm", "Respiratory", 4, "28743005", "CA20", false, null,
                Arrays.asList("wet cough", "phlegm", "productive cough", "balgam wali khasi"), "Cough producing thick yellow, green, or rusty sputum");
        addSymptom("sym_cough_blood_hemoptysis", "Coughing up Blood (Hemoptysis)", "Respiratory", 9, "66857006", "MD32", true,
                "Coughing up blood can indicate pulmonary tuberculosis, pulmonary embolism, or vascular lung lesions.",
                Arrays.asList("coughing blood", "blood in sputum", "hemoptysis", "khasi me khoon"), "Expectoration of blood or blood-streaked sputum");
        addSymptom("sym_wheezing_stridor", "Wheezing / Whistling Breath Sound", "Respiratory", 6, "56018004", "MD12", false, null,
                Arrays.asList("wheezing", "whistling breath", "saans me seeti"), "High-pitched musical whistling sound while exhaling");

        // Abdomen & Gastrointestinal
        addSymptom("sym_abdominal_pain_severe_acute", "Severe Sharp Abdominal Pain (Acute Abdomen / Rigidity)", "Abdomen", 9, "21522001", "MD81", true,
                "Sudden severe abdominal pain with guarding/rigidity indicates possible appendicitis, perforated ulcer, or bowel obstruction.",
                Arrays.asList("severe stomach pain", "acute abdomen", "sharp belly pain", "appendix pain", "pet me teez dard"), "Intense, localized or generalized abdominal agony");
        addSymptom("sym_abdominal_cramping_mild", "Mild Abdominal Cramps / Bloating", "Abdomen", 3, "80394007", "MD80", false, null,
                Arrays.asList("stomach cramps", "belly ache", "bloating", "gas pain", "pet kharab"), "Mild crampy discomfort or gaseous distension in stomach");
        addSymptom("sym_nausea_vomiting", "Nausea and Persistent Vomiting", "Abdomen", 5, "422400008", "MD90", false, null,
                Arrays.asList("vomiting", "nausea", "throwing up", "ulti", "je machlana"), "Inability to retain liquids, recurrent vomiting");
        addSymptom("sym_vomiting_blood_hematemesis", "Vomiting Blood / Coffee-Ground Vomitus", "Abdomen", 10, "249497008", "ME02", true,
                "Upper gastrointestinal bleeding requires immediate emergency transfusion and endoscopy.",
                Arrays.asList("vomiting blood", "blood in vomit", "hematemesis", "ulti me khoon"), "Emesis of bright red blood or dark coffee-ground material");
        addSymptom("sym_diarrhea_watery", "Watery Diarrhea / Loose Stools (>3 times/day)", "Abdomen", 4, "62315008", "ME05", false, null,
                Arrays.asList("diarrhea", "loose motions", "watery stool", "dast", "pet saf na hona"), "Frequent watery bowel movements leading to fluid loss");
        addSymptom("sym_blood_in_stool", "Black Tarry Stool / Blood in Stool (Melena/Hematochezia)", "Abdomen", 8, "249495000", "ME00", true,
                "Significant gastrointestinal hemorrhage requires medical evaluation and blood work.",
                Arrays.asList("blood in stool", "black stool", "melena", "tatti me khoon"), "Visible blood or pitch-black tarry bowel discharge");
        addSymptom("sym_jaundice_yellow_eyes", "Yellowing of Skin/Eyes (Jaundice) & Dark Urine", "Abdomen", 6, "18165001", "ME21", false, null,
                Arrays.asList("jaundice", "yellow eyes", "yellow skin", "piliya", "dark yellow urine"), "Bilirubin deposition causing yellow sclera and skin");

        // Pelvis & Urogenital
        addSymptom("sym_burning_urination_dysuria", "Burning Pain During Urination (Dysuria)", "Pelvis", 4, "49650001", "MF52", false, null,
                Arrays.asList("burning urination", "dysuria", "pain peeing", "peshab me jalan"), "Stinging, burning discomfort when passing urine");
        addSymptom("sym_frequent_urgent_urination", "Frequent / Urgent Need to Urinate", "Pelvis", 3, "162116003", "MF50", false, null,
                Arrays.asList("frequent urination", "urgent peeing", "bar bar peshab"), "Urgent and unusually frequent daytime or nighttime urination");
        addSymptom("sym_blood_in_urine", "Blood in Urine (Hematuria)", "Pelvis", 7, "34436003", "MF53", true,
                "Visible hematuria warrants urgent urological/renal assessment.",
                Arrays.asList("blood in urine", "red urine", "hematuria", "peshab me khoon"), "Pink, red, or cola-colored urine due to red blood cells");
        addSymptom("sym_pelvic_pain_severe_pregnancy", "Severe Pelvic Pain or Vaginal Bleeding in Pregnancy", "Pelvis", 10, "289908002", "JA65", true,
                "Vaginal bleeding or acute pelvic pain during pregnancy can indicate ectopic pregnancy or placental abruption. Immediate OB/GYN referral required.",
                Arrays.asList("bleeding in pregnancy", "severe pelvic pain pregnancy", "garbh me dard", "pregnancy bleeding"), "Acute lower abdominal/pelvic pain with or without bleeding in pregnant patient");

        // Musculoskeletal & Spine
        addSymptom("sym_back_pain_lumbar", "Lower Back Pain / Muscle Stiffness", "Musculoskeletal", 4, "279039007", "ME84", false, null,
                Arrays.asList("back pain", "lower back ache", "kamar dard", "lumbago"), "Aching or stiffness in lower spinal region");
        addSymptom("sym_joint_pain_swelling", "Joint Pain, Swelling, and Morning Stiffness", "Musculoskeletal", 4, "57676002", "FA20", false, null,
                Arrays.asList("joint pain", "swollen joints", "arthritis pain", "jodome dard", "ghutne me dard"), "Inflammation, tenderness, and stiffness in knees, fingers, or wrists");
        addSymptom("sym_trauma_bone_fracture", "Severe Bone/Joint Deformity After Fall/Accident", "Musculoskeletal", 9, "125605004", "NC32", true,
                "Suspected bone fracture or joint dislocation needs immediate splinting and radiography at CHC/Hospital.",
                Arrays.asList("fracture", "broken bone", "bone injury", "haddi tootna"), "Inability to bear weight, severe swelling, visible deformity after injury");

        // Skin & Integumentary
        addSymptom("sym_skin_rash_mild", "Itchy Skin Rash / Hives (Urticaria)", "Skin", 3, "271807003", "EA90", false, null,
                Arrays.asList("skin rash", "itching", "hives", "allergies", "khujli", "daane"), "Localized or generalized erythematous itching patches");
        addSymptom("sym_skin_anaphylaxis_lip_swelling", "Rapid Lip/Tongue/Throat Swelling with Hives (Anaphylaxis)", "Skin", 10, "39579001", "4A84", true,
                "Anaphylactic shock is immediately life-threatening. Administer Epinephrine and dial 108 Emergency.",
                Arrays.asList("anaphylaxis", "lip swelling", "tongue swelling", "throat swelling allergic", "severe allergy"), "Rapid swelling of lips, throat, face accompanied by breathing difficulty");
        addSymptom("sym_non_healing_ulcer", "Chronic Non-Healing Wound / Foot Ulcer", "Skin", 5, "284196006", "EE41", false, null,
                Arrays.asList("foot ulcer", "non healing wound", "diabetic ulcer", "ghao jo bhar nahi raha"), "Persistent open sore, common in diabetic individuals");

        // Systemic & General (Enhanced Fever Triage Suite)
        addSymptom("sym_fever_high_grade", "High-Grade Continuous Fever (>102°F / 38.9°C)", "Systemic", 7, "386661006", "MG26", false, null,
                Arrays.asList("high fever", "continuous fever", "bukhar", "teez bukhar", "high temperature", "tapman"), "Elevated core body temperature above 102 degrees Fahrenheit");
        addSymptom("sym_fever_mild_low_grade", "Mild / Low-Grade Fever (<100.5°F)", "Systemic", 3, "386661006", "MG26", false, null,
                Arrays.asList("mild fever", "low grade fever", "halka bukhar", "subfebrile"), "Slightly elevated body temperature with general malaise");
        addSymptom("sym_fever_chills_rigors_malaria", "High Spiking Fever with Severe Chills, Shivering & Rigors (Malaria Pattern)", "Systemic", 8, "386661006", "1F40", false, null,
                Arrays.asList("chills", "rigors", "shivering fever", "malaria fever", "intermittent fever", "thand lag ke bukhar", "kampkapi bukhar", "periodic fever", "teeth chattering"),
                "Sudden sharp temperature spikes accompanied by violent shivering and rigors, common in Plasmodium infection.");
        addSymptom("sym_fever_breakbone_retro_orbital_dengue", "Breakbone Fever with Severe Pain Behind Eyes & Extreme Body Ache (Dengue Pattern)", "Systemic", 8, "386661006", "1D20", false, null,
                Arrays.asList("breakbone fever", "pain behind eyes", "retro orbital pain", "dengue fever", "severe body ache fever", "haddi tod bukhar", "aankhon ke peeche dard"),
                "Intense deep muscular, bone, and retro-orbital (behind eyeball) throbbing pain associated with acute fever.");
        addSymptom("sym_fever_stepladder_typhoid", "Prolonged Step-Ladder Rising Fever with Coated Tongue & Abdominal Discomfort (Typhoid Pattern)", "Systemic", 7, "4834000", "1A07", false, null,
                Arrays.asList("step ladder fever", "typhoid fever", "continuous fever", "prolonged fever", "motijhara", "meadi bukhar", "coated tongue fever", "enteric fever"),
                "Gradually increasing fever pattern over several days with headache, malaise, coated tongue, and abdominal distress.");
        addSymptom("sym_fever_evening_rise_night_sweats_tb", "Low-Grade Evening Rise of Fever with Drenching Night Sweats (Tuberculosis Pattern)", "Systemic", 7, "43724002", "1B10", false, null,
                Arrays.asList("evening fever", "night sweats", "tb fever", "chronic low fever", "shaam ko bukhar", "raat ko paseena", "subfebrile temperature", "unexplained weight loss fever"),
                "Persistent low-grade temperature elevation peaking in late afternoons/evenings accompanied by profuse nighttime sweating.");
        addSymptom("sym_fever_petechial_rash_purpura", "High Fever with Red Pinpoint Spots / Petechial Rash (Hemorrhagic / Sepsis Alert)", "Skin", 10, "271813007", "1D00", true,
                "High fever with non-blanching petechial skin rash or bleeding spots indicates acute meningococcemia or severe dengue hemorrhagic fever. Immediate Level-1 emergency referral required.",
                Arrays.asList("fever with rash", "petechiae", "red spots on skin with fever", "purpura", "hemorrhagic rash", "bukhar ke sath laal daane", "bleeding spots"),
                "Tiny purple, red, or brown spots on skin that do not turn pale (blanch) when pressed with glass.");
        addSymptom("sym_fever_pediatric_febrile_seizure", "High Fever with Convulsions / Shaking in Child (Febrile Seizure)", "Head", 10, "91175000", "8A60", true,
                "Febrile convulsions in children require emergency airway stabilization, rapid cooling, and urgent pediatric hospital evaluation.",
                Arrays.asList("febrile seizure", "fever fits", "child fever seizure", "fever convulsions", "bacche ko bukhar me daura", "jhatke aana"),
                "Fits, eye-rolling, stiffening, and jerky muscle contractions triggered by a rapid spike in pediatric body temperature.");
        addSymptom("sym_fever_hypothermia_sepsis_shock", "Fever/Hypothermia with Confusion, Rapid Breathing & Low Blood Pressure (Septic Shock)", "Systemic", 10, "76571007", "1G40", true,
                "Septic shock is a life-threatening organ failure emergency requiring immediate IV resuscitation, inotropes, and broad-spectrum antibiotics at District Hospital ICU.",
                Arrays.asList("septic shock", "sepsis", "severe infection shock", "fever with low bp", "mottled skin fever", "septicemia"),
                "Critical systemic inflammatory response with altered sensorium, tachycardia, tachypnea, and hemodynamic collapse.");
        addSymptom("sym_fever_post_viral_myalgia", "Viral Fever with Generalized Body Ache & Muscle Soreness", "Systemic", 4, "68962001", "MG26", false, null,
                Arrays.asList("body ache fever", "viral fever", "muscle soreness fever", "badan dard bukhar", "halki thakan bukhar"),
                "Diffuse aching in arms, legs, and back following acute upper respiratory or gastrointestinal viral infection.");
        addSymptom("sym_extreme_fatigue_lethargy", "Extreme Fatigue / Generalized Weakness", "Systemic", 4, "84229001", "MG22", false, null,
                Arrays.asList("fatigue", "tiredness", "weakness", "kamzori", "thakan", "lethargy"), "Overwhelming exhaustion and lack of physical stamina");
        addSymptom("sym_unexplained_weight_loss", "Unintended Significant Weight Loss", "Systemic", 5, "89362002", "MG25", false, null,
                Arrays.asList("weight loss", "losing weight", "wajan kam hona"), "Noticeable drop in body weight without dietary restriction");
        addSymptom("sym_neck_stiffness_photophobia", "Stiff Neck with High Fever & Light Sensitivity (Meningism)", "Systemic", 10, "414371002", "1D01", true,
                "Classic triad of meningitis (fever, neck stiffness, photophobia). Requires urgent IV antibiotics and lumbar puncture at District Hospital.",
                Arrays.asList("neck stiffness", "stiff neck", "photophobia", "meningitis signs", "gardan akdna"), "Rigid neck resistance to flexion with fever and eye discomfort in bright light");
    }

    private void initDiseaseProfiles() {
        // 1. Acute Coronary Syndrome / Myocardial Infarction
        DiseaseProfile mi = new DiseaseProfile("dis_mi", "Acute Myocardial Infarction (Heart Attack)", "Cardiovascular", "BA41", "EMERGENCY", "DISTRICT_HOSPITAL", 0.05,
                "Acute coronary occlusion causing myocardial ischemia. Requires immediate ECG, thrombolysis/PCI, oxygen, and aspirin.");
        mi.addSymptomWeight("sym_chest_pain_pressure", 0.95);
        mi.addSymptomWeight("sym_shortness_of_breath_severe", 0.70);
        mi.addSymptomWeight("sym_palpitations_rapid_pulse", 0.60);
        mi.addSymptomWeight("sym_nausea_vomiting", 0.40);
        mi.addSymptomWeight("sym_extreme_fatigue_lethargy", 0.50);
        mi.addRedFlag("sym_chest_pain_pressure");
        mi.addHomeCareTip("Keep the patient sitting upright and calm. Do NOT exert physical effort.");
        mi.addHomeCareTip("If prescribed and not allergic, chew 300mg Aspirin while awaiting ambulance.");
        mi.addPrecaution("Do not attempt to drive yourself to the hospital. Call 108 Emergency Ambulance immediately.");
        diseaseProfiles.put(mi.getId(), mi);

        // 2. Acute Stroke / CVA
        DiseaseProfile stroke = new DiseaseProfile("dis_stroke", "Acute Ischemic / Hemorrhagic Stroke", "Neurological", "8B20", "EMERGENCY", "DISTRICT_HOSPITAL", 0.04,
                "Sudden vascular disruption to brain tissue. Time-critical window (<4.5 hrs) for thrombolytic therapy.");
        stroke.addSymptomWeight("sym_facial_droop_weakness", 0.95);
        stroke.addSymptomWeight("sym_confusion_altered_mental", 0.85);
        stroke.addSymptomWeight("sym_headache_severe_thunderclap", 0.60);
        stroke.addSymptomWeight("sym_dizziness_vertigo", 0.50);
        stroke.addRedFlag("sym_facial_droop_weakness");
        stroke.addHomeCareTip("Note the EXACT time symptoms began. Position patient on their side to prevent choking.");
        stroke.addPrecaution("Do NOT give food, water, or blood-thinners before hospital brain CT scan.");
        diseaseProfiles.put(stroke.getId(), stroke);

        // 3. Bacterial Meningitis / Sepsis
        DiseaseProfile meningitis = new DiseaseProfile("dis_meningitis", "Acute Meningitis / CNS Infection", "Infectious", "1D01", "EMERGENCY", "DISTRICT_HOSPITAL", 0.02,
                "Severe infection of protective membranes covering brain and spinal cord. Needs emergency IV antibiotics.");
        meningitis.addSymptomWeight("sym_neck_stiffness_photophobia", 0.95);
        meningitis.addSymptomWeight("sym_fever_high_grade", 0.90);
        meningitis.addSymptomWeight("sym_headache_severe_thunderclap", 0.80);
        meningitis.addSymptomWeight("sym_confusion_altered_mental", 0.75);
        meningitis.addRedFlag("sym_neck_stiffness_photophobia");
        meningitis.addHomeCareTip("Keep patient in a dark, quiet room while preparing urgent transport to District Hospital.");
        meningitis.addPrecaution("Do not delay referral. Every hour counts in bacterial meningitis.");
        diseaseProfiles.put(meningitis.getId(), meningitis);

        // 4. Acute Appendicitis / Peritonitis
        DiseaseProfile appendicitis = new DiseaseProfile("dis_appendicitis", "Acute Appendicitis / Acute Abdomen", "Gastrointestinal", "DB10", "HIGH", "CHC", 0.06,
                "Acute inflammation of vermiform appendix. Risk of perforation and peritonitis without surgical evaluation.");
        appendicitis.addSymptomWeight("sym_abdominal_pain_severe_acute", 0.90);
        appendicitis.addSymptomWeight("sym_nausea_vomiting", 0.70);
        appendicitis.addSymptomWeight("sym_fever_high_grade", 0.50);
        appendicitis.addRedFlag("sym_abdominal_pain_severe_acute");
        appendicitis.addHomeCareTip("Maintain Nil Per Oral (do not eat or drink) to prepare for possible urgent surgical assessment.");
        appendicitis.addPrecaution("Do NOT take strong laxatives or apply direct hot water bottles to abdomen.");
        diseaseProfiles.put(appendicitis.getId(), appendicitis);

        // 5. Severe Pneumonia / Acute Lower Respiratory Infection
        DiseaseProfile pneumonia = new DiseaseProfile("dis_pneumonia", "Community Acquired Pneumonia", "Respiratory", "CA40", "HIGH", "CHC", 0.08,
                "Infection of lung parenchyma causing alveolar exudate, hypoxia, and respiratory distress.");
        pneumonia.addSymptomWeight("sym_cough_wet_phlegm", 0.88);
        pneumonia.addSymptomWeight("sym_fever_high_grade", 0.85);
        pneumonia.addSymptomWeight("sym_shortness_of_breath_severe", 0.75);
        pneumonia.addSymptomWeight("sym_wheezing_stridor", 0.45);
        pneumonia.addHomeCareTip("Ensure patient rests in a propped-up 45 degree posture to aid breathing.");
        pneumonia.addHomeCareTip("Monitor SpO2 saturation with pulse oximeter if available (>94% normal).");
        pneumonia.addPrecaution("Seek urgent oxygen support if lips turn bluish or breathing rate exceeds 25 breaths/min.");
        diseaseProfiles.put(pneumonia.getId(), pneumonia);

        // 6. Acute Viral Upper Respiratory Infection / Common Cold & Flu
        DiseaseProfile viralUri = new DiseaseProfile("dis_viral_uri", "Acute Viral Upper Respiratory Tract Infection (Flu/URI)", "Respiratory", "CA08", "LOW", "PHC", 0.35,
                "Common self-limiting viral infection of upper respiratory mucosa. Manageable with symptomatic primary care.");
        viralUri.addSymptomWeight("sym_nasal_congestion_runny", 0.92);
        viralUri.addSymptomWeight("sym_sore_throat", 0.85);
        viralUri.addSymptomWeight("sym_cough_dry", 0.70);
        viralUri.addSymptomWeight("sym_fever_mild_low_grade", 0.65);
        viralUri.addSymptomWeight("sym_post_viral_myalgia", 0.60);
        viralUri.addSymptomWeight("sym_headache_mild", 0.50);
        viralUri.addHomeCareTip("Steam inhalation twice daily with warm saline gargles for throat relief.");
        viralUri.addHomeCareTip("Drink plenty of warm fluids (herbal teas, warm water, soups) and get adequate rest.");
        viralUri.addHomeCareTip("Paracetamol (500mg/650mg) as per doctor/PHC pharmacist advice for fever/bodyache.");
        viralUri.addPrecaution("Do NOT start unprescribed antibiotics; viral illnesses do not respond to antibiotics.");
        diseaseProfiles.put(viralUri.getId(), viralUri);

        // 7. Acute Gastroenteritis / Food Poisoning
        DiseaseProfile gastroenteritis = new DiseaseProfile("dis_gastroenteritis", "Acute Gastroenteritis / Dehydration Risk", "Gastrointestinal", "1A00", "MODERATE", "PHC", 0.15,
                "Inflammation of stomach and intestines causing diarrhea, cramps, and fluid loss. ORS rehydration is critical.");
        gastroenteritis.addSymptomWeight("sym_diarrhea_watery", 0.95);
        gastroenteritis.addSymptomWeight("sym_nausea_vomiting", 0.85);
        gastroenteritis.addSymptomWeight("sym_abdominal_cramping_mild", 0.75);
        gastroenteritis.addSymptomWeight("sym_fever_mild_low_grade", 0.40);
        gastroenteritis.addHomeCareTip("Drink Oral Rehydration Salts (ORS) solution continuously after every loose stool (1 liter clean water + 1 ORS packet).");
        gastroenteritis.addHomeCareTip("Eat bland, easily digestible foods like rice gruel (kanji), curd, bananas, and coconut water.");
        gastroenteritis.addPrecaution("Visit PHC immediately if unable to keep fluids down for over 8 hours or if urine turns very dark/scanty.");
        diseaseProfiles.put(gastroenteritis.getId(), gastroenteritis);

        // 8. Acute Malaria (Plasmodium Vivax / Falciparum)
        DiseaseProfile malaria = new DiseaseProfile("dis_malaria", "Acute Malaria (Plasmodium Parasitemia)", "Infectious", "1F40", "HIGH", "CHC", 0.09,
                "Protozoan parasitic infection transmitted by Anopheles mosquito causing cyclic high spiking fever, severe rigors, and hemolytic anemia risk.");
        malaria.addSymptomWeight("sym_fever_chills_rigors_malaria", 0.98);
        malaria.addSymptomWeight("sym_fever_high_grade", 0.90);
        malaria.addSymptomWeight("sym_extreme_fatigue_lethargy", 0.85);
        malaria.addSymptomWeight("sym_headache_mild", 0.75);
        malaria.addSymptomWeight("sym_nausea_vomiting", 0.60);
        malaria.addHomeCareTip("Undergo immediate Rapid Diagnostic Test (RDT) or peripheral blood smear for Malaria parasite at PHC/CHC.");
        malaria.addHomeCareTip("Complete full prescribed course of Artemisinin-based Combination Therapy (ACT) / Chloroquine under medical supervision.");
        malaria.addPrecaution("Beware of dark tea-colored urine or acute jaundice, which indicates severe Falciparum malaria complication (Blackwater fever).");
        diseaseProfiles.put(malaria.getId(), malaria);

        // 9. Acute Dengue Hemorrhagic Fever
        DiseaseProfile dengue = new DiseaseProfile("dis_dengue", "Acute Dengue Fever / Thrombocytopenia", "Infectious", "1D20", "HIGH", "DISTRICT_HOSPITAL", 0.12,
                "Flavivirus infection causing sudden high fever, retro-orbital pain, severe arthralgia (breakbone), petechiae, and plasma leakage risk.");
        dengue.addSymptomWeight("sym_fever_breakbone_retro_orbital_dengue", 0.98);
        dengue.addSymptomWeight("sym_fever_high_grade", 0.92);
        dengue.addSymptomWeight("sym_fever_petechial_rash_purpura", 0.70);
        dengue.addSymptomWeight("sym_joint_pain_swelling", 0.80);
        dengue.addSymptomWeight("sym_extreme_fatigue_lethargy", 0.85);
        dengue.addSymptomWeight("sym_nausea_vomiting", 0.65);
        dengue.addHomeCareTip("Monitor daily Complete Blood Count (CBC) with serial platelet counts and hematocrit.");
        dengue.addHomeCareTip("Maintain rigorous oral rehydration with coconut water, ORS, fresh fruit juices (>3 liters/day).");
        dengue.addPrecaution("STRICT CONTRAINDICATION: Do NOT take Aspirin, Ibuprofen, or NSAIDs as they exacerbate bleeding diathesis.");
        diseaseProfiles.put(dengue.getId(), dengue);

        // 10. Enteric / Typhoid Fever
        DiseaseProfile typhoid = new DiseaseProfile("dis_typhoid", "Enteric / Typhoid Fever (Salmonella enterica)", "Infectious", "1A07", "MODERATE", "PHC", 0.08,
                "Systemic bacterial infection causing sustained step-ladder fever, headache, coated tongue, abdominal pain, and hepatosplenomegaly.");
        typhoid.addSymptomWeight("sym_fever_stepladder_typhoid", 0.96);
        typhoid.addSymptomWeight("sym_fever_high_grade", 0.88);
        typhoid.addSymptomWeight("sym_headache_mild", 0.75);
        typhoid.addSymptomWeight("sym_abdominal_cramping_mild", 0.65);
        typhoid.addSymptomWeight("sym_extreme_fatigue_lethargy", 0.75);
        typhoid.addHomeCareTip("Perform Widal test / Blood culture at PHC lab. Ensure strictly boiled drinking water.");
        typhoid.addHomeCareTip("Follow high-calorie, soft, non-spicy diet (khichdi, mashed potatoes, boiled vegetables).");
        typhoid.addPrecaution("Adhere strictly to antibiotic regimen to prevent carrier state or intestinal perforation.");
        diseaseProfiles.put(typhoid.getId(), typhoid);

        // 11. Pulmonary Tuberculosis (TB)
        DiseaseProfile tb = new DiseaseProfile("dis_tb", "Pulmonary Tuberculosis (Koch's Disease)", "Respiratory", "1B10", "HIGH", "CHC", 0.07,
                "Mycobacterium tuberculosis infection causing chronic productive cough, evening subfebrile fever, night sweats, and hemoptysis.");
        tb.addSymptomWeight("sym_fever_evening_rise_night_sweats_tb", 0.95);
        tb.addSymptomWeight("sym_cough_wet_phlegm", 0.85);
        tb.addSymptomWeight("sym_cough_blood_hemoptysis", 0.75);
        tb.addSymptomWeight("sym_unexplained_weight_loss", 0.88);
        tb.addSymptomWeight("sym_extreme_fatigue_lethargy", 0.80);
        tb.addHomeCareTip("Free Sputum NAAT (CBNAAT/Truenat) and Chest X-Ray provided under National TB Elimination Program (NTEP) at PHC/CHC.");
        tb.addHomeCareTip("Free DOTS therapy and Nikshay Poshan monthly direct nutritional support provided by Government.");
        tb.addPrecaution("Wear protective mask around family members and never discontinue anti-TB medications without doctor approval.");
        diseaseProfiles.put(tb.getId(), tb);

        // 12. Pediatric Febrile Convulsions
        DiseaseProfile febrileConvulsion = new DiseaseProfile("dis_febrile_convulsion", "Pediatric Febrile Convulsion / Seizure", "Neurological", "8A60", "EMERGENCY", "CHC", 0.03,
                "Temperature-sensitive paroxysmal seizure in children (6 months to 5 years). Requires urgent airway positioning and antipyretic care.");
        febrileConvulsion.addSymptomWeight("sym_fever_pediatric_febrile_seizure", 0.99);
        febrileConvulsion.addSymptomWeight("sym_fever_high_grade", 0.95);
        febrileConvulsion.addSymptomWeight("sym_seizures_convulsions", 0.90);
        febrileConvulsion.addRedFlag("sym_fever_pediatric_febrile_seizure");
        febrileConvulsion.addHomeCareTip("Place child gently in recovery position on their side on a soft surface. Loosen tight clothing.");
        febrileConvulsion.addHomeCareTip("Apply room-temperature tepid water sponge to forehead and neck (never use ice cold water).");
        febrileConvulsion.addPrecaution("Never place spoons or objects into child's mouth during seizure. Transfer urgently to nearest CHC/Hospital.");
        diseaseProfiles.put(febrileConvulsion.getId(), febrileConvulsion);

        // 13. Septic Shock & Severe Systemic Infection
        DiseaseProfile septicShock = new DiseaseProfile("dis_septic_shock", "Severe Sepsis & Septic Shock Syndrome", "Systemic", "1G40", "EMERGENCY", "DISTRICT_HOSPITAL", 0.02,
                "Dysregulated host response to infection causing refractory hypotension, microvascular thrombosis, and multi-organ failure.");
        septicShock.addSymptomWeight("sym_fever_hypothermia_sepsis_shock", 0.98);
        septicShock.addSymptomWeight("sym_fever_petechial_rash_purpura", 0.80);
        septicShock.addSymptomWeight("sym_confusion_altered_mental", 0.85);
        septicShock.addSymptomWeight("sym_shortness_of_breath_severe", 0.80);
        septicShock.addSymptomWeight("sym_extreme_fatigue_lethargy", 0.90);
        septicShock.addRedFlag("sym_fever_hypothermia_sepsis_shock");
        septicShock.addHomeCareTip("Immediate 108 emergency ICU transfer. Keep patient warm and elevate legs slightly if conscious.");
        septicShock.addPrecaution("Time-critical 'Golden Hour' intervention required: IV fluids, blood cultures, and targeted broad-spectrum IV antibiotics.");
        diseaseProfiles.put(septicShock.getId(), septicShock);

        // 14. Acute Urinary Tract Infection (UTI) / Cystitis
        DiseaseProfile uti = new DiseaseProfile("dis_uti", "Acute Urinary Tract Infection (Cystitis)", "Urogenital", "GC08", "MODERATE", "PHC", 0.12,
                "Bacterial infection of bladder/urethra causing dysuria, frequency, and pelvic discomfort.");
        uti.addSymptomWeight("sym_burning_urination_dysuria", 0.95);
        uti.addSymptomWeight("sym_frequent_urgent_urination", 0.90);
        uti.addSymptomWeight("sym_abdominal_cramping_mild", 0.40);
        uti.addSymptomWeight("sym_fever_mild_low_grade", 0.35);
        uti.addHomeCareTip("Increase water intake to at least 3 liters daily to flush urinary tract.");
        uti.addHomeCareTip("Visit local PHC for urine routine & microscopy test and targeted antibacterial prescription.");
        uti.addPrecaution("Seek urgent medical review if flank back pain or high chills develop (possible kidney infection).");
        diseaseProfiles.put(uti.getId(), uti);

        // 15. Obstetric Emergency / Ectopic / Placental Bleeding
        DiseaseProfile obGynEmergency = new DiseaseProfile("dis_ob_gyn_emergency", "High-Risk Obstetric / Pregnancy Complication", "Obstetric", "JA65", "EMERGENCY", "CHC", 0.03,
                "Acute complications in pregnancy requiring immediate First Referral Unit (FRU/CHC) with Obstetrician and blood bank.");
        obGynEmergency.addSymptomWeight("sym_pelvic_pain_severe_pregnancy", 0.98);
        obGynEmergency.addSymptomWeight("sym_extreme_fatigue_lethargy", 0.60);
        obGynEmergency.addSymptomWeight("sym_dizziness_vertigo", 0.55);
        obGynEmergency.addRedFlag("sym_pelvic_pain_severe_pregnancy");
        obGynEmergency.addHomeCareTip("Keep the pregnant patient in left lateral position (lying on left side) during transport.");
        obGynEmergency.addPrecaution("Immediate transfer to CHC/District Hospital via 102/108 Janani Shishu Suraksha ambulance.");
        diseaseProfiles.put(obGynEmergency.getId(), obGynEmergency);
    }

    public Symptom getSymptom(String id) { return symptomsById.get(id); }
    public List<Symptom> getAllSymptoms() { return Collections.unmodifiableList(allSymptoms); }
    public Map<String, DiseaseProfile> getAllDiseaseProfiles() { return Collections.unmodifiableMap(diseaseProfiles); }
    public DiseaseProfile getDiseaseProfile(String id) { return diseaseProfiles.get(id); }
}
