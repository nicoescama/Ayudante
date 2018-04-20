package com.energismart.ayudante;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nicoescama on 19/02/2018.
 */

public class Truck {

    public String placa;
    public String clase;
    public String linea;
    public String modelo;
    public String cilidraje;
    public String ejesCuantos;
    public String soat;
    public String owner;

    public Map<String, Boolean> stars = new HashMap<>();

    public Truck() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Truck(String placa, String clase, String linea, String modelo, String cilindraje, String ejesCuantos, String soat, String owner) {
        this.placa = placa;
        this.clase = clase;
        this.linea = linea;
        this.modelo = modelo;
        this.cilidraje = cilindraje;
        this.ejesCuantos = ejesCuantos;
        this.soat = soat;
        this.owner = owner;
    }


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("placa", placa);
        result.put("clase", clase);
        result.put("linea", linea);
        result.put("modelo", modelo);
        result.put("cilindraje", cilidraje);
        result.put("ejesCuantos",ejesCuantos);
        result.put("soat",soat);
        result.put("owner",owner);
        return result;
    }
}
