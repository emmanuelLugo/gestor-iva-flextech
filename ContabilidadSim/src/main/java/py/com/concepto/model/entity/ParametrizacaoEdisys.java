package py.com.concepto.model.entity;

public class ParametrizacaoEdisys {
	private Long id;
	private Long codigoEmpresa;
	private Long codigoFormaPago;
	private Long codigoMoeda;
	private Long codigoCotacao;
	private String codigoContaContavelIva0;
	private String codigoContaContavelIva5;
	private String codigoContaContavelIva10;
	private String codigoCentroCusto;
	private Long iva;
	private Long nroComprovanteContado;
	private Long nroComprovanteCredito;
	private String impuneIva;
	private String impuneIre;
	private String impuneIrpRps;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public Long getCodigoEmpresa() { return codigoEmpresa; }
	public void setCodigoEmpresa(Long codigoEmpresa) { this.codigoEmpresa = codigoEmpresa; }

	public Long getCodigoFormaPago() { return codigoFormaPago; }
	public void setCodigoFormaPago(Long codigoFormaPago) { this.codigoFormaPago = codigoFormaPago; }

	public Long getCodigoMoeda() { return codigoMoeda; }
	public void setCodigoMoeda(Long codigoMoeda) { this.codigoMoeda = codigoMoeda; }

	public Long getCodigoCotacao() { return codigoCotacao; }
	public void setCodigoCotacao(Long codigoCotacao) { this.codigoCotacao = codigoCotacao; }

	public String getCodigoContaContavelIva0() { return codigoContaContavelIva0; }
	public void setCodigoContaContavelIva0(String codigoContaContavelIva0) { this.codigoContaContavelIva0 = codigoContaContavelIva0; }

	public String getCodigoContaContavelIva5() { return codigoContaContavelIva5; }
	public void setCodigoContaContavelIva5(String codigoContaContavelIva5) { this.codigoContaContavelIva5 = codigoContaContavelIva5; }

	public String getCodigoContaContavelIva10() { return codigoContaContavelIva10; }
	public void setCodigoContaContavelIva10(String codigoContaContavelIva10) { this.codigoContaContavelIva10 = codigoContaContavelIva10; }

	public String getCodigoCentroCusto() { return codigoCentroCusto; }
	public void setCodigoCentroCusto(String codigoCentroCusto) { this.codigoCentroCusto = codigoCentroCusto; }

	public Long getIva() { return iva; }
	public void setIva(Long iva) { this.iva = iva; }

	public Long getNroComprovanteContado() { return nroComprovanteContado; }
	public void setNroComprovanteContado(Long nroComprovanteContado) { this.nroComprovanteContado = nroComprovanteContado; }

	public Long getNroComprovanteCredito() { return nroComprovanteCredito; }
	public void setNroComprovanteCredito(Long nroComprovanteCredito) { this.nroComprovanteCredito = nroComprovanteCredito; }

	public String getImpuneIva() { return impuneIva; }
	public void setImpuneIva(String impuneIva) { this.impuneIva = impuneIva; }

	public String getImpuneIre() { return impuneIre; }
	public void setImpuneIre(String impuneIre) { this.impuneIre = impuneIre; }

	public String getImpuneIrpRps() { return impuneIrpRps; }
	public void setImpuneIrpRps(String impuneIrpRps) { this.impuneIrpRps = impuneIrpRps; }
}
