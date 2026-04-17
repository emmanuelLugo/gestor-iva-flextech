package py.com.concepto.model.entity.dto;

import java.math.BigDecimal;
import java.util.Date;

public class IntegracaoVendaHechaukaDto {

	//	tipoRegistro CÓDIGO TIPO DE REGISTRO 
	//	codigoIdentificacion CÓDIGO TIPO DE IDENTIFICACIÓN DEL COMPRADOR
	//	ruc NÚMERO DE IDENTIFICACIÓN DEL COMPRADOR
	//	nombreCliente NOMBRE O RAZÓN SOCIAL DEL COMPRADOR A
	//	codigoComprobante CÓDIGO TIPO DE COMPROBANTE
	//	fecha FECHA DE EMISIÓN DEL COMPROBANTE 
	//	nrTimbrado NÚMERO DE TIMBRADO
	//	nroFactura NÚMERO DEL COMPROBANTE
	//	montoIva10 MONTO GRAVADO AL 10% (IVA INCLUIDO)
	//	montoIva5 MONTO GRAVADO AL 5% (IVA INCLUIDO)
	//	montoExenta MONTO NO GRAVADO O EXENTO
	//	totalGeneral MONTO TOTAL DEL COMPROBANTE
	//	codigoCondicionVenta CÓDIGO CONDICIÓN DE VENTA
	//	monedaExtranjera OPERACIÓN EN MONEDA EXTRANJERA
	//	imputaIva IMPUTA AL IVA
	//	imputaIre IMPUTA AL IRE
	//	imputaIrpRsp IMPUTA AL IRP-RSP
	//	nrComprobanteVenta NÚMERO DEL COMPROBANTE DE VENTA ASOCIADO
	//	timbradoComprobanteVenta TIMBRADO DEL COMPROBANTE DE VENTA ASOCIADO
	
	private Long tipoRegistro;
	private Long codigoIdentificacion;
	private String ruc;
	private String nombreCliente;
	private Long codigoComprobante;
	private Date fecha;
	private String nrTimbrado;
	private String nroFactura;
	private BigDecimal montoIva10;
	private BigDecimal montoIva5;
	private BigDecimal montoExenta;
	private BigDecimal totalGeneral;	
	private Long codigoCondicionVenta;	
	private String monedaExtranjera;	
	private String imputaIva;	
	private String imputaIre;	
	private String imputaIrpRsp;	
	private String nrComprobanteVenta;	
	private String timbradoComprobanteVenta;
	
	public Long getTipoRegistro() {
		return tipoRegistro;
	}
	public void setTipoRegistro(Long tipoRegistro) {
		this.tipoRegistro = tipoRegistro;
	}
	public Long getCodigoIdentificacion() {
		return codigoIdentificacion;
	}
	public void setCodigoIdentificacion(Long codigoIdentificacion) {
		this.codigoIdentificacion = codigoIdentificacion;
	}
	public String getRuc() {
		return ruc;
	}
	public void setRuc(String ruc) {
		this.ruc = ruc;
	}
	public String getNombreCliente() {
		return nombreCliente;
	}
	public void setNombreCliente(String nombreCliente) {
		this.nombreCliente = nombreCliente;
	}
	public Long getCodigoComprobante() {
		return codigoComprobante;
	}
	public void setCodigoComprobante(Long codigoComprobante) {
		this.codigoComprobante = codigoComprobante;
	}
	public Date getFecha() {
		return fecha;
	}
	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}
	public String getNrTimbrado() {
		return nrTimbrado;
	}
	public void setNrTimbrado(String nrTimbrado) {
		this.nrTimbrado = nrTimbrado;
	}
	public String getNroFactura() {
		return nroFactura;
	}
	public void setNroFactura(String nroFactura) {
		this.nroFactura = nroFactura;
	}
	public BigDecimal getMontoIva10() {
		return montoIva10;
	}
	public void setMontoIva10(BigDecimal montoIva10) {
		this.montoIva10 = montoIva10;
	}
	public BigDecimal getMontoIva5() {
		return montoIva5;
	}
	public void setMontoIva5(BigDecimal montoIva5) {
		this.montoIva5 = montoIva5;
	}
	public BigDecimal getMontoExenta() {
		return montoExenta;
	}
	public void setMontoExenta(BigDecimal montoExenta) {
		this.montoExenta = montoExenta;
	}
	public BigDecimal getTotalGeneral() {
		return totalGeneral;
	}
	public void setTotalGeneral(BigDecimal totalGeneral) {
		this.totalGeneral = totalGeneral;
	}
	public Long getCodigoCondicionVenta() {
		return codigoCondicionVenta;
	}
	public void setCodigoCondicionVenta(Long codigoCondicionVenta) {
		this.codigoCondicionVenta = codigoCondicionVenta;
	}
	public String getMonedaExtranjera() {
		return monedaExtranjera;
	}
	public void setMonedaExtranjera(String monedaExtranjera) {
		this.monedaExtranjera = monedaExtranjera;
	}
	public String getImputaIva() {
		return imputaIva;
	}
	public void setImputaIva(String imputaIva) {
		this.imputaIva = imputaIva;
	}
	public String getImputaIre() {
		return imputaIre;
	}
	public void setImputaIre(String imputaIre) {
		this.imputaIre = imputaIre;
	}
	public String getImputaIrpRsp() {
		return imputaIrpRsp;
	}
	public void setImputaIrpRsp(String imputaIrpRsp) {
		this.imputaIrpRsp = imputaIrpRsp;
	}
	public String getNrComprobanteVenta() {
		return nrComprobanteVenta;
	}
	public void setNrComprobanteVenta(String nrComprobanteVenta) {
		this.nrComprobanteVenta = nrComprobanteVenta;
	}
	public String getTimbradoComprobanteVenta() {
		return timbradoComprobanteVenta;
	}
	public void setTimbradoComprobanteVenta(String timbradoComprobanteVenta) {
		this.timbradoComprobanteVenta = timbradoComprobanteVenta;
	}
	
}
