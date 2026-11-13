# 📘 SSS Medical Assist: Academic & Technical Presentation Guide

This guide provides an in-depth breakdown of the architecture, algorithms, and healthcare impact of **SSS Medical Assist (AI Symptom Checker with Referral to Nearby Government Hospitals/PHCs)**.

---

## 🎯 Problem Statement & Healthcare Motivation

In many developing regions and rural communities, primary health access faces significant bottlenecks:
1. **Unnecessary Delays in Critical Emergencies**: Patients often visit distant facilities or delay seeking care for acute conditions like myocardial infarction, stroke, or severe sepsis.
2. **Overcrowding of Tertiary Apex Hospitals**: Patients with mild, self-limiting ailments (e.g. viral colds, mild gastroenteritis) bypass local Primary Health Centres (PHCs) and directly crowd apex medical colleges.
3. **Lack of Accessible Digital Triage**: Patients lack preliminary, understandable health categorization to make informed decisions about where and when to seek medical help.

**Sanjeevani AI** bridges this critical gap by connecting intelligent symptom screening directly with the public healthcare tiered infrastructure (Sub-Centre $\rightarrow$ PHC $\rightarrow$ CHC $\rightarrow$ District Hospital $\rightarrow$ Medical College/AIIMS).

---

## 🧠 Core Algorithmic Framework

### 1. Bayesian Posterior Probability Engine
Given a set of observed symptoms $S = \{s_1, s_2, \dots, s_k\}$ and patient risk multipliers $M$, the posterior probability $P(D_i | S)$ for condition $D_i$ is computed as:

$$P(D_i | S) \propto P(D_i) \cdot \prod_{j=1}^{k} P(s_j | D_i) \cdot M(\text{age}, \text{pregnancy}, \text{comorbidities})$$

Where:
- $P(D_i)$ is the baseline population prevalence.
- $P(s_j | D_i)$ is the conditional likelihood of symptom $s_j$ in condition $D_i$, weighted by the user-reported severity score ($1 \le \text{severity} \le 10$).
- $M$ applies demographic and chronic disease risk multipliers (e.g. age $\ge 60$ increases cardiovascular disease prior by 1.8x).

### 2. NEWS2 Physiological Early Warning Scoring
The system dynamically calculates a vital signs score based on physiological parameters:
- **Oxygen Saturation ($\text{SpO}_2$)**: $<90\%$ triggers immediate Red-Flag emergency and $+3.0$ score.
- **Systolic Blood Pressure**: $<90\text{ mmHg}$ triggers shock warning.
- **Body Temperature**: $\ge 104^\circ\text{F}$ triggers hyperpyrexia alert.
- **Respiratory Rate**: $>25\text{ breaths/min}$ triggers tachypnea warning.

### 3. Spatial Haversine Routing Formula
Distance $d$ between patient coordinates $(\phi_1, \lambda_1)$ and health facility coordinates $(\phi_2, \lambda_2)$ is computed using the Great-Circle Haversine formula:

$$a = \sin^2\left(\frac{\Delta \phi}{2}\right) + \cos(\phi_1)\cos(\phi_2)\sin^2\left(\frac{\Delta \lambda}{2}\right)$$
$$d = 2 R \cdot \text{atan2}\left(\sqrt{a}, \sqrt{1-a}\right) \quad (R = 6371\text{ km})$$

Transit time is calculated dynamically taking road infrastructure and emergency dispatch speed into account:

$$T_{\text{transit}} \approx \left(\frac{d}{35\text{ km/h}}\right) \times 60 + 5\text{ minutes overhead}$$

---

## 🏥 Public Healthcare Referral Tier Hierarchy

| Care Tier | Facility Type | Primary Capabilities | Target Triage Level |
|---|---|---|---|
| **Tier 1** | Ayushman Arogya Mandir (Sub-Centre) | Basic health screening, immunization, maternal wellness | `LOW (GREEN)` |
| **Tier 2** | Primary Health Centre (PHC) | General Medical Officer, free pharmacy, outpatient diagnostics | `MODERATE (YELLOW)` |
| **Tier 3** | Community Health Centre (CHC / FRU) | 30-50 beds, Obstetrician, Pediatrician, Minor Surgery, X-Ray | `HIGH (AMBER)` |
| **Tier 4** | District Hospital (DH) | 24x7 Emergency casualty, ICU, Blood Bank, Major Surgery | `HIGH / EMERGENCY` |
| **Tier 5** | Apex Medical College / AIIMS | Level-1 Trauma, Cath Lab, Neurosurgery, Advanced Critical Care | `EMERGENCY (RED)` |

---

## 📊 Live Verification & Demo Checklist

1. **Test Case 1: Acute Heart Attack (Cardiac Emergency)**
   - Enter symptoms: "Crushing chest pain radiating to left arm with breathlessness"
   - Outcome: **RED-STAT EMERGENCY**, Urgency Score 91+, immediate 108 ambulance trigger, route to 24/7 District Hospital.

2. **Test Case 2: Common Flu (Mild Primary Care)**
   - Enter symptoms: "Runny nose, mild sore throat, and slight headache"
   - Outcome: **GREEN-REGULAR LOW**, Urgency Score 25, route to nearest Primary Health Centre (PHC) with home steam inhalation and hydration tips.

3. **Test Case 3: Pediatric High Fever with Chills (Moderate/High)**
   - Enter symptoms: "High grade fever, severe headache, joint pain"
   - Outcome: **AMBER-URGENT HIGH**, Urgency Score 65+, recommended CBC test and priority OPD token.

---

## 🎓 Viva Questions & Answers

**Q1: Why is this system implemented in Java?**
*Answer:* Java delivers enterprise-grade multithreading (`java.util.concurrent`), cross-platform portability, high memory safety, robust embedded HTTP serving capabilities, and zero runtime package corruption issues.

**Q2: How does the system prevent hallucinated or dangerous medical advice?**
*Answer:* The engine relies on a curated clinical knowledge base modeled on SNOMED CT and ICD-11 classifications with strict Red-Flag overrides. High-risk symptoms instantly bypass low-level triage to enforce emergency protocols.

**Q3: How does the referral slip prevent tampering?**
*Answer:* Each slip generates a cryptographic SHA-256 hash derived from the patient data, timestamp, and slip ID, represented as a scannable QR matrix on the official certificate.
