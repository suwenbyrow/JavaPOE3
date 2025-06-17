/**
 * Author: Suwen Byrow
 * Date: 2025-06-16
 * Description: A simple Java messaging application that allows user registration,
 * sending, viewing, and deleting messages stored in JSON format.
 */
package javaapplication2;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.*;
import java.util.*;

public class JavaApplication2 {

    private static final String MESSAGE_FILE = "messages.json";
    private static final String DELETED_MESSAGE_FILE = "deleted_messages.json";
    private static final Scanner scanner = new Scanner(System.in);
    private static JSONArray messagesArray = new JSONArray();
    private static JSONArray deletedMessagesArray = new JSONArray();
    private static String lastRecipient = null;

    public static void main(String[] args) {
        String savedName;
        String savedSurname;
        String savedUsername;
        String savedPassword;
        String savedPhone;

        // ---------- USER REGISTRATION ----------
        System.out.print("Enter your name (optional): ");
        savedName = scanner.nextLine();

        System.out.print("Enter your surname (optional): ");
        savedSurname = scanner.nextLine();

        while (true) {
            System.out.print("Enter username (must contain underscore and max 5 characters): ");
            savedUsername = scanner.nextLine();
            if (savedUsername.contains("_") && savedUsername.length() <= 5) {
                System.out.println("Username successfully captured.");
                break;
            }
            System.out.println("Username is not correctly formatted.");
        }

        while (true) {
            System.out.print("Enter password (min 8 chars, 1 capital letter, 1 number, 1 special char): ");
            savedPassword = scanner.nextLine();
            if (isStrongPassword(savedPassword)) {
                System.out.println("Password successfully captured.");
                break;
            }
            System.out.println("Password is incorrectly formatted.");
        }

        while (true) {
            System.out.print("Enter phone (+27 followed by 9 digits): ");
            savedPhone = scanner.nextLine();
            if (savedPhone.matches("\\+27\\d{9}")) {
                break;
            }
            System.out.println("Invalid phone number.");
        }

        // ---------- CONFIRMATION ----------
        while (true) {
            System.out.println("\nPlease re-enter your credentials to confirm:");
            System.out.print("Username: ");
            if (!scanner.nextLine().equals(savedUsername)) continue;
            System.out.print("Phone: ");
            if (!scanner.nextLine().equals(savedPhone)) continue;
            System.out.print("Password: ");
            if (!scanner.nextLine().equals(savedPassword)) continue;
            // No need to confirm name and surname
            break;
        }

        // ---------- LOAD STORED DATA ----------
        loadMessages();
        loadDeletedMessages();

        // ---------- MAIN MENU LOOP ----------
        while (true) {
            System.out.println("\n1. Send Message\n2. Show Messages\n3. View Deleted Messages\n4. Reports\n5. Quit\n6. Delete Message by ID");
            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice.");
                continue;
            }

            switch (choice) {
                case 1 -> sendMessage(savedPhone);
                case 2 -> showMessages(savedPhone);
                case 3 -> viewDeletedMessages();
                case 4 -> showReports();
                case 5 -> {
                    System.out.println("Exiting. Goodbye!");
                    return;
                }
                case 6 -> deleteMessageById();
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // ---------- UTILITY METHOD FOR PASSWORD VALIDATION ----------
    private static boolean isStrongPassword(String password) {
        if (password.length() < 8) return false;
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[@#$%*&?/\\\\{}()><.,:].*");
        return hasUpper && hasNumber && hasSpecial;
    }

    // ---------- SEND MESSAGE ----------
    private static void sendMessage(String userPhone) {
        System.out.print("Enter recipient number (+27XXXXXXXXX): ");
        String recipient = scanner.nextLine();
        if (!recipient.matches("\\+27\\d{9}")) {
            System.out.println("Invalid recipient phone number.");
            return;
        }
        lastRecipient = recipient;

        System.out.print("Enter your message (max 250 characters): ");
        String message = scanner.nextLine();
        if (message.length() > 250) {
            System.out.println("Message too long.");
            return;
        }

        JSONObject msg = new JSONObject();
        String messageID = UUID.randomUUID().toString();
        msg.put("id", messageID);
        msg.put("sender", userPhone);
        msg.put("recipient", recipient);
        msg.put("message", message);
        msg.put("timestamp", new Date().toString());

        messagesArray.add(msg);
        saveMessages();

        System.out.println("Message sent!");
        postMessageOptions(userPhone);
    }

    // ---------- DELETE MESSAGE BY ID ----------
    private static void deleteMessageById() {
        System.out.print("Enter Message ID to delete: ");
        String id = scanner.nextLine();
        Iterator<Object> iterator = messagesArray.iterator();
        boolean found = false;

        while (iterator.hasNext()) {
            JSONObject msg = (JSONObject) iterator.next();
            if (msg.get("id").equals(id)) {
                deletedMessagesArray.add(msg);
                iterator.remove();
                System.out.println("Message deleted.");
                found = true;
                break;
            }
        }

        if (!found) {
            System.out.println("Message ID not found.");
        }

        saveMessages();
        saveDeletedMessages();
    }

    // ---------- DISPLAY MESSAGES ----------
    private static void showMessages(String userPhone) {
        System.out.println("All messages sent or received:");
        for (Object obj : messagesArray) {
            JSONObject msg = (JSONObject) obj;
            String sender = (String) msg.get("sender");
            String recipient = (String) msg.get("recipient");

            if (sender.equals(userPhone) || recipient.equals(userPhone)) {
                System.out.println("ID: " + msg.get("id"));
                System.out.println("From: " + sender);
                System.out.println("To: " + recipient);
                System.out.println("Message: " + msg.get("message"));
                System.out.println("Time: " + msg.get("timestamp"));
                System.out.println("---------------------------");
            }
        }
    }

    // ---------- VIEW DELETED MESSAGES ----------
    private static void viewDeletedMessages() {
        if (deletedMessagesArray.isEmpty()) {
            System.out.println("No deleted messages.");
            return;
        }

        System.out.println("Deleted Messages:");
        for (Object obj : deletedMessagesArray) {
            JSONObject msg = (JSONObject) obj;
            System.out.println("ID: " + msg.get("id"));
            System.out.println("From: " + msg.get("sender"));
            System.out.println("To: " + msg.get("recipient"));
            System.out.println("Message: " + msg.get("message"));
            System.out.println("Time: " + msg.get("timestamp"));
            System.out.println("---------------------------");
        }
    }

    // ---------- REPORT OPTIONS ----------
    private static void showReports() {
        while (true) {
            System.out.println("\nReport Options:");
            System.out.println("a. Display sender and recipient of all messages");
            System.out.println("b. Display longest message");
            System.out.println("c. Search by ID");
            System.out.println("d. Search by recipient");
            System.out.println("e. Delete message by ID");
            System.out.println("f. Full message report");
            System.out.println("x. Exit reports");

            String option = scanner.nextLine();
            switch (option) {
                case "a" -> messagesArray.forEach(obj -> {
                    JSONObject msg = (JSONObject) obj;
                    System.out.println("From: " + msg.get("sender") + ", To: " + msg.get("recipient"));
                });
                case "b" -> {
                    JSONObject longest = null;
                    for (Object obj : messagesArray) {
                        JSONObject msg = (JSONObject) obj;
                        if (longest == null || ((String) msg.get("message")).length() > ((String) longest.get("message")).length()) {
                            longest = msg;
                        }
                    }
                    if (longest != null) {
                        System.out.println("Longest message: " + longest.get("message"));
                    }
                }
                case "c" -> {
                    System.out.print("Enter Message ID: ");
                    String id = scanner.nextLine();
                    boolean found = false;
                    for (Object obj : messagesArray) {
                        JSONObject msg = (JSONObject) obj;
                        if (msg.get("id").equals(id)) {
                            System.out.println("To: " + msg.get("recipient"));
                            System.out.println("Message: " + msg.get("message"));
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        System.out.println("ID not found.");
                    }
                }
                case "d" -> {
                    System.out.print("Enter recipient phone: ");
                    String rec = scanner.nextLine();
                    boolean foundAny = false;
                    for (Object obj : messagesArray) {
                        JSONObject msg = (JSONObject) obj;
                        if (msg.get("recipient").equals(rec)) {
                            System.out.println("Message: " + msg.get("message"));
                            foundAny = true;
                        }
                    }
                    if (!foundAny) {
                        System.out.println("No messages for recipient " + rec);
                    }
                }
                case "e" -> deleteMessageById();
                case "f" -> messagesArray.forEach(obj -> {
                    JSONObject msg = (JSONObject) obj;
                    System.out.println("ID: " + msg.get("id"));
                    System.out.println("From: " + msg.get("sender"));
                    System.out.println("To: " + msg.get("recipient"));
                    System.out.println("Message: " + msg.get("message"));
                    System.out.println("Time: " + msg.get("timestamp"));
                    System.out.println("---------------------------");
                });
                case "x" -> {
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    // ---------- POST MESSAGE OPTIONS ----------
    private static void postMessageOptions(String userPhone) {
        System.out.println("\n1. Continue chat\n2. View messages\n3. Delete all messages");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid choice.");
            return;
        }

        switch (choice) {
            case 1 -> sendMessage(userPhone);
            case 2 -> showMessages(userPhone);
            case 3 -> {
                deletedMessagesArray.addAll(messagesArray);
                messagesArray.clear();
                saveMessages();
                saveDeletedMessages();
                System.out.println("All messages deleted.");
            }
            default -> System.out.println("Invalid choice.");
        }
    }

    // ---------- SAVE TO FILE ----------
    private static void saveMessages() {
        try (FileWriter writer = new FileWriter(MESSAGE_FILE)) {
            writer.write(messagesArray.toJSONString());
        } catch (IOException e) {
            System.out.println("Error saving messages: " + e.getMessage());
        }
    }

    private static void saveDeletedMessages() {
        try (FileWriter writer = new FileWriter(DELETED_MESSAGE_FILE)) {
            writer.write(deletedMessagesArray.toJSONString());
        } catch (IOException e) {
            System.out.println("Error saving deleted messages: " + e.getMessage());
        }
    }

    // ---------- LOAD FROM FILE ----------
    private static void loadMessages() {
        File file = new File(MESSAGE_FILE);
        if (!file.exists()) return;
        try (FileReader reader = new FileReader(file)) {
            JSONParser parser = new JSONParser();
            messagesArray = (JSONArray) parser.parse(reader);
        } catch (Exception e) {
            System.out.println("Error loading messages: " + e.getMessage());
        }
    }

    private static void loadDeletedMessages() {
        File file = new File(DELETED_MESSAGE_FILE);
        if (!file.exists()) return;
        try (FileReader reader = new FileReader(file)) {
            JSONParser parser = new JSONParser();
            deletedMessagesArray = (JSONArray) parser.parse(reader);
        } catch (Exception e) {
            System.out.println("Error loading deleted messages: " + e.getMessage());
        }
    }
}
