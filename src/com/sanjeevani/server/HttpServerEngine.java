package com.sanjeevani.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import com.sanjeevani.models.*;
import com.sanjeevani.engine.*;
import com.sanjeevani.data.HospitalDatabase;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * High-performance embedded HTTP server powering the REST API
 * and serving the Sanjeevani AI frontend web application.
 */
public class HttpServerEngine {
    private HttpServer server;
    private int port;
    private Path webRootDir;
    private long serverStartTime;

    private SymptomKnowledgeBase kb;
    private BayesianTriageEngine triageEngine;
    private NLPTextExtractor nlpExtractor;
    private FacilityLocatorService facilityLocator;
    private ReferralSlipGenerator slipGenerator;

    public HttpServerEngine(int port, String webRootPath) {
        this.port = port;
        Path candidate = Paths.get(webRootPath).toAbsolutePath();
        if (!Files.exists(candidate)) {
            Path[] candidates = new Path[] {
                Paths.get("web").toAbsolutePath(),
                Paths.get("..", "web").toAbsolutePath(),
                Paths.get(System.getProperty("user.dir", "."), "web").toAbsolutePath()
            };
            for (Path c : candidates) {
                if (Files.exists(c)) {
                    candidate = c;
                    break;
                }
            }
        }
        this.webRootDir = candidate;
        this.serverStartTime = System.currentTimeMillis();

        this.kb = SymptomKnowledgeBase.getInstance();
        this.triageEngine = new BayesianTriageEngine();
        this.nlpExtractor = new NLPTextExtractor();
        this.facilityLocator = new FacilityLocatorService();
        this.slipGenerator = ReferralSlipGenerator.getInstance();
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(16));

        // REST API Handlers
        server.createContext("/api/health", this::handleHealth);
        server.createContext("/api/symptoms", this::handleSymptoms);
        server.createContext("/api/nlp-parse", this::handleNlpParse);
        server.createContext("/api/triage", this::handleTriage);
        server.createContext("/api/referrals/find-facilities", this::handleFindFacilities);
        server.createContext("/api/referrals/generate-slip", this::handleGenerateSlip);
        server.createContext("/api/referrals/slips", this::handleGetSlips);
        server.createContext("/api/dashboard/stats", this::handleDashboardStats);

        // Static Web Asset Handler
        server.createContext("/", this::handleStaticFiles);

        server.start();
        System.out.println("[SSS Medical Assist Server] Started successfully on port " + port);
        System.out.println("[SSS Medical Assist Server] Serving web assets from: " + webRootDir);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("[SSS Medical Assist Server] Server stopped.");
        }
    }

    // --- REST API Handlers ---

    private void handleHealth(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
            return;
        }

        long uptimeSec = (System.currentTimeMillis() - serverStartTime) / 1000;
        int totalSyms = kb.getAllSymptoms().size();
        int totalDiseases = kb.getAllDiseaseProfiles().size();
        int totalFacilities = HospitalDatabase.getInstance().getAllFacilities().size();

        String json = "{"
                + "\"status\":\"UP\","
                + "\"service\":\"SSS Medical Assist Symptom Checker & Referral Network\","
                + "\"version\":\"2.0.0\","
                + "\"uptimeSeconds\":" + uptimeSec + ","
                + "\"totalSymptomsLoaded\":" + totalSyms + ","
                + "\"totalDiseasesConfigured\":" + totalDiseases + ","
                + "\"totalGovernmentFacilities\":" + totalFacilities
                + "}";

        sendResponse(exchange, 200, json, "application/json");
    }

    private void handleSymptoms(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
            return;
        }

        List<Symptom> list = kb.getAllSymptoms();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).toJson());
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");

        sendResponse(exchange, 200, sb.toString(), "application/json");
    }

    private void handleNlpParse(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 204, "", "application/json");
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
            return;
        }

        String body = readRequestBody(exchange);
        String text = extractJsonString(body, "text");
        if (text == null) text = "";

        NLPTextExtractor.ExtractedClinicalData data = nlpExtractor.parseNarrative(text);
        sendResponse(exchange, 200, data.toJson(), "application/json");
    }

    private void handleTriage(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 204, "", "application/json");
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
            return;
        }

        String body = readRequestBody(exchange);
        
        // Extract Patient Profile
        PatientProfile patient = parsePatientProfile(body);
        
        // Extract Symptom IDs
        List<String> symptomIds = extractJsonStringArray(body, "symptomIds");
        
        // Extract Severities
        Map<String, Integer> severities = extractJsonIntMap(body, "symptomSeverities");

        TriageResult result = triageEngine.evaluate(symptomIds, patient, severities);
        sendResponse(exchange, 200, result.toJson(), "application/json");
    }

    private void handleFindFacilities(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 204, "", "application/json");
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
            return;
        }

        String body = readRequestBody(exchange);
        double lat = extractJsonDouble(body, "latitude", 12.9716);
        double lng = extractJsonDouble(body, "longitude", 77.5946);
        String typeFilter = extractJsonString(body, "facilityType");
        boolean emergencyOnly = extractJsonBoolean(body, "emergencyOnly", false);
        String triageTier = extractJsonString(body, "triageTier");
        int maxResults = extractJsonInt(body, "maxResults", 10);

        List<HealthFacility> facilities = facilityLocator.findNearbyFacilities(lat, lng, typeFilter, emergencyOnly, triageTier, maxResults);
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < facilities.size(); i++) {
            sb.append(facilities.get(i).toJson());
            if (i < facilities.size() - 1) sb.append(",");
        }
        sb.append("]");

        sendResponse(exchange, 200, sb.toString(), "application/json");
    }

    private void handleGenerateSlip(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 204, "", "application/json");
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
            return;
        }

        String body = readRequestBody(exchange);
        PatientProfile patient = parsePatientProfile(body);
        
        List<String> symptomIds = extractJsonStringArray(body, "symptomIds");
        Map<String, Integer> severities = extractJsonIntMap(body, "symptomSeverities");
        TriageResult triage = triageEngine.evaluate(symptomIds, patient, severities);

        List<String> symptomNames = new ArrayList<>();
        for (String id : symptomIds) {
            Symptom s = kb.getSymptom(id);
            if (s != null) symptomNames.add(s.getName());
            else symptomNames.add(id);
        }

        String facilityId = extractJsonString(body, "facilityId");
        String assignedDept = extractJsonString(body, "assignedDepartment");

        ReferralSlip slip = slipGenerator.generateSlip(patient, triage, symptomNames, facilityId, assignedDept);
        sendResponse(exchange, 200, slip.toJson(), "application/json");
    }

    private void handleGetSlips(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
            return;
        }

        List<ReferralSlip> list = slipGenerator.getAllIssuedSlips();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i).toJson());
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");

        sendResponse(exchange, 200, sb.toString(), "application/json");
    }

    private void handleDashboardStats(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}", "application/json");
            return;
        }

        List<ReferralSlip> slips = slipGenerator.getAllIssuedSlips();
        int totalIssued = slips.size();
        int emergencyCount = 0;
        int urgentCount = 0;
        int routineCount = 0;

        for (ReferralSlip s : slips) {
            if ("EMERGENCY".equalsIgnoreCase(s.getTriageRiskLevel())) emergencyCount++;
            else if ("HIGH".equalsIgnoreCase(s.getTriageRiskLevel())) urgentCount++;
            else routineCount++;
        }

        List<HealthFacility> facilities = HospitalDatabase.getInstance().getAllFacilities();
        int totalBeds = 0;
        int availableBeds = 0;
        for (HealthFacility f : facilities) {
            totalBeds += f.getTotalBeds();
            availableBeds += f.getAvailableBeds();
        }

        String json = "{"
                + "\"totalReferrals\":" + totalIssued + ","
                + "\"emergencyTriageCount\":" + emergencyCount + ","
                + "\"highUrgencyCount\":" + urgentCount + ","
                + "\"routineCount\":" + routineCount + ","
                + "\"activeFacilitiesCount\":" + facilities.size() + ","
                + "\"totalNetworkBeds\":" + totalBeds + ","
                + "\"availableNetworkBeds\":" + availableBeds
                + "}";

        sendResponse(exchange, 200, json, "application/json");
    }

    // --- Static File Serving ---

    private void handleStaticFiles(HttpExchange exchange) throws IOException {
        String pathStr = exchange.getRequestURI().getPath();
        if (pathStr.equals("/") || pathStr.isEmpty()) {
            pathStr = "/index.html";
        }

        // Clean path to prevent path traversal
        pathStr = pathStr.replace("../", "").replace("..\\", "");
        if (pathStr.startsWith("/")) pathStr = pathStr.substring(1);

        Path filePath = webRootDir.resolve(pathStr).normalize();
        if (!filePath.startsWith(webRootDir) || !Files.exists(filePath) || Files.isDirectory(filePath)) {
            // Check fallback to index.html
            Path indexFile = webRootDir.resolve("index.html");
            if (Files.exists(indexFile)) {
                filePath = indexFile;
            } else {
                sendResponse(exchange, 404, "404 Not Found", "text/plain");
                return;
            }
        }

        String mime = getMimeType(filePath.getFileName().toString());
        byte[] bytes = Files.readAllBytes(filePath);

        exchange.getResponseHeaders().set("Content-Type", mime);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.sendResponseHeaders(200, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    // --- Helper Methods ---

    private void sendResponse(HttpExchange exchange, int statusCode, String responseText, String contentType) throws IOException {
        byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[2048];
            int n;
            while ((n = is.read(buf)) != -1) {
                baos.write(buf, 0, n);
            }
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private String getMimeType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html";
        if (lower.endsWith(".css")) return "text/css";
        if (lower.endsWith(".js")) return "application/javascript";
        if (lower.endsWith(".json")) return "application/json";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".ico")) return "image/x-icon";
        if (lower.endsWith(".woff2")) return "font/woff2";
        if (lower.endsWith(".woff")) return "font/woff";
        if (lower.endsWith(".ttf")) return "font/ttf";
        return "application/octet-stream";
    }

    // Lightweight robust JSON parsers without external JAR dependency
    private PatientProfile parsePatientProfile(String json) {
        PatientProfile p = new PatientProfile();
        if (json == null) return p;

        String name = extractJsonString(json, "patientName");
        if (name == null) name = extractJsonString(json, "name");
        if (name != null) p.setName(name);

        p.setAge(extractJsonInt(json, "age", 30));
        p.setGender(extractJsonString(json, "gender") != null ? extractJsonString(json, "gender") : "Male");
        p.setPregnant(extractJsonBoolean(json, "isPregnant", false));
        p.setPregnancyTrimester(extractJsonInt(json, "pregnancyTrimester", 0));
        
        List<String> comorbs = extractJsonStringArray(json, "comorbidities");
        if (!comorbs.isEmpty()) p.setComorbidities(comorbs);

        p.setLatitude(extractJsonDouble(json, "latitude", 12.9716));
        p.setLongitude(extractJsonDouble(json, "longitude", 77.5946));
        p.setState(extractJsonString(json, "state") != null ? extractJsonString(json, "state") : "Karnataka");
        p.setDistrict(extractJsonString(json, "district") != null ? extractJsonString(json, "district") : "Bengaluru Urban");
        p.setPincode(extractJsonString(json, "pincode") != null ? extractJsonString(json, "pincode") : "560001");

        // Vitals
        Double temp = extractJsonDoubleNullable(json, "bodyTempF");
        if (temp != null) p.setBodyTempF(temp);

        Integer sbp = extractJsonIntNullable(json, "systolicBp");
        if (sbp != null) p.setSystolicBp(sbp);

        Integer hr = extractJsonIntNullable(json, "heartRateBpm");
        if (hr != null) p.setHeartRateBpm(hr);

        Integer spo2 = extractJsonIntNullable(json, "spo2Percent");
        if (spo2 != null) p.setSpo2Percent(spo2);

        Integer rr = extractJsonIntNullable(json, "respiratoryRate");
        if (rr != null) p.setRespiratoryRate(rr);

        return p;
    }

    private String extractJsonString(String json, String key) {
        if (json == null) return null;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"" + java.util.regex.Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) return m.group(1);
        return null;
    }

    private int extractJsonInt(String json, String key, int defaultVal) {
        Integer val = extractJsonIntNullable(json, key);
        return val != null ? val : defaultVal;
    }

    private Integer extractJsonIntNullable(String json, String key) {
        if (json == null) return null;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"" + java.util.regex.Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+)");
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            try { return Integer.parseInt(m.group(1)); } catch (Exception ignored) {}
        }
        return null;
    }

    private double extractJsonDouble(String json, String key, double defaultVal) {
        Double val = extractJsonDoubleNullable(json, key);
        return val != null ? val : defaultVal;
    }

    private Double extractJsonDoubleNullable(String json, String key) {
        if (json == null) return null;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"" + java.util.regex.Pattern.quote(key) + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)");
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            try { return Double.parseDouble(m.group(1)); } catch (Exception ignored) {}
        }
        return null;
    }

    private boolean extractJsonBoolean(String json, String key, boolean defaultVal) {
        if (json == null) return defaultVal;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"" + java.util.regex.Pattern.quote(key) + "\"\\s*:\\s*(true|false)");
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return "true".equalsIgnoreCase(m.group(1));
        }
        return defaultVal;
    }

    private List<String> extractJsonStringArray(String json, String key) {
        List<String> res = new ArrayList<>();
        if (json == null) return res;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"" + java.util.regex.Pattern.quote(key) + "\"\\s*:\\s*\\[([^\\]]*)\\]");
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            String arrContent = m.group(1);
            java.util.regex.Pattern itemPat = java.util.regex.Pattern.compile("\"([^\"]*)\"");
            java.util.regex.Matcher itemMat = itemPat.matcher(arrContent);
            while (itemMat.find()) {
                res.add(itemMat.group(1));
            }
        }
        return res;
    }

    private Map<String, Integer> extractJsonIntMap(String json, String key) {
        Map<String, Integer> map = new HashMap<>();
        if (json == null) return map;
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"" + java.util.regex.Pattern.quote(key) + "\"\\s*:\\s*\\{([^\\}]*)\\}");
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            String mapContent = m.group(1);
            java.util.regex.Pattern entryPat = java.util.regex.Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\\d+)");
            java.util.regex.Matcher entryMat = entryPat.matcher(mapContent);
            while (entryMat.find()) {
                map.put(entryMat.group(1), Integer.parseInt(entryMat.group(2)));
            }
        }
        return map;
    }
}
