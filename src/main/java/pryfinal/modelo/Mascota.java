// Paquete
package pryfinal.modelo;

// Clase Mascota
public class Mascota {
	// Variables
	private String cedulaDueno;
	private String nombreMascota;
	private String especie;
	private float edad;
	private String sexo;
	private String raza;
	private int peso;

	// Constructor para Jackson
	public Mascota() {}

	// Constructor
	public Mascota(String cedulaDueno, String nombreMascota, String especie, float edad, String sexo, String raza, int peso) {
		this.cedulaDueno = cedulaDueno;
		this.nombreMascota = nombreMascota;
		this.especie = especie;
		this.edad = edad;
		this.sexo = sexo;
		this.raza = raza;
		this.peso = peso;
	}

	// Getters
	public String getCedulaDueno() { return cedulaDueno; }
	public String getNombreMascota() { return nombreMascota; }
	public String getEspecie() { return especie; }
	public float getEdad() { return edad; }
	public String getSexo() { return sexo; }
	public String getRaza() { return raza; }
	public int getPeso() { return peso; }

	// Setters
	public void setCedulaDueno(String cedulaDueno) { this.cedulaDueno = cedulaDueno; }
	public void setNombreMascota(String nombreMascota) { this.nombreMascota = nombreMascota; }
	public void setEspecie(String especie) { this.especie = especie; }
	public void setEdad(float edad) { this.edad = edad; }
	public void setSexo(String sexo) { this.sexo = sexo; }
	public void setRaza(String raza) { this.raza = raza; }
	public void setPeso(int peso) { this.peso = peso; }
}
