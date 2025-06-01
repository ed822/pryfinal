// Paquete
package pryfinal.modelo;

// Clase Usuario
public class Usuario {
	// Variables
	private String tipo;
	private String nombre;
	private String contrasena;

	// Constructor para Jackson
	public Usuario() {}

	// Constructor
	public Usuario (String tipo, String nombre, String contrasena) {
		this.tipo = tipo;
		this.nombre = nombre;
		this.contrasena = contrasena;
	}

	// Setters
	public void setTipo(String tipo) { this.tipo = tipo; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public void setContrasena(String contrasena) { this.contrasena = contrasena; }

	// Getters
	public String getTipo() { return this.tipo; }
	public String getNombre() { return this.nombre; }
	public String getContrasena() { return this.contrasena; }
}
