package py.com.concepto.model.entity.dto;

import java.math.BigDecimal;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import py.com.concepto.model.entity.Moeda;
import py.com.concepto.model.entity.NotaFaturada;
import py.com.concepto.model.entity.Venda;
import py.com.concepto.util.variados.ConversorMoedaSetCompra;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LivroVendaDto {
	private Moeda moeda;
	private Date dtVenda;
	private Date dtInicial;
	private String ruc;
	private String cliente;
	// CREDITO O CONTADO
	private String tipoDocumento;
	// TIMBRADO + NR NOTA
	private String timbrado;
	private String nrFatura;
	private String nrDocumento;
	private BigDecimal vlGravada5;
	private BigDecimal vlGravada10;
	private BigDecimal vlIva5;
	private BigDecimal vlIva10;
	private BigDecimal vlTotalExcento;

	public static LivroVendaDto toNotaFaturada(NotaFaturada nf, Venda venda, Moeda moeda,
			ConversorMoedaSetCompra conversorMoedaSetCompra) {
		LivroVendaDto dto = new LivroVendaDto();
		dto.setCliente(nf.getPessoa().getNome());
		dto.setDtVenda(nf.getDtFatura());
		dto.setDtInicial(nf.getDtFatura());
		dto.setMoeda(moeda);
		dto.setTimbrado(nf.getTimbrado().getTimbrado().toString());
		dto.setNrFatura(nf.getNrFilial() + "-" + nf.getNrBoca() + "-" + String.format("%07d", nf.getNrFatura()));

		dto.setNrDocumento(nf.getTimbrado().getTimbrado() + " " + nf.getNrFilial() + "-" + nf.getNrBoca() + "-"
				+ String.format("%07d", nf.getNrFatura()));
		dto.setRuc(nf.getPessoa().getRuc());
		if (nf.getCancelado()) {
			dto.setTipoDocumento("ANULADO");
			dto.setVlGravada10(BigDecimal.ZERO);
			dto.setVlGravada5(BigDecimal.ZERO);
			dto.setVlIva10(BigDecimal.ZERO);
			dto.setVlIva5(BigDecimal.ZERO);
			dto.setVlTotalExcento(BigDecimal.ZERO);
			if (moeda.getId().intValue() == 2) {
				dto.setVlGravada10(BigDecimal.ZERO);
				dto.setVlGravada5(BigDecimal.ZERO);
				dto.setVlIva10(BigDecimal.ZERO);
				dto.setVlIva5(BigDecimal.ZERO);
				dto.setVlTotalExcento(BigDecimal.ZERO);
			}
		} else {
			if (nf.getContado()) {
				dto.setTipoDocumento("CONTADO");
			} else {
				dto.setTipoDocumento("CREDITO");
			}
			dto.setVlGravada10(nf.getProdutosIvaDez().subtract(nf.getIvaDez()));
			dto.setVlGravada5(nf.getProdutosIvaCinco().subtract(nf.getIvaCinco()));
			dto.setVlIva10(nf.getIvaDez());
			dto.setVlIva5(nf.getIvaCinco());
			dto.setVlTotalExcento(nf.getProdutosIvaZero());
//			if(moeda.getId().intValue() == 2){
//				dto.setVlGravada10(conversorMoedaSetCompra.converteDolarAGuarani(nf.getProdutosIvaDez()));
//				dto.setVlGravada5(conversorMoedaSetCompra.converteDolarAGuarani(nf.getProdutosIvaCinco()));
//				dto.setVlIva10(conversorMoedaSetCompra.converteDolarAGuarani(nf.getIvaDez()));
//				dto.setVlIva5(conversorMoedaSetCompra.converteDolarAGuarani(nf.getIvaCinco()));
//				dto.setVlTotalExcento(conversorMoedaSetCompra.converteDolarAGuarani(nf.getProdutosIvaZero()));
//			}
		}

		return dto;
	}

}
