// Paquetes
package pryfinal.modelo;

//cedulaDueño, historiaClinica guardaran un codigo unico que servira de clave primaria para representar a una entidad con mas informacion
//Esto se hara con el objetivo de no sobrecargar esta clase de informacion y tener todo mucho mas ordenado
//estadoActual no se si usarlo tambien de clave primaria para un reporte mas detallado o si solo sea unos parrafos de texto que digan el estado de la mascota

// Clase Mascota
public class Mascota {
	// Variables
	private String cedula;
	private String nombre;
	private String especie;
	private int edad;
	private boolean sexo;
	private String raza;
	private int peso;

	// Contructor
	public Mascota(String cedula, String nombre, String especie, int edad, boolean sexo, String raza, int peso) {
		this.cedula = cedula;
		this.nombre = nombre;
		this.especie = especie;
		this.edad = edad;
		this.sexo = sexo;
		this.raza = raza;
		this.peso = peso;
	}

	// Setters
	public void setCedula(String cedula) { this.cedula = cedula; }
	public void setNombre(String nombre) { this.nombre= nombre; }
	public void setEspecie(String especie) { this.especie= especie; }
	public void setEdad(int edad) { this.edad= edad; }
	public void setSexo(boolean sexo) { this.sexo= sexo; }
	public void setRaza(String raza) { this.raza= raza; }
	public void setPeso(int peso) { this.peso= peso; }

	// Getters
	public String getCedula() { return this.cedula; }
	public String getNombre() { return this.nombre; }
	public String getEspecie() { return this.especie; }
	public int getEdad() { return this.edad; }
	public boolean getSexo() { return this.sexo; }
	public String getRaza() { return this.raza; }
	public int getPeso() { return this.peso; }
}
