package hu.bitnet.smartparking.objects;

/**
 * Created by Attila on 2017.08.04..
 */

public class Profile {

    private String firstName;
    private String lastName;
    private String sessionId;
    private String email;
    private String phone;
    private String newsletter;
    private String userId;
    private String password;

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getSessionId() { return sessionId; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    private String getNewsletter() { return newsletter; }
    private String getUserId() { return userId; }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }

}
