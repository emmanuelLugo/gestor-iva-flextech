package py.com.concepto.simulador.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Venda {
    private Long id;
    private Long idPessoa;
    private BigDecimal vlTotal;
    private BigDecimal vlDesconto;
    private String boca;
    private List<ItemVenda> itens = new ArrayList<>();

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getIdPessoa() { return idPessoa; }
    public void setIdPessoa(Long idPessoa) { this.idPessoa = idPessoa; }
    public BigDecimal getVlTotal() { return vlTotal != null ? vlTotal : BigDecimal.ZERO; }
    public void setVlTotal(BigDecimal vlTotal) { this.vlTotal = vlTotal; }
    public BigDecimal getVlDesconto() { return vlDesconto != null ? vlDesconto : BigDecimal.ZERO; }
    public void setVlDesconto(BigDecimal vlDesconto) { this.vlDesconto = vlDesconto; }
    public String getBoca() { return boca; }
    public void setBoca(String boca) { this.boca = boca; }
    public List<ItemVenda> getItens() { return itens; }
    public void setItens(List<ItemVenda> itens) { this.itens = itens; }
}
