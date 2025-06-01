// Paquete
package pryfinal.modelo;

// Clase OrdenMedica
public class OrdenMedica {
	// Variables
	private String numero;
	private String fecha;
	private long cedula;
	private String nombre;
	private String veterinario;
	private String dosis;
	private String intrucciones;
	private String duracion;
	private String notas;

	// Constructor para Jackson
	public OrdenMedica() {}

	// Constructor
	public OrdenMedica(String numero, String fecha, long cedula, String nombre,
			String veterinario, String dosis, String intrucciones, String duracion, String notas) {
		this.numero = numero;
		this.fecha = fecha;
		this.cedula = cedula;
		this.nombre = nombre;
		this.veterinario = veterinario;
		this.dosis = dosis;
		this.intrucciones = intrucciones;
		this.duracion = duracion;
		this.notas = notas;
	}

	// Setters
	public void setNumero(String numero) { this.numero = numero; }
	public void setFecha(String fecha) { this.fecha = fecha; }
	public void setCedula(long cedula) { this.cedula = cedula; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public void setVeterinario(String veterinario) { this.veterinario = veterinario; }
	public void setDosis(String dosis) { this.dosis = dosis; }
	public void setIntrucciones(String intrucciones) { this.intrucciones = intrucciones; }
	public void setDuracion(String duracion) { this.duracion = duracion; }
	public void setNotas(String notas) { this.notas = notas; }

	// Getters
	public String getNumero() { return this.numero; }
	public String getFecha() { return this.fecha; }
	public long getCedula() { return this.cedula; }
	public String getNombre() { return this.nombre; }
	public String getVeterinario() { return this.veterinario; }
	public String getDosis() { return this.dosis; }
	public String getIntrucciones() { return this.intrucciones; }
	public String getDuracion() { return this.duracion; }
	public String getNotas() { return this.notas; }
}
