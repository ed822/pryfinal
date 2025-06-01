// Paquete
package pryfinal.modelo;

// Clase Persona
public class Persona {
	// Variables
	private long cedula;
	private String nombre;
	private String apellido;
	private String tipo;
	private long celular;
	private String direccion;
	private String email;

	// Constructor para Jackson
	public Persona() {}

	// Constructor
	public Persona (long cedula, String nombre, String apellido, String tipo, long celular, String direccion, String email) {
		this.cedula = cedula;
		this.nombre = nombre;
		this.apellido = apellido;
		this.tipo = tipo;
		this.celular = celular;
		this.direccion = direccion;
		this.email = email;
	}

	// Setters
	public void setCedula(long cedula) { this.cedula = cedula; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public void setApellido(String apellido) { this.apellido = apellido; }
	public void setTipo(String tipo) { this.tipo = tipo; }
	public void setCelular(long celular) { this.celular = celular; }
	public void setDireccion (String direccion) { this.direccion = direccion; }
	public void setEmail(String email) { this.email = email; }

	// Getters
	public long getCedula() { return this.cedula; }
	public String getNombre() { return this.nombre; }
	public String getApellido() { return this.apellido; }
	public String getTipo() { return this.tipo; }
	public long getCelular() { return this.celular; }
	public String getDireccion() { return this.direccion; }
	public String getEmail() { return this.email; }
}
