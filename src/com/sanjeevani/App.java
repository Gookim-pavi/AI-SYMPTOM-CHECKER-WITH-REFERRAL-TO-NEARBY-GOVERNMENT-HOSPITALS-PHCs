package com.sanjeevani;

import com.sanjeevani.server.HttpServerEngine;
import com.sanjeevani.engine.SymptomKnowledgeBase;
import com.sanjeevani.data.HospitalDatabase;

import java.awt.Desktop;
import java.net.URI;

/**
 * Main application bootstrap for SSS Medical Assist
 * (AI Symptom Checker with Referral to Nearby Government Hospitals/PHCs).
 */
public class App {
    public static final int DEFAULT_PORT = 8080;
    public static final String WEB_ROOT = "web";

    public static void main(String[] args) {
        System.out.println("=========================================================================");
        System.out.println("   SSS MEDICAL ASSIST - SMART HEALTHCARE TRIAGE & GOVT PHC REFERRAL    ");
        System.out.println("        Empowering Rural & Urban Communities with Accessible AI Care     ");
        System.out.println("=========================================================================");

        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("[WARN] Invalid port provided: " + args[0] + ", using default: " + DEFAULT_PORT);
            }
        }

        try {
            // Warm up singletons
            SymptomKnowledgeBase kb = SymptomKnowledgeBase.getInstance();
            HospitalDatabase db = HospitalDatabase.getInstance();

            System.out.println("[INIT] Clinical Knowledge Graph Loaded: " + kb.getAllSymptoms().size() + " Symptoms, " 
                               + kb.getAllDiseaseProfiles().size() + " Disease Protocols.");
            System.out.println("[INIT] Public Health Facility Network: " + db.getAllFacilities().size() + " Government Institutions.");

            // Start HTTP Server
            HttpServerEngine serverEngine = new HttpServerEngine(port, WEB_ROOT);
            serverEngine.start();

            String appUrl = "http://localhost:" + port;
            System.out.println("\n[READY] SSS Medical Assist is live and accessible at:");
            System.out.println("        -> " + appUrl);
            System.out.println("        -> REST API Health: " + appUrl + "/api/health");
            System.out.println("        -> Press Ctrl+C in terminal to stop server.\n");

            // Open browser if supported
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    // Small non-blocking attempt
                    new Thread(() -> {
                        try {
                            Thread.sleep(800);
                            Desktop.getDesktop().browse(new URI(appUrl));
                        } catch (Exception ignored) {}
                    }).start();
                } catch (Exception ignored) {}
            }

            // Keep main thread alive
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n[SHUTDOWN] Shutting down SSS Medical Assist Server...");
                serverEngine.stop();
            }));

        } catch (Exception e) {
            System.err.println("[ERROR] Failed to start SSS Medical Assist Server: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
