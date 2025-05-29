// Paquete
package pryfinal.modelo;

// Clase Persona
public class Persona {
	// Variables
	private String cedula;
	private String nombre;
	private String apellido;
	private String tipo;
	private String celular;
	private String direccion;
	private String email;

	// Constructor
	public Persona (String cedula, String nombre, String apellido, String tipo, String celular, String direccion, String email) {
		this.cedula = cedula;
		this.nombre = nombre;
		this.apellido = apellido;
		this.celular = celular;
		this.direccion = direccion;
		this.email = email;
	}

	// Setters
	public void setCedula(String cedula) { this.cedula = cedula; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public void setApellido(String apellido) { this.apellido = apellido; }
	public void setCelular(String celular) { this.celular = celular; }
	public void setDireccion (String direccion) { this.direccion = direccion; }
	public void setEmail(String email) { this.email = email; }

	// Getters
	public String getCedula() { return this.cedula; }
	public String getNombre() { return this.nombre; }
	public String getApellido() { return this.apellido; }
	public String getCelular() { return this.celular; }
	public String getDireccion() { return this.direccion; }
	public String getEmail() { return this.email; }
}
