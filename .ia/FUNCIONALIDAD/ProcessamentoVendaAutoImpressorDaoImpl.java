package py.com.concepto.dao.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import br.com.db1.myBatisPersistence.dao.GenericMyBatisDaoImpl;
import br.com.db1.myBatisPersistence.util.MyBatisUtil;
import py.com.concepto.dao.ProcessamentoVendaAutoImpressorDao;
import py.com.concepto.model.entity.ProcessamentoVendaAutoImpressor;



@Repository("processamentoVendaAutoImpressorDao")
public class ProcessamentoVendaAutoImpressorDaoImpl extends GenericMyBatisDaoImpl<ProcessamentoVendaAutoImpressor, Long> implements ProcessamentoVendaAutoImpressorDao {

	public ProcessamentoVendaAutoImpressorDaoImpl() {
		super(ProcessamentoVendaAutoImpressor.class); 
	}

	@Override
	public void limpaTabelasAutoImpressao() {
		this.getSqlSession().delete("removeItensProcessamento", MyBatisUtil.createMap("id", 1L));
		this.getSqlSession().delete("removeProcessamento", MyBatisUtil.createMap("id", 1L));
	}

	@Override
	public List<ProcessamentoVendaAutoImpressor> findParaReimpressao(String boca) {
		return this.getSqlSession().selectList("findParaReimpressao", MyBatisUtil.createMap("boca", boca));
	}


}