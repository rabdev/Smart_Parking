package hu.bitnet.smartparking.ServerResponses;

import hu.bitnet.smartparking.objects.BLE;
import hu.bitnet.smartparking.objects.History;
import hu.bitnet.smartparking.objects.MQTT;
import hu.bitnet.smartparking.objects.Parking_places;
import hu.bitnet.smartparking.objects.Place;
import hu.bitnet.smartparking.objects.Profile;
import hu.bitnet.smartparking.objects.Sum;

/**
 * Created by Attila on 2017.10.13..
 */

public class ServerResponse {

    private Profile profile;
    private Error error;
    private String alert;
    private Parking_places[] parking_places;
    private Parking_places[] addresses;
    private MQTT mqtt;
    private BLE ble;
    private Sum sum;
    private History[] history;
    private String start;
    private String stop;
    private String time;
    private String price;
    private Place place;
    private String free;
    private String devaddr;

    public Profile getProfile() { return profile; }
    public Error getError() { return error; }
    public String getAlert() { return alert; }
    public Parking_places[] getParking_places() { return parking_places; }
    public Parking_places[] getAddress() { return addresses; }
    public MQTT getMQTT() { return mqtt; }
    public BLE getBLE() { return ble; }
    public Sum getSum() { return sum; }
    public History[] getHistory() { return history; }
    public String getStart() { return start; }
    public String getStop() { return stop; }
    public String getTime() { return time; }
    public String getPrice() { return price; }
    public Place getPlace() { return place; }
    public String getFree() { return free; }
    public String getDevaddr() { return devaddr; }

}
