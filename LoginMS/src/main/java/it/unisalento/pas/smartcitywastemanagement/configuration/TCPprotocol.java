package it.unisalento.pas.smartcitywastemanagement.configuration;

public class TCPprotocol implements IoTprotocol {
    @Override
    public void initialize() {
        System.out.println("Inizializzo protocollo TCP");
    }
}
