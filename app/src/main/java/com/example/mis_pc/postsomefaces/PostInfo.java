package com.example.mis_pc.postsomefaces;


public class PostInfo {
    public String _foto;
    public String _ubicacion;
    public String _descripcion;
    public String _usuario;


    public PostInfo(String foto, String ubicacion, String descripcion, String usuario) {
        this._foto = foto;
        this._ubicacion = ubicacion;
        this._descripcion = descripcion;
        this._usuario = usuario;
    }

    public String get_foto() { return this._foto; }
    public String get_ubicacion() {
        return this._ubicacion;
    }
    public String get_descripcion() {
        return this._descripcion;
    }
    public String get_usuario() {
        return this._usuario;
    }

}

