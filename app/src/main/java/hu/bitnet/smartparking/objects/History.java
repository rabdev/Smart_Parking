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
    private String time;
    private String start;
    private String stopped_at;
    private String parking_price;
    private String zone_price;
    private String price;
    private Zone zone;

    public Profile getProfile() { return profile; }
    public BLE getBle() { return ble; }
    public MQTT getMQTT() { return mqtt; }
    public Parking_places getParking_places() { return parking_places; }
    public Sum getSum() { return sum; }
    public Place getPlace() { return place; }
    public String getZoneId() { return zone_id; }
    public String getTime() { return time; }
    public String getStart() { return start; }
    public String getStoppedAt() { return stopped_at; }
    public String getParkingPrice() { return parking_price; }
    public String getZonePrice() { return zone_price; }
    public String getPrice() { return price; }
    public Zone getZone() { return zone; }

}
