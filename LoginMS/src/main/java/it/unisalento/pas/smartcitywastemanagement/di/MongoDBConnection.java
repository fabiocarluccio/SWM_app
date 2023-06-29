package it.unisalento.pas.smartcitywastemanagement.di;

import org.springframework.stereotype.Component;

@Component
public class MongoDBConnection implements IDBConnection {

    @Override
    public void connetti() {
        System.out.println("Connesso al db Mongo....");
    }
}
