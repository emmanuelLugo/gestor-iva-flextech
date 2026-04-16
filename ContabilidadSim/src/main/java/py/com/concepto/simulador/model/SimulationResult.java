package py.com.concepto.simulador.model;

import java.math.BigDecimal;

public class SimulationResult {
    private int countVendas = 0;
    private BigDecimal totalOriginal = BigDecimal.ZERO;
    private BigDecimal totalSimulado = BigDecimal.ZERO;
    private BigDecimal totalIva5 = BigDecimal.ZERO;
    private BigDecimal totalIva10 = BigDecimal.ZERO;

    public void addVenda(BigDecimal original, BigDecimal simulado, BigDecimal iva5, BigDecimal iva10) {
        countVendas++;
        totalOriginal = totalOriginal.add(original);
        totalSimulado = totalSimulado.add(simulado);
        totalIva5 = totalIva5.add(iva5);
        totalIva10 = totalIva10.add(iva10);
    }

    // Getters
    public int getCountVendas() { return countVendas; }
    public BigDecimal getTotalOriginal() { return totalOriginal; }
    public BigDecimal getTotalSimulado() { return totalSimulado; }
    public BigDecimal getTotalIva5() { return totalIva5; }
    public BigDecimal getTotalIva10() { return totalIva10; }
    public BigDecimal getDiferenca() { return totalOriginal.subtract(totalSimulado); }
}
