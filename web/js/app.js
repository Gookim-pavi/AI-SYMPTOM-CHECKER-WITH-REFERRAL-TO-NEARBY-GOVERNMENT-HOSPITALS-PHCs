/**
 * ============================================================================
 * SSS MEDICAL ASSIST - ADVANCED CLIENT LOGIC & INTERACTION ENGINE
 * AI Symptom Checker & Government Hospital / PHC Referral Network
 * ============================================================================
 */

// Global Application State
const state = {
    allSymptoms: [],
    selectedSymptoms: new Map(), // symptomId -> { severity: 1..10, duration: '1-3 days' }
    patient: {
        name: '',
        age: null,
        gender: '',
        isPregnant: false,
        pregnancyTrimester: 0,
        comorbidities: [],
        latitude: 12.9716,
        longitude: 77.5946,
        state: 'Karnataka',
        district: 'Bengaluru Urban',
        pincode: '560001',
        bodyTempF: null,
        spo2Percent: null,
        heartRateBpm: null,
        systolicBp: null
    },
    currentTriageResult: null,
    currentReferralSlip: null,
    facilitiesList: [],
    activeFilterSystem: 'ALL',
    activeFacilityCategory: 'ALL',
    emergencyOnlyFacilityFilter: false,
    map: null,
    mapMarkers: [],
    userLocationMarker: null,
    userLocationCircle: null,
    voiceGuidanceEnabled: true,
    speechRecognition: null,
    isListening: false,
    currentLanguage: 'en'
};

// --- DOM Elements ---
const DOM = {
    // Nav Tabs
    tabs: document.querySelectorAll('.nav-tab'),
    tabPanes: document.querySelectorAll('.tab-pane'),
    activeSlipsCount: document.getElementById('active-slips-count'),
    triageStatusBadge: document.getElementById('triage-status-badge'),
    
    // Patient Form
    patientName: document.getElementById('patient-name'),
    patientAge: document.getElementById('patient-age'),
    patientGender: document.getElementById('patient-gender'),
    pregnancyGroup: document.getElementById('pregnancy-group'),
    isPregnant: document.getElementById('is-pregnant'),
    btnAutoLocate: document.getElementById('btn-autolocate'),
    currentLocationText: document.getElementById('current-location-text'),
    coordsText: document.getElementById('coords-text'),
    changeLocationBtn: document.getElementById('change-location-btn'),
    comorbChips: document.querySelectorAll('.comorb-chip'),
    vitalsToggleBtn: document.getElementById('vitals-toggle-btn'),
    vitalsBody: document.getElementById('vitals-body'),
    vitalTemp: document.getElementById('vital-temp'),
    vitalSpo2: document.getElementById('vital-spo2'),
    vitalPulse: document.getElementById('vital-pulse'),
    vitalSbp: document.getElementById('vital-sbp'),

    // NLP & Voice
    nlSymptomText: document.getElementById('nl-symptom-text'),
    btnParseNlp: document.getElementById('btn-parse-nlp'),
    voiceInputBtn: document.getElementById('voice-input-btn'),
    voiceBtnText: document.getElementById('voice-btn-text'),
    promptChips: document.querySelectorAll('.prompt-chip'),

    // Body Map & Tag Cloud
    bodySvg: document.getElementById('human-body-svg'),
    bodyParts: document.querySelectorAll('.body-part-interactive'),
    sysPills: document.querySelectorAll('.sys-pill'),
    symptomFilterInput: document.getElementById('symptom-filter-input'),
    symptomsTagCloud: document.getElementById('symptoms-tag-cloud'),
    selectedItemsList: document.getElementById('selected-items-list'),
    selectedCount: document.getElementById('selected-count'),
    btnClearSymptoms: document.getElementById('btn-clear-symptoms'),
    btnRunTriage: document.getElementById('btn-run-triage'),
    btnBodyFront: document.getElementById('btn-body-front'),
    btnBodyBack: document.getElementById('btn-body-back'),

    // Triage Tab
    noTriagePlaceholder: document.getElementById('no-triage-placeholder'),
    triageDynamicContent: document.getElementById('triage-dynamic-content'),
    urgencyBanner: document.getElementById('urgency-banner'),
    urgencyMainIcon: document.getElementById('urgency-main-icon'),
    urgencyLevelTag: document.getElementById('urgency-level-tag'),
    urgencyHeadline: document.getElementById('urgency-headline'),
    urgencyRecommendation: document.getElementById('urgency-recommendation'),
    gaugeScoreNum: document.getElementById('gauge-score-num'),
    gaugeFill: document.getElementById('gauge-fill'),
    emergencyActionStrip: document.getElementById('emergency-action-strip'),
    clinicalSummaryText: document.getElementById('clinical-summary-text'),
    redflagsList: document.getElementById('redflags-list'),
    vitalScoreBadge: document.getElementById('vital-score-badge'),
    differentialList: document.getElementById('differential-list'),
    questionsList: document.getElementById('questions-list'),
    tierName: document.getElementById('tier-name'),
    tierDesc: document.getElementById('tier-desc'),
    dosList: document.getElementById('dos-list'),
    dontsList: document.getElementById('donts-list'),
    btnGenerateReferral: document.getElementById('btn-generate-referral'),
    btnViewMapReferral: document.getElementById('btn-view-map-referral'),

    // Locator Tab
    facilityFilterPills: document.querySelectorAll('#facility-filter-pills .filt-pill'),
    chkEmergencyOnly: document.getElementById('chk-emergency-only'),
    facilitiesScrollList: document.getElementById('facilities-scroll-list'),
    mapStatsText: document.getElementById('map-stats-text'),

    // Referral Slip Tab
    emptySlipState: document.getElementById('empty-slip-state'),
    printableCertificate: document.getElementById('printable-certificate'),
    btnPrintActiveSlip: document.getElementById('btn-print-active-slip'),
    certSlipId: document.getElementById('cert-slip-id'),
    certTokenVal: document.getElementById('cert-token-val'),
    certTriageTier: document.getElementById('cert-triage-tier'),
    certPriorityRibbon: document.getElementById('cert-priority-ribbon'),
    certPatName: document.getElementById('cert-pat-name'),
    certPatAgeSex: document.getElementById('cert-pat-age-sex'),
    certAbhaId: document.getElementById('cert-abha-id'),
    certIssuedAt: document.getElementById('cert-issued-at'),
    certFacName: document.getElementById('cert-fac-name'),
    certDeptName: document.getElementById('cert-dept-name'),
    certFacAddr: document.getElementById('cert-fac-addr'),
    certFacPhone: document.getElementById('cert-fac-phone'),
    certClinicalImpression: document.getElementById('cert-clinical-impression'),
    certReportedSymptoms: document.getElementById('cert-reported-symptoms'),
    certRedflagsRow: document.getElementById('cert-redflags-row'),
    certRedflagText: document.getElementById('cert-redflag-text'),
    certHashVal: document.getElementById('cert-hash-val'),
    qrCanvas: document.getElementById('qr-canvas'),

    // Doctor Desk Tab
    statEmergencyCount: document.getElementById('stat-emergency-count'),
    statUrgentCount: document.getElementById('stat-urgent-count'),
    statTotalReferrals: document.getElementById('stat-total-referrals'),
    statNetworkBeds: document.getElementById('stat-network-beds'),
    doctorReferralsList: document.getElementById('doctor-referrals-list'),
    btnRefreshDoctorFeed: document.getElementById('btn-refresh-doctor-feed'),
    verifyTokenInput: document.getElementById('verify-token-input'),
    btnVerifyToken: document.getElementById('btn-verify-token'),
    verificationResultBox: document.getElementById('verification-result-box'),

    // Emergency SOS Modal
    sosTriggerBtn: document.getElementById('sos-trigger-btn'),
    sosModalOverlay: document.getElementById('sos-modal-overlay'),
    sosCloseBtn: document.getElementById('sos-close-btn'),
    sosGpsText: document.getElementById('sos-gps-text'),

    // Global Header Controls
    themeToggleBtn: document.getElementById('theme-toggle-btn'),
    voiceGuideBtn: document.getElementById('voice-guide-btn'),
    languageSelect: document.getElementById('language-select'),
    toastContainer: document.getElementById('toast-container')
};

// ============================================================================
// INITIALIZATION
// ============================================================================
document.addEventListener('DOMContentLoaded', async () => {
    const loginScreen = document.getElementById('login-screen');
    const appWrapper = document.getElementById('app-wrapper');
    const loginForm = document.getElementById('login-form');
    const logoutButton = document.getElementById('logout-btn');
    let appInitialized = false;

    const showApplication = () => {
        loginScreen.hidden = true;
        appWrapper.hidden = false;
        if (appInitialized) return;
        appInitialized = true;

        initializeApplication();
    };

    loginForm.addEventListener('submit', event => {
        event.preventDefault();
        sessionStorage.setItem('sss-medical-assist-demo-session', 'active');
        showApplication();
    });

    logoutButton.addEventListener('click', () => {
        sessionStorage.removeItem('sss-medical-assist-demo-session');
        appWrapper.hidden = true;
        loginScreen.hidden = false;
        loginForm.reset();
    });

    if (sessionStorage.getItem('sss-medical-assist-demo-session') === 'active') {
        showApplication();
    }
});

async function initializeApplication() {
    initBackgroundCanvas();
    setupNavigation();
    setupPatientFormEvents();
    setupSpeechRecognition();
    setupAnatomicalMap();
    setupTriageActionEvents();
    setupFacilityLocatorEvents();
    setupDoctorDeskEvents();
    setupSosModal();
    setupThemeAndVoiceControls();

    // Fetch initial data from Java backend
    await loadSymptoms();
    await loadFacilities();
    await loadDoctorStats();
}

// ============================================================================
// BACKGROUND CANVAS ANIMATION (CYBER-MEDICAL NODES)
// ============================================================================
function initBackgroundCanvas() {
    const canvas = document.getElementById('bg-canvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    let width = canvas.width = window.innerWidth;
    let height = canvas.height = window.innerHeight;

    window.addEventListener('resize', () => {
        width = canvas.width = window.innerWidth;
        height = canvas.height = window.innerHeight;
    });

    const particles = [];
    const particleCount = Math.min(width > 768 ? 55 : 25, 60);

    for (let i = 0; i < particleCount; i++) {
        particles.push({
            x: Math.random() * width,
            y: Math.random() * height,
            vx: (Math.random() - 0.5) * 0.6,
            vy: (Math.random() - 0.5) * 0.6,
            radius: Math.random() * 2 + 1,
            alpha: Math.random() * 0.4 + 0.1
        });
    }

    function animate() {
        ctx.clearRect(0, 0, width, height);

        // Update and draw particles
        for (let i = 0; i < particles.length; i++) {
            const p = particles[i];
            p.x += p.vx;
            p.y += p.vy;

            if (p.x < 0) p.x = width;
            if (p.x > width) p.x = 0;
            if (p.y < 0) p.y = height;
            if (p.y > height) p.y = 0;

            ctx.beginPath();
            ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
            ctx.fillStyle = `rgba(6, 182, 212, ${p.alpha})`;
            ctx.fill();

            // Connect nearby nodes
            for (let j = i + 1; j < particles.length; j++) {
                const p2 = particles[j];
                const dx = p.x - p2.x;
                const dy = p.y - p2.y;
                const dist = Math.sqrt(dx * dx + dy * dy);

                if (dist < 120) {
                    ctx.beginPath();
                    ctx.moveTo(p.x, p.y);
                    ctx.lineTo(p2.x, p2.y);
                    ctx.strokeStyle = `rgba(59, 130, 246, ${0.15 * (1 - dist / 120)})`;
                    ctx.lineWidth = 0.8;
                    ctx.stroke();
                }
            }
        }

        requestAnimationFrame(animate);
    }

    animate();
}

// ============================================================================
// NAVIGATION TABS
// ============================================================================
function setupNavigation() {
    DOM.tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            const targetId = tab.getAttribute('data-tab');
            DOM.tabs.forEach(t => t.classList.remove('active'));
            DOM.tabPanes.forEach(pane => pane.classList.remove('active'));

            tab.classList.add('active');
            const activePane = document.getElementById(targetId);
            if (activePane) activePane.classList.add('active');

            if (targetId === 'tab-hospital-locator') {
                setTimeout(initOrRefreshMap, 200);
            }
            if (targetId === 'tab-doctor-desk') {
                loadDoctorStats();
                loadDoctorReferralsFeed();
            }
        });
    });
}

function switchTab(tabId) {
    const tabBtn = document.querySelector(`[data-tab="${tabId}"]`);
    if (tabBtn) tabBtn.click();
}

// ============================================================================
// PATIENT FORM & LOCATION
// ============================================================================
function setupPatientFormEvents() {
    DOM.patientName.addEventListener('input', e => state.patient.name = e.target.value);
    DOM.patientAge.addEventListener('input', e => state.patient.age = parseInt(e.target.value, 10) || null);
    DOM.patientGender.addEventListener('change', e => {
        state.patient.gender = e.target.value;
        if (e.target.value === 'Female') {
            DOM.pregnancyGroup.style.display = 'flex';
        } else {
            DOM.pregnancyGroup.style.display = 'none';
            state.patient.isPregnant = false;
        }
    });

    DOM.isPregnant.addEventListener('change', e => {
        state.patient.isPregnant = e.target.value !== 'no';
        if (e.target.value === 'trimester1') state.patient.pregnancyTrimester = 1;
        else if (e.target.value === 'trimester2') state.patient.pregnancyTrimester = 2;
        else if (e.target.value === 'trimester3') state.patient.pregnancyTrimester = 3;
        else state.patient.pregnancyTrimester = 0;
    });

    // Comorbidities Chips
    DOM.comorbChips.forEach(chip => {
        chip.addEventListener('click', () => {
            const val = chip.getAttribute('data-val');
            chip.classList.toggle('selected');
            if (chip.classList.contains('selected')) {
                if (!state.patient.comorbidities.includes(val)) state.patient.comorbidities.push(val);
            } else {
                state.patient.comorbidities = state.patient.comorbidities.filter(c => c !== val);
            }
        });
    });

    // Vitals Accordion
    DOM.vitalsToggleBtn.addEventListener('click', () => {
        DOM.vitalsBody.classList.toggle('open');
        DOM.vitalsToggleBtn.classList.toggle('active');
    });

    DOM.vitalTemp.addEventListener('input', e => state.patient.bodyTempF = parseFloat(e.target.value) || null);
    DOM.vitalSpo2.addEventListener('input', e => state.patient.spo2Percent = parseInt(e.target.value) || null);
    DOM.vitalPulse.addEventListener('input', e => state.patient.heartRateBpm = parseInt(e.target.value) || null);
    DOM.vitalSbp.addEventListener('input', e => state.patient.systolicBp = parseInt(e.target.value) || null);

    // Auto GPS Location
    DOM.btnAutoLocate.addEventListener('click', handleAutoLocation);
    DOM.changeLocationBtn.addEventListener('click', handleManualDistrictChange);
}

function handleAutoLocation() {
    if ('geolocation' in navigator) {
        showToast('Requesting GPS location from device...', 'info');
        navigator.geolocation.getCurrentPosition(
            pos => {
                state.patient.latitude = pos.coords.latitude;
                state.patient.longitude = pos.coords.longitude;
                updateLocationDisplay('Detected Location (GPS Active)', pos.coords.latitude, pos.coords.longitude);
                showToast('GPS coordinates acquired successfully!', 'success');
                if (state.map) initOrRefreshMap();
                loadFacilities();
            },
            err => {
                showToast('GPS access denied or unavailable. Using standard district coordinates.', 'info');
            },
            { timeout: 8000 }
        );
    } else {
        showToast('Geolocation is not supported by your browser.', 'info');
    }
}

function handleManualDistrictChange() {
    const districts = [
        { name: 'Bengaluru Urban, Karnataka (560001)', lat: 12.9716, lng: 77.5946, state: 'Karnataka', district: 'Bengaluru Urban' },
        { name: 'New Delhi / Central NCR (110001)', lat: 28.6139, lng: 77.2090, state: 'Delhi', district: 'New Delhi' },
        { name: 'Mumbai / Thane Region (400001)', lat: 18.9388, lng: 72.8354, state: 'Maharashtra', district: 'Mumbai' },
        { name: 'Chennai / Chengalpattu (600001)', lat: 13.0827, lng: 80.2707, state: 'Tamil Nadu', district: 'Chennai' },
        { name: 'Lucknow / Mohanlalganj (226001)', lat: 26.8467, lng: 80.9462, state: 'Uttar Pradesh', district: 'Lucknow' }
    ];

    const sel = prompt('Select Location Preset:\n1. Bengaluru Urban (Karnataka)\n2. New Delhi (Central NCR)\n3. Mumbai (Maharashtra)\n4. Chennai (Tamil Nadu)\n5. Lucknow (Uttar Pradesh)', '1');
    const idx = parseInt(sel) - 1;
    if (idx >= 0 && idx < districts.length) {
        const d = districts[idx];
        state.patient.latitude = d.lat;
        state.patient.longitude = d.lng;
        state.patient.state = d.state;
        state.patient.district = d.district;
        updateLocationDisplay(d.name, d.lat, d.lng);
        showToast(`Location updated to ${d.district}`, 'success');
        if (state.map) initOrRefreshMap();
        loadFacilities();
    }
}

function updateLocationDisplay(name, lat, lng) {
    DOM.currentLocationText.textContent = name;
    DOM.coordsText.textContent = `Lat: ${lat.toFixed(4)}, Lng: ${lng.toFixed(4)}`;
    DOM.sosGpsText.textContent = `Latitude: ${lat.toFixed(4)}° N, Longitude: ${lng.toFixed(4)}° E (${state.patient.district})`;
}

// ============================================================================
// NATURAL LANGUAGE AI PARSER & VOICE RECOGNITION
// ============================================================================
function setupSpeechRecognition() {
    if ('webkitSpeechRecognition' in window || 'SpeechRecognition' in window) {
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        state.speechRecognition = new SpeechRecognition();
        state.speechRecognition.continuous = false;
        state.speechRecognition.interimResults = false;
        state.speechRecognition.lang = 'en-IN';

        state.speechRecognition.onstart = () => {
            state.isListening = true;
            DOM.voiceInputBtn.classList.add('listening');
            DOM.voiceBtnText.textContent = 'Listening...';
        };

        state.speechRecognition.onresult = (event) => {
            const transcript = event.results[0][0].transcript;
            DOM.nlSymptomText.value = transcript;
            showToast('Voice transcribed. Running AI Entity extraction...', 'info');
            parseNlpSymptoms();
        };

        state.speechRecognition.onerror = (err) => {
            console.warn('Speech recognition error:', err);
            stopListening();
        };

        state.speechRecognition.onend = () => {
            stopListening();
        };

        DOM.voiceInputBtn.addEventListener('click', () => {
            if (state.isListening) {
                state.speechRecognition.stop();
            } else {
                try {
                    state.speechRecognition.start();
                } catch (e) {
                    console.error(e);
                }
            }
        });
    } else {
        DOM.voiceInputBtn.addEventListener('click', () => {
            showToast('Speech Recognition is not supported on this browser. Please type symptoms.', 'info');
        });
    }

    // Natural Language Parse Button
    DOM.btnParseNlp.addEventListener('click', parseNlpSymptoms);

    // Prompt Chips
    DOM.promptChips.forEach(chip => {
        chip.addEventListener('click', () => {
            DOM.nlSymptomText.value = chip.getAttribute('data-text');
            parseNlpSymptoms();
        });
    });
}

function stopListening() {
    state.isListening = false;
    DOM.voiceInputBtn.classList.remove('listening');
    DOM.voiceBtnText.textContent = 'Voice Input';
}

async function parseNlpSymptoms() {
    const text = DOM.nlSymptomText.value.trim();
    if (!text) {
        showToast('Please enter or speak a symptom description first.', 'info');
        return;
    }

    try {
        const res = await fetch('/api/nlp-parse', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ text })
        });
        const data = await res.json();

        if (data.extractedSymptomIds && data.extractedSymptomIds.length > 0) {
            data.extractedSymptomIds.forEach(id => {
                const sev = (data.symptomSeverities && data.symptomSeverities[id]) || 6;
                addSymptomToSelection(id, sev);
            });
            showToast(`AI extracted ${data.extractedSymptomIds.length} symptom(s) successfully!`, 'success');
            speakVoiceText(`Identified symptoms: ${data.extractedSymptomIds.length} clinical conditions detected.`);
        } else {
            showToast('No matching medical symptoms identified. Try selecting from the body map.', 'info');
        }
    } catch (e) {
        console.error('NLP Parse error:', e);
        showToast('Error communicating with Java NLP engine.', 'error');
    }
}

// ============================================================================
// ANATOMICAL MAP & SYMPTOM CATALOG
// ============================================================================
function setupAnatomicalMap() {
    // Body Part SVG clicks
    DOM.bodyParts.forEach(part => {
        part.addEventListener('click', () => {
            const system = part.getAttribute('data-system');
            DOM.bodyParts.forEach(p => p.classList.remove('active'));
            part.classList.add('active');
            filterSymptomsBySystem(system);
            speakVoiceText(`Filtering symptoms for ${system} region.`);
        });
    });

    // System Pills clicks
    DOM.sysPills.forEach(pill => {
        pill.addEventListener('click', () => {
            const filter = pill.getAttribute('data-filter');
            DOM.sysPills.forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            filterSymptomsBySystem(filter);
        });
    });

    // Symptom search input
    DOM.symptomFilterInput.addEventListener('input', e => {
        renderSymptomTagCloud(e.target.value);
    });

    // Clear All Symptoms
    DOM.btnClearSymptoms.addEventListener('click', () => {
        state.selectedSymptoms.clear();
        updateSelectedSymptomsTray();
        renderSymptomTagCloud();
    });

    // Front/Back body buttons
    DOM.btnBodyFront.addEventListener('click', () => {
        DOM.btnBodyFront.classList.add('active');
        DOM.btnBodyBack.classList.remove('active');
        DOM.bodySvg.style.transform = 'scaleX(1)';
    });
    DOM.btnBodyBack.addEventListener('click', () => {
        DOM.btnBodyBack.classList.add('active');
        DOM.btnBodyFront.classList.remove('active');
        DOM.bodySvg.style.transform = 'scaleX(-1)';
        filterSymptomsBySystem('Musculoskeletal');
    });
}

async function loadSymptoms() {
    try {
        const res = await fetch('/api/symptoms');
        state.allSymptoms = await res.json();
        renderSymptomTagCloud();
    } catch (e) {
        console.error('Failed to load symptoms:', e);
        DOM.symptomsTagCloud.innerHTML = '<div class="empty-state-text text-danger">Failed to load symptoms catalog from Java server.</div>';
    }
}

function filterSymptomsBySystem(system) {
    state.activeFilterSystem = system;
    DOM.sysPills.forEach(p => {
        if (p.getAttribute('data-filter') === system) p.classList.add('active');
        else p.classList.remove('active');
    });
    renderSymptomTagCloud(DOM.symptomFilterInput.value);
}

function renderSymptomTagCloud(searchQuery = '') {
    const query = (searchQuery || '').toLowerCase().trim();
    let filtered = state.allSymptoms;

    if (state.activeFilterSystem !== 'ALL') {
        filtered = filtered.filter(s => s.system.equalsIgnoreCase ? s.system.equalsIgnoreCase(state.activeFilterSystem) : s.system.toLowerCase() === state.activeFilterSystem.toLowerCase());
    }

    if (query) {
        filtered = filtered.filter(s => {
            return s.name.toLowerCase().includes(query) ||
                   (s.synonyms && s.synonyms.some(syn => syn.toLowerCase().includes(query))) ||
                   s.system.toLowerCase().includes(query);
        });
    }

    if (filtered.length === 0) {
        DOM.symptomsTagCloud.innerHTML = '<div class="empty-state-text">No matching symptoms found. Try searching another term or change system filter.</div>';
        return;
    }

    DOM.symptomsTagCloud.innerHTML = filtered.map(sym => {
        const isSelected = state.selectedSymptoms.has(sym.id);
        const isRedFlag = sym.isRedFlag;
        return `
            <button class="symptom-tag ${isSelected ? 'selected' : ''} ${isRedFlag ? 'redflag-tag' : ''}" 
                    data-id="${sym.id}" 
                    title="${sym.description || sym.name}">
                ${isRedFlag ? '<i class="fa-solid fa-triangle-exclamation"></i>' : ''}
                ${sym.name}
                ${isSelected ? '<i class="fa-solid fa-check"></i>' : '<i class="fa-solid fa-plus"></i>'}
            </button>
        `;
    }).join('');

    // Attach click listeners to tags
    DOM.symptomsTagCloud.querySelectorAll('.symptom-tag').forEach(tag => {
        tag.addEventListener('click', () => {
            const symId = tag.getAttribute('data-id');
            toggleSymptomSelection(symId);
        });
    });
}

function toggleSymptomSelection(symId) {
    if (state.selectedSymptoms.has(symId)) {
        state.selectedSymptoms.delete(symId);
    } else {
        const sym = state.allSymptoms.find(s => s.id === symId);
        const defaultSev = sym ? sym.severity : 5;
        state.selectedSymptoms.set(symId, { severity: defaultSev, duration: '1-3 days' });
    }
    updateSelectedSymptomsTray();
    renderSymptomTagCloud(DOM.symptomFilterInput.value);
}

function addSymptomToSelection(symId, severity = 5) {
    state.selectedSymptoms.set(symId, { severity: severity, duration: '1-3 days' });
    updateSelectedSymptomsTray();
    renderSymptomTagCloud(DOM.symptomFilterInput.value);
}

function updateSelectedSymptomsTray() {
    const count = state.selectedSymptoms.size;
    DOM.selectedCount.textContent = count;
    DOM.btnRunTriage.disabled = count === 0;

    if (count === 0) {
        DOM.selectedItemsList.innerHTML = '<div class="empty-state-text">No symptoms selected yet. Click on tags or use the body map above.</div>';
        return;
    }

    let html = '';
    state.selectedSymptoms.forEach((data, symId) => {
        const sym = state.allSymptoms.find(s => s.id === symId);
        const name = sym ? sym.name : symId;
        const isRedFlag = sym ? sym.isRedFlag : false;
        const sevClass = data.severity >= 8 ? 'high' : (data.severity >= 5 ? 'med' : 'low');

        html += `
            <div class="selected-item-row">
                <div class="item-left">
                    ${isRedFlag ? '<i class="fa-solid fa-triangle-exclamation text-danger"></i>' : ''}
                    <strong>${name}</strong>
                    <span class="item-severity-pill ${sevClass}">Sev: ${data.severity}/10</span>
                </div>
                <button class="btn-remove-tag" data-id="${symId}" title="Remove symptom">
                    <i class="fa-solid fa-circle-xmark"></i>
                </button>
            </div>
        `;
    });

    DOM.selectedItemsList.innerHTML = html;

    // Attach remove event listeners
    DOM.selectedItemsList.querySelectorAll('.btn-remove-tag').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.getAttribute('data-id');
            state.selectedSymptoms.delete(id);
            updateSelectedSymptomsTray();
            renderSymptomTagCloud(DOM.symptomFilterInput.value);
        });
    });
}

// ============================================================================
// AI TRIAGE EXECUTION & RESULTS PRESENTATION
// ============================================================================
function setupTriageActionEvents() {
    DOM.btnRunTriage.addEventListener('click', runAiTriage);
    DOM.btnGenerateReferral.addEventListener('click', generateOfficialReferralSlip);
    DOM.btnViewMapReferral.addEventListener('click', () => switchTab('tab-hospital-locator'));
}

async function runAiTriage() {
    if (state.selectedSymptoms.size === 0) {
        showToast('Please select at least one symptom.', 'info');
        return;
    }

    showToast('Running Bayesian Clinical Triage Engine...', 'info');

    // Build payload
    const symptomIds = Array.from(state.selectedSymptoms.keys());
    const symptomSeverities = {};
    state.selectedSymptoms.forEach((val, key) => {
        symptomSeverities[key] = val.severity;
    });

    const payload = {
        patientName: state.patient.name,
        age: state.patient.age,
        gender: state.patient.gender,
        isPregnant: state.patient.isPregnant,
        pregnancyTrimester: state.patient.pregnancyTrimester,
        comorbidities: state.patient.comorbidities,
        latitude: state.patient.latitude,
        longitude: state.patient.longitude,
        state: state.patient.state,
        district: state.patient.district,
        pincode: state.patient.pincode,
        bodyTempF: state.patient.bodyTempF,
        spo2Percent: state.patient.spo2Percent,
        heartRateBpm: state.patient.heartRateBpm,
        systolicBp: state.patient.systolicBp,
        symptomIds: symptomIds,
        symptomSeverities: symptomSeverities
    };

    try {
        const res = await fetch('/api/triage', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const result = await res.json();
        state.currentTriageResult = result;

        displayTriageResults(result);
        switchTab('tab-triage-results');

        // Play audio alert if emergency
        if (result.riskLevel === 'EMERGENCY') {
            speakVoiceText("Warning: Critical health indicators detected. Emergency medical referral advised immediately.");
        } else {
            speakVoiceText(`Triage Assessment Complete. Urgency risk grade: ${result.riskLevel}.`);
        }
    } catch (e) {
        console.error('Triage execution failed:', e);
        showToast('Failed to run AI triage. Ensure Java server is active.', 'error');
    }
}

function displayTriageResults(result) {
    DOM.noTriagePlaceholder.style.display = 'none';
    DOM.triageDynamicContent.style.display = 'block';

    // Update Nav Badge
    DOM.triageStatusBadge.textContent = result.riskLevel;
    DOM.triageStatusBadge.className = `nav-badge ${result.riskLevel.toLowerCase()}`;

    // Urgency Banner Styling
    DOM.urgencyBanner.className = `urgency-banner tier-${result.riskLevel.toLowerCase()}`;
    DOM.gaugeScoreNum.textContent = result.urgencyScore;

    if (result.riskLevel === 'EMERGENCY') {
        DOM.urgencyLevelTag.textContent = 'CRITICAL EMERGENCY (RED TIER)';
        DOM.urgencyMainIcon.className = 'fa-solid fa-triangle-exclamation';
        DOM.emergencyActionStrip.style.display = 'flex';
        DOM.urgencyHeadline.textContent = 'Immediate Emergency Medical Care Required';
    } else if (result.riskLevel === 'HIGH') {
        DOM.urgencyLevelTag.textContent = 'HIGH URGENCY (AMBER TIER)';
        DOM.urgencyMainIcon.className = 'fa-solid fa-hourglass-half';
        DOM.emergencyActionStrip.style.display = 'none';
        DOM.urgencyHeadline.textContent = 'Urgent Hospital / CHC Consultation Needed';
    } else if (result.riskLevel === 'MODERATE') {
        DOM.urgencyLevelTag.textContent = 'MODERATE RISK (YELLOW TIER)';
        DOM.urgencyMainIcon.className = 'fa-solid fa-user-doctor';
        DOM.emergencyActionStrip.style.display = 'none';
        DOM.urgencyHeadline.textContent = 'Primary Health Centre (PHC) Visit Recommended';
    } else {
        DOM.urgencyLevelTag.textContent = 'LOW RISK (GREEN TIER)';
        DOM.urgencyMainIcon.className = 'fa-solid fa-circle-check';
        DOM.emergencyActionStrip.style.display = 'none';
        DOM.urgencyHeadline.textContent = 'Sub-Centre Consultation / Primary Monitoring';
    }

    DOM.urgencyRecommendation.textContent = result.primaryCareRecommendation;
    DOM.clinicalSummaryText.textContent = result.clinicalSummary;

    // Red Flags List
    if (result.redFlagsDetected && result.redFlagsDetected.length > 0) {
        DOM.redflagsList.innerHTML = result.redFlagsDetected.map(rf => `<li><i class="fa-solid fa-circle-exclamation"></i> ${rf}</li>`).join('');
        document.getElementById('redflags-box').style.display = 'block';
    } else {
        document.getElementById('redflags-box').style.display = 'none';
    }

    // Vital Score Badge
    DOM.vitalScoreBadge.textContent = `NEWS2 Physiological Risk Score: ${result.vitalAlertScore.toFixed(1)}`;

    // Differential Diagnoses
    if (result.differentialPossibilities && result.differentialPossibilities.length > 0) {
        DOM.differentialList.innerHTML = result.differentialPossibilities.map(diff => `
            <div class="differential-card">
                <div class="diff-header">
                    <span class="diff-title">${diff.name}</span>
                    <span class="diff-icd">ICD-11: ${diff.icd11}</span>
                </div>
                <div class="diff-bar-wrap">
                    <div class="diff-bar-fill" style="width: ${Math.max(15, diff.probabilityPercent)}%;"></div>
                </div>
                <p class="diff-desc">${diff.explanation}</p>
            </div>
        `).join('');
    }

    // Follow-up Questions
    if (result.suggestedFollowUpQuestions && result.suggestedFollowUpQuestions.length > 0) {
        DOM.questionsList.innerHTML = result.suggestedFollowUpQuestions.map(q => `
            <div class="question-row">
                <i class="fa-solid fa-circle-question text-cyan"></i>
                <span>${q}</span>
            </div>
        `).join('');
        document.getElementById('followup-questions-box').style.display = 'block';
    } else {
        document.getElementById('followup-questions-box').style.display = 'none';
    }

    // Recommended Tier
    DOM.tierName.textContent = getTierDisplayName(result.recommendedFacilityCategory);
    DOM.tierDesc.textContent = getTierDescription(result.recommendedFacilityCategory);

    // Dos & Donts
    DOM.dosList.innerHTML = (result.immediateActionSteps || []).map(s => `<li>${s}</li>`).join('');
    DOM.dontsList.innerHTML = (result.whatNotToDo || []).map(s => `<li>${s}</li>`).join('');
}

function getTierDisplayName(tier) {
    switch (tier) {
        case 'TERTIARY_AIIMS': return 'Apex Medical College & Trauma Centre (AIIMS)';
        case 'DISTRICT_HOSPITAL': return 'District Government Hospital (DH)';
        case 'CHC': return 'Community Health Centre (CHC / FRU)';
        case 'PHC': return 'Primary Health Centre (PHC)';
        case 'SUB_CENTRE': return 'Ayushman Arogya Mandir (Sub-Centre)';
        default: return 'Government Primary Health Centre (PHC)';
    }
}

function getTierDescription(tier) {
    switch (tier) {
        case 'TERTIARY_AIIMS': return 'Super-specialty multidisciplinary care, advanced ICU, Cath lab, and neurotrauma.';
        case 'DISTRICT_HOSPITAL': return '24x7 Emergency casualty, surgical wards, blood bank, and inpatient care.';
        case 'CHC': return 'First Referral Unit (FRU) with general surgery, pediatrics, and maternity care.';
        case 'PHC': return 'General Medical Officer consultation, free pharmacy, and primary screening.';
        default: return 'Primary healthcare and first-aid center.';
    }
}

// ============================================================================
// OFFICIAL GOVERNMENT REFERRAL PASS ISSUANCE
// ============================================================================
async function generateOfficialReferralSlip(facilityId = null) {
    if (!state.currentTriageResult) {
        showToast('Please run a triage evaluation first.', 'info');
        return;
    }

    showToast('Issuing Official Government Referral Pass with QR Code...', 'info');

    const symptomIds = Array.from(state.selectedSymptoms.keys());
    const symptomSeverities = {};
    state.selectedSymptoms.forEach((val, key) => {
        symptomSeverities[key] = val.severity;
    });

    const payload = {
        patientName: state.patient.name,
        age: state.patient.age,
        gender: state.patient.gender,
        isPregnant: state.patient.isPregnant,
        latitude: state.patient.latitude,
        longitude: state.patient.longitude,
        state: state.patient.state,
        district: state.patient.district,
        pincode: state.patient.pincode,
        symptomIds: symptomIds,
        symptomSeverities: symptomSeverities,
        facilityId: facilityId || null
    };

    try {
        const res = await fetch('/api/referrals/generate-slip', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const slip = await res.json();
        state.currentReferralSlip = slip;

        renderReferralCertificate(slip);
        switchTab('tab-referral-passes');
        showToast(`Official Referral Pass Issued: Token ${slip.opdTokenNumber}`, 'success');
        speakVoiceText(`Official Referral Pass generated. Token assigned: ${slip.opdTokenNumber}.`);
        loadDoctorStats();
    } catch (e) {
        console.error('Failed to issue referral slip:', e);
        showToast('Error creating referral pass.', 'error');
    }
}

function renderReferralCertificate(slip) {
    DOM.emptySlipState.style.display = 'none';
    DOM.printableCertificate.style.display = 'block';
    DOM.btnPrintActiveSlip.disabled = false;
    DOM.activeSlipsCount.textContent = '1';

    DOM.certSlipId.textContent = slip.slipId;
    DOM.certTokenVal.textContent = slip.opdTokenNumber;
    DOM.certTriageTier.textContent = `TRIAGE: ${slip.priorityQueueCode}`;

    // Priority ribbon color
    if (slip.priorityQueueCode.includes('RED')) {
        DOM.certPriorityRibbon.style.background = '#fee2e2';
        DOM.certPriorityRibbon.style.borderColor = '#f87171';
        DOM.certPriorityRibbon.style.color = '#991b1b';
    } else if (slip.priorityQueueCode.includes('AMBER')) {
        DOM.certPriorityRibbon.style.background = '#fef3c7';
        DOM.certPriorityRibbon.style.borderColor = '#fbbf24';
        DOM.certPriorityRibbon.style.color = '#92400e';
    } else {
        DOM.certPriorityRibbon.style.background = '#d1fae5';
        DOM.certPriorityRibbon.style.borderColor = '#34d399';
        DOM.certPriorityRibbon.style.color = '#065f46';
    }

    DOM.certPatName.textContent = slip.patientName;
    DOM.certPatAgeSex.textContent = `${slip.patientAge} Yrs / ${slip.patientGender}`;
    DOM.certAbhaId.textContent = slip.abhaNumberPlaceholder;
    DOM.certIssuedAt.textContent = slip.issuedAt;

    DOM.certFacName.textContent = slip.destinationFacilityName;
    DOM.certDeptName.textContent = slip.assignedDepartment;
    DOM.certFacAddr.textContent = slip.destinationAddress;
    DOM.certFacPhone.textContent = slip.destinationHelpline;

    DOM.certClinicalImpression.textContent = slip.primaryImpression;

    // Reported Symptoms Tags
    if (slip.reportedSymptoms && slip.reportedSymptoms.length > 0) {
        DOM.certReportedSymptoms.innerHTML = slip.reportedSymptoms.map(s => `<span class="cert-tag-pill">${s}</span>`).join('');
    }

    // Redflags Row
    if (slip.redFlags && slip.redFlags.length > 0) {
        DOM.certRedflagText.textContent = slip.redFlags.join('; ');
        DOM.certRedflagsRow.style.display = 'block';
    } else {
        DOM.certRedflagsRow.style.display = 'none';
    }

    DOM.certHashVal.textContent = `SHA256: ${slip.verificationHash}`;

    // Draw QR Code on Canvas
    drawQrMatrix(DOM.qrCanvas, slip.slipId + '|' + slip.opdTokenNumber + '|' + slip.verificationHash);

    DOM.btnPrintActiveSlip.onclick = () => window.print();
}

function drawQrMatrix(canvas, text) {
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    const size = canvas.width;
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, size, size);

    // High quality mock QR matrix with position markers
    ctx.fillStyle = '#0f172a';

    // Top-Left Corner Box
    drawQrBox(ctx, 10, 10, 24);
    // Top-Right Corner Box
    drawQrBox(ctx, size - 34, 10, 24);
    // Bottom-Left Corner Box
    drawQrBox(ctx, 10, size - 34, 24);

    // Random but seeded hash visual dots
    let seed = 0;
    for (let i = 0; i < text.length; i++) seed += text.charCodeAt(i);

    const gridSize = 14;
    const cellSize = (size - 20) / gridSize;

    for (let r = 0; r < gridSize; r++) {
        for (let c = 0; c < gridSize; c++) {
            if ((r < 4 && c < 4) || (r < 4 && c > gridSize - 5) || (r > gridSize - 5 && c < 4)) continue;
            const pseudo = Math.sin(seed * (r + 1) * (c + 1)) * 10000;
            if (pseudo - Math.floor(pseudo) > 0.5) {
                ctx.fillRect(10 + c * cellSize, 10 + r * cellSize, cellSize - 1, cellSize - 1);
            }
        }
    }
}

function drawQrBox(ctx, x, y, size) {
    ctx.fillRect(x, y, size, size);
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(x + 4, y + 4, size - 8, size - 8);
    ctx.fillStyle = '#0f172a';
    ctx.fillRect(x + 8, y + 8, size - 16, size - 16);
}

// ============================================================================
// INTERACTIVE GOVT HOSPITAL & PHC LOCATOR (LEAFLET GIS)
// ============================================================================
function setupFacilityLocatorEvents() {
    DOM.facilityFilterPills.forEach(pill => {
        pill.addEventListener('click', () => {
            DOM.facilityFilterPills.forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            state.activeFacilityCategory = pill.getAttribute('data-type');
            loadFacilities();
        });
    });

    DOM.chkEmergencyOnly.addEventListener('change', e => {
        state.emergencyOnlyFacilityFilter = e.target.checked;
        loadFacilities();
    });
}

async function loadFacilities() {
    try {
        const payload = {
            latitude: state.patient.latitude,
            longitude: state.patient.longitude,
            facilityType: state.activeFacilityCategory,
            emergencyOnly: state.emergencyOnlyFacilityFilter,
            triageTier: state.currentTriageResult ? state.currentTriageResult.riskLevel : null,
            maxResults: 15
        };

        const res = await fetch('/api/referrals/find-facilities', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        state.facilitiesList = await res.json();
        renderFacilitiesList(state.facilitiesList);
        if (state.map) updateMapMarkers(state.facilitiesList);
    } catch (e) {
        console.error('Failed to load facilities:', e);
        DOM.facilitiesScrollList.innerHTML = '<div class="empty-state-text text-danger">Error retrieving nearby health centers.</div>';
    }
}

function renderFacilitiesList(list) {
    if (!list || list.length === 0) {
        DOM.facilitiesScrollList.innerHTML = '<div class="empty-state-text">No government facilities matching selected filters.</div>';
        DOM.mapStatsText.textContent = '0 Facilities Found';
        return;
    }

    DOM.mapStatsText.textContent = `Showing ${list.length} Facilities near ${state.patient.district}`;

    DOM.facilitiesScrollList.innerHTML = list.map(fac => {
        const typePillClass = fac.facilityType.toLowerCase().includes('phc') ? 'phc' : 
                             (fac.facilityType.toLowerCase().includes('chc') ? 'chc' : 
                             (fac.facilityType.toLowerCase().includes('aiims') ? 'aiims' : 'hospital'));

        return `
            <div class="facility-card" data-id="${fac.id}">
                <div class="facility-top-row">
                    <div class="fac-title-wrap">
                        <h4>${fac.name}</h4>
                        <span class="fac-type-pill ${typePillClass}">${fac.typeDisplayName}</span>
                    </div>
                    <div class="fac-dist-badge">
                        <i class="fa-solid fa-route"></i> ${fac.distanceKm} km (~${fac.estimatedTravelMinutes} min)
                    </div>
                </div>

                <div class="fac-address">
                    <i class="fa-solid fa-location-dot"></i> ${fac.address}, ${fac.district}
                </div>

                <div class="fac-features-row">
                    <span class="fac-feat-chip ${fac.is24x7Emergency ? 'active' : ''}">
                        <i class="fa-solid fa-truck-medical"></i> 24x7 Emergency: ${fac.is24x7Emergency ? 'YES' : 'OPD Only'}
                    </span>
                    <span class="fac-feat-chip ${fac.hasIcu ? 'active' : ''}">
                        <i class="fa-solid fa-bed-pulse"></i> ICU: ${fac.hasIcu ? 'Available' : 'None'}
                    </span>
                    <span class="fac-feat-chip ${fac.hasFreePharmacy ? 'active' : ''}">
                        <i class="fa-solid fa-pills"></i> Jan Aushadhi Free Drugs
                    </span>
                    <span class="fac-feat-chip">
                        <i class="fa-solid fa-bed"></i> Beds: ${fac.availableBeds}/${fac.totalBeds}
                    </span>
                    <span class="fac-feat-chip">
                        <i class="fa-solid fa-clock"></i> Wait: ~${fac.estimatedWaitMinutes}m
                    </span>
                </div>

                <div class="fac-actions-row">
                    <span class="text-dim"><i class="fa-solid fa-phone"></i> ${fac.contactNumber}</span>
                    <button class="btn-fac-referral" onclick="generateOfficialReferralSlip('${fac.id}')">
                        <i class="fa-solid fa-file-signature"></i> Refer to this Facility
                    </button>
                </div>
            </div>
        `;
    }).join('');
}

function initOrRefreshMap() {
    const mapElement = document.getElementById('leaflet-map');
    if (!mapElement) return;

    if (!state.map) {
        state.map = L.map('leaflet-map').setView([state.patient.latitude, state.patient.longitude], 12);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 18,
            attribution: '© OpenStreetMap contributors | SSS Medical Assist'
        }).addTo(state.map);
    } else {
        state.map.setView([state.patient.latitude, state.patient.longitude], 12);
        state.map.invalidateSize();
    }

    // Add Patient Location Marker
    if (state.userLocationMarker) state.map.removeLayer(state.userLocationMarker);
    if (state.userLocationCircle) state.map.removeLayer(state.userLocationCircle);

    state.userLocationMarker = L.marker([state.patient.latitude, state.patient.longitude], {
        icon: L.divIcon({
            className: 'user-pin-icon',
            html: '<div style="background: #3b82f6; width: 18px; height: 18px; border-radius: 50%; border: 3px solid #ffffff; box-shadow: 0 0 15px #3b82f6;"></div>',
            iconSize: [18, 18],
            iconAnchor: [9, 9]
        })
    }).addTo(state.map).bindPopup(`<strong>Your Location</strong><br>${state.patient.district}`);

    state.userLocationCircle = L.circle([state.patient.latitude, state.patient.longitude], {
        color: '#06b6d4',
        fillColor: '#06b6d4',
        fillOpacity: 0.1,
        radius: 3000
    }).addTo(state.map);

    updateMapMarkers(state.facilitiesList);
}

function updateMapMarkers(facilities) {
    if (!state.map) return;

    // Clear existing markers
    state.mapMarkers.forEach(m => state.map.removeLayer(m));
    state.mapMarkers = [];

    facilities.forEach(fac => {
        let pinColor = '#10b981'; // Green for PHC
        if (fac.facilityType === 'CHC') pinColor = '#3b82f6';
        else if (fac.facilityType === 'DISTRICT_HOSPITAL') pinColor = '#8b5cf6';
        else if (fac.facilityType === 'TERTIARY_AIIMS') pinColor = '#f59e0b';

        const customIcon = L.divIcon({
            className: 'fac-map-pin',
            html: `<div style="background: ${pinColor}; color: white; width: 26px; height: 26px; border-radius: 50%; display: flex; align-items: center; justify-content: center; border: 2px solid white; box-shadow: 0 0 10px ${pinColor}; font-size: 11px;">🏥</div>`,
            iconSize: [26, 26],
            iconAnchor: [13, 13]
        });

        const marker = L.marker([fac.latitude, fac.longitude], { icon: customIcon })
            .addTo(state.map)
            .bindPopup(`
                <div style="font-family: inherit; font-size: 12px; color: #0f172a;">
                    <strong style="color: #0369a1;">${fac.name}</strong><br>
                    <span>${fac.typeDisplayName}</span><br>
                    <strong>Distance:</strong> ${fac.distanceKm} km (~${fac.estimatedTravelMinutes} min)<br>
                    <strong>Emergency:</strong> ${fac.is24x7Emergency ? '24x7 Open' : 'OPD'}<br>
                    <strong>Beds Available:</strong> ${fac.availableBeds}/${fac.totalBeds}<br>
                    <strong>Phone:</strong> ${fac.contactNumber}<br><br>
                    <button onclick="generateOfficialReferralSlip('${fac.id}')" style="background: #0284c7; color: white; border: none; padding: 4px 8px; border-radius: 4px; cursor: pointer;">
                        Issue Referral Here
                    </button>
                </div>
            `);

        state.mapMarkers.push(marker);
    });
}

// ============================================================================
// PHC DOCTOR TELE-TRIAGE DESK
// ============================================================================
function setupDoctorDeskEvents() {
    DOM.btnRefreshDoctorFeed.addEventListener('click', () => {
        loadDoctorStats();
        loadDoctorReferralsFeed();
        showToast('Refreshed tele-triage referral feed.', 'info');
    });

    DOM.btnVerifyToken.addEventListener('click', verifyReferralToken);
}

async function loadDoctorStats() {
    try {
        const res = await fetch('/api/dashboard/stats');
        const stats = await res.json();

        DOM.statEmergencyCount.textContent = stats.emergencyTriageCount || 0;
        DOM.statUrgentCount.textContent = stats.highUrgencyCount || 0;
        DOM.statTotalReferrals.textContent = stats.totalReferrals || 0;
        DOM.statNetworkBeds.textContent = `${stats.availableNetworkBeds.toLocaleString()} / ${stats.totalNetworkBeds.toLocaleString()}`;
    } catch (e) {
        console.error('Failed to load doctor stats:', e);
    }
}

async function loadDoctorReferralsFeed() {
    try {
        const res = await fetch('/api/referrals/slips');
        const slips = await res.json();

        if (!slips || slips.length === 0) {
            DOM.doctorReferralsList.innerHTML = '<div class="empty-state-text">No active referrals recorded in this session.</div>';
            return;
        }

        DOM.doctorReferralsList.innerHTML = slips.map(s => {
            const riskClass = s.triageRiskLevel.toLowerCase();
            return `
                <div class="doc-referral-item">
                    <div>
                        <span class="item-severity-pill ${riskClass === 'emergency' ? 'high' : (riskClass === 'high' ? 'med' : 'low')}">
                            ${s.priorityQueueCode}
                        </span>
                        <strong style="margin-left: 8px;">${s.patientName}</strong> (${s.patientAge}Y / ${s.patientGender})
                        <div class="text-dim" style="font-size: 0.75rem; margin-top: 4px;">
                            Token: <code>${s.opdTokenNumber}</code> | Referred To: ${s.destinationFacilityName}
                        </div>
                    </div>
                    <div>
                        <button class="btn-secondary btn-sm" onclick="showSlipDetailsModal('${s.slipId}')">
                            <i class="fa-solid fa-eye"></i> View Slip
                        </button>
                    </div>
                </div>
            `;
        }).join('');
    } catch (e) {
        console.error('Failed to load doctor feed:', e);
    }
}

async function verifyReferralToken() {
    const input = DOM.verifyTokenInput.value.trim().toUpperCase();
    if (!input) {
        showToast('Please enter a Slip ID or OPD Token Number.', 'info');
        return;
    }

    try {
        const res = await fetch('/api/referrals/slips');
        const slips = await res.json();
        const found = slips.find(s => s.slipId.toUpperCase() === input || 
                                      s.opdTokenNumber.toUpperCase() === input || 
                                      s.verificationHash.toUpperCase() === input);

        if (found) {
            DOM.verificationResultBox.style.display = 'block';
            DOM.verificationResultBox.innerHTML = `
                <div style="background: rgba(16, 185, 129, 0.15); border: 1px solid #10b981; border-radius: 8px; padding: 14px; color: #ffffff;">
                    <div style="display: flex; align-items: center; gap: 8px; color: #10b981; font-weight: 800;">
                        <i class="fa-solid fa-circle-check"></i> REFERRAL PASS VALID & AUTHENTIC
                    </div>
                    <div style="margin-top: 8px; font-size: 0.85rem;">
                        <strong>Patient:</strong> ${found.patientName} (${found.patientAge} Yrs, ${found.patientGender})<br>
                        <strong>Triage Tier:</strong> ${found.priorityQueueCode}<br>
                        <strong>OPD Token:</strong> <code>${found.opdTokenNumber}</code><br>
                        <strong>Assigned Facility:</strong> ${found.destinationFacilityName}<br>
                        <strong>Impression:</strong> ${found.primaryImpression}
                    </div>
                </div>
            `;
            showToast('Referral token verified successfully!', 'success');
        } else {
            DOM.verificationResultBox.style.display = 'block';
            DOM.verificationResultBox.innerHTML = `
                <div style="background: rgba(239, 68, 68, 0.15); border: 1px solid #ef4444; border-radius: 8px; padding: 14px; color: #fca5a5;">
                    <i class="fa-solid fa-triangle-exclamation"></i> Token / Slip ID not found in active health database.
                </div>
            `;
            showToast('Invalid or unverified token.', 'error');
        }
    } catch (e) {
        console.error(e);
    }
}

// ============================================================================
// EMERGENCY SOS MODAL & ACTIONS
// ============================================================================
function setupSosModal() {
    DOM.sosTriggerBtn.addEventListener('click', () => {
        DOM.sosModalOverlay.classList.add('open');
        speakVoiceText("Emergency Red Alert Activated. Emergency Helpline numbers and First Aid CPR guidelines are on screen.");
    });

    DOM.sosCloseBtn.addEventListener('click', () => {
        DOM.sosModalOverlay.classList.remove('open');
    });

    DOM.sosModalOverlay.addEventListener('click', e => {
        if (e.target === DOM.sosModalOverlay) {
            DOM.sosModalOverlay.classList.remove('open');
        }
    });
}

// ============================================================================
// VOICE SYNTHESIS (TTS) & THEME TOGGLE
// ============================================================================
function setupThemeAndVoiceControls() {
    DOM.themeToggleBtn.addEventListener('click', () => {
        document.body.classList.toggle('light-theme');
        const isLight = document.body.classList.contains('light-theme');
        DOM.themeToggleBtn.innerHTML = isLight ? '<i class="fa-solid fa-sun"></i>' : '<i class="fa-solid fa-moon"></i>';
    });

    DOM.voiceGuideBtn.addEventListener('click', () => {
        state.voiceGuidanceEnabled = !state.voiceGuidanceEnabled;
        DOM.voiceGuideBtn.innerHTML = state.voiceGuidanceEnabled ? '<i class="fa-solid fa-volume-high"></i>' : '<i class="fa-solid fa-volume-xmark"></i>';
        showToast(state.voiceGuidanceEnabled ? 'Voice audio feedback enabled.' : 'Voice audio feedback muted.', 'info');
    });

    DOM.languageSelect.addEventListener('change', e => {
        state.currentLanguage = e.target.value;
        showToast(`Language switched to ${DOM.languageSelect.options[DOM.languageSelect.selectedIndex].text}`, 'success');
    });
}

function speakVoiceText(text) {
    if (!state.voiceGuidanceEnabled || !('speechSynthesis' in window)) return;
    try {
        window.speechSynthesis.cancel();
        const utterance = new SpeechSynthesisUtterance(text);
        utterance.rate = 1.0;
        utterance.pitch = 1.0;
        window.speechSynthesis.speak(utterance);
    } catch (e) {
        console.warn('TTS speech failed:', e);
    }
}

// ============================================================================
// TOAST NOTIFICATIONS
// ============================================================================
function showToast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    let icon = 'fa-circle-info';
    if (type === 'success') icon = 'fa-circle-check';
    if (type === 'error') icon = 'fa-triangle-exclamation';

    toast.innerHTML = `<i class="fa-solid ${icon}"></i><span>${message}</span>`;
    DOM.toastContainer.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(50px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}
