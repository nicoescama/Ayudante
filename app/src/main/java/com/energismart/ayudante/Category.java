package com.energismart.ayudante;

/**
 * Created by nicoescama on 27/03/2018.
 */

import android.graphics.drawable.Drawable;


public class Category {
    private String lugar;
    private String fecha;
    private String categoryId;
    private String costo;
    private Drawable imagen;
    private String tipo;
    private boolean tieneFoto;
    public Category() {
        super();
    }
    public Category(String categoryId, String lugar, String fecha, String costo, Drawable imagen, String tipo, boolean tieneFoto) {
        super();
        this.lugar = lugar;
        this.fecha = fecha;
        this.costo = costo;
        this.imagen = imagen;
        this.categoryId = categoryId;
        this.tipo=tipo;
        this.tieneFoto=tieneFoto;
    }
    public String getLugar() {
        return lugar;
    }
    public void setLugar(String lugar) {
        this.lugar = lugar;
    }
    public String getFecha() {
        return fecha;
    }
    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
    public String getCosto() {
        return costo;
    }
    public void setCosto(String costo) {
        this.costo = costo;
    }
    public Drawable getImage() {
        return imagen;
    }
    public void setImagen(Drawable imagen) {
        this.imagen = imagen;
    }
    public String getCategoryId(){
        return categoryId;
    }
    public void setCategoryId(String categoryId){
        this.categoryId = categoryId;
    }
    public String getTipo(){
        return tipo;
    }
    public void setTipo(String tipo){
        this.tipo = tipo;
    }
    public void setTieneFoto(boolean tieneFoto){this.tieneFoto=tieneFoto;}
    public boolean getTieneFoto(){return tieneFoto;}
}
