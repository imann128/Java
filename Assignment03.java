package com.mycompany.assignment03;

// Imports
import java.util.InputMismatchException;
import java.util.ArrayList;
import java.util.Scanner;
import java.awt.Desktop;
import java.net.URI;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.twilio.Twilio;
import com.twilio.converter.Promoter;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import java.net.URI;
import java.math.BigDecimal;
import com.twilio.rest.verify.v2.service.Verification;
import java.util.Properties;
import javax.lang.model.SourceVersion;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.activation.*;
import io.github.cdimascio.dotenv.Dotenv;

//Interfaces
interface NotificationService {
    void sendNotification(String to, String message);
}

interface Notifiable {
    void sendNotification(String to, String message) throws NotificationException;
}

class NotificationException extends Exception {
    public NotificationException(String message) {
        super(message);
    }
}

  class EmailNotification implements NotificationService {
    
    private static final Dotenv dotenv = Dotenv.load();
      
      
    @Override
    public void sendNotification(String to, String message) {
        sendEmail(to, "Health Monitoring Notification", message);
    }

    public static void sendEmail(String toEmail, String subject, String body) {
    final String username ="GMAIL";
    final String password ="GOOGLE_PASSWORD";

    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.starttls.enable", "true");
    props.put("mail.smtp.host", "smtp.gmail.com");
    props.put("mail.smtp.port", "587");

    Session session = Session.getInstance(props, new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password);
        }
    });

    try {
        jakarta.mail.Message emailMessage = new MimeMessage(session);
        emailMessage.setFrom(new InternetAddress(username));
        emailMessage.setRecipients(jakarta.mail.Message.RecipientType.TO, InternetAddress.parse(toEmail));
        emailMessage.setSubject(subject); 
        emailMessage.setText(body);       
        Transport.send(emailMessage);
        System.out.println("Email sent!");

    } catch (MessagingException e) {
        throw new RuntimeException(e);
    }
}
}

class SMSNotification implements Notifiable {
    
    private static final Dotenv dotenv = Dotenv.load();
    
    public static final String ACCOUNT_SID = "TWILIO_ACCOUNT_SID";  // virtual phone numbers
    public static final String AUTH_TOKEN = "TWILIO_AUTH_TOKEN";

    @Override
    public void sendNotification(String to, String messageBody) throws NotificationException {
        try {
            // Initialize Twilio
            Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

            // Send SMS
             com.twilio.rest.api.v2010.account.Message smsMessage = 
                com.twilio.rest.api.v2010.account.Message.creator(
                new com.twilio.type.PhoneNumber(to),
                new com.twilio.type.PhoneNumber("TWILIO_PHONE_NUMBER"),
                messageBody
            ).create();

            System.out.println("[SMS] Sent to: " + to + " | SID: " + smsMessage.getSid());
        } catch (Exception e) {
            throw new NotificationException("SMS failed: " + e.getMessage());
        }
    }
}

class ReminderService {
    private NotificationService email;
    private Notifiable sms;  // Changed from NotificationService to Notifiable

    public ReminderService(NotificationService email, Notifiable sms) {
        this.email = email;
        this.sms = sms;
    }

    public void sendAppointmentReminder(Patient patient) {
        String msg = "Reminder: You have upcoming appointments!";
        email.sendNotification(patient.getEmail(), msg);
        try {
            sms.sendNotification(patient.getPhone(), msg); 
        } catch (NotificationException e) {
            System.err.println("SMS reminder failed: " + e.getMessage());
        }
    }
}


class PanicButton {
    
    private static final Dotenv dotenv = Dotenv.load();
    
    private NotificationService notifier;
    
    public PanicButton(NotificationService notifier) {
        this.notifier = notifier;
    }

    public void press(Patient patient) {
        notifier.sendNotification(
            "GMAIL",
            "PANIC BUTTON PRESSED by " + patient.getName()
        );
    }
}

class ChatServer {
    public void startChat() {
        System.out.println("[Chat Server] Ready to connect...");
    }
}

class ChatClient {
    public void sendMessage(String message) {
        System.out.println("[Chat Message] " + message);
    }
}

    class VideoCall {
    private Scanner sc = new Scanner(System.in);
    
    public void startCall(String from, String to) {
        try {
            // Generate room name (alphanumeric only)
            String roomName = "health-" + 
                from.replaceAll("[^a-zA-Z0-9]", "").toLowerCase() + "-" + 
                to.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            
            // Add password protection
            System.out.print("Set a password for the call (leave empty for none): ");
            String password = sc.nextLine();
            
            String callUrl = "https://meet.jit.si/" + roomName + 
                (password.isEmpty() ? "" : "?password=" + password);
            
            // Copy link to clipboard
            copyToClipboard(callUrl);
            System.out.println("\n[Link copied to clipboard! Share it with the doctor]");
            
            // Open in browser
            if(Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(callUrl));
            } else {
                System.out.println("Open manually: " + callUrl);
            }
            
        } catch (Exception e) {
            System.out.println("[Error] " + e.getMessage());
        }
    }
    
    private void copyToClipboard(String text) {
        try {
            StringSelection selection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, null);
        } catch (Exception e) {
            System.out.println("[Warning] Couldn't copy link automatically");
        }
    }
}

class VitalSign {
    
    private static final Dotenv dotenv = Dotenv.load();
    
    private int heartRate;
    private int oxygenLevel;
    private String bloodPressure;
    private double temperature;

    public VitalSign(int heartRate, int oxygenLevel, String bloodPressure, double temperature) {
        this.heartRate = heartRate;
        this.oxygenLevel = oxygenLevel;
        this.bloodPressure = bloodPressure;
        this.temperature = temperature;
    }

    // Getters
    public int getHeartRate() { return heartRate; }
    public int getOxygenLevel() { return oxygenLevel; }
    public String getBloodPressure() { return bloodPressure; }
    public double getTemperature() { return temperature; }
    
      @Override
    public String toString(){
        return "Class VitalSign\nThe patient " + 
                "\t" + "has following vital signs\n" + 
                "Heart Rate: " + heartRate + 
                "\nOxygen Level: " + oxygenLevel +
                "\nBlood Pressure: " + bloodPressure + 
                "\nTemperature: " + temperature + "\n\n";
    }
}

class EmergencyAlert {
    private NotificationService notifier;
    private static final int HIGH_HR_THRESHOLD = 120;
    private static final int LOW_HR_THRESHOLD = 50;
    private static final int LOW_OXYGEN_THRESHOLD = 90;

    public EmergencyAlert(NotificationService notifier, Object par1) {
        this.notifier = notifier;
    }
   
    
  
    public void checkVitals(Patient patient) {
        for (VitalSign vital : patient.getVitals()) {
            // Heart rate check
            if (vital.getHeartRate() > HIGH_HR_THRESHOLD) {
                try {
                    triggerAlert(patient,
                            "CRITICAL: High heart rate (" + vital.getHeartRate() + " bpm)");
                } catch (NotificationException ex) {
                    Logger.getLogger(EmergencyAlert.class.getName()).log(Level.SEVERE, null, ex);
                }
            } 
            else if (vital.getHeartRate() < LOW_HR_THRESHOLD) {
                try {
                    triggerAlert(patient,
                            "CRITICAL: Low heart rate (" + vital.getHeartRate() + " bpm)");
                } catch (NotificationException ex) {
                    Logger.getLogger(EmergencyAlert.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            // Oxygen level check
            if (vital.getOxygenLevel() < LOW_OXYGEN_THRESHOLD) {
                try {
                    triggerAlert(patient,
                            "CRITICAL: Low oxygen (" + vital.getOxygenLevel() + "%)");
                } catch (NotificationException ex) {
                    Logger.getLogger(EmergencyAlert.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void triggerAlert(Patient patient, String message) throws NotificationException {
       
       // Alert doctor
        if (patient.getPrimaryDoctor() != null) {
            notifier.sendNotification(
                patient.getPrimaryDoctor().getEmail(),
                "PATIENT ALERT: " + patient.getName() + "\n" + message
            );
        }
        
         
        // Alert emergency contact
        notifier.sendNotification(
            "GMAIL",   
            "EMERGENCY: " + patient.getName() + " (" + patient.getId() + ")\n" + message
        );
    }

    // Manual alert trigger (for panic button)
    public void triggerManualAlert(Patient patient, String reason) throws NotificationException {
        triggerAlert(patient, "MANUAL ALERT: " + reason);
    }

   
}

// ===== User Class =====
// The base class for all users (patients, doctors, and admins). 
// Implements common attributes and methods.
class User {
    private String id;     
    private String name;   
    private String email;  
    
    // Constructor to initialize User attributes
    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Getter methods to access private attributes
    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}

// ===== Patient Class =====
// Handles vital uploads, viewing doctor feedback, and scheduling appointments.
class Patient extends User {
    private ArrayList<VitalSign> vitals = new ArrayList<>();        
    private ArrayList<Appointment> appointments = new ArrayList<>(); 
    private ArrayList<Feedback> feedbacks = new ArrayList<>();      
    private MedicalHistory medicalHistory; 
    private Doctor primaryDoctor;
    private int phoneNumber;
    public String to;
    public String message;

    // Constructor to initialize Patient attributes
    public Patient(String id, String name, String email) {
        super(id, name, email);
        this.medicalHistory = new MedicalHistory(); 
    }

    
    //Patient patient = new Patient("Imann", "iman", "iman@email.com");
 
    // Adds a new vital sign entry for the patient
    public void addVital(VitalSign vital) { vitals.add(vital); }

    // Schedules an appointment for the patient
    public void scheduleAppointment(Appointment appointment) { appointments.add(appointment); }

    // Getters to retrieve patient's vital signs, appointments, and feedback
    public ArrayList<VitalSign> getVitals() { return vitals; }
    public ArrayList<Appointment> getAppointments() { return appointments; }
    public ArrayList<Feedback> getFeedbacks() { return feedbacks; }

    // Displays feedback received from doctors
    public void viewFeedback() {
        if (feedbacks.isEmpty()) {
            System.out.println("[INFO] No feedback available.");
        } else {
            for (Feedback f : feedbacks) System.out.println(f);
        }
    }

    // Adds a medical record entry for the patient
    public void addMedicalRecord(String diagnosis, String treatment, String medication, String dosage, String schedule, String doctor, String consultation) {
        medicalHistory.addDiagnosis(diagnosis);
        medicalHistory.addTreatment(treatment);
        medicalHistory.addPrescription(medication, dosage, schedule, doctor);
        medicalHistory.addPastConsultation(consultation);
    }
    
    
     public Doctor getPrimaryDoctor() {
        return primaryDoctor;
    }

    // Feedback Management
    public void addFeedback(Feedback feedback) {
        feedbacks.add(feedback);
    }

    // Getters
    public int getPhoneNumber() {
        return phoneNumber;
    }

    // Displays the patient's medical history
    public void viewMedicalHistory() {
       System.out.println(medicalHistory); 
    } 
    
    public void handle(Scanner sc, ArrayList<Doctor> doctors, AppointmentManager manager,
                   ReminderService reminderService, EmergencyAlert emergencyAlert,
                   PanicButton panicButton, ChatClient chatClient, VideoCall videoCall) {
    while (true) {
        try {
            System.out.println("\n[Patient Menu]");
            System.out.println("[1] Upload Vital Signs");
            System.out.println("[2] View Doctor Feedback");
            System.out.println("[3] Schedule Appointment");
            System.out.println("[4] View My Appointments");
            System.out.println("[5] View My Vital Signs");
            System.out.println("[6] Panic Button");
            System.out.println("[7] Chat Doctor");
            System.out.println("[8] Video Call Doctor");
            System.out.println("[9] Receive Reminders");
            System.out.println("[10] Emergency Alert");
            System.out.println("[11] Send Email");
            System.out.println("[0] Exit");

            System.out.print("Select an option: ");
            int ch = sc.nextInt(); sc.nextLine();  // Catch invalid integer input here

            switch (ch) {
                case 1:
                    try {
                        System.out.print("Enter Heart Rate: ");
                        int hr = sc.nextInt();

                        System.out.print("Enter Oxygen: ");
                        int ox = sc.nextInt();

                        System.out.print("Enter BP: ");
                        String bp = sc.next();

                        System.out.print("Enter Temp: ");
                        double temp = sc.nextDouble();
                        sc.nextLine();

                        addVital(new VitalSign(hr, ox, bp, temp));
                    } catch (InputMismatchException e) {
                        System.out.println("[Error] Invalid input type. Please enter correct values.");
                        sc.nextLine(); // clear the buffer
                    }
                    break;

                case 2:
                    viewFeedback();
                    break;

                case 3:
                    System.out.println("[Available Doctors]");
                    for (Doctor d : doctors) {
                        System.out.println("- " + d.getName());
                    }

                    System.out.print("Enter your preferred Doctor's Name: ");
                    String docName = sc.nextLine();
                    Doctor d = Assignment03.findDoctorByName(doctors, docName);

                    if (d != null) {
                        System.out.print("Enter your preferred Appointment Date (YYYY-MM-DD): ");
                        String date = sc.nextLine();
                        Appointment appt = new Appointment(date, this, d);
                        manager.requestAppointment(appt);
                        scheduleAppointment(appt);
                        d.addAppointment(appt);
                    } else {
                        System.out.println("[Doctor not found]");
                    }
                    break;

                case 4:
                    for (Appointment a : appointments) {
                        System.out.println("- " + a);
                    }
                    break;

               
                   case 5:
                      System.out.println("\n=== Patient Vital Signs ===");
                      for (VitalSign v : vitals) {
                      System.out.println(v);  // This will use your toString() method
                     }
                     break;

                case 6:
                    Patient patient = new Patient("Imann", "iman", "parrisanaeem67@email.com");
                    panicButton.press(patient);
                    break;

                case 7:
                    System.out.print("Message: ");
                    String msg = sc.nextLine();
                    SMSNotification sms1 = new SMSNotification();
                    sms1.sendNotification("+18777804236", "Test text message");
                    break;

                case 8:
                     System.out.print("Doctor Name: ");
                     String dname = sc.nextLine();
                     videoCall.startCall(getName(), dname); // This will now open Jitsi
                     break;

                case 9:
                    reminderService.sendAppointmentReminder(this);
                    break;

                case 10:
                     Patient patient1 = new Patient("Imann", "iman", "iman@email.com");
                    emergencyAlert.triggerAlert(patient1, "Patient " + getName() + " triggered emergency!");
                    break;
                    
                 case 11:
                     System.out.print("Name: ");
                     String name = sc.nextLine();
                     EmailNotification emailNotifier = new EmailNotification();
                     emailNotifier.sendNotification("parrisanaeem67@gmail.com", "Test email message");
                     break;
                   

                case 0:
                    return;

                default:
                    System.out.println("[Invalid Choice]");
            }
        } catch (InputMismatchException e) {
            System.out.println("[Error] Invalid input. Please enter a valid number.");
            sc.nextLine(); // Clear buffer to avoid infinite loop
        } catch (Exception e) {
            System.out.println("[Unexpected Error] " + e.getMessage());
        }
    }
}

    String getPhone() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}

// ===== Doctor Class =====
// Allows doctors to view patient data, provide feedback, and manage appointments.
class Doctor extends User {
    private String password;  
    private ArrayList<Appointment> doctorAppointments = new ArrayList<>(); 
    private ArrayList<Feedback> feedbacks = new ArrayList<>(); 

    // Constructor to initialize Doctor attributes
    public Doctor(String id, String name, String email, String password) {
        super(id, name, email);
        this.password = password;
    }

    // Authenticates a doctor's login
    public boolean authenticate(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    // Adds an appointment to the doctor's schedule
    public void addAppointment(Appointment appointment) { doctorAppointments.add(appointment); }

    // Displays the doctor's scheduled appointments
    public void viewAppointments() {
        System.out.println("\n[Doctor's Appointments]");
        if (doctorAppointments.isEmpty()) {
            System.out.println("No scheduled appointments.");
        } else {
            for (Appointment apt : doctorAppointments) {
                System.out.println("- " + apt);
            }
        }
    }

    // Adds feedback for a patient
    public void addFeedback(Patient patient, String comment) {
        Feedback feedback = new Feedback(this.getName(), comment);
        patient.getFeedbacks().add(feedback); // Adds feedback to patient's feedback list
        System.out.println("[INFO] Feedback added for " + patient.getName());
    }
    
    public void handle(Scanner sc, ArrayList<Patient> patients) {
    while (true) {
        try {
            System.out.println("\n[Doctor Menu]");
            System.out.println("[1] View Patient List");
            System.out.println("[2] Provide Feedback");
            System.out.println("[3] View Appointments");
            System.out.println("[0] Exit");

            System.out.print("Select: ");
            int ch = sc.nextInt(); sc.nextLine(); // Read user input and handle buffer

            switch (ch) {
                case 1:
                    // Display patient list
                    if (patients.isEmpty()) {
                        System.out.println("[No Patients Available]");
                    } else {
                        for (Patient p : patients) {
                            System.out.println(p.getId() + ": " + p.getName());
                        }
                    }
                    break;

                case 2:
                    // Provide feedback for a patient
                    System.out.print("Enter Patient ID: ");
                    String pid = sc.nextLine();
                    Patient p = Assignment03.findPatientById(patients, pid);
                    if (p != null) {
                        System.out.print("Enter Feedback for your patient: ");
                        String fb = sc.nextLine();
                        addFeedback(p, fb); 
                    } else {
                        System.out.println("[Patient Not Found]");
                    }
                    break;

                case 3:
                    // View appointments
                    viewAppointments();
                    break;

                case 0:
                    return; // Exit the menu

                default:
                    System.out.println("[Invalid Choice]");
            }

        } catch (InputMismatchException e) {
            System.out.println("[Error] Invalid input. Please enter a valid number.");
            sc.nextLine(); // Clear the input buffer to avoid infinite loop
        } catch (Exception e) {
            System.out.println("[Unexpected Error] " + e.getMessage());
        }
    }
}

}

// ===== Appointment Class =====
// Stores appointment details (date, doctor, patient, status).
class Appointment {
    private String date;     
    private Patient patient; 
    private Doctor doctor;   
    private String status;   

    // Constructor to initialize Appointment attributes
    public Appointment(String date, Patient patient, Doctor doctor) {
        this.date = date;
        this.patient = patient;
        this.doctor = doctor;
        this.status = "Pending"; // Default status is Pending
    }

    // Sets the status of an appointment
    public void setStatus(String status) { this.status = status; }

    // Getter methods for patient and doctor
    public Patient getPatient() { return patient; }
    public Doctor getDoctor() { return doctor; }

    // Overrides toString() to display appointment details
    @Override
    public String toString() {
        return String.format("Date: %s | Patient: %s | Doctor: %s | Status: %s", 
                              date, patient.getName(), doctor.getName(), status);
    }
}
// ===== Appointment Manager Class =====
// Manages appointment requests, approvals, rejections, cancellations, and viewing appointments.
class AppointmentManager {
    private ArrayList<Appointment> appointments = new ArrayList<>(); // List of all appointments

    // Constructor to initialize the AppointmentManager with existing appointments
    public AppointmentManager(ArrayList<Appointment> appointments) {
        this.appointments = appointments;
    }  

    // Requests an appointment by adding it to the list
    public void requestAppointment(Appointment appointment) {
        appointments.add(appointment);
        System.out.println("[INFO] Appointment requested successfully.");
    }

    // Approves an appointment by updating its status
    public void approveAppointment(Appointment appointment) {
        appointment.setStatus("Approved");
        System.out.println("[INFO] Appointment approved for " + appointment.getPatient().getName());
    }

    // Rejects an appointment by updating its status
    public void rejectAppointment(Appointment appointment) {
        appointment.setStatus("Rejected");
        System.out.println("[INFO] Appointment rejected for " + appointment.getPatient().getName());
    }

    // Cancels an appointment by removing it from the list
    public void cancelAppointment(Appointment appointment) {
        appointments.remove(appointment);
        System.out.println("[INFO] Appointment canceled for " + appointment.getPatient().getName());
    }

    // Displays all scheduled appointments
    public void viewAppointments() {
        System.out.println("\n=== All Appointments ===");
        if (appointments.isEmpty()) {
            System.out.println("No scheduled appointments.");
        } else {
            for (Appointment appt : appointments) {
                System.out.println("- " + appt);
            }
        }
    }

    // Finds appointments by patient id
    public ArrayList<Appointment> findAppointmentsByPatientID(String patientID) {
        ArrayList<Appointment> foundAppointments = new ArrayList<>();
        for (Appointment appt : appointments) {
            if (appt.getPatient().getId().equals(patientID)) {
                foundAppointments.add(appt);
            }
        }
        return foundAppointments;
    }
    
    public void handle(Scanner sc) {
    while (true) {
        try {
            System.out.println("\n[Appointment Manager]");
            System.out.println("[1] View Appointments");
            System.out.println("[2] Approve/Reject");
            System.out.println("[0] Exit");

            int ch = sc.nextInt(); sc.nextLine(); // Read menu choice
            switch (ch) {
                case 1:
                    viewAppointments(); // Display all appointments
                    break;

                case 2:
                    System.out.print("Enter Patient ID: ");
                    String id = sc.nextLine();
                    ArrayList<Appointment> appts = findAppointmentsByPatientID(id); // Find appointments by patient ID
                    if (appts.isEmpty()) {
                        System.out.println("[No Appointments]");
                    } else {
                        for (int i = 0; i < appts.size(); i++) {
                            System.out.println("[" + (i + 1) + "] " + appts.get(i));
                        }
                        System.out.print("Select Index: ");
                        int i = sc.nextInt() - 1; sc.nextLine(); // Read selected index
                        if (i >= 0 && i < appts.size()) {
                            System.out.print("[1] Approve [2] Reject: ");
                            int d = sc.nextInt(); sc.nextLine(); // Read approve or reject choice
                            if (d == 1) {
                                approveAppointment(appts.get(i)); // Approve the appointment
                            } else if (d == 2) {
                                rejectAppointment(appts.get(i)); // Reject the appointment
                            } else {
                                System.out.println("[Invalid choice]");
                            }
                        } else {
                            System.out.println("[Invalid Index]");
                        }
                    }
                    break;

                case 0:
                    return; // Exit the Appointment Manager

                default:
                    System.out.println("[Invalid Choice]");
            }

        } catch (InputMismatchException e) {
            System.out.println("[Error] Invalid input. Please enter a valid number.");
            sc.nextLine(); // Clear the input buffer to avoid infinite loop
        } catch (Exception e) {
            System.out.println("[Unexpected Error] " + e.getMessage());
        }
    }
}

}
// ===== Administrator Class =====
// The Administrator manages doctors, patients, and system logs.
class Administrator {
    private String adminUsername = "admin";  
    private String adminPassword = "admin123"; 
    private ArrayList<Doctor> doctors;  
    private ArrayList<Patient> patients; 

    // Constructor initializes the administrator with doctor and patient lists
    public Administrator(ArrayList<Doctor> doctors, ArrayList<Patient> patients) {
        this.doctors = doctors;
        this.patients = patients;
    }

    // Authenticates admin login
    public boolean authenticate(String username, String password) {
        return adminUsername.equals(username) && adminPassword.equals(password);
    }

    // Adds a new doctor to the system
    public void addDoctor(String id, String name, String email, String password) {
        doctors.add(new Doctor(id, name, email, password));
        System.out.println("[INFO] Doctor added successfully.");
        logAction("Doctor " + name + " added.");
    }

    // Adds a new patient to the system
    public void addPatient(String id, String name, String email) {
        patients.add(new Patient(id, name, email));
        System.out.println("[INFO] Patient added successfully.");
        logAction("Patient " + name + " added.");
    }

    private ArrayList<String> systemLogs = new ArrayList<>(); // Stores system logs

    // Logs admin actions
    public void logAction(String message) {
        systemLogs.add("[LOG] " + message);
    }

    // Displays system logs
    public void viewLogs() {
        if (systemLogs.isEmpty()) {
            System.out.println("[INFO] No logs available.");
        } else {
            for (String log : systemLogs) System.out.println(log);
        }
    }
    
    public void handle(Scanner sc) {
    while (true) {
        try {
            System.out.println("\n[Admin Menu]");
            System.out.println("[1] Add Patient");
            System.out.println("[2] Add Doctor");
            System.out.println("[3] View Logs");
            System.out.println("[0] Exit");

            int ch = sc.nextInt(); sc.nextLine(); // Read menu choice

            switch (ch) {
                case 1:
                    System.out.print("Enter Patient ID: ");
                    String id = sc.nextLine();
                    System.out.print("Enter Patient Name: ");
                    String name = sc.nextLine();
                    System.out.print("Enter Patient Email: ");
                    String email = sc.nextLine();
                    addPatient(id, name, email); // Add new patient
                    break;

                case 2:
                    System.out.print("Enter Doctor ID: ");
                    String did = sc.nextLine();
                    System.out.print("Enter Doctor Name: ");
                    String dname = sc.nextLine();
                    System.out.print("Enter Doctor Email: ");
                    String demail = sc.nextLine();
                    System.out.print("Enter Doctor Password: ");
                    String dpass = sc.nextLine();
                    addDoctor(did, dname, demail, dpass); // Add new doctor
                    break;

                case 3:
                    viewLogs(); // View the logs
                    break;

                case 0:
                    return; // Exit the Admin Menu

                default:
                    System.out.println("[Invalid Choice]");
            }
        } catch (InputMismatchException e) {
            System.out.println("[Error] Invalid input. Please enter a valid number.");
            sc.nextLine(); // Clear the input buffer to avoid infinite loop
        } catch (Exception e) {
            System.out.println("[Unexpected Error] " + e.getMessage());
        }
    }
}

}



// ===== Vitals Database Class =====
// Manages storage and retrieval of patient vital signs.
class VitalsDatabase {
    private ArrayList<VitalSign> vitals = new ArrayList<>(); 

    // Adds a new vital sign record
    public void addVital(VitalSign vital) { vitals.add(vital); }

    // Displays all recorded vitals
    public void displayVitals() {
        for (VitalSign v : vitals) System.out.println(v);
    }
}

// ===== Feedback Class =====
// Stores doctor feedback on a patient's condition.
class Feedback {
    private String comments;   
    private String doctorName; 

    // Constructor to initialize feedback attributes
    public Feedback(String doctorName, String comments) {
        this.doctorName = doctorName;
        this.comments = comments;
    }

    // Overrides toString() to display feedback details
    @Override
    public String toString() {
        return "Doctor: " + doctorName + " | Feedback: " + comments;
    }
}

// ===== Prescription Class =====
// Stores details about a prescribed medication.
class Prescription {
    private String medication;       
    private String dosage;           
    private String schedule;         
    private String prescribingDoctor; 

    // Constructor to initialize prescription details
    public Prescription(String medication, String dosage, String schedule, String prescribingDoctor) {
        this.medication = medication;
        this.dosage = dosage;
        this.schedule = schedule;
        this.prescribingDoctor = prescribingDoctor;
    }

    // Overrides toString() to display prescription details
    @Override
    public String toString() {
        return medication + " (" + dosage + ", " + schedule + "), Prescribed by: " + prescribingDoctor;
    }
}

// ===== Medical History Class =====
// Stores a patient's medical history, including diagnoses, treatments, and prescriptions.
class MedicalHistory {
    private ArrayList<String> diagnoses;      
    private ArrayList<String> treatments;     
    private ArrayList<Prescription> prescriptions; 
    private ArrayList<String> pastConsultations; 

    // Constructor initializes empty medical history
    public MedicalHistory() {
        this.diagnoses = new ArrayList<>();
        this.treatments = new ArrayList<>();
        this.prescriptions = new ArrayList<>();
        this.pastConsultations = new ArrayList<>();
    }

    // Adds a diagnosis to the medical history
    public void addDiagnosis(String diagnosis) {
        diagnoses.add(diagnosis);
    }

    // Adds a treatment record
    public void addTreatment(String treatment) {
        treatments.add(treatment);
    }

    // Adds a prescription record
    public void addPrescription(String medication, String dosage, String schedule, String doctor) {
        prescriptions.add(new Prescription(medication, dosage, schedule, doctor));
    }

    // Adds a past consultation record
    public void addPastConsultation(String consultation) {
        pastConsultations.add(consultation);
    }

    // Displays the complete medical history
    @Override
    public String toString() {
    return "--- Medical History ---\n" +
           "Diagnoses: " + diagnoses + "\n" +
           "Treatments: " + treatments + "\n" +
           "Prescriptions: " + prescriptions + "\n" +
           "Past Consultations: " + pastConsultations;
}

}


// ===== Main System =====
public class Assignment03 {
    public static void main(String[] args) {
   
        Scanner scanner = new Scanner(System.in);
        ArrayList<Patient> patients = new ArrayList<>();
        ArrayList<Doctor> doctors = new ArrayList<>();
        ArrayList<Appointment> appointments = new ArrayList<>();
        AppointmentManager appointmentManager = new AppointmentManager(appointments);
        Administrator admin = new Administrator(doctors, patients);
        
        
        
        NotificationService emailService = new EmailNotification();
        SMSNotification sms1 = new SMSNotification();
       
        doctors.add(new Doctor("D001", "Dr. Naeem", "naeem@gmail.com", "doc1"));
        doctors.add(new Doctor("D002", "Dr. Qazi", "qazi@gmail.com", "doc2"));

        Patient patient = new Patient("Iman", "Iman@test.com", "123456789");
        patients.add(new Patient("P001","Iman","iman@email.com"));
        
        VitalSign vs = new VitalSign(72, 98, "120/80", 98.6);
        
        ReminderService reminderService = new ReminderService(emailService, sms1);
        EmergencyAlert emergencyAlert = new EmergencyAlert(emailService,patient );
        EmergencyAlert alert2 = new EmergencyAlert(emailService, patient);
        alert2.checkVitals(patient);

        PanicButton panicButton = new PanicButton(emailService);
        ChatServer chatServer = new ChatServer();
        ChatClient chatClient = new ChatClient();
        VideoCall videoCall = new VideoCall();
       

        while (true) {
            try {
                System.out.println("Login");
                System.out.println("\n=== Remote Health Monitoring ===");
                System.out.println("[1] Patient");
                System.out.println("[2] Doctor");
                System.out.println("[3] Admin");
                System.out.println("[4] Appointment Manager");
                System.out.println("[0] Exit");
                System.out.print("Select an option based on your role: ");

                int ch = scanner.nextInt();
                scanner.nextLine(); // consume newline

                switch (ch) {
           
                     case 1:
                        System.out.print("Patient ID: ");
                        String pid = scanner.nextLine();
                        Patient p = findPatientById(patients, pid);
                        if (p != null) {
                            try {
                                p.handle(scanner, doctors, appointmentManager, reminderService,
                                        emergencyAlert, panicButton, chatClient, videoCall);
                            } catch (Exception e) {
                                System.out.println("[Error while handling patient]: " + e.getMessage());
                            }
                        } else {
                            System.out.println("[Patient Not Found]");
                        }
                        break;

                    case 2:
                        System.out.print("Doctor Name: ");
                        String dname = scanner.nextLine();
                        Doctor d = findDoctorByName(doctors, dname);
                        if (d != null) {
                            System.out.print("Password: ");
                            String password = scanner.nextLine();
                            if (d.authenticate(password)) {
                                try {
                                    d.handle(scanner, patients);
                                } catch (Exception e) {
                                    System.out.println("[Error while handling doctor]: " + e.getMessage());
                                }
                            } else {
                                System.out.println("[Wrong Password]");
                            }
                        } else {
                            System.out.println("[Doctor Not Found]");
                        }
                        break;

                    case 3:
                        System.out.print("Username: ");
                        String username = scanner.nextLine();
                        System.out.print("Password: ");
                        String pw = scanner.nextLine();
                        if (admin.authenticate(username, pw)) {
                            try {
                                admin.handle(scanner);
                            } catch (Exception e) {
                                System.out.println("[Admin Error]: " + e.getMessage());
                            }
                        } else {
                            System.out.println("[Invalid Login]");
                        }
                        break;

                    case 4:
                        try {
                            appointmentManager.handle(scanner);
                        } catch (Exception e) {
                            System.out.println("[Appointment Handling Error]: " + e.getMessage());
                        }
                        break;

              
                    default:
                        System.out.println("[Invalid Choice]");
                }

            } catch (InputMismatchException ime) {
                System.out.println("[Invalid input: Enter a number]");
                scanner.nextLine(); // Clear the wrong input
            } catch (Exception e) {
                System.out.println("[Unexpected error]: " + e.getMessage());
            }
        }
    }
    public static Patient findPatientById(ArrayList<Patient> patients, String id) {
        for (Patient p : patients) if (p.getId().equalsIgnoreCase(id)) return p;
        return null;
    }

    public static Doctor findDoctorByName(ArrayList<Doctor> doctors, String name) {
        for (Doctor d : doctors) if (d.getName().equalsIgnoreCase(name)) return d;
        return null;
    }
}


