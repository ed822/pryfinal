package pryfinal.modelo;
public class Persona {
    private String nombre;
    private String apellido;
    private String cedula;
    private String celular;
    private int edad;
    private String direccion;
    
    public Persona (String n, String a, String cd, String cl, int e, String d) {
        this.nombre= n;
        this.apellido= a;
        this.cedula= cd;
        this.celular= cl;
        this.edad= e;
        this.direccion= d;
    }

    public void setNombre(String no) {
        this.nombre= no;
    }
    public String getNombre() {
        return this.nombre;
    }

    public void setApellido(String ap) {
        this.apellido= ap;
    }
    public String getApellido() {
        return this.apellido;
    }

    public void setCedula(String ced) {
        this.cedula= ced;
    }
    public String getCedula() {
        return this.cedula;
    }

    public void setCelular(String cel) {
        this.celular= cel;
    }
    public String getCelular() {
        return this.celular;
    }

    public void setEdad(int ed) {
        this.edad= ed;
    }
    public int getEdad() {
        return this.edad;
    }

    public void setDireccion(String di) {
        this.direccion= di;
    }
    public String getDireccion() {
        return this.direccion;
    }

}