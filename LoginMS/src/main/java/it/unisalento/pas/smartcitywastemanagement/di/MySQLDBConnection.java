package it.unisalento.pas.smartcitywastemanagement.di;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component // Se metto il @Primary, indipendentemente da come ho chiamato la variabile, mi prendera sempre questa classe invece dela MongoDBConnection
public class MySQLDBConnection implements IDBConnection {

    public void connetti() {
        System.out.println("Connesso al database MySQL");
    }

}
