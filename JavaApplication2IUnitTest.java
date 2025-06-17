package javaapplication2;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Method;
import java.util.UUID;

import static org.junit.Assert.*;

public class JavaApplication2Test {

    @Test
    public void testStrongPassword_Valid() throws Exception {
        assertTrue(invokeStrongPassword("Abcdef1@"));
    }

    @Test
    public void testStrongPassword_NoUppercase() throws Exception {
        assertFalse(invokeStrongPassword("abcdef1@"));
    }

    @Test
    public void testStrongPassword_NoDigit() throws Exception {
        assertFalse(invokeStrongPassword("Abcdefgh@"));
    }

    @Test
    public void testStrongPassword_NoSpecialChar() throws Exception {
        assertFalse(invokeStrongPassword("Abcdefg1"));
    }

    @Test
    public void testStrongPassword_TooShort() throws Exception {
        assertFalse(invokeStrongPassword("A1@a"));
    }

    @Test
    public void testMessageCreation() {
        String sender = "+27123456789";
        String recipient = "+27987654321";
        String content = "Hello!";

        JSONObject message = JavaApplication2.createMessage(sender, recipient, content);

        assertEquals(sender, message.get("sender"));
        assertEquals(recipient, message.get("recipient"));
        assertEquals(content, message.get("message"));
        assertNotNull(message.get("id"));
        assertNotNull(message.get("timestamp"));
    }

    @Test
    public void testDeleteMessageById() {
        JSONArray messages = new JSONArray();
        JSONArray deleted = new JSONArray();

        JSONObject msg = JavaApplication2.createMessage("+27123456789", "+27987654321", "Test Message");
        String messageId = (String) msg.get("id");
        messages.add(msg);

        boolean result = JavaApplication2.deleteMessageById(messageId, messages, deleted);

        assertTrue(result);
        assertEquals(0, messages.size());
        assertEquals(1, deleted.size());
        assertEquals(messageId, ((JSONObject) deleted.get(0)).get("id"));
    }

    @Test
    public void testDeleteMessageById_NotFound() {
        JSONArray messages = new JSONArray();
        JSONArray deleted = new JSONArray();

        JSONObject msg = JavaApplication2.createMessage("+27123456789", "+27987654321", "Test Message");
        messages.add(msg);

        boolean result = JavaApplication2.deleteMessageById("non-existent-id", messages, deleted);

        assertFalse(result);
        assertEquals(1, messages.size());
        assertEquals(0, deleted.size());
    }

    // Helper to access private static method using reflection
    private boolean invokeStrongPassword(String password) throws Exception {
        Method method = JavaApplication2.class.getDeclaredMethod("isStrongPassword", String.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(null, password);
    }
}
