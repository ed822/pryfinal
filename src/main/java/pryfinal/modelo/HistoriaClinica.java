// Paquete
package pryfinal.modelo;

// Clase HistoriaClinica
public class HistoriaClinica {
	// Variables
	private long cedula;
	private String nombre;
	private String fecha;
	private String motivo;
	private String diagnostico;
	private String tratamiento;
	private String observaciones;
	private String veterinario;

	// Constructor para Jackson
	public HistoriaClinica() {}

	// Constructor
	public HistoriaClinica(long cedula, String nombre, String fecha, String motivo,
			String diagnostico, String tratamiento, String observaciones, String veterinario) {
		this.cedula = cedula;
		this.nombre = nombre;
		this.fecha = fecha;
		this.motivo = motivo;
		this.diagnostico = diagnostico;
		this.tratamiento = tratamiento;
		this.observaciones = observaciones;
		this.veterinario = veterinario;
	}

	// Setters
	public void setCedula(long cedula) { this.cedula = cedula; }
	public void setNombre(String nombre) { this.nombre= nombre; }
	public void setFecha(String fecha) { this.fecha= fecha; }
	public void setMotivo(String motivo) { this.motivo= motivo; }
	public void setDiagnostico(String diagnostico) { this.diagnostico= diagnostico; }
	public void setTratamiento(String tratamiento) { this.tratamiento= tratamiento; }
	public void setObservaciones(String observaciones) { this.observaciones= observaciones; }
	public void setVeterinario(String veterinario) { this.veterinario= veterinario; }

	// Getters
	public long getCedula() { return this.cedula; }
	public String getNombre() { return this.nombre; }
	public String getFecha() { return this.fecha; }
	public String getMotivo() { return this.motivo; }
	public String getDiagnostico() { return this.diagnostico; }
	public String getTratamiento() { return this.tratamiento; }
	public String getObservaciones() { return this.observaciones; }
	public String getVeterinario() { return this.veterinario; }
}
