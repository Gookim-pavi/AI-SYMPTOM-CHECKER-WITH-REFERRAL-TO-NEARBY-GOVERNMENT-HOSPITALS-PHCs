package com.sanjeevani.engine;

import com.sanjeevani.models.Symptom;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Natural Language Processing (NLP) extractor for patient symptom narratives.
 * Supports tokenization, multi-lingual synonym mapping (Hindi/English),
 * negation detection, severity modifier extraction, and duration parsing.
 */
public class NLPTextExtractor {
    private SymptomKnowledgeBase kb;

    private static final Set<String> NEGATION_TRIGGERS = new HashSet<>(Arrays.asList(
            "no", "not", "without", "denies", "deny", "none", "neither", "nor", "nahi", "nahi hai", "bina"
    ));

    private static final Set<String> SEVERE_MODIFIERS = new HashSet<>(Arrays.asList(
            "severe", "extreme", "unbearable", "worst", "crushing", "intense", "very high", "acute", "heavy", "teez", "bahut"
    ));

    private static final Set<String> MILD_MODIFIERS = new HashSet<>(Arrays.asList(
            "mild", "slight", "little", "minor", "subtle", "halka", "thoda"
    ));

    public static class ExtractedClinicalData {
        public List<String> extractedSymptomIds = new ArrayList<>();
        public Map<String, Integer> symptomSeverities = new HashMap<>();
        public String parsedDuration = "Not specified";
        public List<String> negatedSymptoms = new ArrayList<>();
        public int overallUrgencyEstimate = 20;

        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            sb.append("\"parsedDuration\":\"").append(escapeJson(parsedDuration)).append("\",");
            sb.append("\"overallUrgencyEstimate\":").append(overallUrgencyEstimate).append(",");
            
            sb.append("\"extractedSymptomIds\":[");
            for (int i = 0; i < extractedSymptomIds.size(); i++) {
                sb.append("\"").append(escapeJson(extractedSymptomIds.get(i))).append("\"");
                if (i < extractedSymptomIds.size() - 1) sb.append(",");
            }
            sb.append("],");

            sb.append("\"negatedSymptoms\":[");
            for (int i = 0; i < negatedSymptoms.size(); i++) {
                sb.append("\"").append(escapeJson(negatedSymptoms.get(i))).append("\"");
                if (i < negatedSymptoms.size() - 1) sb.append(",");
            }
            sb.append("],");

            sb.append("\"symptomSeverities\":{");
            int c = 0;
            for (Map.Entry<String, Integer> entry : symptomSeverities.entrySet()) {
                sb.append("\"").append(escapeJson(entry.getKey())).append("\":").append(entry.getValue());
                if (++c < symptomSeverities.size()) sb.append(",");
            }
            sb.append("}");

            sb.append("}");
            return sb.toString();
        }

        private static String escapeJson(String raw) {
            if (raw == null) return "";
            return raw.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
        }
    }

    public NLPTextExtractor() {
        this.kb = SymptomKnowledgeBase.getInstance();
    }

    public ExtractedClinicalData parseNarrative(String userText) {
        ExtractedClinicalData result = new ExtractedClinicalData();
        if (userText == null || userText.trim().isEmpty()) {
            return result;
        }

        String lower = userText.toLowerCase();

        // 1. Duration extraction
        Pattern durationPattern = Pattern.compile("(\\d+\\s*(?:days?|hours?|weeks?|months?|dino?|ghante?))|yesterday|today|since morning|kal se|aaj se");
        Matcher durationMatcher = durationPattern.matcher(lower);
        if (durationMatcher.find()) {
            result.parsedDuration = durationMatcher.group(0);
        }

        // 2. Global Severity check
        boolean globalSevere = false;
        for (String sm : SEVERE_MODIFIERS) {
            if (lower.contains(sm)) {
                globalSevere = true;
                break;
            }
        }

        // Split text into clauses/sentences to respect negation boundaries
        String[] clauses = lower.split("[,;.\\n]+|\\bbut\\b|\\blekin\\b|\\bparantu\\b");

        List<Symptom> allSymptoms = kb.getAllSymptoms();

        for (String clause : clauses) {
            String trimmedClause = clause.trim();
            if (trimmedClause.isEmpty()) continue;

            // Check if clause is negated
            boolean isClauseNegated = false;
            for (String trig : NEGATION_TRIGGERS) {
                if (trimmedClause.matches(".*\\b" + Pattern.quote(trig) + "\\b.*")) {
                    isClauseNegated = true;
                    break;
                }
            }

            for (Symptom sym : allSymptoms) {
                boolean matched = false;

                // Match symptom name
                if (trimmedClause.contains(sym.getName().toLowerCase())) {
                    matched = true;
                }

                // Match synonyms
                if (!matched && sym.getSynonyms() != null) {
                    for (String syn : sym.getSynonyms()) {
                        if (trimmedClause.contains(syn.toLowerCase())) {
                            matched = true;
                            break;
                        }
                    }
                }

                if (matched) {
                    if (isClauseNegated) {
                        if (!result.negatedSymptoms.contains(sym.getName())) {
                            result.negatedSymptoms.add(sym.getName());
                        }
                    } else {
                        if (!result.extractedSymptomIds.contains(sym.getId())) {
                            result.extractedSymptomIds.add(sym.getId());
                            
                            int severity = sym.getDefaultSeverity();
                            if (globalSevere || sym.isRedFlag()) {
                                severity = Math.max(severity, 8);
                            }
                            result.symptomSeverities.put(sym.getId(), severity);
                        }
                    }
                }
            }
        }

        // Urgency estimation
        if (!result.extractedSymptomIds.isEmpty()) {
            int maxS = 1;
            for (String id : result.extractedSymptomIds) {
                Symptom s = kb.getSymptom(id);
                if (s != null) {
                    if (s.isRedFlag()) maxS = 10;
                    else if (s.getDefaultSeverity() > maxS) maxS = s.getDefaultSeverity();
                }
            }
            result.overallUrgencyEstimate = maxS >= 9 ? 90 : (maxS >= 7 ? 70 : (maxS >= 4 ? 45 : 25));
        }

        return result;
    }
}
