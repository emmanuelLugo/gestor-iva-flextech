package py.com.concepto.simulador;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import py.com.concepto.simulador.model.*;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class SimulationService {

    public SimulationResult runSimulation(List<Venda> vendas, Long clientePadrao, BigDecimal valorMaximo, Consumer<String> logger, BooleanSupplier isCancelled) throws InterruptedException {
        SimulationResult result = new SimulationResult();
        int totalOriginalItems = 0;
        int totalSimulatedItems = 0;

        for (Venda venda : vendas) {
            if (isCancelled.getAsBoolean()) {
                throw new InterruptedException("Simulación cancelada por el usuario");
            }
            List<ItemVenda> filteredItens = new ArrayList<>();
            BigDecimal originalTotal = venda.getVlTotal().subtract(venda.getVlDesconto());
            int removedCount = 0;
            
            if (venda.getIdPessoa().equals(clientePadrao)) {
                if (venda.getItens().size() == 1) {
                    filteredItens.add(venda.getItens().get(0));
                } else {
                    for (ItemVenda item : venda.getItens()) {
                        if (item.getProcessavel()) {
                            if (valorMaximo.compareTo(BigDecimal.ZERO) == 0) {
                                filteredItens.add(item);
                            } else if (item.getVlTotal().compareTo(valorMaximo) < 0) {
                                filteredItens.add(item);
                            } else {
                                removedCount++;
                            }
                        } else {
                            removedCount++;
                        }
                    }
                    if (filteredItens.isEmpty() && !venda.getItens().isEmpty()) {
                        filteredItens.add(venda.getItens().get(0));
                        removedCount--; // Mantuvimos uno forzadamente
                    }
                }
            } else {
                filteredItens.addAll(venda.getItens());
            }

            totalOriginalItems += venda.getItens().size();
            totalSimulatedItems += filteredItens.size();

            if (removedCount > 0) {
                logger.accept("Venta ID " + venda.getId() + ": " + removedCount + " ítems removidos (Superan monto máx o no procesables)");
            } else if (venda.getIdPessoa().equals(clientePadrao)) {
                logger.accept("Venta ID " + venda.getId() + ": Procesada sin cambios (Cliente estándar)");
            }

            BigDecimal simulatedVlTotal = BigDecimal.ZERO;
            BigDecimal vlIVA5 = BigDecimal.ZERO;
            BigDecimal vlIVA10 = BigDecimal.ZERO;

            BigDecimal percDesconto = BigDecimal.ZERO;
            if (venda.getVlDesconto().compareTo(BigDecimal.ZERO) > 0) {
                if (venda.getVlTotal().compareTo(BigDecimal.ZERO) > 0) {
                    percDesconto = venda.getVlDesconto().multiply(new BigDecimal("100"))
                                    .divide(venda.getVlTotal(), 4, RoundingMode.HALF_UP);
                }
            }

            BigDecimal totalGravado5 = BigDecimal.ZERO;
            BigDecimal totalGravado10 = BigDecimal.ZERO;

            for (ItemVenda item : filteredItens) {
                BigDecimal itemDesconto = item.getVlTotal().multiply(percDesconto)
                                            .divide(new BigDecimal("100"), RoundingMode.HALF_UP);
                BigDecimal itemSimuladoTotal = item.getVlTotal().subtract(itemDesconto);
                
                // Store simulated value back into item
                item.setVlSimulado(itemSimuladoTotal);
                
                simulatedVlTotal = simulatedVlTotal.add(itemSimuladoTotal);
                
                if (item.getIva() == 5) {
                    totalGravado5 = totalGravado5.add(itemSimuladoTotal);
                } else if (item.getIva() == 10) {
                    totalGravado10 = totalGravado10.add(itemSimuladoTotal);
                }
            }

            vlIVA5 = totalGravado5.divide(new BigDecimal("21"), 0, RoundingMode.HALF_UP);
            vlIVA10 = totalGravado10.divide(new BigDecimal("11"), 0, RoundingMode.HALF_UP);

            result.addVenda(originalTotal, simulatedVlTotal, vlIVA5, vlIVA10);
            
            // To allow DatabaseService to use filtered list later, we update the venda object's list
            // Note: This modifies the input list, but it's acceptable for this standalone tool.
            venda.setItens(filteredItens);
            venda.setVlTotal(simulatedVlTotal); // Simulated total
        }

        return result;
    }
}
