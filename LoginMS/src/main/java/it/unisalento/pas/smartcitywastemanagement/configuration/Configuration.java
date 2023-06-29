package it.unisalento.pas.smartcitywastemanagement.configuration;

import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    public IoTprotocol mqttProtocol() {
        return new MQTTprotocol();
    }

    @Bean
    public IoTprotocol tcpProtocol() {
        return new TCPprotocol();
    }
}
