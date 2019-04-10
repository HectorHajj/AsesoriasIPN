package com.example.tutor40;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

public class Mensajes {

    public String UserID;

    public String Nombre;

    public String Mensaje;

    public Date Fecha;
}

class SortByDate implements Comparator<Mensajes> {

    @Override
    public int compare(Mensajes o1, Mensajes o2) {
        return o1.Fecha.compareTo(o2.Fecha);
    }
}