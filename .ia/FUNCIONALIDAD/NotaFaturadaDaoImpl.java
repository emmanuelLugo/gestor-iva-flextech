package py.com.concepto.dao.impl;

import java.math.RoundingMode;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import br.com.db1.myBatisPersistence.dao.GenericMyBatisDaoImpl;
import br.com.db1.myBatisPersistence.util.MyBatisUtil;
import py.com.concepto.dao.NotaFaturadaDao;
import py.com.concepto.model.entity.NotaFaturada;
import py.com.concepto.model.entity.Timbrado;
import py.com.concepto.model.entity.Venda;
import py.com.concepto.model.entity.dto.RelatorioAditoriaFaturaLegalDto;
import py.com.concepto.util.exception.ConceptoValidateException;



@Repository("notaFaturadaDao")
public class NotaFaturadaDaoImpl extends GenericMyBatisDaoImpl<NotaFaturada, Long> implements NotaFaturadaDao {

	public NotaFaturadaDaoImpl() {
		super(NotaFaturada.class); 
	}

	@Override
	public Long findNumeroNota(Timbrado timbrado) {
		return (Long) this.getSqlSession().selectOne("findUltimaNotaValida", timbrado.getId());
	}
	
	@Override
	public List<NotaFaturada> findNotasByVenda(Venda venda) {
		return this.getSqlSession().selectList("findNotasByVenda", MyBatisUtil.createMap("idVenda", venda.getId()));
	}



	@Override
	public NotaFaturada findNotasSemVenda(Long idTimbrado, String nrFatura) {
		return (NotaFaturada) this.getSqlSession().selectOne("findNotasSemVenda", MyBatisUtil.createMap("idTimbrado", idTimbrado, "nrFatura", nrFatura));
	}
	
	
	@Override
	public Long findNumeroFaturaLegalAutoImpressor(long idTimbrado, String bocaFaturacao ,String tipoDocumento) {
		if(bocaFaturacao == null){
			throw new ConceptoValidateException("No existe una boca de facturación configurada");
		}
		Long rnNota = (Long) this.getSqlSession().selectOne("findNumeroFaturaLegalAutoImpressor", MyBatisUtil.createMap("idTimbrado", idTimbrado, "bocaFaturacao", bocaFaturacao, "tipoDocumento", tipoDocumento));
		if(rnNota == 0){
			throw new ConceptoValidateException("No existe un timbrado vigente");
		}
		return rnNota;
	}

	@Override
	public Long findNumeroFaturaLegalAutoImpressorNoCache(long idTimbrado, String bocaFaturacao ,String tipoDocumento) {
		if(bocaFaturacao == null){
			throw new ConceptoValidateException("No existe una boca de facturación configurada");
		}
		java.util.Map<String, Object> params = br.com.db1.myBatisPersistence.util.MyBatisUtil.createMap("idTimbrado", idTimbrado, "bocaFaturacao", bocaFaturacao, "tipoDocumento", tipoDocumento);
		params.put("dummy", System.nanoTime()); // Forzamos bypass de caché L1
		Long rnNota = (Long) this.getSqlSession().selectOne("findNumeroFaturaLegalAutoImpressorNoCache", params);
		if(rnNota == 0){
			throw new ConceptoValidateException("No existe un timbrado vigente");
		}
		return rnNota;
	}
	
	@Override
	public List<RelatorioAditoriaFaturaLegalDto> relatorioAuditoriaFaturaLegalAutoImpressor(String condition) {
		return this.getSqlSession().selectList("relatorioAuditoriaFaturasLegaisAutoImpressor", MyBatisUtil.createMap("condition", condition));
	}

	
	
	
	
	/** MODULO AUTO IMPRESSOR **/
	@Override
	public NotaFaturada findNotaByVenda(Venda venda) {
		return (NotaFaturada) this.getSqlSession().selectOne("findNotaByVenda", MyBatisUtil.createMap("idVenda", venda.getId()));
	}
	
	@Override
	public NotaFaturada findNotaSimplesByVenda(Venda venda,Long nrFatura) {
		return (NotaFaturada) this.getSqlSession().selectOne("findNotaSimplesByVenda", MyBatisUtil.createMap("idVenda", venda.getId(),"nrFatura", nrFatura));
	}
	
	@Override
	public NotaFaturada findVendaByNrFatura(Long nrFactura, Timbrado timbrado) {
		return (NotaFaturada) this.getSqlSession().selectOne("findVendaByNrFatura", MyBatisUtil.createMap("nrFatura", nrFactura, "idTimbrado", timbrado.getId()));
	}
	
	@Override
	public void atualizaNumeroFatura(Long nrFatura, NotaFaturada nota) {
		this.getSqlSession().update("actualizaNrFatura", MyBatisUtil.createMap("nrFatura", nrFatura, "id", nota.getTimbrado().getId()));
		
	}
	

}