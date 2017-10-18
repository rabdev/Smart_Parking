package hu.bitnet.smartparking.objects;

/**
 * Created by Attila on 2017.08.04..
 */

public class History {

    private Profile profile;
    private BLE ble;
    private MQTT mqtt;
    private Parking_places parking_places;
    private Sum sum;
    private Place place;
    private String zone_id;
    private String elapsed_time;
    private String started_at;
    private String stopped_at;
    private String parking_price;
    private String zone_price;

    public Profile getProfile() { return profile; }
    public BLE getBle() { return ble; }
    public MQTT getMQTT() { return mqtt; }
    public Parking_places getParking_places() { return parking_places; }
    public Sum getSum() { return sum; }
    public Place getPlace() { return place; }
    public String getZoneId() { return zone_id; }
    public String getElapsedTime() { return elapsed_time; }
    public String getStartedAt() { return started_at; }
    public String getStoppedAt() { return stopped_at; }
    public String getParkingPrice() { return parking_price; }
    public String getZonePrice() { return zone_price; }

}
