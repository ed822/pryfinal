// Paquete
package pryfinal.modelo;

// Clase Factura
public class Factura {
	// Variables
	private String factura;
	private String fecha;
	private long cedula;
	private String nombre;
	private String descripcion;
	private double subtotal;
	private int iva;
	private double total;
	private String metodo;

	// Constructor para Jackson
	public Factura() {}

	// Constructor
	public Factura(String factura, String fecha, long cedula, String nombre,
			String descripcion, double subtotal, int iva, double total, String metodo) {
		this.factura = factura;
		this.fecha = fecha;
		this.cedula = cedula;
		this.nombre = nombre;
		this.descripcion = descripcion;
		this.subtotal = subtotal;
		this.iva = iva;
		this.total = total;
		this.metodo = metodo;
	}

	// Setters
	public void setFactura(String factura) { this.factura= factura; }
	public void setFecha(String fecha) { this.fecha= fecha; }
	public void setCedula(long cedula) { this.cedula = cedula; }
	public void setNombre(String nombre) { this.nombre= nombre; }
	public void setDescripcion(String descripcion) { this.descripcion= descripcion; }
	public void setSubtotal(double subtotal) { this.subtotal= subtotal; }
	public void setIva(int iva) { this.iva= iva; }
	public void setTotal(double total) { this.total= total; }
	public void setMetodo(String metodo) { this.metodo= metodo; }

	// Getters
	public String getFactura() { return this.factura; }
	public String getFecha() { return this.fecha; }
	public long getCedula() { return this.cedula; }
	public String getNombre() { return this.nombre; }
	public String getDescripcion() { return this.descripcion; }
	public double getSubtotal() { return this.subtotal; }
	public int getIva() { return this.iva; }
	public double getTotal() { return this.total; }
	public String getMetodo() { return this.metodo; }
}
