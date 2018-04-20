package com.energismart.ayudante;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nicoescama on 25/02/2018.
 */

public class Trip {
    public String placa;
    public String origen;
    public String destino;
    public String puntoCargue;
    public String puntoDescargue;
    public String anticipo;
    public String fecha;
    public String hora;
    public String driver;
    public String truck;
    public String gastos;

    public Map<String, Boolean> stars = new HashMap<>();

    public Trip() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public String getPlaca() {
        return placa;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public String getPuntoCargue() {
        return puntoCargue;
    }

    public String getPuntoDescargue() {
        return puntoDescargue;
    }

    public String getAnticipo() {
        return anticipo;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }

    public String getDriver() {
        return driver;
    }

    public String getTruck() {
        return truck;
    }

    public String getGastos() {
        return gastos;
    }

    public Trip(String placa, String origen, String puntoCargue, String destino, String puntoDescargue, String anticipo, String fecha, String hora, String driver, String truck) {
        this.placa = placa;
        this.origen = origen;
        this.puntoCargue = puntoCargue;
        this.destino = destino;
        this.puntoDescargue = puntoDescargue;
        this.anticipo = anticipo;
        this.fecha = fecha;
        this.hora = hora;
        this.driver = driver;
        this.truck = truck;
        this.gastos = "0";
    }

    public Trip(String placa, String origen, String puntoCargue, String destino, String puntoDescargue, String anticipo, String fecha, String hora, String driver, String truck, String gastos) {
        this.placa = placa;
        this.origen = origen;
        this.puntoCargue = puntoCargue;
        this.destino = destino;
        this.puntoDescargue = puntoDescargue;
        this.anticipo = anticipo;
        this.fecha = fecha;
        this.hora = hora;
        this.driver = driver;
        this.truck = truck;
        this.gastos = gastos;
    }

    public Trip(Trip value) {
    }


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("placa", placa);
        result.put("origen", origen);
        result.put("puntoCargue", puntoCargue);
        result.put("destino", destino);
        result.put("puntoDescargue", puntoDescargue);
        result.put("anticipo",anticipo);
        result.put("fecha",fecha);
        result.put("hora",hora);
        result.put("driver",driver);
        result.put("truck",truck);
        result.put("gastos",gastos);
        return result;
    }
}
