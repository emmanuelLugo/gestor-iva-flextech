package py.com.concepto.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.db1.myBatisPersistence.service.GenericMyBatisServiceImpl;
import py.com.concepto.dao.NotaFaturadaDao;
import py.com.concepto.dao.ParametrosDao;
import py.com.concepto.dao.ProcessamentoVendaAutoImpressorDao;
import py.com.concepto.dao.RecebimentoDao;
import py.com.concepto.model.entity.ItemProcessamentoVendaAutoImpressor;
import py.com.concepto.model.entity.ItemVenda;
import py.com.concepto.model.entity.Moeda;
import py.com.concepto.model.entity.NotaFaturada;
import py.com.concepto.model.entity.Parametros;
import py.com.concepto.model.entity.ProcessamentoVendaAutoImpressor;
import py.com.concepto.model.entity.Venda;
import py.com.concepto.model.entity.dto.LivroVendaDto;
import py.com.concepto.service.ProcessamentoVendaAutoImpressorService;
import py.com.concepto.service.VendaService;
import py.com.concepto.util.exception.ConceptoValidateException;
import py.com.concepto.util.variados.ConversorMoedaSetCompra;

@Service(value = "processamentoVendaAutoImpressorService")
@Transactional(rollbackFor = Exception.class)
public class ProcessamentoVendaAutoImpressorServiceImpl
		extends GenericMyBatisServiceImpl<ProcessamentoVendaAutoImpressor, Long>
		implements ProcessamentoVendaAutoImpressorService {

	@Autowired
	private VendaService vendaService;

	@Autowired
	private ParametrosDao parametrosDao;

	@Autowired
	private ProcessamentoVendaAutoImpressorDao processamentoVendaAutoImpressorDao;

	@Autowired
	private NotaFaturadaDao notaFaturadaDao;

	@Autowired
	private RecebimentoDao recebimentoDao;

	@Autowired
	private ConversorMoedaSetCompra conversorMoedaSetCompra;

	public void setProcessamentoVendaAutoImpressorDao(
			ProcessamentoVendaAutoImpressorDao processamentoVendaAutoImpressorDao) {
		this.processamentoVendaAutoImpressorDao = processamentoVendaAutoImpressorDao;
		this.setGenericDao(processamentoVendaAutoImpressorDao);
	}

	@Override
	public BigDecimal processaVendas(String where, BigDecimal valorMaximo) {

		BigDecimal vlTotalProcessado = BigDecimal.ZERO;
		processamentoVendaAutoImpressorDao.limpaTabelasAutoImpressao();

		Parametros consumidorOcasional = parametrosDao.findParametro("CLIENTE_PADRAO");
		Long clientePadrao = new Long(consumidorOcasional.getValor());
		List<NotaFaturada> notas = notaFaturadaDao.findByCondition(where);

		for (NotaFaturada nota : notas) {

			Venda venda = vendaService.findVendaComItensById(nota.getVenda().getId());
			ProcessamentoVendaAutoImpressor processamento = ProcessamentoVendaAutoImpressor.byVenda(venda);

			// SE O CLIENTE FOR PADRAO
			if (venda.getPessoa().getId().intValue() == clientePadrao.intValue()) {

				if (venda.getItensVenda().size() == 1) {
					ItemProcessamentoVendaAutoImpressor itemProcessamento = ItemProcessamentoVendaAutoImpressor
							.toItemVenda(venda.getItensVenda().get(0), processamento, venda);
					List<ItemProcessamentoVendaAutoImpressor> list = new ArrayList<>();
					list.add(itemProcessamento);
					vlTotalProcessado = vlTotalProcessado.add(itemProcessamento.getVlTotal());
					processamento.setItens(list);
				} else {
					List<ItemProcessamentoVendaAutoImpressor> list = new ArrayList<>();
					for (ItemVenda item : venda.getItensVenda()) {
						if (item.getProduto().getProcessavel()) {
							if (valorMaximo.doubleValue() == 0) {
								ItemProcessamentoVendaAutoImpressor itemVenda = ItemProcessamentoVendaAutoImpressor
										.toItemVenda(item, processamento, venda);
								list.add(itemVenda);
								vlTotalProcessado = vlTotalProcessado.add(itemVenda.getVlTotal());
							} else if (item.getVlTotal().doubleValue() < valorMaximo.doubleValue()) {
								ItemProcessamentoVendaAutoImpressor itemVenda = ItemProcessamentoVendaAutoImpressor
										.toItemVenda(item, processamento, venda);
								list.add(itemVenda);
								vlTotalProcessado = vlTotalProcessado.add(itemVenda.getVlTotal());
							}
						}
					}
					if (list.size() == 0) {
						ItemProcessamentoVendaAutoImpressor i = ItemProcessamentoVendaAutoImpressor
								.toItemVenda(venda.getItensVenda().get(0), processamento, venda);
						vlTotalProcessado = vlTotalProcessado.add(i.getVlTotal());
						list.add(i);
					}
					processamento.setItens(list);
				}
			} else {
				List<ItemProcessamentoVendaAutoImpressor> list = new ArrayList<>();
				for (ItemVenda item : venda.getItensVenda()) {

					ItemProcessamentoVendaAutoImpressor itemVenda = ItemProcessamentoVendaAutoImpressor
							.toItemVenda(item, processamento, venda);
					list.add(itemVenda);
					vlTotalProcessado = vlTotalProcessado.add(itemVenda.getVlTotal());
				}
				processamento.setItens(list);
			}
			processamento.calculaTotal();
			saveOrUpdate(processamento);
		}
		return vlTotalProcessado;
	}

	@Override
	public List<LivroVendaDto> relatorioAnalisisAutoImpressor(String where) {

		String[] condicoes = where.split("\n");

		BigDecimal valorMaximo = new BigDecimal(condicoes[1]);

		Parametros parametro = parametrosDao.findParametro("MOEDA_BASE_VENDA");
		Moeda moeda = new Moeda();
		if (parametro.getValor().compareTo("GUARANI") == 0) {
			moeda.setId(1L);
		} else {
			moeda.setId(2L);
		}

		List<ProcessamentoVendaAutoImpressor> itens = new ArrayList<>();
		Parametros consumidorOcasional = parametrosDao.findParametro("CLIENTE_PADRAO");
		Long clientePadrao = new Long(consumidorOcasional.getValor());
		List<NotaFaturada> notas = notaFaturadaDao.findByCondition(condicoes[0]);

		for (NotaFaturada nota : notas) {

			Venda venda = vendaService.findVendaComItensById(nota.getVenda().getId());
			if (venda.getNrFatura() != null) {

				ProcessamentoVendaAutoImpressor processamento = ProcessamentoVendaAutoImpressor.byVenda(venda);

				// SE O CLIENTE FOR PADRAO
				if (venda.getPessoa().getId().intValue() == clientePadrao.intValue()) {

					if (venda.getItensVenda().size() == 1) {
						ItemProcessamentoVendaAutoImpressor itemProcessamento = ItemProcessamentoVendaAutoImpressor
								.toItemVenda(venda.getItensVenda().get(0), processamento, venda);
						List<ItemProcessamentoVendaAutoImpressor> list = new ArrayList<>();
						list.add(itemProcessamento);
						processamento.setItens(list);
					} else {
						List<ItemProcessamentoVendaAutoImpressor> list = new ArrayList<>();
						for (ItemVenda item : venda.getItensVenda()) {
							if (item.getProduto().getProcessavel()) {
								if (valorMaximo.doubleValue() == 0) {
									list.add(ItemProcessamentoVendaAutoImpressor.toItemVenda(item, processamento,
											venda));
								} else if (item.getVlTotal().doubleValue() < valorMaximo.doubleValue()) {
									list.add(ItemProcessamentoVendaAutoImpressor.toItemVenda(item, processamento,
											venda));
								} else {
									// System.out.println(item.getId() +"
									// "+item.getVlTotal());
									System.out.println(valorMaximo.intValue() + " - " + item.getVlTotal().intValue());
								}

							}
						}
						if (list.size() == 0) {
							ItemProcessamentoVendaAutoImpressor i = ItemProcessamentoVendaAutoImpressor
									.toItemVenda(venda.getItensVenda().get(0), processamento, venda);
							list.add(i);
						}
						processamento.setItens(list);
					}
				} else {
					List<ItemProcessamentoVendaAutoImpressor> list = new ArrayList<>();
					for (ItemVenda item : venda.getItensVenda()) {
						list.add(ItemProcessamentoVendaAutoImpressor.toItemVenda(item, processamento, venda));
					}
					processamento.setItens(list);
				}
				processamento.calculaTotal();

				itens.add(processamento);
			}
		}

		List<LivroVendaDto> notasProcessadas = new ArrayList<>();

		for (ProcessamentoVendaAutoImpressor processamento : itens) {

			BigDecimal vlIVA5 = BigDecimal.ZERO;
			BigDecimal vlIVA10 = BigDecimal.ZERO;
			BigDecimal vlGravadoIVA0 = BigDecimal.ZERO;
			BigDecimal vlGravadoIVA5 = BigDecimal.ZERO;
			BigDecimal vlGravadoIVA10 = BigDecimal.ZERO;
			BigDecimal vlTotal = BigDecimal.ZERO;

			// CRIA ITENS DE VENDA PARA REIMPRESSAO
			List<ItemVenda> itensVendaParaReimpressao = new ArrayList<>();

			for (ItemProcessamentoVendaAutoImpressor item : processamento.getItens()) {
				/// Venda v = vendaService.findById("findVendaSimples",
				/// processamento.getVenda().getId());
				if (item.getIva().intValue() == 0) {
					vlGravadoIVA0 = vlGravadoIVA0.add(item.getVlTotal());
				}
				if (item.getIva().intValue() == 5) {
					vlGravadoIVA5 = vlGravadoIVA5.add(item.getVlTotal());
				}
				if (item.getIva().intValue() == 10) {
					vlGravadoIVA10 = vlGravadoIVA10.add(item.getVlTotal());
				}
				vlTotal = vlTotal.add(item.getVlTotal());

				itensVendaParaReimpressao.add(ItemProcessamentoVendaAutoImpressor.toItemVenda(item));
			}
			vlIVA5 = vlGravadoIVA5.divide(new BigDecimal(21), RoundingMode.HALF_UP);
			vlIVA10 = vlGravadoIVA10.divide(new BigDecimal(11), RoundingMode.HALF_UP);
			
			NotaFaturada nota = new NotaFaturada();
			nota.setPessoa(processamento.getPessoa());
			nota.setDtFatura(processamento.getDtVenda());
			nota.setIvaCinco(vlIVA5);
			nota.setIvaDez(vlIVA10);
			nota.setProdutosIvaCinco(vlGravadoIVA5);
			nota.setProdutosIvaDez(vlGravadoIVA10);
			nota.setProdutosIvaZero(vlGravadoIVA0);
			nota.setTotalIva(vlIVA5.add(vlIVA10));
			nota.setVlFatura(vlTotal);
			nota.setTimbrado(processamento.getVenda().getTimbrado());
			nota.setNrBoca(processamento.getVenda().getBoca());
			nota.setNrFilial(processamento.getVenda().getFilial());
			nota.setNrFatura(processamento.getVenda().getNrFatura());
			nota.setProcessado(true);
			nota.setCancelado(processamento.getVenda().getCancelado());
			
			if(processamento.getVenda().getContaReceber() != null) {
				nota.setContado(false);
			}else {
				nota.setContado(true);
			}

			notasProcessadas
					.add(LivroVendaDto.toNotaFaturada(nota, processamento.getVenda(), moeda, conversorMoedaSetCompra));
		}

		return notasProcessadas;
	}

	@Override
	public List<Venda> imprimir(String boca) {
		List<ProcessamentoVendaAutoImpressor> list = processamentoVendaAutoImpressorDao.findParaReimpressao(boca);
		List<Venda> vendasProcessadas = new ArrayList<>();
		Parametros moedaPadraoVenda = parametrosDao.findParametro("MOEDA_BASE_VENDA");
		for (ProcessamentoVendaAutoImpressor processamento : list) {
			NotaFaturada faturado = notaFaturadaDao.findNotaByVenda(processamento.getVenda());

			// if(faturado.getProcessado()){
			// throw new ConceptoValidateException(
			// "Existen Facturas ya procesadas");
			// }
			BigDecimal vlIVA5 = BigDecimal.ZERO;
			BigDecimal vlIVA10 = BigDecimal.ZERO;
			BigDecimal vlGravadoIVA0 = BigDecimal.ZERO;
			BigDecimal vlGravadoIVA5 = BigDecimal.ZERO;
			BigDecimal vlGravadoIVA10 = BigDecimal.ZERO;
			BigDecimal vlTotal = BigDecimal.ZERO;

			System.out.println("NOTAFATURADA: " + faturado.getId() + " PROCESSAMENTO " + processamento.getId());
			System.out.println("NotaFaturada: " + faturado.getVlFatura());
			System.out.println("ProcessamentoVendaAutoImpressor: " + processamento.getVlTotal());
			System.out.println("Total Reprocessado: " + vlTotal);

			// Venda v = vendaService.findById("findVendaSimples",
			// processamento.getVenda().getId());

			// CRIA ITENS DE VENDA PARA REIMPRESSAO
			List<ItemVenda> itensVendaParaReimpressao = new ArrayList<>();

			for (ItemProcessamentoVendaAutoImpressor item : processamento.getItens()) {
				if (item.getIva().intValue() == 0) {
					vlGravadoIVA0 = vlGravadoIVA0.add(item.getVlTotal());
				}
				if (item.getIva().intValue() == 5) {
					vlGravadoIVA5 = vlGravadoIVA5.add(item.getVlTotal());
				}
				if (item.getIva().intValue() == 10) {
					vlGravadoIVA10 = vlGravadoIVA10.add(item.getVlTotal());
				}
				vlTotal = vlTotal.add(item.getVlTotal());

				itensVendaParaReimpressao.add(ItemProcessamentoVendaAutoImpressor.toItemVenda(item));
			}
			vlIVA5 = vlGravadoIVA5.divide(new BigDecimal(21), RoundingMode.HALF_UP);
			vlIVA10 = vlGravadoIVA10.divide(new BigDecimal(11), RoundingMode.HALF_UP);
			faturado.setIvaCinco(vlIVA5);
			faturado.setIvaDez(vlIVA10);
			faturado.setProdutosIvaCinco(vlGravadoIVA5);
			faturado.setProdutosIvaDez(vlGravadoIVA10);
			faturado.setProdutosIvaZero(vlGravadoIVA0);
			faturado.setTotalIva(vlIVA5.add(vlIVA10));
			faturado.setVlFatura(vlTotal);
			faturado.setProcessado(true);

			notaFaturadaDao.update(faturado);

			// CRIA VENDA PARA REIMPRESSAO
			Venda venda = new Venda();
			venda.setId(processamento.getVenda().getId());
			venda.setBoca(processamento.getBoca());
			venda.setCancelado(processamento.getCancelado());
			venda.setCodigo(processamento.getCodigo());
			venda.setContaReceber(processamento.getContaReceber());
			venda.setDtVenda(processamento.getDtVenda());
			venda.setFilial(processamento.getFilial());
			venda.setItensVenda(itensVendaParaReimpressao);
			venda.setNrFatura(processamento.getNrFatura());
			venda.setPessoa(processamento.getPessoa());
			venda.setTimbrado(processamento.getTimbrado());
			venda.setUsuario(processamento.getUsuario());
			venda.setVlCotDolar(processamento.getVlCotDolar());
			venda.setVlCotGuarani(processamento.getVlCotGuarani());
			venda.setVlCotReal(processamento.getVlCotReal());
			venda.calculaTotal();

			BigDecimal vlTotalGuarani = BigDecimal.ZERO;
			BigDecimal vlTotalDolar = BigDecimal.ZERO;
			BigDecimal vlTotalReal = BigDecimal.ZERO;
			if (moedaPadraoVenda.getValor().compareTo("GUARANI") == 0) {
				vlTotalGuarani = venda.getVlTotal();
				vlTotalDolar = venda.getVlTotal().multiply(venda.getVlCotDolar()).divide(new BigDecimal(1000),
						RoundingMode.HALF_UP);
				vlTotalReal = venda.getVlTotal().multiply(venda.getVlCotReal()).divide(new BigDecimal(1000),
						RoundingMode.HALF_UP);
			}
			venda.setVlReal(vlTotalReal);
			venda.setVlDolar(vlTotalDolar);
			venda.setVlGuarani(vlTotalGuarani);

			vendasProcessadas.add(venda);

		}

		return vendasProcessadas;

	}

}