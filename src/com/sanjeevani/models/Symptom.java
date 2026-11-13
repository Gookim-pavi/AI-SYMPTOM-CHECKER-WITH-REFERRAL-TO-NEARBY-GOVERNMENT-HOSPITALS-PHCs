package com.sanjeevani.models;

import java.util.List;
import java.util.ArrayList;

/**
 * Medical Symptom representation with anatomical classification, severity scale,
 * SNOMED/ICD code mapping, red-flag triggers, and synonym aliases for NLP matching.
 */
public class Symptom {
    private String id;
    private String name;
    private String anatomicalSystem; // Head, ENT, Chest, Respiratory, Abdomen, Pelvis, Musculoskeletal, Skin, Neurological, Systemic
    private int defaultSeverity;     // Scale 1 to 10
    private String snomedCode;
    private String icd11Code;
    private boolean isRedFlag;        // Acute red-flag indicator requiring immediate attention
    private String redFlagMessage;
    private List<String> synonyms;
    private String description;

    public Symptom(String id, String name, String anatomicalSystem, int defaultSeverity, 
                   String snomedCode, String icd11Code, boolean isRedFlag, 
                   String redFlagMessage, List<String> synonyms, String description) {
        this.id = id;
        this.name = name;
        this.anatomicalSystem = anatomicalSystem;
        this.defaultSeverity = defaultSeverity;
        this.snomedCode = snomedCode;
        this.icd11Code = icd11Code;
        this.isRedFlag = isRedFlag;
        this.redFlagMessage = redFlagMessage;
        this.synonyms = synonyms != null ? synonyms : new ArrayList<>();
        this.description = description;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAnatomicalSystem() { return anatomicalSystem; }
    public int getDefaultSeverity() { return defaultSeverity; }
    public String getSnomedCode() { return snomedCode; }
    public String getIcd11Code() { return icd11Code; }
    public boolean isRedFlag() { return isRedFlag; }
    public String getRedFlagMessage() { return redFlagMessage; }
    public List<String> getSynonyms() { return synonyms; }
    public String getDescription() { return description; }

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"id\":\"").append(escapeJson(id)).append("\",");
        sb.append("\"name\":\"").append(escapeJson(name)).append("\",");
        sb.append("\"system\":\"").append(escapeJson(anatomicalSystem)).append("\",");
        sb.append("\"severity\":").append(defaultSeverity).append(",");
        sb.append("\"snomedCode\":\"").append(escapeJson(snomedCode)).append("\",");
        sb.append("\"icd11Code\":\"").append(escapeJson(icd11Code)).append("\",");
        sb.append("\"isRedFlag\":").append(isRedFlag).append(",");
        sb.append("\"redFlagMessage\":\"").append(escapeJson(redFlagMessage != null ? redFlagMessage : "")).append("\",");
        sb.append("\"description\":\"").append(escapeJson(description != null ? description : "")).append("\",");
        sb.append("\"synonyms\":[");
        for (int i = 0; i < synonyms.size(); i++) {
            sb.append("\"").append(escapeJson(synonyms.get(i))).append("\"");
            if (i < synonyms.size() - 1) sb.append(",");
        }
        sb.append("]}");
        return sb.toString();
    }

    private static String escapeJson(String raw) {
        if (raw == null) return "";
        return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }
}
