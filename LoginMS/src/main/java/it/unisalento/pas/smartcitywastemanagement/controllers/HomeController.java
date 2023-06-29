package it.unisalento.pas.smartcitywastemanagement.controllers;

import it.unisalento.pas.smartcitywastemanagement.configuration.IoTprotocol;
import it.unisalento.pas.smartcitywastemanagement.di.IDBConnection;
import it.unisalento.pas.smartcitywastemanagement.di.MySQLDBConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @Autowired
    IDBConnection mySQLDBConnection;

    @Autowired
    IDBConnection mongoDBConnection;

    @Autowired
    IoTprotocol mqttProtocol;   // se cambio il nome della variabile in tcpProtocol, mi andrà a
                                // usare la classe TCPprotocol invece della MQTTprotocol

    @RequestMapping("/home")
    public String home() {

        mySQLDBConnection.connetti();
        mongoDBConnection.connetti();
        mqttProtocol.initialize();
        return "home";
    }

}
