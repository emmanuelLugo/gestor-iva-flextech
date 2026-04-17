package py.com.concepto.simulador.model;

import java.math.BigDecimal;
import java.util.Date;
import py.com.concepto.model.entity.Moeda;

public class LivroVendaDto {
    private String cliente;
    private Date dtInicial;
    private Date dtVenda;
    private Moeda moeda;
    private String nrDocumento;
    private String nrFatura;
    private String ruc;
    private String timbrado;
    private String tipoDocumento;
    private BigDecimal vlGravada10;
    private BigDecimal vlGravada5;
    private BigDecimal vlIva10;
    private BigDecimal vlIva5;
    private BigDecimal vlTotalExcento;

    // Getters and Setters
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }

    public Date getDtInicial() { return dtInicial; }
    public void setDtInicial(Date dtInicial) { this.dtInicial = dtInicial; }

    public Date getDtVenda() { return dtVenda; }
    public void setDtVenda(Date dtVenda) { this.dtVenda = dtVenda; }

    public Moeda getMoeda() { return moeda; }
    public void setMoeda(Moeda moeda) { this.moeda = moeda; }

    public String getNrDocumento() { return nrDocumento; }
    public void setNrDocumento(String nrDocumento) { this.nrDocumento = nrDocumento; }

    public String getNrFatura() { return nrFatura; }
    public void setNrFatura(String nrFatura) { this.nrFatura = nrFatura; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getTimbrado() { return timbrado; }
    public void setTimbrado(String timbrado) { this.timbrado = timbrado; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public BigDecimal getVlGravada10() { return vlGravada10; }
    public void setVlGravada10(BigDecimal vlGravada10) { this.vlGravada10 = vlGravada10; }

    public BigDecimal getVlGravada5() { return vlGravada5; }
    public void setVlGravada5(BigDecimal vlGravada5) { this.vlGravada5 = vlGravada5; }

    public BigDecimal getVlIva10() { return vlIva10; }
    public void setVlIva10(BigDecimal vlIva10) { this.vlIva10 = vlIva10; }

    public BigDecimal getVlIva5() { return vlIva5; }
    public void setVlIva5(BigDecimal vlIva5) { this.vlIva5 = vlIva5; }

    public BigDecimal getVlTotalExcento() { return vlTotalExcento; }
    public void setVlTotalExcento(BigDecimal vlTotalExcento) { this.vlTotalExcento = vlTotalExcento; }
}
