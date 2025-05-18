package gid.modelo;

public class Mascota {
    //cedulaDueño, historiaClinica guardaran un codigo unico que servira de clave primaria para representar a una entidad con mas informacion
    //Esto se hara con el objetivo de no sobrecargar esta clase de informacion y tener todo mucho mas ordenado
    //estadoActual no se si usarlo tambien de clave primaria para un reporte mas detallado o si solo sea unos parrafos de texto que digan el estado de la mascota
    private String nombre;
    private String especie;
    private String raza;
    private int edad;
    private int peso;
    private int altura;
    private boolean sexo;
    private String cedulaDueño;
    private String historiaClinica;
    private String estadoActual;

    public Mascota(String n, String es, String r, int ed, int p, int a, boolean s, String cd, String hc, String ea) {
        this.nombre= n;
        this.especie= es;
        this.raza= r;
        this.edad= ed;
        this.peso= p;
        this.altura= a;
        this.sexo= s;
        this.cedulaDueño= cd;
        this.historiaClinica= hc;
        this.estadoActual= ea;
    }

    public void setNombre(String no) {
        this.nombre= no;
    }
    public String getNombre() {
        return this.nombre;
    }

    public void setEspecie(String esp) {
        this.especie= esp;
    }
    public String getEspecie() {
        return this.especie;
    }

    public void setRaza(String ra) {
        this.raza= ra;
    }
    public String getRaza() {
        return this.raza;
    }

    public void setEdad(int ed) {
        this.edad= ed;
    }
    public int getEdad() {
        return this.edad;
    }
    
    public void setPeso(int pe) {
        this.peso= pe;
    }
    public int getPeso() {
        return this.peso;
    }

    public void setAltura(int al) {
        this.altura= al;
    }
    public int getAltura() {
        return this.altura;
    }

    public void setSexo(boolean se) {
        this.sexo= se;
    }
    public boolean getSexo() {
        return this.sexo;
    }

    public void setCedula(String ced) {
        this.cedulaDueño= ced;
    }
    public String getCedula() {
        return this.cedulaDueño;
    }

    public void setHistoria(String his) {
        this.historiaClinica= his;
    }
    public String getHistoria() {
        return this.historiaClinica;
    }

    public void setEstado(String est) {
        this.estadoActual= est;
    }
    public String getEstado() {
        return this.estadoActual;
    }
}