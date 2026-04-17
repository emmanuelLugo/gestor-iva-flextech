package py.com.concepto.integracao.hechauka;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import py.com.concepto.dao.ParametrizacaoEdisysDao;
import py.com.concepto.dao.ParametrosDao;
import py.com.concepto.dao.PessoaDao;
import py.com.concepto.dao.TimbradoDao;
import py.com.concepto.dao.VendaDao;
import py.com.concepto.model.entity.NotaFaturada;
import py.com.concepto.model.entity.ParametrizacaoEdisys;
import py.com.concepto.model.entity.Parametros;
import py.com.concepto.model.entity.Pessoa;
import py.com.concepto.model.entity.Timbrado;
import py.com.concepto.model.entity.Venda;
import py.com.concepto.model.entity.dto.IntegracaoVendaHechaukaDto;
import py.com.concepto.service.NotaFaturadaService;

@Service(value = "integracaoHechaukaService")
@Transactional(rollbackFor = Exception.class)
public class IntegracaoHechaukaService {

	@Autowired
	private VendaDao vendaDao;

	@Autowired
	private ParametrizacaoEdisysDao parametrizacaoEdisysDao;

	@Autowired
	private TimbradoDao timbradoDao;

	@Autowired
	private PessoaDao pessoaDao;

	@Autowired
	private NotaFaturadaService notaFaturadaService;

	@Autowired
	private ParametrosDao parametrosDao;

	public List<IntegracaoVendaHechaukaDto> integracaoVendaHechauka(String where) {
		List<IntegracaoVendaHechaukaDto> integracaoEchaukaList = new ArrayList<IntegracaoVendaHechaukaDto>();
		List<NotaFaturada> notas = notaFaturadaService.findByCondition(where);
		ParametrizacaoEdisys parametros = parametrizacaoEdisysDao.findById(1L);
		Parametros clientePadrao = parametrosDao.findParametro("CLIENTE_PADRAO");
		Pessoa consumidorFinal = pessoaDao.findById(new Long(clientePadrao.getValor()));

		for (NotaFaturada notaFaturada : notas) {
			Timbrado timbrado = timbradoDao.findById(notaFaturada.getTimbrado().getId());
			
			Long codigoCondicaoVenda = 1L;
			if(notaFaturada.getVenda() != null) {
				Venda venda = vendaDao.findByIdSimples(notaFaturada.getVenda().getId());
				if (venda.getContaReceber() != null) {
					codigoCondicaoVenda = 2L;
				}
			}else {
				if(notaFaturada.getContado() == false) {
					codigoCondicaoVenda = 2L;
				}
			}
			
		
			String nroFatura = notaFaturada.getNrFilial() + "-" + notaFaturada.getNrBoca() + "-"
					+ StringUtils.leftPad(notaFaturada.getNrFatura().toString(), 7, "0");
			IntegracaoVendaHechaukaDto dto = null;
			String ruc = pessoaDao.findByIdSimples(notaFaturada.getPessoa().getId()).getRuc();

			if(notaFaturada.getCancelado() == false){

				dto = new IntegracaoVendaHechaukaDto();
				dto.setTipoRegistro(1L);

				if(notaFaturada.getPessoa().getId().compareTo(consumidorFinal.getId()) == 0){
					dto.setCodigoIdentificacion(15L);
					dto.setRuc("X");
					dto.setNombreCliente("SIN NOMBRE");
				}else{
					if(ruc == null){
						ruc = "";
					}
					if(ruc.indexOf("-") <= 0){
						dto.setCodigoIdentificacion(12L);
					}else{
						dto.setCodigoIdentificacion(11L);
					}
					String[] array = ruc.split("-");
					dto.setRuc(array[0]);
					dto.setNombreCliente(notaFaturada.getPessoa().getNome());
				}

				dto.setCodigoComprobante(109L);
				dto.setFecha(notaFaturada.getDtFatura());
				dto.setNrTimbrado(timbrado.getTimbrado().toString());
				dto.setNroFactura(nroFatura);
				dto.setMontoIva10(notaFaturada.getProdutosIvaDez());
				dto.setMontoIva5(notaFaturada.getProdutosIvaCinco());
				dto.setMontoExenta(notaFaturada.getProdutosIvaZero());
				dto.setTotalGeneral(notaFaturada.getVlFatura());
				dto.setCodigoCondicionVenta(codigoCondicaoVenda);
				dto.setMonedaExtranjera("N");
				dto.setImputaIva(parametros.getImpuneIva());
				dto.setImputaIre(parametros.getImpuneIre());
				dto.setImputaIrpRsp(parametros.getImpuneIrpRps());
				dto.setNrComprobanteVenta(" ");
				dto.setTimbradoComprobanteVenta(" ");

			}

			List<IntegracaoVendaHechaukaDto> integracao = new ArrayList<>();
			if(dto != null){
				integracao.add(dto);
			}

			for (Long i = 0L; i < integracao.size(); i++) {
				IntegracaoVendaHechaukaDto dtoVenda = integracao.get(i.intValue());
				integracaoEchaukaList.add(dtoVenda);
			}
		}

		return integracaoEchaukaList;
	}
}