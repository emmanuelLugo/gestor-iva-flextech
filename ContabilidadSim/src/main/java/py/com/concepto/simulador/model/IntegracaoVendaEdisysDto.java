package py.com.concepto.simulador.model;

import java.math.BigDecimal;
import java.util.Date;

public class IntegracaoVendaEdisysDto {

	private Long codigoVenta;
	private Long codigoEmpresa;
	private Date fecha;
	private Long tipoComprovante;
	private String ruc;
	private Long tipoOperacion;
	private String nroFactura;
	private Long codigoMoneda;
	private Long cotizacion;
	private BigDecimal importeExcenta;
	private BigDecimal importeGrabada5;
	private BigDecimal importeGrabada10;
	private BigDecimal totalGeneral;
	private String cuentaContable;
	private String detalle;
	private BigDecimal montoExenta;
	private BigDecimal montoSinIva;
	private String ivaIncluido;
	private Long nrItemDetalle;
	private Long porcentajeIva;
	private BigDecimal montoIva5;
	private BigDecimal montoIva10;
	private BigDecimal totalIva;
	private String nombreCliente;
	private String nrTimbrado;
	private String nota;
	private Long cantidadCuota;
	private String codigoCentroCosto = "1.00";
	private String estado;
	private Long origen;

	public IntegracaoVendaEdisysDto(){
		  montoIva5= BigDecimal.ZERO;
		  montoIva10 = BigDecimal.ZERO;
		  totalIva = BigDecimal.ZERO;	
		  montoExenta = BigDecimal.ZERO;
		  montoSinIva = BigDecimal.ZERO;
		  importeExcenta = BigDecimal.ZERO;
		  importeGrabada5 = BigDecimal.ZERO;
		  importeGrabada10 = BigDecimal.ZERO;
		  totalGeneral = BigDecimal.ZERO;
	}
	
	public Long getCodigoVenta() {
		return codigoVenta;
	}

	public void setCodigoVenta(Long codigoVenta) {
		this.codigoVenta = codigoVenta;
	}

	public Long getCodigoEmpresa() {
		return codigoEmpresa;
	}

	public void setCodigoEmpresa(Long codigoEmpresa) {
		this.codigoEmpresa = codigoEmpresa;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Long getTipoComprovante() {
		return tipoComprovante;
	}

	public void setTipoComprovante(Long tipoComprovante) {
		this.tipoComprovante = tipoComprovante;
	}

	public String getRuc() {
		return ruc;
	}

	public void setRuc(String ruc) {
		this.ruc = ruc;
	}

	public Long getTipoOperacion() {
		return tipoOperacion;
	}

	public void setTipoOperacion(Long tipoOperacion) {
		this.tipoOperacion = tipoOperacion;
	}

	public String getNroFactura() {
		return nroFactura;
	}

	public void setNroFactura(String nroFactura) {
		this.nroFactura = nroFactura;
	}

	public Long getCodigoMoneda() {
		return codigoMoneda;
	}

	public void setCodigoMoneda(Long codigoMoneda) {
		this.codigoMoneda = codigoMoneda;
	}

	public Long getCotizacion() {
		return cotizacion;
	}

	public void setCotizacion(Long cotizacion) {
		this.cotizacion = cotizacion;
	}

	public BigDecimal getImporteExcenta() {
		return importeExcenta;
	}

	public void setImporteExcenta(BigDecimal importeExcenta) {
		this.importeExcenta = importeExcenta;
	}

	public BigDecimal getImporteGrabada5() {
		return importeGrabada5;
	}

	public void setImporteGrabada5(BigDecimal importeGrabada5) {
		this.importeGrabada5 = importeGrabada5;
	}

	public BigDecimal getImporteGrabada10() {
		return importeGrabada10;
	}

	public void setImporteGrabada10(BigDecimal importeGrabada10) {
		this.importeGrabada10 = importeGrabada10;
	}

	public BigDecimal getTotalGeneral() {
		return totalGeneral;
	}

	public void setTotalGeneral(BigDecimal totalGeneral) {
		this.totalGeneral = totalGeneral;
	}

	public String getCuentaContable() {
		return cuentaContable;
	}

	public void setCuentaContable(String cuentaContable) {
		this.cuentaContable = cuentaContable;
	}

	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}

	public BigDecimal getMontoExenta() {
		return montoExenta;
	}

	public void setMontoExenta(BigDecimal montoExenta) {
		this.montoExenta = montoExenta;
	}

	public BigDecimal getMontoSinIva() {
		return montoSinIva;
	}

	public void setMontoSinIva(BigDecimal montoSinIva) {
		this.montoSinIva = montoSinIva;
	}

	public String getIvaIncluido() {
		return ivaIncluido;
	}

	public void setIvaIncluido(String ivaIncluido) {
		this.ivaIncluido = ivaIncluido;
	}

	public Long getNrItemDetalle() {
		return nrItemDetalle;
	}

	public void setNrItemDetalle(Long nrItemDetalle) {
		this.nrItemDetalle = nrItemDetalle;
	}

	public Long getPorcentajeIva() {
		return porcentajeIva;
	}

	public void setPorcentajeIva(Long porcentajeIva) {
		this.porcentajeIva = porcentajeIva;
	}

	public BigDecimal getMontoIva5() {
		return montoIva5;
	}

	public void setMontoIva5(BigDecimal montoIva5) {
		this.montoIva5 = montoIva5;
	}

	public BigDecimal getMontoIva10() {
		return montoIva10;
	}

	public void setMontoIva10(BigDecimal montoIva10) {
		this.montoIva10 = montoIva10;
	}

	public BigDecimal getTotalIva() {
		return totalIva;
	}

	public void setTotalIva(BigDecimal totalIva) {
		this.totalIva = totalIva;
	}

	public String getNombreCliente() {
		return nombreCliente;
	}

	public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}

	public String getNrTimbrado() {
		return nrTimbrado;
	}

	public void setNrTimbrado(String nrTimbrado) {
		this.nrTimbrado = nrTimbrado;
	}

	public String getNota() {
		return nota;
	}

	public void setNota(String nota) {
		this.nota = nota;
	}

	public Long getCantidadCuota() {
		return cantidadCuota;
	}

	public void setCantidadCuota(Long cantidadCuota) {
		this.cantidadCuota = cantidadCuota;
	}

	public String getCodigoCentroCosto() {
		return codigoCentroCosto;
	}

	public void setCodigoCentroCosto(String codigoCentroCosto) {
		this.codigoCentroCosto = codigoCentroCosto;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getEstado() {
		return estado;
	}

	public Long getOrigen() {
		return origen;
	}

	public void setOrigen(Long origen) {
		this.origen = origen;
	}
	
}
