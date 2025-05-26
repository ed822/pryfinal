// Paquete
package pryfinal.modelo;

// Imports
import java.time.LocalDate;

/*
Antecedentes: enfermedades previas, cirugías, medicamentos que toma, alergias, vacunas.
Pruebas diagnósticas: Radiografías, análisis de sangre, etc.
Tratamientos: Vacunas, medicamentos, etc.
Procesos médicos: Desparasitación, cirugía, etc.
Plan de tratamiento: medicamentos, dosis, duración del tratamiento, cuidados especiales. 
Evolución del paciente: seguimiento del tratamiento, progreso o regresión de los síntomas. 
*/

// Clase HistoriaClinica
public class HistoriaClinica {
	// Variables
	private String cedula;
	private String nombre;
	private String fecha;
	private String motivo;
	private String diagnostico;
	private String tratamiento;
	private String observaciones;
	private String veterinario;

	// Contructor
	public HistoriaClinica(String cedula, String nombre, String fecha, String motivo,
			String diagnostico, String tratamiento, String observaciones, String veterinario) {
		this.cedula = cedula;
		this.nombre = nombre;
		this.fecha = fecha;
		this.motivo = motivo;
		this.diagnostico = diagnostico;
		this.tratamiento = tratamiento;
		this.veterinario = veterinario;
	}

	// Setters
	public void setCedula(String cedula) { this.cedula = cedula; }
	public void setNombre(String nombre) { this.nombre= nombre; }
	public void setFecha(String fecha) { this.fecha= fecha; }
	public void setMotivo(String motivo) { this.motivo= motivo; }
	public void setDiagnostico(String diagnostico) { this.diagnostico= diagnostico; }
	public void setTratamiento(String tratamiento) { this.tratamiento= tratamiento; }
	public void setVeterinario(String veterinario) { this.veterinario= veterinario; }

	// Getters
	public String getCedula() { return this.cedula; }
	public String getNombre() { return this.nombre; }
	public String getFecha() { return this.fecha; }
	public String getMotivo() { return this.motivo; }
	public String getDiagnostico() { return this.diagnostico; }
	public String getTratamiento() { return this.tratamiento; }
	public String getVeterinario() { return this.veterinario; }
}
