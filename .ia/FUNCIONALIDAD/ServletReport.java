package py.com.concepto.report;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import br.com.db1.myBatisPersistence.session.ConceptoSession;
import flex.messaging.io.ArrayList;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRVirtualizer;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRCsvExporterParameter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSwapFile;
import py.com.concepto.model.entity.Filial;
import py.com.concepto.model.entity.Parametros;
import py.com.concepto.util.serie.HostnameUtil;

/**
 * 
 * @author root
 * @version
 */

@SuppressWarnings("serial")
public class ServletReport extends HttpServlet {

	private ServletContext sc;

	private FactoryService factoryService = new FactoryService();
	
	
	private static final Map<String, JasperReport> JASPER_CACHE = new ConcurrentHashMap<>();

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		System.setProperty("java.awt.headless", "true");
		sc = config.getServletContext();

		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(sc);

		AutowireCapableBeanFactory autowireCapableBeanFactory = webApplicationContext.getAutowireCapableBeanFactory();

		autowireCapableBeanFactory.configureBean(factoryService, "FactoryService");

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPost(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.doGet(req, resp);
	}

	@SuppressWarnings("unchecked")
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		List<?> dados = new ArrayList();

		HashMap parameters = new HashMap();
		
		Date dtInicialRelatorio = new Date();

		String sessioID = request.getParameter("sessionID");
		ConceptoSession.getInstance().setServletSessionID(sessioID);
		parameters.put("P_FILIAL", request.getParameter("filial"));
		parameters.put("P_USUARIO", request.getParameter("usuario"));
		parameters.put("P_RESUMIDO", new Boolean(request.getParameter("resumido")));
		parameters.put("P_REMOVE_CABECALHO", new Boolean(request.getParameter("removeCabecalho")));
		parameters.put("P_FILTROS", request.getParameter("filtros"));
		parameters.put("P_OBJECT_FILIAL", ConceptoSession.getInstance().getAttribute("FILIAL_LOGADA"));

		Boolean geraXLS = new Boolean(request.getParameter("xls"));
		Boolean geraTXT = new Boolean(request.getParameter("txt"));
		Boolean geraCSV = new Boolean(request.getParameter("csv"));

		if (request.getParameter("moedaFaturamento").compareTo("null") == 0 || request.getParameter("moedaFaturamento").compareTo("") == 0) {
			Parametros moedaVenda = factoryService.parametrosDao.findParametro("MOEDA_BASE_VENDA");
			parameters.put("P_MOEDA_FATURAMENTO", moedaVenda.getValor());
			parameters.put("P_MOEDA_BASE_VENDA", moedaVenda.getValor());
		} else {
			parameters.put("P_MOEDA_FATURAMENTO", request.getParameter("moedaFaturamento"));
			parameters.put("P_MOEDA_BASE_VENDA", request.getParameter("moedaFaturamento"));
		}

		Parametros moedaBase = factoryService.parametrosDao.findParametro("MOEDA_BASE");
		parameters.put("P_MOEDA_BASE", moedaBase.getValor());

		String nomeRelatorio = request.getParameter("relatorio");
		String where = request.getParameter("where");

		/**
		 * definido qual metodo vai ser chamado para gerar os relatoriso
		 */

		if (nomeRelatorio.equals("RelatorioVenda")) {
			Boolean exibeRecebimentos = false;
			if (where.indexOf("**") != -1) {
				exibeRecebimentos = true;
				parameters.put("P_EXIBE_RECEBIMENTOS", true);
				where = where.replace("**", "");
			} else {
				parameters.put("P_EXIBE_RECEBIMENTOS", false);
			}

			if (where.indexOf("--") != -1) {
				parameters.put("P_SEPARA_IDENTIFICADOR_PRODUTO", true);
				where = where.replace("--", "");
			} else {
				parameters.put("P_SEPARA_IDENTIFICADOR_PRODUTO", false);
			}
			
			dados = factoryService.vendaService.relatorioGeralVendas(where, exibeRecebimentos);
		}

		if (nomeRelatorio.equals("RelatorioVendaPorFilial")) {
			nomeRelatorio = "RelatorioVenda";
			Boolean exibeRecebimentos = false;
			if (where.indexOf("**") != -1) {
				exibeRecebimentos = true;
				parameters.put("P_EXIBE_RECEBIMENTOS", true);
				where = where.replace("**", "");
			} else {
				parameters.put("P_EXIBE_RECEBIMENTOS", false);
			}
			dados = factoryService.vendaService.relatorioGeralVendaPorFilial(where, exibeRecebimentos);
		}
		
		
		if (nomeRelatorio.equals("RelatorioContaReceber")) {
			dados = factoryService.relatorioContaReceberFactory.relatorioContasReceber(where);
		}
		if (nomeRelatorio.equals("RelatorioContaReceberComProduto")) {
			dados = factoryService.relatorioContaReceberFactory.relatorioContasReceberComProduto(where);
		}
		if (nomeRelatorio.equals("RelatorioContaPagar")) {
			dados = factoryService.contaPagarService.relatorioContasPagar(where);
		}
		if (nomeRelatorio.equals("RelatorioCaixa")) {
			dados = factoryService.caixaService.relatorioCaixa(where);
		}
		if (nomeRelatorio.equals("RelatorioCaixaDetalhada")) {
			dados = factoryService.caixaService.relatorioCaixa(where);
		}
		if (nomeRelatorio.equals("RelatorioEstoque")) {
			if (where.split("\n").length == 3) {
				parameters.put("P_CUSTO_VISIVEL", true);
			} else {
				parameters.put("P_CUSTO_VISIVEL", false);
			}
			dados = factoryService.movimentacaoEstoqueService.relatorioEstoque(where);
		}
		if (nomeRelatorio.equals("RelatorioEstoqueGeral")) {
			dados = factoryService.movimentacaoEstoqueService.relatorioEstoqueGeral(where);
		}
		if (nomeRelatorio.equals("RelatorioEstoqueValorizado")) {
			if (where.split("\n").length == 2) {
				parameters.put("P_REMOVE_CABECALHO", true);
				where = where.replace("**", "");
			} else {
				parameters.put("P_REMOVE_CABECALHO", false);
			}
			
			dados = factoryService.movimentacaoEstoqueService.relatorioEstoqueValorizado(where, true);
		}

		if (nomeRelatorio.equals("RelatorioEstoqueValorizadoPorExistencia")) {
			if (where.split("\n").length == 2) {
				parameters.put("P_REMOVE_CABECALHO", true);
				where = where.replace("**", "");
			} else {
				parameters.put("P_REMOVE_CABECALHO", false);
			}
			
			dados = factoryService.movimentacaoEstoqueService.relatorioEstoqueValorizado(where, false);
		}

		if (nomeRelatorio.equals("RelatorioEstoqueValorizadoAgrupadoPorFilial")) {
			if (where.split("\n").length == 2) {
				parameters.put("P_REMOVE_CABECALHO", true);
				where = where.replace("**", "");
			} else {
				parameters.put("P_REMOVE_CABECALHO", false);
			}
			
			dados = factoryService.movimentacaoEstoqueService.relatorioEstoqueValorizadoAgrupadoPorFilial(where);
		}
		if (nomeRelatorio.equals("RelatorioRankingVenda")) {
			dados = factoryService.vendaService.relatorioRankingVenda(where);
			Parametros moedaVenda = factoryService.parametrosDao.findParametro("MOEDA_BASE_VENDA");
			parameters.put("P_MOEDA_VENDA", moedaVenda.getValor());
		}
		if (nomeRelatorio.equals("RelatorioCompra") || nomeRelatorio.equals("RelatorioPedidoCompra")) {
			dados = factoryService.notaFiscalService.relatorioCompras(where);
		}
		if (nomeRelatorio.equals("RelatorioCompraComPrecoVenda")) {
			dados = factoryService.notaFiscalService.relatorioCompras(where);
		}
		if (nomeRelatorio.equals("RelatorioVendaLucro")) {
			dados = factoryService.vendaService.relatorioVendaLucro(where);
			Parametros moedaVenda = factoryService.parametrosDao.findParametro("MOEDA_BASE_VENDA");
			parameters.put("P_MOEDA_VENDA", moedaVenda.getValor());
		}
		if (nomeRelatorio.equals("RelatorioVendaLucroPorProduto")) {
			dados = factoryService.vendaService.relatorioVendaLucroPorProducto(where);
			Parametros moedaVenda = factoryService.parametrosDao.findParametro("MOEDA_BASE_VENDA");
			parameters.put("P_MOEDA_VENDA", moedaVenda.getValor());
		}
		if (nomeRelatorio.equals("RelatorioVendaLucroResumido")) {
			dados = factoryService.vendaService.findByConditionStatement("findVendaSimples", where);
			Parametros moedaVenda = factoryService.parametrosDao.findParametro("MOEDA_BASE_VENDA");
			parameters.put("P_MOEDA_VENDA", moedaVenda.getValor());
		}
		if (nomeRelatorio.equals("RelatorioVendaLucroRobertKiyozaky")) {
			dados = factoryService.vendaService.findByConditionStatement("findVendaSimples", where);
			Parametros moedaVenda = factoryService.parametrosDao.findParametro("MOEDA_BASE_VENDA");
			parameters.put("P_MOEDA_VENDA", moedaVenda.getValor());
		}

		if (nomeRelatorio.equals("RelatorioComissaoVendaVistaEPrazo")) {
			dados = factoryService.relatorioVendaFactory.relatorioComissaoVendaVistaEPrazo(where);
			Parametros moedaVenda = factoryService.parametrosDao.findParametro("MOEDA_BASE_VENDA");
			parameters.put("P_MOEDA_BASE_VENDA", moedaVenda.getValor());
		}
		if (nomeRelatorio.equals("RelatorioComissaoVendaVistaEPrazoDetalhado")) {
			dados = factoryService.relatorioVendaFactory.relatorioComissaoVendaVistaEPrazoDetalhado(where);
			Parametros moedaVenda = factoryService.parametrosDao.findParametro("MOEDA_BASE_VENDA");
			parameters.put("P_MOEDA_BASE_VENDA", moedaVenda.getValor());
		}
		if (nomeRelatorio.equals("RelatorioComissaoSobreVendaOLucro")) {
			Parametros moedaVenda = factoryService.parametrosDao.findParametro("MOEDA_BASE_VENDA");
			parameters.put("P_MOEDA_BASE_VENDA", moedaVenda.getValor());
			dados = factoryService.relatorioVendaFactory.relatorioComissaoSobreVendaOLucro(where);
		}
		
		if (nomeRelatorio.equals("RelatorioProdutosAPedir")) {
			dados = factoryService.produtoService.relatorioProdutosPedir(where);
		}
		if (nomeRelatorio.equals("RelatorioCaixaChica")) {
			dados = factoryService.caixaChicaService.findRelatorioCaixaChica(where);
		}
		if (nomeRelatorio.equals("RelatorioRankingVendaFamilia")) {
			dados = factoryService.vendaService.relatorioRankingPorFamilia(where);
		}
		if (nomeRelatorio.equals("RelatorioRankingVendaPorCliente")) {
			dados = factoryService.vendaService.relatorioRankingVendaPorCliente(where);
		}
		if (nomeRelatorio.equals("RelatorioContagemEstoque")) {
			dados = factoryService.produtoService.relatorioContagemEstoque(where);
		}
		if (nomeRelatorio.equals("RelatorioProduto")) {
			Parametros moedaVenda = factoryService.parametrosDao.findParametro("MOEDA_BASE_VENDA");
			Parametros precoAtacadoVarejo = factoryService.parametrosDao.findParametro("USA_PRECO_ATACADO_VAREJO");
			parameters.put("P_USA_PRECO_ATACADO_VAREJO", precoAtacadoVarejo.getValor());
			parameters.put("P_MOEDA_BASE_VENDA", moedaVenda.getValor());

			if (where.contains("ESCONDECUSTO")) {
				parameters.put("P_ESCONDE_CUSTO", true);
			} else {
				parameters.put("P_ESCONDE_CUSTO", false);
			}
			if (where.contains("ESCONDEMAIORISTA")) {
				parameters.put("P_ESCONDE_MAIORISTA", true);
			} else {
				parameters.put("P_ESCONDE_MAIORISTA", false);
			}
			if (where.contains("ESCONDEMINORISTA")) {
				parameters.put("P_ESCONDE_MINORISTA", true);
			} else {
				parameters.put("P_ESCONDE_MINORISTA", false);
			}
			if (where.contains("ESCONDEEXISTENCIA")) {
				parameters.put("P_ESCONDE_EXISTENCIA", true);
			} else {
				parameters.put("P_ESCONDE_EXISTENCIA", false);
			}
			
			dados = factoryService.produtoService.findRelatorioProduto(where);
		}
		if (nomeRelatorio.equals("RelatorioProdutoFormula")) {
			Parametros moedaVenda = factoryService.parametrosDao.findParametro("MOEDA_BASE_VENDA");
			Parametros precoAtacadoVarejo = factoryService.parametrosDao.findParametro("USA_PRECO_ATACADO_VAREJO");
			parameters.put("P_USA_PRECO_ATACADO_VAREJO", precoAtacadoVarejo.getValor());
			parameters.put("P_MOEDA_BASE_VENDA", moedaVenda.getValor());
			
			if (where.contains("ESCONDECUSTO")) {
				parameters.put("P_ESCONDE_CUSTO", true);
			} else {
				parameters.put("P_ESCONDE_CUSTO", false);
			}
			if (where.contains("ESCONDEMAIORISTA")) {
				parameters.put("P_ESCONDE_MAIORISTA", true);
			} else {
				parameters.put("P_ESCONDE_MAIORISTA", false);
			}
			if (where.contains("ESCONDEMINORISTA")) {
				parameters.put("P_ESCONDE_MINORISTA", true);
			} else {
				parameters.put("P_ESCONDE_MINORISTA", false);
			}
			if (where.contains("ESCONDEEXISTENCIA")) {
				parameters.put("P_ESCONDE_EXISTENCIA", true);
			} else {
				parameters.put("P_ESCONDE_EXISTENCIA", false);
			}
			
			dados = factoryService.produtoService.findtRelatorioProdutoFormula(where);
		}
		if (nomeRelatorio.equals("RelatorioPrecos")) {
			Parametros moedaVenda = factoryService.parametrosDao.findParametro("MOEDA_BASE_VENDA");
			Parametros precoAtacadoVarejo = factoryService.parametrosDao.findParametro("USA_PRECO_ATACADO_VAREJO");
			parameters.put("P_MOEDA_BASE_VENDA", moedaVenda.getValor());
			parameters.put("P_USA_PRECO_ATACADO_VAREJO", precoAtacadoVarejo.getValor());
			
			if (where.contains("ESCONDECUSTO")) {
				parameters.put("P_ESCONDE_CUSTO", true);
			} else {
				parameters.put("P_ESCONDE_CUSTO", false);
			}
			if (where.contains("ESCONDEMAIORISTA")) {
				parameters.put("P_ESCONDE_MAIORISTA", true);
			} else {
				parameters.put("P_ESCONDE_MAIORISTA", false);
			}
			if (where.contains("ESCONDEMINORISTA")) {
				parameters.put("P_ESCONDE_MINORISTA", true);
			} else {
				parameters.put("P_ESCONDE_MINORISTA", false);
			}
			
			dados = factoryService.precoService.relatorioHistoricoPrecos(where);
		}
		if (nomeRelatorio.equals("RelatorioVendaPorPeriodo")) {
			Parametros moedaVenda = factoryService.parametrosDao.findParametro("MOEDA_BASE_VENDA");
			parameters.put("P_MOEDA_BASE_VENDA", moedaVenda.getValor());
			dados = factoryService.vendaService.relatorioVendaPorPeriodo(where);
		}
		if (nomeRelatorio.equals("RelatorioOperacoesBanco")) {
			dados = factoryService.operacaoBancoService.relatorioOperacaoBanco(where);
		}
		if (nomeRelatorio.equals("RelatorioRecebimentosCaixaChica")) {
			dados = factoryService.caixaChicaService.findRecebimentosCaixaChica(where);
		}
		if (nomeRelatorio.equals("RelatorioRecebimentosCaixaChicaDinamico")) {
			dados = factoryService.caixaChicaService.findRecebimentosCaixaChicaDinamico(where);
		}
		if (nomeRelatorio.equals("RelatorioEstoqueBaixaRotacao")) {
			dados = factoryService.vendaService.relatorioBaixaRotacao(where);
		}
		if (nomeRelatorio.equals("RelatorioVendaPorCaixeiro")) {
			Parametros moedaVenda = factoryService.parametrosDao.findParametro("MOEDA_BASE");
			parameters.put("P_MOEDA", moedaVenda.getValor());
			dados = factoryService.vendaService.relatorioVendaCaixeiro(where);
		}
		if (nomeRelatorio.equals("Pagare")) {
			String[] condition = where.split("\t");
			nomeRelatorio = condition[0];
			dados = factoryService.pagareService.pagareByContaReceber(condition[1]);
		}
		
		if (nomeRelatorio.equals("PagareDuploConsorcio")) {
			dados = factoryService.pagareService.pagareDuploConsorcio(where);
		}
		
		if (nomeRelatorio.equals("ReciboDuploContaReceber")) {
			parameters.put("P_EMPRESA", factoryService.parametrosDao.findParametro("NOME_EMPRESA").getValor());
			parameters.put("P_TELEFONE", factoryService.parametrosDao.findParametro("INFO_EMPRESA").getValor_alternativo());
			parameters.put("P_ENDERECO", factoryService.parametrosDao.findParametro("INFO_EMPRESA").getValor());
			
			Filial filial = (Filial) ConceptoSession.getInstance().getAttribute("FILIAL_LOGADA");
			parameters.put("P_RUC", ((filial.getRuc() == null || filial.getRuc().isEmpty()) ? "CONFIGURAR RUC EN FILIAL" : filial.getRuc()));
			parameters.put("P_ATIVIDADE_ECONOMICA", (filial.getAtividadeEconomica() == null || 
			filial.getAtividadeEconomica().isEmpty() ? "Configurar Actividade Economica en Filial" : filial.getAtividadeEconomica()));
			String[] condition = where.split("\t");
			nomeRelatorio = condition[0];
			dados = factoryService.reciboService.reciboDuploByContaReceber(condition[1]);
		}
		
		if (nomeRelatorio.equals("ReciboContaReceberAgrupado")) {
			parameters.put("P_EMPRESA", factoryService.parametrosDao.findParametro("NOME_EMPRESA").getValor());
			parameters.put("P_TELEFONE", factoryService.parametrosDao.findParametro("INFO_EMPRESA").getValor_alternativo());
			parameters.put("P_ENDERECO", factoryService.parametrosDao.findParametro("INFO_EMPRESA").getValor());
			
			Filial filial = (Filial) ConceptoSession.getInstance().getAttribute("FILIAL_LOGADA");
			parameters.put("P_RUC", ((filial.getRuc() == null || filial.getRuc().isEmpty()) ? "CONFIGURAR RUC EN FILIAL" : filial.getRuc()));
			parameters.put("P_ATIVIDADE_ECONOMICA", (filial.getAtividadeEconomica() == null || 
			filial.getAtividadeEconomica().isEmpty() ? "Configurar Actividade Economica en Filial" : filial.getAtividadeEconomica()));
			String[] condition = where.split("\\|");
			nomeRelatorio = condition[0];
			dados = factoryService.reciboService.reciboAgrupado(condition[1]);
		}
		
		if (nomeRelatorio.equals("ReciboTriploVisionSeg")) {
			dados = factoryService.reciboService.reciboByContaReceber(where);
		}
		if (nomeRelatorio.equals("ReciboDuploVendaContado")) {
			dados = factoryService.reciboService.reciboDuploByVendaContado(where);
		}
		if (nomeRelatorio.equals("ReciboContaPagar")) {
			dados = factoryService.reciboService.reciboByContaPagar(where);
		}
		if (nomeRelatorio.equals("PedidoVendaByMoeda")) {
			dados = factoryService.vendaService.geraPedidoVendaByMoeda(where);
		}
		if (nomeRelatorio.equals("RelatorioProdutosAVencer")) {
			dados = factoryService.notaFiscalService.relatorioProdutosAVencer(where);
		}
		if (nomeRelatorio.equals("RelatorioTotalizadorPedidos")) {
			dados = factoryService.vendaService.relatorioTotalizadorPedidos(where);
		}
		if (nomeRelatorio.equals("RelatorioTotalizadorPedidosOrcamento")) {
			dados = factoryService.orcamentoService.relatorioPedidosOrcamento(where);
		}
		if (nomeRelatorio.equals("RelatorioParcelasContaReceber") && geraXLS == false) {
			dados = factoryService.relatorioContaReceberFactory.relatorioParcelasSemControleSessao(where);
		}
		if (nomeRelatorio.equals("RelatorioParcelasContaReceber") && geraXLS == true) {
			nomeRelatorio = "RelatorioParcelasContaReceberExcel";
			dados = factoryService.relatorioContaReceberFactory.relatorioParcelasSemControleSessao(where);
		}
		if (nomeRelatorio.equals("RelatorioCobradorContaReceber")) {
			dados = factoryService.relatorioContaReceberFactory.relatorioParcelasSemControleSessao(where);
		}
		if (nomeRelatorio.equals("RelatorioDevolucao")) {
			dados = factoryService.devolucaoService.relatorioDevolucao(where);
		}
		if (nomeRelatorio.equals("RelatorioDescontoPorCaixeiro")) {
			dados = factoryService.vendaService.relatorioDescontoPorCaixeiro(where);
		}
		if (nomeRelatorio.equals("RelatorioDescontoPorVendedorDetalhado")) {
			dados = factoryService.vendaService.relatorioDescontoPorVendedorDetalhado(where);
		}
		if (nomeRelatorio.equals("RelatorioFaturaLegalFaturada")) {
			dados = factoryService.vendaService.relatorioNotaLegalFaturada(where);
		}
		if (nomeRelatorio.equals("RelatorioInventarioManual")) {
			dados = factoryService.existenciaService.relatorioInventarioManual(where);
		}
		if (nomeRelatorio.equals("RelatorioInventarioAutomatizado")) {
			dados = factoryService.relatorioInventarioFactory.relatorioInventarioAutomatizado(where);
		}
		if (nomeRelatorio.equals("RelatorioAnalisesCompra")) {
			dados = factoryService.notaFiscalService.relatorioAnalisesCompra(where);
		}
		if (nomeRelatorio.equals("RelatorioIva")) {
			dados = factoryService.vendaService.relatorioIva(where);
		}
		if (nomeRelatorio.equals("ExportacaoVendasEdisys")) {
			dados = factoryService.integracaoEdisysService.integracaoVendaEdisys(where);
		}
		if (nomeRelatorio.equals("ExportacaoVendasEdisysNovo")) {
			dados = factoryService.integracaoEdisysService.integracaoVendaEdisysNovo(where);
		}
		if (nomeRelatorio.equals("ExportacaoVendasStarSoft")) {
			dados = factoryService.integracaoStarSoftService.integracaoVendaStarSoft(where);
		}
		if (nomeRelatorio.equals("RelatorioProductosCancelados")) {
			dados = factoryService.produtoCanceladoService.relatorioProdutoCancelado(where);
		}
		if (nomeRelatorio.equals("RelatorioNotaCreditoAplicada")) {
			dados = factoryService.notaCreditoService.relatorioNotasCreditoAplicadas(where);
		}
		if (nomeRelatorio.equals("RelatorioNotaCredito")) {
			dados = factoryService.notaCreditoService.relatorioNotaCredito(where);
		}
		if (nomeRelatorio.equals("RelatorioRecebimentosCaixa")) {
			dados = factoryService.caixaService.relatorioRecebimentosCaixa(where);
		}
		if (nomeRelatorio.equals("RelatorioDevolucaoFornecedor")) {
			dados = factoryService.devolucaoFornecedorService.findByCondition(where);
		}
		if (nomeRelatorio.equals("LibroCompra")) {
			if (where.split("\n").length == 3) {
				parameters.put("P_REMOVE_CABECALHO", true);
				where = where.replace("**", "");
			} else {
				parameters.put("P_REMOVE_CABECALHO", false);
			}
			
			dados = factoryService.notaFiscalService.relatorioLivroCompra(where);
		}
		if (nomeRelatorio.equals("RelatorioHechaukaCompra")) {
			dados = factoryService.notaFiscalService.relatorioHechaukaLivroCompra(where);
		}
		if (nomeRelatorio.equals("LibroVenta")) {
			if (where.split("\n").length == 2) {
				parameters.put("P_REMOVE_CABECALHO", true);
				where = where.replace("**", "");
			} else {
				parameters.put("P_REMOVE_CABECALHO", false);
			}
			
			dados = factoryService.relatorioVendaFactory.relatorioLivroVenda(where);
		}
		if (nomeRelatorio.equals("LibroVentaAnalisisFatura")) {
			nomeRelatorio = "LibroVenta";
			dados = factoryService.processamentoVendaAutoImpressorService.relatorioAnalisisAutoImpressor(where);
		}
		if (nomeRelatorio.equals("RelatorioGeralVendaCaixaChica")) {
			dados = factoryService.vendaService.relatorioGeralCaixaChica(where);
		}
		if (nomeRelatorio.equals("RelatorioGeralVendaCaixa")) {
			dados = factoryService.vendaService.relatorioGeralCaixa(where);
		}
		if (nomeRelatorio.equals("RelatorioAnalisisCusto")) {
			dados = factoryService.movimentacaoEstoqueService.findMovimentacaoEstoqueAnalisisCusto(where);
		}
		if (nomeRelatorio.equals("RelatorioContaReceberCobradas")) {
			dados = factoryService.relatorioContaReceberFactory.relatorioContaReceberCobradas(where);
		}
		if (nomeRelatorio.equals("RelatorioParcelasContaReceberPessoaDetalhada")) {
			dados = factoryService.relatorioContaReceberFactory.relatorioParcelasVencidasDetalhadas(where);
		}
		if (nomeRelatorio.equals("RelatorioGasto")) {
			dados = factoryService.gastoService.findByConditionStatement("relatorioGastoDetalhado", where);
		}
		if (nomeRelatorio.equals("RelatorioGastos")) {
			dados = factoryService.gastoService.findByConditionStatement("relatorioGasto", where);
		}
		if (nomeRelatorio.equals("RelatorioCliente")) {
			dados = factoryService.pessoaService.findByConditionStatement("relatorioCliente", where);
		}
		if (nomeRelatorio.equals("RelatorioInventarioRegimeTurismo")) {
			dados = factoryService.movimentacaoEstoqueService.relatorioInventarioRegimenTurismo(where);
		}
		if (nomeRelatorio.equals("RelatorioAuditoriaFaturaAutoimpressor")) {
			dados = factoryService.notaFaturadaService.relatorioAuditoriaFaturaLegalAutoimpressor(where);
		}
		if (nomeRelatorio.equals("RelatorioAjusteEstoque")) {
			dados = factoryService.ajusteEstoqueService.findByCondition(where);
		}
		if (nomeRelatorio.equals("RelatorioVendaLucroPorFamilia")) {
			dados = factoryService.vendaService.relatorioVendaLucroPorFamilia(where);
		}
		if (nomeRelatorio.equals("RelatorioOrcamento")) {
			
			if (where.indexOf("--") != -1) {
				parameters.put("P_SEPARA_IDENTIFICADOR_PRODUTO", true);
				where = where.replace("--", "");
			} else {
				parameters.put("P_SEPARA_IDENTIFICADOR_PRODUTO", false);
			}
			
			dados = factoryService.orcamentoService.findByCondition(where);
		}
		if (nomeRelatorio.equals("RelatorioOrcamentoComLucro")) {
			dados = factoryService.orcamentoService.findByCondition(where);
		}
		if (nomeRelatorio.equals("RelatorioParcelasCobradas") && geraXLS == false) {
			dados = factoryService.relatorioContaReceberFactory.relatorioParcelaCobradas(where);
		}
		if (nomeRelatorio.equals("RelatorioParcelasCobradas") && geraXLS == true) {
			nomeRelatorio = "RelatorioParcelasCobradasExcel";
			dados = factoryService.relatorioContaReceberFactory.relatorioParcelaCobradas(where);
		}
		if (nomeRelatorio.equals("RelatorioParcelasPagadas") && geraXLS == false) {
			dados = factoryService.contaPagarService.relatorioParcelaPagadas(where);
		}
		if (nomeRelatorio.equals("RelatorioParcelasPagadas") && geraXLS == true) {
			nomeRelatorio = "RelatorioParcelasPagadasExcel";
			dados = factoryService.contaPagarService.relatorioParcelaPagadas(where);
		}
		if (nomeRelatorio.equals("RelatorioParcelasPagadasComCheque")) {
			dados = factoryService.contaPagarService.relatorioParcelasPagadasComCheque(where);
		}
		if (nomeRelatorio.equals("RelatorioPagamentoContaPagarComCheque")) {
			dados = factoryService.pagamentoComChequeService.findByCondition(where);
		}
		if (nomeRelatorio.equals("RelatorioGerencial")) {
			dados = factoryService.relatorioService.relatorioGerencial(where);
		}
		if (nomeRelatorio.equals("RelatorioTransferencia")) {
			if (where.contains("MOSTRACUSTO")) {
				parameters.put("P_MOSTRA_CUSTO", true);
			}
			if (where.contains("MOSTRAPRECOVAREJO")) {
				parameters.put("P_MOSTRA_PRECO_VAREJO", true);
			}
			if (where.contains("MOSTRAPRECOATACADO")) {
				parameters.put("P_MOSTRA_PRECO_ATACADO", true);
			}
			
			dados = factoryService.transferenciaService.relatorioTransferencia(where);
		}

		if (nomeRelatorio.equals("RelatorioTransferenciaDetalhado")) {
			if (where.contains("MOSTRACUSTO")) {
				parameters.put("P_MOSTRA_CUSTO", true);
			}
			if (where.contains("MOSTRAPRECOVAREJO")) {
				parameters.put("P_MOSTRA_PRECO_VAREJO", true);
			}
			if (where.contains("MOSTRAPRECOATACADO")) {
				parameters.put("P_MOSTRA_PRECO_ATACADO", true);
			}
			
			if (where.contains("MOSTRAPRECOS")) {
				parameters.put("P_MOSTRA_PRECO_VAREJO", true);
				parameters.put("P_MOSTRA_PRECO_ATACADO", true);
			}
			
			dados = factoryService.transferenciaService.relatorioTransferencia(where);
		}
		if (nomeRelatorio.equals("RelatorioTransferenciaEstoque")) {
			dados = factoryService.transferenciaService.findRelatorioTransferenciaComVenda(where);
		}
		if (nomeRelatorio.equals("RelatorioOrdemProducao")) {
			dados = factoryService.ordemProducaoService.findByCondition(where);
		}
		if (nomeRelatorio.equals("RelatorioProducao")) {
			dados = factoryService.gestaoProducaoService.findByCondition(where);
		}
		if (nomeRelatorio.equals("RelatorioProducaoSubformulas")) {
			dados = factoryService.gestaoProducaoService.relatorioProducaoSubformulas(where);
		}
		if (nomeRelatorio.equals("RelatorioProducaoResumido")) {
			dados = factoryService.gestaoProducaoService.relatorioProducaoResumido(where);
		}
		if (nomeRelatorio.equals("RelatorioListaOperacaoBancoPorSemana")) {
			dados = factoryService.operacaoBancoService.relatorioOperacaoBancoPorSemana(where);
		}
		if (nomeRelatorio.equals("RelatorioListaOperacaoBancoPorSemanaPorBanco")) {
			String[] array = where.split("\n");
			parameters.put("P_MOEDA", new Long(array[2]));
			dados = factoryService.operacaoBancoService.relatorioOperacaoBancoPorSemanaPorBanco(where);
		}
		if (nomeRelatorio.equals("RelatorioMovimentacaoGeralProduto")) {
			dados = factoryService.movimentacaoEstoqueService.relatorioMovimentacaoGeralProduto(where);
		}
		if (nomeRelatorio.equals("RelatorioResultadoAjusteEstoque")) {
			dados = factoryService.ajusteEstoqueService.relatorioResultadoAjusteEstoque(where);
		}
		if (nomeRelatorio.equals("RelatorioContavelNotaCredito")) {
			dados = factoryService.notaCreditoVendaValorService.relatorioContavelNotasCredito(where);
		}
		if (nomeRelatorio.equals("RelatorioEstoqueFilial")) {
			dados = factoryService.produtoService.relatorioEstoquePorFilial(where);
		}
		if (nomeRelatorio.equals("RelatorioListaGasto")) {
			dados = factoryService.gastoService.findByConditionStatement("relatorioListaGasto", where);
		}

		if (nomeRelatorio.equals("RelatorioHistoricoOperacao")) {
			dados = factoryService.historicoOperacaoService.findByConditionStatement("relatorioHistoricoOperacao", where);
		}
		
		if (nomeRelatorio.equals("RelatorioRetiradaCaixaChica")) {
			dados = factoryService.retiradaCaixaChicaService.findByConditionStatement("relatorioRetiradaCaixaChica", where);
		}
		if (nomeRelatorio.equals("RelatorioEntradaCaixaChica")) {
			dados = factoryService.retiradaCaixaChicaService.findByConditionStatement("relatorioEntradaCaixaChica", where);
		}
		
		if (nomeRelatorio.equals("RelatorioPagamentoComChequeDetallado")) {
			dados = factoryService.pagamentoComChequeService.findRelatorioPagamentoComChequeDetallado(where);
		}
		
		if (nomeRelatorio.equals("RelatorioEstoqueValorizadoPorProveedor")) {
			dados = factoryService.movimentacaoEstoqueService.relatorioEstoqueValorizadoPorProveedor(where, true);
		}
		
		if (nomeRelatorio.equals("RelatorioEstoqueValorizadoPorProveedorPorExistencia")) {
			dados = factoryService.movimentacaoEstoqueService.relatorioEstoqueValorizadoPorProveedor(where, false);
		}
		
		if (nomeRelatorio.equals("RelatorioUltimoRecebimentoContaReceber")) {
			dados = factoryService.relatorioContaReceberFactory.findRelatorioUltimoRecebimentoContaReceber(where);
		}
		if (nomeRelatorio.equals("RelatorioIntegracaoBalancaMScale")) {
			dados = factoryService.produtoService.geraIntegracaoBalancaMScale(where);
		}
		if (nomeRelatorio.equals("RelatorioPromocoes") && geraXLS == false) {
			dados = factoryService.promocaoService.relatorioPromocoes(where);
		}
		if (nomeRelatorio.equals("RelatorioPromocoes") && geraXLS == true) {
			nomeRelatorio = "RelatorioPromocoesEXCEL";
			dados = factoryService.promocaoService.relatorioPromocoes(where);
		}

		if (nomeRelatorio.equals("RelatorioAuditoriaDesconto")) {
			dados = factoryService.auditoriaDescontoDao.findByConditionStatement("relatorioAuditoriaDesconto", where);
		}
		if (nomeRelatorio.equals("RelatorioAuditoriaDesconto")) {
			dados = factoryService.auditoriaDescontoDao.findByConditionStatement("relatorioAuditoriaDesconto", where);
		}
		
		if (nomeRelatorio.equals("RelatorioOrdemServico")) {
			dados = factoryService.ordemServicoService.findByCondition(where);
		}
		
		if (nomeRelatorio.equals("RelatorioAberturaOrdemServico")) {
			dados = factoryService.aberturaOrdemServicoService.findByCondition(where);
		}
		if (nomeRelatorio.equals("ComprobanteAberturaOrdemServico")) {
			dados = factoryService.aberturaOrdemServicoService.findByCondition(where);
			parameters.put("P_ENDERECO", factoryService.parametrosDao.findParametro("INFO_EMPRESA").getValor());
			parameters.put("P_TELEFONE", factoryService.parametrosDao.findParametro("INFO_EMPRESA").getValor_alternativo());
			parameters.put("P_LOGO", factoryService.parametrosDao.findParametro("DIRETORIO_LOGO").getValor());
		}
		if (nomeRelatorio.equals("RelatorioVendaLucroPorProdutoResumido")) {
			String[] condition = where.split("\n");
			dados = factoryService.vendaService.relatorioVendaLucroPorProductoResumido(condition[0]);
			Parametros moedaVenda = factoryService.parametrosDao.findParametro("MOEDA_BASE_VENDA");
			parameters.put("P_MOEDA_VENDA", moedaVenda.getValor());
			parameters.put("P_REMOVE_TITULOS", new Boolean(condition[1]));
		}
		
		if(nomeRelatorio.equals("RelatorioComissaoServico")){
			dados = factoryService.ordemServicoService.relatorioComissao(where);
		}
		
		if (nomeRelatorio.equals("RelatorioContratoContaReceber")) {
			Filial filial = (Filial) ConceptoSession.getInstance().getAttribute("FILIAL_LOGADA");
			parameters.put("P_CIDADE", (filial.getCidade() == null || 
					filial.getCidade().isEmpty() ? "CONFIGURAR CIDADE EN FILIAL" : filial.getCidade()));

			dados = factoryService.relatorioContaReceberFactory.relatorioContrato(where);
		}
		if (nomeRelatorio.equals("RelatorioEntradaEstoque")) {
			if (where.contains("DETALLADO")) {
				parameters.put("P_DETALLADO", true);
				where = where.replace("DETALLADO", "");
			} else {
				parameters.put("P_DETALLADO", false);
			}
			dados = factoryService.entradaEstoqueDao.findByConditionStatement("relatorioEntradaEstoque", where);
		}

		if (nomeRelatorio.equals("RelatorioBaixaEstoque")) {
			if (where.contains("DETALLADO")) {
				parameters.put("P_DETALLADO", true);
				where = where.replace("DETALLADO", "");
			} else {
				parameters.put("P_DETALLADO", false);
			}
			dados = factoryService.baixaEstoqueService.findByCondition(where);
		}
		
		if (nomeRelatorio.equals("RelatorioBaixaEstoqueResumido")) {
			if (where.contains("DETALLADO")) {
				nomeRelatorio = "RelatorioBaixaEstoqueDetallado";
				parameters.put("P_DETALLADO", true);
				where = where.replace("DETALLADO", "");
				dados = factoryService.baixaEstoqueService.findByCondition(where);
			} else {
				dados = factoryService.baixaEstoqueService.relatorioBaixaEstoqueResumido(where);
			}
		}
		
		if(nomeRelatorio.equals("RelatorioComissaoServicoDetalhado")){
			dados = factoryService.ordemServicoService.relatorioComissaoDetalhado(where);
		}
		if(nomeRelatorio.equals("RelatorioIngresso")){
			dados = factoryService.ingressoService.findByConditionStatement("relatorioIngresso", where);
		}
		
		if (nomeRelatorio.equals("RelatorioIntegracaoVendaContabilidadePreImpresso")) {
			dados = factoryService.notaFaturadaService.findByConditionStatement("relatorioGastoIntegracaoVendaContabilidadePreImpresso", where);		
		}
		
		if (nomeRelatorio.equals("RelatorioEntradaCaixa")) {
			dados = factoryService.entradaCaixaService.findByConditionStatement("relatorioEntradaCaixa", where);
		}
		
		if (nomeRelatorio.equals("RelatorioRetiradaCaixa")) {
			dados = factoryService.retiradaCaixaService.findByConditionStatement("relatorioRetiradaCaixa", where);
		}
		
		if (nomeRelatorio.equals("RelatorioParcelasCobradasValorizada")) {
			dados = factoryService.relatorioContaReceberFactory.relatorioParcelaCobradasValorizada(where);
		}
		
		if (nomeRelatorio.equals("RelatorioSolicitacaoCreditoCliente")) {
			dados = factoryService.solicitacaoCreditoClienteService.relatorioSolicitacaoCreditoCliente(where);
		}
		
		if (nomeRelatorio.equals("RelatorioImportacaoExportacaoAppCobrancas")) {
			dados = factoryService.importacaoExportacaoAppCobrancasService.findByConditionStatement("relatorioImportacaoExportacaoAppCobrancas", where);
		}
		
		if (nomeRelatorio.equals("RelatorioContaReceberDiasAtrasados")) {
			dados = factoryService.relatorioContaReceberFactory.relatorioContaReceberDiasAtrasados(where);
		}
		
		if (nomeRelatorio.equals("RelatorioVendaVendedorPorCliente")) {
			dados = factoryService.vendaService.findVendaVendedorByCliente(where);
		}
		if (nomeRelatorio.equals("RelatorioPessoaSemVenda")) {
			dados = factoryService.vendaService.relatorioPessoaSemVenda(where);
		}
		if (nomeRelatorio.equals("RelatorioAlteracaoPrecoVendaCompra")) {
			dados = factoryService.notaFiscalService.findRelatorioAlteracaoPrecoVendaCompra(where);
		}
		if (nomeRelatorio.equals("RelatorioMapaOrcamento")) {
			dados = factoryService.orcamentoService.findRelatorioMapaOrcamento(where);
		}
		if (nomeRelatorio.equals("RelatorioContaReceberClientesTotalizados")) {
			dados = factoryService.relatorioContaReceberFactory.relatorioContaReceberAgrupado(where);
		}
		if (nomeRelatorio.equals("RelatorioContaPagarProveedorTotalizados") && geraXLS == false) {
			dados = factoryService.contaPagarService.relatorioContaPagarAgrupado(where);
		}
		if (nomeRelatorio.equals("RelatorioContaPagarProveedorTotalizados") && geraXLS == true) {
			nomeRelatorio = "RelatorioContaPagarProveedorTotalizadosExcel";
			dados = factoryService.contaPagarService.relatorioContaPagarAgrupado(where);
		}
		if (nomeRelatorio.equals("RelatorioCustosFixosVariaveis")) {
			dados = factoryService.relatoriFinanceiroService.relatorioCustosFixosVariaveis(where);
		}
		if (nomeRelatorio.equals("RelatorioVendaNaoCobrada")) {
			dados = factoryService.vendaService.findByConditionStatement("findVendaSimples", where);
			Parametros moedaVenda = factoryService.parametrosDao.findParametro("MOEDA_BASE_VENDA");
			parameters.put("P_MOEDA_VENDA", moedaVenda.getValor());
		}
		if (nomeRelatorio.equals("RelatorioListaContaReceber") && geraXLS == false) {
			dados = factoryService.relatorioContaReceberFactory.relatorioListaContasReceber(where);
		}
		if (nomeRelatorio.equals("RelatorioListaContaReceber") && geraXLS == true) {
			nomeRelatorio = "RelatorioListaContaReceberExcel";
			dados = factoryService.relatorioContaReceberFactory.relatorioListaContasReceber(where);
		}
		if (nomeRelatorio.equals("RelatorioListaContaPagar") && geraXLS == false) {
			dados = factoryService.contaPagarService.relatorioListaContaPagar(where);
		}
		if (nomeRelatorio.equals("RelatorioListaContaPagar") && geraXLS == true) {
			nomeRelatorio = "RelatorioListaContaPagarExcel";
			dados = factoryService.contaPagarService.relatorioListaContaPagar(where);
		}
		if (nomeRelatorio.equals("RelatorioGanhadoresConsorcio")) {
			dados = factoryService.grupoConsorcioService.relatorioGanhadoresConsorcio(where);
		}
		if (nomeRelatorio.equals("RelatorioConsorcio")) {
			dados = factoryService.grupoConsorcioService.relatorioConsorcio(where);
		}
		if (nomeRelatorio.equals("RelatorioEstadoContaConsorcio")) {
			dados = factoryService.grupoConsorcioService.findRelatorioEstadoContaConsorcio(where);
		}
		if (nomeRelatorio.equals("RelatorioCreditoClienteVenda")) {
			dados = factoryService.creditoClienteVendaService.findByConditionStatement("findRelatorioCreditoClienteVenda", where);
		}
		if (nomeRelatorio.equals("RelatorioRecebimentoConsorcio")) {
			dados = factoryService.recebimentoDao.findRelatorioRecebimentoConsorcio(where);
		}
		if (nomeRelatorio.equals("RelatorioCreditoClienteComContaReceber")) {
			dados = factoryService.creditoClienteVendaService.relatorioCreditoClienteComContaReceber(where);
		}
		
		if (nomeRelatorio.equals("RelatorioGraficoVendaPorSemanaAndMesAndAno")) {
			dados = factoryService.graficoVendaService.findGraficoVendaPorSemanaAndMesAndAno(where);
		}
		if (nomeRelatorio.equals("RelatorioAuditoriaPessoaProduto")) {
			dados = factoryService.auditoriaPessoaProdutoDao.findByConditionStatement("relatorioAuditoriaPessoaProduto",where);
		}
		if (nomeRelatorio.equals("RelatorioAuditoriaEntradaRetiradaCaixa")) {
			dados = factoryService.auditoriaEntradaRetiradaCaixaDao.findByConditionStatement("relatorioAuditoriaEntradaRetiradaCaixa",where);
		}
		if (nomeRelatorio.equals("RelatorioHistoricoPontos")) {
			dados = factoryService.movimentoPontoService.relatorioMovimentoPontos(where);
		}
		if (nomeRelatorio.equals("RelatorioHistoricoProdutosOriginarios")) {
			dados = factoryService.movimentacaoDerivadosService.relatorioMovimentoProdutoOriginario(where);
		}
		if (nomeRelatorio.equals("ExportacaoVendaRg90")) {
			dados = factoryService.integracaoHechaukaService.integracaoVendaHechauka(where);
		}
		if (nomeRelatorio.equals("RelatorioProdutoVendidoPorCliente")) {
			dados = factoryService.vendaService.findByConditionStatement("findRelatorioProdutoVendidoPorCliente", where);
		}
		if (nomeRelatorio.equals("RelatorioProdutoVendidoAlternativoParaExel")) {
			dados = factoryService.vendaService.findByConditionStatement("findRelatorioProdutoVendidoAlternativoParaExel", where);
		}
		if (nomeRelatorio.equals("RelatorioDelivery")) {
			dados = factoryService.deliveryService.findByConditionStatement("findRelatorioDelivery", where);
		}
		if (nomeRelatorio.equals("ExportacaoBalancaToledoAmericana")) {
			dados = factoryService.parametroBalanzaDao.exportacaoBalancaToledoAmericana(where);
		}

		if (nomeRelatorio.equals("RelatorioAtivaExistencia")) {
			dados = factoryService.ativaDesativaExistenciaService.findByCondition(where);
		}
		
		if(nomeRelatorio.equals("RelatorioAuditoriaValoresFaturados")){
			dados = factoryService.auditoriaValoresFaturadosService.findRelatorioValoresFaturados(where);
		}
		
		if (nomeRelatorio.equals("RelatorioOrdemCompra")) {
			dados = factoryService.ordemCompraService.findByCondition(where);
		}
		
		if (nomeRelatorio.equals("RelatorioRecepcaoOrdemCompra")) {
			dados = factoryService.recepcaoOrdemCompraService.findByCondition(where);
		}
		
		if (nomeRelatorio.equals("RelatorioHistoricoSolicitacaoCobro")) {
			dados = factoryService.historicoSolicitacaoCobroDao.findByConditionStatement("relatorioSolicitacaoCobro", where);
		}
		
		if (nomeRelatorio.equals("RelatorioRankingCompra")) {
			dados = factoryService.notaFiscalService.findRelatorioRankingCompra(where);
		}
		
		if (nomeRelatorio.equals("NotaRemissaoAutoImpressor")) {
			Filial filial = (Filial) ConceptoSession.getInstance().getAttribute("FILIAL_LOGADA");
			parameters.put("P_LOGO", factoryService.parametrosDao.findParametro("DIRETORIO_LOGO").getValor());
			parameters.put("P_ATIVIDADE_ECONOMICA", (filial.getAtividadeEconomica() == null || 
					filial.getAtividadeEconomica().isEmpty() ? "Configurar Actividade Economica en Filial" : filial.getAtividadeEconomica()));

			parameters.put("P_CIDADE", (filial.getCidade() == null || 
					filial.getCidade().isEmpty() ? "Configurar Cidade en Filial" : filial.getCidade()));

			parameters.put("P_ENDERECO", factoryService.parametrosDao.findParametro("INFO_EMPRESA").getValor());
			parameters.put("P_TELEFONE", factoryService.parametrosDao.findParametro("INFO_EMPRESA").getValor_alternativo());

			parameters.put("P_REPRESENTANTE_LEGAL", (filial.getEmpresa().getNomeRepresentanteLegal() == null || 
					filial.getEmpresa().getNomeRepresentanteLegal().isEmpty() ? "Configurar Representate Legal en Empresa" : filial.getEmpresa().getNomeRepresentanteLegal()));
			parameters.put("P_RUC", ((filial.getRuc() == null || filial.getRuc().isEmpty()) ? "CONFIGURAR RUC EN FILIAL" : filial.getRuc()));
			dados = factoryService.notaRemissaoService.findNotaRemissao(where);
		}

		if (nomeRelatorio.equals("RelatorioReposicao")) {
			Parametros moedaVenda = factoryService.parametrosDao.findParametro("MOEDA_BASE_VENDA");
			parameters.put("P_MOEDA_BASE_VENDA", moedaVenda.getValor());
			
			Filial filial = (Filial) ConceptoSession.getInstance().getAttribute("FILIAL_LOGADA");
			parameters.put("P_LOGO", factoryService.parametrosDao.findParametro("DIRETORIO_LOGO").getValor());
			parameters.put("P_ATIVIDADE_ECONOMICA", (filial.getAtividadeEconomica() == null || 
					filial.getAtividadeEconomica().isEmpty() ? "Configurar Actividade Economica en Filial" : filial.getAtividadeEconomica()));

			parameters.put("P_CIDADE", (filial.getCidade() == null || 
					filial.getCidade().isEmpty() ? "Configurar Cidade en Filial" : filial.getCidade()));

			parameters.put("P_ENDERECO", factoryService.parametrosDao.findParametro("INFO_EMPRESA").getValor());
			parameters.put("P_TELEFONE", factoryService.parametrosDao.findParametro("INFO_EMPRESA").getValor_alternativo());

			parameters.put("P_REPRESENTANTE_LEGAL", (filial.getEmpresa().getNomeRepresentanteLegal() == null || 
					filial.getEmpresa().getNomeRepresentanteLegal().isEmpty() ? "Configurar Representate Legal en Empresa" : filial.getEmpresa().getNomeRepresentanteLegal()));
			
			
			if (where.contains("MOSTRAPRECOVAREJO")) {
				parameters.put("P_MOSTRA_PRECO_VAREJO", true);
			}else {
				parameters.put("P_MOSTRA_PRECO_VAREJO", false);
			}
			
			String[] array = where.split("\n");
			
			
			dados = factoryService.reposicaoService.findByConditionStatement("findRelatorioReposicao", array[0]);
		}
		
		if (nomeRelatorio.equals("RelatorioVendaPorCidadeAgrupado")) {
			dados = factoryService.vendaService.findRelatorioVendaPorCidade(where);
		}
		
		/**
		 * RRHH
		 * RECURSOS HUMANOS
		 */
		if (nomeRelatorio.equals("RHReciboMovimento")) {
			Parametros empresa = factoryService.parametrosDao.findParametro("DIRETORIO_LOGO");
			parameters.put("P_LOGO", empresa.getValor());
			dados = factoryService.fechamentoMesService.geraReciboMovimento(where);
		}
		if (nomeRelatorio.equals("RHReciboSalarioMinimoFechamentoMes")) {
			Parametros empresa = factoryService.parametrosDao.findParametro("DIRETORIO_LOGO");
			parameters.put("P_LOGO", empresa.getValor());
			dados = factoryService.fechamentoMesService.relatorioReciboSalario(where);
		}
		if (nomeRelatorio.equals("RHReciboFechamentoMes")) {
			Parametros empresa = factoryService.parametrosDao.findParametro("DIRETORIO_LOGO");
			parameters.put("P_LOGO", empresa.getValor());
			dados = factoryService.fechamentoMesService.relatorioReciboSalario(where);
		}
		if (nomeRelatorio.equals("RHReciboIPS")) {
			Parametros empresa = factoryService.parametrosDao.findParametro("DIRETORIO_LOGO");
			parameters.put("P_LOGO", empresa.getValor());
			dados = factoryService.fechamentoMesService.relatorioReciboIPS(where);
		}
		
		if (nomeRelatorio.equals("RHReciboFeriasIPS")) {
			Parametros logo = factoryService.parametrosDao.findParametro("DIRETORIO_LOGO");
			parameters.put("P_LOGO", logo.getValor());
			Parametros empresa = factoryService.parametrosDao.findParametro("NOME_EMPRESA");
			parameters.put("P_EMPRESA", empresa.getValor());
			Parametros nroPatronal = factoryService.parametrosDao.findParametro("NUMERO_PATRONAL");
			parameters.put("P_NR_PATRONAL", nroPatronal.getValor());
			dados = factoryService.feriasRHService.reciboFerias(where);
		}
		
		if (nomeRelatorio.equals("ReporteEstructuraGastosRH")) {
			dados = factoryService.movimentoRHService.relatorioEstruturaGastosRH(where);
		}
		if (nomeRelatorio.equals("RelatorioFechamentoMesRH")) {
			dados = factoryService.fechamentoMesService.relatorioFechamentoMes(where);
		}
		if (nomeRelatorio.equals("RelatorioFuncionarioAndFechamentoMesRH")) {
			dados = factoryService.fechamentoMesService.relatorioFuncionarioAndFechamentoMes(where);
		}
		if (nomeRelatorio.equals("RelatorioFechamentoQuinzenaRH")) {
			dados = factoryService.fechamentoQuinzenaService.relatorioFechamentoQuinzena(where);
		}
		if (nomeRelatorio.equals("RelatorioMovimentoRH")) {
			dados = factoryService.movimentoRHService.relatorioMovimentoRH(where);
		}
		
		if(nomeRelatorio.equals("ExportacaoPagoBancoVision")){
			
			dados = factoryService.fechamentoMesService.findByConditionStatement("relatorioExportacaoPagoBancoVision",where);
		}
		
		if (nomeRelatorio.equals("RelatorioMovimentosCreditos")) {
			dados = factoryService.movimentoRHService.relatorioMovimientoCredito(where);
		}
		
		if (nomeRelatorio.equals("RelatorioMovimentoAguinaldo")) {
			dados = factoryService.movimentoRHService.relatorioMovimientoAguinaldo(where);
		}
		
		if (nomeRelatorio.equals("RelatorioAguinaldoRH")) {
			dados = factoryService.aguinaldoRHService.findByCondition(where);
		}
		
		if (nomeRelatorio.equals("RelatorioFuncionario")) {
			dados = factoryService.pessoaService.findByCondition(where);
		}
		if (nomeRelatorio.equals("RelatorioFuncionarioRH")) {
			dados = factoryService.funcionarioService.findByCondition(where);
		}
		
		if (nomeRelatorio.equals("RelatorioPessoaCanais")) {
			dados = factoryService.pessoaService.findByConditionStatement("relatoriPessoaCanais", where);
		}
		
		if (nomeRelatorio.equals("RelatorioCargaEstoque")) {
			Parametros moedaVenda = factoryService.parametrosDao.findParametro("MOEDA_BASE_VENDA");
			parameters.put("P_MOEDA_BASE_VENDA", moedaVenda.getValor());
			dados = factoryService.cargaEstoqueService.findByCondition(where);
		}
		if (nomeRelatorio.equals("OrdemOrcamento")) {
			
			Filial filial =(Filial) ConceptoSession.getInstance().getAttribute("FILIAL_LOGADA");
			parameters.put("P_ENDERECO", factoryService.parametrosDao.findParametro("INFO_EMPRESA").getValor());
			parameters.put("P_TELEFONE", factoryService.parametrosDao.findParametro("INFO_EMPRESA").getValor_alternativo());
			parameters.put("P_LOGO", factoryService.parametrosDao.findParametro("DIRETORIO_LOGO").getValor());
			parameters.put("P_EMAIL", factoryService.parametrosDao.findParametro("EMAIL").getValor());
			parameters.put("P_ATIVIDADE_ECONOMICA", (filial.getAtividadeEconomica() == null || filial.getAtividadeEconomica().isEmpty() ? "Configurar Actividade Economica en Filial" : filial.getAtividadeEconomica()));
			dados = factoryService.orcamentoService.findByCondition(where);
		}
		if (nomeRelatorio.equals("RelatorioOrdemPago")) {
			dados = factoryService.ordemPagoService.findByConditionStatement("relatorioOrdemPago", where);
		}
		if (nomeRelatorio.equals("OrdemPago")) {
			Filial filial =(Filial) ConceptoSession.getInstance().getAttribute("FILIAL_LOGADA");
			parameters.put("P_ENDERECO", factoryService.parametrosDao.findParametro("INFO_EMPRESA").getValor());
			parameters.put("P_TELEFONE", factoryService.parametrosDao.findParametro("INFO_EMPRESA").getValor_alternativo());
			parameters.put("P_LOGO", factoryService.parametrosDao.findParametro("DIRETORIO_LOGO").getValor());
			parameters.put("P_ATIVIDADE_ECONOMICA", (filial.getAtividadeEconomica() == null || filial.getAtividadeEconomica().isEmpty() ? "Configurar Actividade Economica en Filial" : filial.getAtividadeEconomica()));
			dados = factoryService.ordemPagoService.findByConditionStatement("relatorioOrdemPago", where);
		}
		if (nomeRelatorio.equals("RelatorioAuditoriaEnvioWhatsApp")) {
			dados = factoryService.auditoriaEnvioMensagemWhatsAppDao.findByCondition(where);
		}
		
		if (nomeRelatorio.equals("RelatorioTransferenciaConta")) {
			dados = factoryService.transferenciaContaBancoService.findByConditionStatement("relatorioTranferenciaConta",where);
		}
		
		if (nomeRelatorio.equals("RelatorioCashback")) {
			dados = factoryService.fidelizacaoCashbackService.findByConditionStatement("findRelatorioCashback", where);
		}
		
		if (nomeRelatorio.equals("RelatorioAjusteCusto")) {
			dados = factoryService.ajusteCustoService.findByConditionStatement("relatorioAjusteCusto",where);
		}
		
		if (nomeRelatorio.equals("RelatorioParcelasContaReceberVencidasPorIntervaloData")) {
			dados = factoryService.relatorioContaReceberFactory.relatorioParcelasVencidasPorIntervaloData(where);
		}

		if (nomeRelatorio.equals("RelatorioParcelasContaReceberVencidasPorIntervaloDataAgrupado")) {
			dados = factoryService.relatorioContaReceberFactory.relatorioParcelasVencidasPorIntervaloDataAgrupado(where);
		}
		
		if (nomeRelatorio.equals("RelatorioVendaComRecebimento")) {
			dados = factoryService.vendaService.relatorioVendaComRecebimento(where);
		}
		
		if (nomeRelatorio.equals("RelatorioInventarioPorPeriodo")) {
			dados = factoryService.relatorioInventarioFactory.relatorioInventarioPorPeriodo(where);
		}

		if (nomeRelatorio.equals("RelatorioTransferenciaPorProduto")) {
			dados = factoryService.transferenciaService.relatorioTransferenciaPorProduto(where);
		}

		if (nomeRelatorio.equals("RelatorioVisitaVendedor")) {
			dados = factoryService.visitaVendedorService.findByConditionStatement("findRelatorioVisitaVendedor", where);
		}
		
		if (nomeRelatorio.equals("RelatorioCompraVsVenda")) {
			dados = factoryService.movimentacaoEstoqueService.relatorioCompraVsVenda(where);
		}
		
		if (nomeRelatorio.equals("RelatorioImportacaoCompraExcelVsVenda")) {
			dados = factoryService.movimentacaoEstoqueService.relatorioImportacaoCompraExcelVsVenda(where);
		}
		
		if (nomeRelatorio.equals("RelatorioRecebimentoFormaCobro")) {
			dados = factoryService.relatorioVendaFactory.findRelatorioRecebimentoFormaCobro(where);
		}
		
		if (nomeRelatorio.equals("RelatorioHistoricoSorteados")) {
			dados = factoryService.sorteioCuponsDao.findByCondition(where);
		}
		
		if (nomeRelatorio.equals("RelatorioListadoCartaoFidelizado")) {
			dados = factoryService.cartaoFidelicacaoService.findByCondition(where);
		}

		if (nomeRelatorio.equals("RelatorioTransferenciaEntreCaixaChica")) {
			dados = factoryService.transferenciaEntreCaixaChicaService.findByCondition(where);
		}
		
		if (nomeRelatorio.equals("RelatorioOperacaoPOS")) {
			dados = factoryService.operacaoPOSService.findRelatorioOperacaoPOS(where);
		}
		
		if (nomeRelatorio.equals("RelatorioFaturaAutoImpressorA4")) {
			parameters.put("RESUMIDO", false);
			Parametros moedaVenda = factoryService.parametrosDao.findParametro("MOEDA_BASE_VENDA");
			parameters.put("P_MOEDA_FATURAMENTO", moedaVenda.getValor());
			parameters.put("P_FILIAL", factoryService.parametrosDao.findParametro("INFO_EMPRESA").getValor());
			parameters.put("P_LOGO", factoryService.parametrosDao.findParametro("DIRETORIO_LOGO").getValor());
			
			dados = factoryService.relatorioVendaFactory.relatorioFaturaAutoImpressorA4(where);
		}
		
		if (nomeRelatorio.equals("RelatorioListaVenda")) {
			dados = factoryService.vendaService.findByConditionStatement("findVendaSimples", where);
			Parametros moedaVenda = factoryService.parametrosDao.findParametro("MOEDA_BASE_VENDA");
			parameters.put("P_MOEDA_VENDA", moedaVenda.getValor());
		}

		if (nomeRelatorio.equals("RelatorioDescarteProducao")) {
			dados = factoryService.descarteProducaoService.findByCondition(where);
		}
		if (nomeRelatorio.equals("RelatorioCanjeFidelizado")) {
			dados = factoryService.canjeFidelizacaoService.findByCondition(where);
		}
		
		if (nomeRelatorio.equals("RelatorioGastoResumido")) {
			dados = factoryService.gastoService.findByConditionStatement("relatorioGastoResumido", where);
		}
		if (nomeRelatorio.equals("RelatorioFragmentacaoProducao")) {
			dados = factoryService.fragmentacaoProducaoService.findByCondition(where);
		}
		
		if(nomeRelatorio.equals("RelatorioComissaoPorProdutoResumido")) {
			dados = factoryService.relatorioVendaFactory.findRelatorioComissaoPorProduto(where, true);
		}

		if(nomeRelatorio.equals("RelatorioComissaoPorProdutoDetalhado")) {
			dados = factoryService.relatorioVendaFactory.findRelatorioComissaoPorProduto(where, false);
		}
		
		if (nomeRelatorio.equals("RelatorioParcelaContaReceberTotalizado")) {
			dados = factoryService.relatorioContaReceberFactory.relatorioParcelaContaReceberAgrupado(where);
		}
		if (nomeRelatorio.equals("RelatorioDesmancheProduto")) {
			dados = factoryService.desmancheProdutoService.findByCondition(where);
		}
		if (nomeRelatorio.equals("RelatorioFechamentoCaixaChica")) {
			dados = factoryService.fechamentoCaixaChicaService.findByCondition(where);
		}
		if (nomeRelatorio.equals("RelatorioAnalisisVendaPorProveedor") && geraXLS == false) {
			dados = factoryService.relatorioVendaFactory.findRelatorioAnalisisVendaPorProveedorParaProveedor(where);
		}
		if (nomeRelatorio.equals("RelatorioAnalisisVendaPorProveedor") && geraXLS == true) {
			nomeRelatorio = "RelatorioAnalisisVendaPorProveedorExcel";
			dados = factoryService.relatorioVendaFactory.findRelatorioAnalisisVendaPorProveedorParaProveedor(where);
		}
		if (nomeRelatorio.equals("RelatorioAnalisisRupturaEstoque") && geraXLS == false) {
			dados = factoryService.relatorioVendaFactory.findRelatorioAnalisisRupturaEstoque(where);
		}
		if (nomeRelatorio.equals("RelatorioAnalisisRupturaEstoque") && geraXLS == true) {
			nomeRelatorio = "RelatorioAnalisisRupturaEstoqueEXCEL";
			dados = factoryService.relatorioVendaFactory.findRelatorioAnalisisRupturaEstoque(where);
		}
		if (nomeRelatorio.equals("RelatorioCurvaAbc") && geraXLS == false) {
			dados = factoryService.relatorioVendaFactory.findRelatorioCurvaAbc(where);
		}
		if (nomeRelatorio.equals("RelatorioCurvaAbc") && geraXLS == true) {
			nomeRelatorio = "RelatorioCurvaAbcExcel";
			dados = factoryService.relatorioVendaFactory.findRelatorioCurvaAbc(where);
		}
		if (nomeRelatorio.equals("RelatorioRecepcaoNotaCredito") && geraXLS == false) {
			dados = factoryService.gastoService.findByConditionStatement("relatorioRecepcaoNotaCredito", where);
		}

		if (nomeRelatorio.equals("RelatorioResumoVendaPorDia")) {
			dados = factoryService.relatorioVendaFactory.relatorioResumoVentaPorDia(where);
		}
		if (nomeRelatorio.equals("RelatorioRecepcaoNotaCredito") && geraXLS == true) {
			nomeRelatorio = "RelatorioRecepcaoNotaCreditoExcel";
			dados = factoryService.gastoService.findByConditionStatement("relatorioRecepcaoNotaCredito", where);
		}
		
		if(nomeRelatorio.equals("RelatorioInventario")){
			dados = factoryService.relatorioInventarioFactory.relatorioInventario(where);
		}
		
		if (nomeRelatorio.equals("RelatorioPacote")) {
			dados = factoryService.pacoteDao.findByCondition(where);
		}
		
		if (nomeRelatorio.equals("RelatorioEstoqueValorizadoPorFamilia")) {
			if (where.split("\n").length == 2) {
				parameters.put("P_REMOVE_CABECALHO", true);
				where = where.replace("**", "");
			} else {
				parameters.put("P_REMOVE_CABECALHO", false);
			}
			
			dados = factoryService.movimentacaoEstoqueService.relatorioEstoqueValorizadoPorFamilia(where);
		}
		

		if (geraXLS) {
			geraXLS(dados, nomeRelatorio, parameters, response);
		} else if (geraTXT) {
			geraTXT(dados, nomeRelatorio, parameters, response);
		} else if (geraCSV) {
			geraCSV(dados, nomeRelatorio, parameters, response);
		} else {
			geraPDF(dados, nomeRelatorio, parameters, response);
		}

		factoryService.pessoaService.salvaAuditoriaRelatorio(nomeRelatorio, dtInicialRelatorio, where);
	}
	
	private boolean isDebugJvm() {
	    return java.lang.management.ManagementFactory
	            .getRuntimeMXBean()
	            .getInputArguments()
	            .toString()
	            .contains("jdwp");
	}

	private void geraPDF(
	        List<?> dados,
	        String nomeRelatorio,
	        HashMap<String, Object> parameters,
	        HttpServletResponse response
	) throws ServletException, IOException {

	    boolean isDebug = isDebugJvm();
	    long inicio = System.nanoTime();

	    String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss")
	            .format(new java.util.Date());
	    String fileName = nomeRelatorio + "_" + timestamp + ".pdf";

	    String path = getServletContext().getRealPath("WEB-INF/reports/");
	    parameters.put("SUBREPORT_DIR", path + File.separator);

	    JRVirtualizer virtualizer = null;

	    try {
	        // ==========================
	        // Virtualizer SOLO en PROD
	        // ==========================
	        if (!isDebug) {
	            JRSwapFile swapFile = new JRSwapFile(
	                    System.getProperty("java.io.tmpdir"),
	                    4096,
	                    100
	            );
	            virtualizer = new JRSwapFileVirtualizer(50, swapFile);
	            parameters.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
	        }

	        JasperReport jasperReport;

	        if (isDebug) {
	            File jrxml = new File(path + nomeRelatorio + ".jrxml");
	            jasperReport =
	                    JasperCompileManager.compileReport(jrxml.getAbsolutePath());
	        } else {
	            jasperReport =
	                    JasperReportCache.get(nomeRelatorio, path);
	        }

	        JasperPrint print;
	        JRDataSource jrds = new JRBeanCollectionDataSource(dados);

	        ensureSubreportsCompiled(path);
	        print = JasperFillManager.fillReport(
	                jasperReport,
	                parameters,
	                jrds
	        );

	        response.setContentType("application/pdf");
	        response.setHeader(
	                "Content-Disposition",
	                "inline; filename=\"" + fileName + "\""
	        );

	        try (ServletOutputStream out = response.getOutputStream()) {
	            JasperExportManager.exportReportToPdfStream(print, out);
	            out.flush();
	        }

	    } catch (Exception e) {
	        throw new ServletException("Error generando PDF", e);
	    } finally {
	        if (virtualizer != null) {
	            virtualizer.cleanup();
	        }
	    }

	    long fin = System.nanoTime();
	    long duracionMs = (fin - inicio) / 1_000_000;

	    System.out.println(
	            "⏳ PDF [" + nomeRelatorio + "] generado en "
	                    + duracionMs + " ms | virtualizer="
	                    + (!isDebug)
	    );
	}


	private void geraXLS(List<?> dados, String nomeRelatorio, HashMap parameters, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			
			String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new java.util.Date());
		    String fileName = nomeRelatorio + "_" + timestamp + ".xls";

			File reportFile = null;
			String path = getServletContext().getRealPath("WEB-INF" + File.separator + "reports" + File.separator);
			parameters.put("SUBREPORT_DIR", path + File.separator);

			byte[] bytes = null;
			JRDataSource jrds = new JRBeanCollectionDataSource(dados);

			JasperReport report = JasperCompileManager.compileReport(path + File.separator + nomeRelatorio + ".jrxml");
			ensureSubreportsCompiled(path);
			JasperPrint print = JasperFillManager.fillReport(report, parameters, jrds);
			OutputStream out = response.getOutputStream();

			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
			JRXlsExporter exporterXLS = new JRXlsExporter();

			exporterXLS.setParameter(JRXlsExporterParameter.JASPER_PRINT, print);
			exporterXLS.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, arrayOutputStream);
			exporterXLS.setParameter(JRXlsExporterParameter.IS_ONE_PAGE_PER_SHEET, Boolean.FALSE);
			exporterXLS.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
			exporterXLS.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, Boolean.FALSE);
			exporterXLS.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, Boolean.TRUE);
			exporterXLS.exportReport();

			response.setHeader("Content-disposition", "attachment; filename=" + fileName);
			response.setContentType("application/vnd.ms-excel");
			response.setContentLength(arrayOutputStream.toByteArray().length);
			out.write(arrayOutputStream.toByteArray());
			out.flush();
			out.close();

		} catch (JRException e) {
			e.printStackTrace();
		}

	}

	private void geraTXT(List<?> dados, String nomeRelatorio, HashMap parameters, HttpServletResponse response) {

		Locale locale = new Locale("pt", "BR");
		parameters.put(JRParameter.REPORT_LOCALE, locale);
		JasperReport pathjrxml = null;

		String path = getServletContext().getRealPath("WEB-INF" + File.separator + "reports" + File.separator);
		parameters.put("SUBREPORT_DIR", path + File.separator);
		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
		try {
			
			String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new java.util.Date());
		    String fileName = nomeRelatorio + "_" + timestamp + ".txt";
			
			pathjrxml = JasperCompileManager.compileReport(path + "\\" + nomeRelatorio + ".jrxml");
			
			
			// Gera a �rea de impress�o para o JasperReport
			JasperPrint jasperPrint = JasperFillManager.fillReport(pathjrxml, parameters,
					new JRBeanCollectionDataSource(dados));

			// Captura informa��es sobre as dimens�es do documento (iReport)
			Integer pageHeight = jasperPrint.getPageHeight();
			Integer pageWidth = jasperPrint.getPageWidth();

			// Instancia o objeto que ir� gerar o arquivo Texto formatado.
			JRTextExporter jrTextExporter = new JRTextExporter();

			// Defini��es e par�metros para gerar o arquivo Texto.
			jrTextExporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
			jrTextExporter.setParameter(JRTextExporterParameter.PAGE_HEIGHT, pageHeight);

			jrTextExporter.setParameter(JRTextExporterParameter.PAGE_HEIGHT, pageHeight);
			/*
			 * Aqui voc� pode inserir um caractere de quebra de p�gina caso
			 * algum interpretador do seu arquivo texto necesite
			 */
			// jrTextExporter.setParameter(JRTextExporterParameter.BETWEEN_PAGES_TEXT,
			// "\f");
			// jrTextExporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME,
			// diretorioAExportarOPDF);

			/*
			 * Aqui est� o segredo, fazendo a divis�o da largura da p�gina do
			 * iReport com o n�mero de colunas do relat�ri o teremos o n�mero
			 * correto de pixels para definir a largura dos caracteres
			 */
			jrTextExporter.setParameter(JRTextExporterParameter.CHARACTER_WIDTH, new Float(4.5));
			jrTextExporter.setParameter(JRTextExporterParameter.CHARACTER_HEIGHT, new Float(13));

			jrTextExporter.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, arrayOutputStream);
			// Exporta de fato o arquivo TXT
			jrTextExporter.exportReport();
			OutputStream out = response.getOutputStream();
			response.setHeader("Content-disposition", "attachment; filename=" + fileName);
			response.setContentType("text/plain");
			response.setContentLength(arrayOutputStream.toByteArray().length);
			out.write(arrayOutputStream.toByteArray());
			out.flush();
			out.close();

		} catch (JRException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			throw new exception.ConceptoValidateException(e.getMessage());
		}

	}

	private void geraCSV(List<?> dados, String nomeRelatorio, HashMap parameters, HttpServletResponse response) {
		ByteArrayOutputStream xlsReport = new ByteArrayOutputStream();

		File reportFile = null;
		String path = getServletContext().getRealPath("WEB-INF" + File.separator + "reports" + File.separator);
		parameters.put("SUBREPORT_DIR", path + File.separator);

		byte[] bytes = null;
		JRDataSource jrds = new JRBeanCollectionDataSource(dados);

		JasperReport report;
		try {
			report = JasperCompileManager.compileReport(path + File.separator + nomeRelatorio + ".jrxml");
			

			JRCsvExporter exporterCSV = new JRCsvExporter();
			JasperPrint jasperPrint = JasperFillManager.fillReport(report, parameters, jrds);
			exporterCSV.setParameter(JRXlsExporterParameter.JASPER_PRINT, jasperPrint);
			exporterCSV.setParameter(JRXlsExporterParameter.OUTPUT_STREAM, xlsReport);
			exporterCSV.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_COLUMNS, true);
			exporterCSV.setParameter(JRXlsExporterParameter.IS_REMOVE_EMPTY_SPACE_BETWEEN_ROWS, true);
			exporterCSV.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, true);
			exporterCSV.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, true);
			exporterCSV.setParameter(JRCsvExporterParameter.FIELD_DELIMITER, ";");

			exporterCSV.exportReport();

			byte[] buffer = xlsReport.toByteArray();
			try {
				xlsReport.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			response.setContentType("application/vnd.ms-excel");
			response.setHeader("Content-disposition", "filename=arquivoCSVJasper.csv");

			ServletOutputStream out = null;
			try {
				out = response.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (buffer != null) {
				out.write(buffer);
			}

		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Compiles Sub*.jrxml files to .jasper when the .jrxml is newer than the .jasper
	 * (or .jasper doesn't exist). Prevents NoClassDefFoundError caused by stale .jasper
	 * files compiled with a different JasperReports version.
	 */
	private void ensureSubreportsCompiled(String reportsDir) {
	    File dir = new File(reportsDir);
	    File[] jrxmlFiles = dir.listFiles((d, name) ->
	            name.startsWith("Sub") && name.endsWith(".jrxml"));
	    if (jrxmlFiles == null) return;
	    for (File jrxmlFile : jrxmlFiles) {
	        String baseName = jrxmlFile.getName().replace(".jrxml", "");
	        File jasperFile = new File(reportsDir + baseName + ".jasper");
	        if (!jasperFile.exists() || jrxmlFile.lastModified() > jasperFile.lastModified()) {
	            try {
	                JasperCompileManager.compileReportToFile(
	                        jrxmlFile.getAbsolutePath(),
	                        jasperFile.getAbsolutePath());
	                System.out.println("[JasperCompile] recompiled subreport: " + jrxmlFile.getName());
	            } catch (JRException e) {
	                System.err.println("[JasperCompile] error compiling " + jrxmlFile.getName() + ": " + e.getMessage());
	            }
	        }
	    }
	}

}