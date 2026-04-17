package py.com.concepto.model.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import br.com.db1.myBatisPersistence.annotations.MBCascade;
import br.com.db1.myBatisPersistence.annotations.MBClass;
import br.com.db1.myBatisPersistence.annotations.MBSequenceGenerator;

@Entity
@Table(name = "CON_PARAMETRIZACAO_EDISYS")
@MBClass(additionalInsertColumns = "ID_FILIAL", additionalInsertValues = "#{@sessionHandler.ID_FILIAL}")
public class ParametrizacaoEdisys {

	@Id
	@Column(name = "ID_PARAMETRIZACAO", insertable = true, updatable = false, unique = true)
	private Long id;

	@Column(name = "CD_EMPRESA", nullable = false)
	private Long codigoEmpresa;

	@Column(name = "CD_FORMA_PAGO", nullable = false)
	private Long codigoFormaPago;

	@Column(name = "CD_MONEDA", nullable = false)
	private Long codigoMoeda;

	@Column(name = "CD_COTACAO", nullable = false)
	private Long codigoCotacao;

	@Column(name = "CD_CONTA_CONTAVEL_IVA_0", nullable = false)
	private String codigoContaContavelIva0;

	@Column(name = "CD_CONTA_CONTAVEL_IVA_5", nullable = false)
	private String codigoContaContavelIva5;

	@Column(name = "CD_CONTA_CONTAVEL_IVA_10", nullable = false)
	private String codigoContaContavelIva10;

	@Column(name = "CD_CENTRO_CUSTO", nullable = false)
	private String codigoCentroCusto;

	@Column(name = "IVA", nullable = false)
	private Long iva;
	
	@Column(name = "NRO_COMPROVANTE_CONTADO", nullable = false)
	private Long nroComprovanteContado;
	
	@Column(name = "NRO_COMPROVANTE_CREDITO", nullable = false)
	private Long nroComprovanteCredito;
	
	@Column(name = "IMPUNE_IVA", nullable = false)
	private String impuneIva;
	
	@Column(name = "IMPUNE_IRE", nullable = false)
	private String impuneIre;
	
	@Column(name = "IMPUNE_IRP_RSP", nullable = false)
	private String impuneIrpRps;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCodigoEmpresa() {
		return codigoEmpresa;
	}

	public void setCodigoEmpresa(Long codigoEmpresa) {
		this.codigoEmpresa = codigoEmpresa;
	}

	public Long getCodigoFormaPago() {
		return codigoFormaPago;
	}

	public void setCodigoFormaPago(Long codigoFormaPago) {
		this.codigoFormaPago = codigoFormaPago;
	}

	public Long getCodigoMoeda() {
		return codigoMoeda;
	}

	public void setCodigoMoeda(Long codigoMoeda) {
		this.codigoMoeda = codigoMoeda;
	}

	public Long getCodigoCotacao() {
		return codigoCotacao;
	}

	public void setCodigoCotacao(Long codigoCotacao) {
		this.codigoCotacao = codigoCotacao;
	}

	public void setIva(Long iva) {
		this.iva = iva;
	}

	public Long getIva() {
		return iva;
	}

	public void setCodigoCentroCusto(String codigoCentroCusto) {
		this.codigoCentroCusto = codigoCentroCusto;
	}

	public String getCodigoCentroCusto() {
		return codigoCentroCusto;
	}

	public String getCodigoContaContavelIva0() {
		return codigoContaContavelIva0;
	}

	public void setCodigoContaContavelIva0(String codigoContaContavelIva0) {
		this.codigoContaContavelIva0 = codigoContaContavelIva0;
	}

	public String getCodigoContaContavelIva5() {
		return codigoContaContavelIva5;
	}

	public void setCodigoContaContavelIva5(String codigoContaContavelIva5) {
		this.codigoContaContavelIva5 = codigoContaContavelIva5;
	}

	public String getCodigoContaContavelIva10() {
		return codigoContaContavelIva10;
	}

	public void setCodigoContaContavelIva10(String codigoContaContavelIva10) {
		this.codigoContaContavelIva10 = codigoContaContavelIva10;
	}

	public Long getNroComprovanteContado() {
		return nroComprovanteContado;
	}

	public void setNroComprovanteContado(Long nroComprovanteContado) {
		this.nroComprovanteContado = nroComprovanteContado;
	}

	public Long getNroComprovanteCredito() {
		return nroComprovanteCredito;
	}

	public void setNroComprovanteCredito(Long nroComprovanteCredito) {
		this.nroComprovanteCredito = nroComprovanteCredito;
	}

	public String getImpuneIva() {
		return impuneIva;
	}

	public void setImpuneIva(String impuneIva) {
		this.impuneIva = impuneIva;
	}

	public String getImpuneIre() {
		return impuneIre;
	}

	public void setImpuneIre(String impuneIre) {
		this.impuneIre = impuneIre;
	}

	public String getImpuneIrpRps() {
		return impuneIrpRps;
	}

	public void setImpuneIrpRps(String impuneIrpRps) {
		this.impuneIrpRps = impuneIrpRps;
	}
	
}
