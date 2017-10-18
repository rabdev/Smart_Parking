package hu.bitnet.smartparking.ServerRequests;

/**
 * Created by Attila on 2017.10.13..
 */

public class ServerRequest {

    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String sessionId;
    private String latitude;
    private String longitude;
    private String address;
    private String id;
    //private Profile profile;

    public void setEmail(String email) {
        this.email = email;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public void setPhone(String phone) { this.phone = phone; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public void setLatitude(String latitude) { this.latitude = latitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }
    public void setAddress(String address) { this.address = address; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    //public void setProfile(Profile profile) { this.profile = profile; }

}
