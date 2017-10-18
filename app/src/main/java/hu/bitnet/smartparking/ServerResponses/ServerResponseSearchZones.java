package hu.bitnet.smartparking.ServerResponses;

import hu.bitnet.smartparking.objects.Addresses;

/**
 * Created by Attila on 2017.10.18..
 */

public class ServerResponseSearchZones {

    private Addresses[] addresses;
    private Addresses[] address;
    private Error error;
    private String alert;

    public Addresses[] getAddresses() { return addresses; }
    public Addresses[] getAddress() { return address; }
    public Error getError() { return error; }
    public String getAlert() { return alert; }

}
