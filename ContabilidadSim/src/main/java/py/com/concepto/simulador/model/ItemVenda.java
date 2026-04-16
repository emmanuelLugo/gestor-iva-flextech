package py.com.concepto.simulador.model;

import java.math.BigDecimal;

public class ItemVenda {
    private Long id;
    private Long idProducto;
    private BigDecimal vlTotal;
    private Long iva;
    private Boolean processavel;
    private BigDecimal quantidade;
    private BigDecimal vlPrecoVenda;
    private String codigoBarra;
    
    // Field for simulation result
    private BigDecimal vlSimulado;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdProducto() { return idProducto; }
    public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }
    public BigDecimal getVlTotal() { return vlTotal != null ? vlTotal : BigDecimal.ZERO; }
    public void setVlTotal(BigDecimal vlTotal) { this.vlTotal = vlTotal; }
    public Long getIva() { return iva; }
    public void setIva(Long iva) { this.iva = iva; }
    public Boolean getProcessavel() { return processavel != null ? processavel : true; }
    public void setProcessavel(Boolean processavel) { this.processavel = processavel; }
    public BigDecimal getQuantidade() { return quantidade != null ? quantidade : BigDecimal.ZERO; }
    public void setQuantidade(BigDecimal quantidade) { this.quantidade = quantidade; }
    public BigDecimal getVlPrecoVenda() { return vlPrecoVenda != null ? vlPrecoVenda : BigDecimal.ZERO; }
    public void setVlPrecoVenda(BigDecimal vlPrecoVenda) { this.vlPrecoVenda = vlPrecoVenda; }
    public String getCodigoBarra() { return codigoBarra; }
    public void setCodigoBarra(String codigoBarra) { this.codigoBarra = codigoBarra; }
    public BigDecimal getVlSimulado() { return vlSimulado != null ? vlSimulado : BigDecimal.ZERO; }
    public void setVlSimulado(BigDecimal vlSimulado) { this.vlSimulado = vlSimulado; }
}
