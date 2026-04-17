package py.com.concepto.simulador;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import py.com.concepto.model.entity.Filial;
import py.com.concepto.model.entity.Moeda;
import py.com.concepto.model.entity.ParametrizacaoEdisys;
import py.com.concepto.simulador.model.IntegracaoVendaHechaukaDto;
import py.com.concepto.simulador.model.ItemVenda;
import py.com.concepto.simulador.model.LivroVendaDto;
import py.com.concepto.simulador.model.Venda;

public class DatabaseService {
    private Connection connection;

    public Filial getFilialData() throws SQLException {
        Filial f = new Filial();
        String sql = "SELECT E.DS_EMPRESA, F.RUC, F.ENDERECO " +
                     "FROM filial F " +
                     "INNER JOIN empresa E ON E.ID_EMPRESA = F.ID_EMPRESA " +
                     "LIMIT 1"; // Ajustar si hay múltiples filiales
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                f.setDescricao(rs.getString("DS_EMPRESA"));
                f.setRuc(rs.getString("RUC"));
                f.setEndereco(rs.getString("ENDERECO"));
            }
        }
        return f;
    }

    public List<LivroVendaDto> relatorioLivroVenda(String fechaInicio, String fechaFin, String bocaFiltro) throws SQLException {
        List<LivroVendaDto> dtoList = new ArrayList<>();
        
        // Obtener moneda base
        Moeda monedaBase = new Moeda();
        String sqlMoeda = "SELECT VALOR FROM sys_parametros WHERE PARAMETRO = 'MOEDA_BASE_VENDA'";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sqlMoeda)) {
            if (rs.next()) {
                if ("GUARANI".equals(rs.getString(1))) {
                    monedaBase.setId(1L);
                    monedaBase.setNome("GUARANI");
                } else {
                    monedaBase.setId(2L);
                    monedaBase.setNome("DOLAR");
                }
            }
        }

        StringBuilder sql = new StringBuilder("SELECT " +
                     "NF.DT_FATURA, NF.NR_FATURA, NF.NR_BOCA, NF.NR_FILIAL, NF.BO_CANCELADO, NF.BO_CONTADO, " +
                     "NF.PRODUTOS_IVA_DEZ, NF.PRODUTOS_IVA_CINCO, NF.PRODUTOS_IVA_ZERO, " +
                     "NF.IVA_DEZ, NF.IVA_CINCO, NF.VL_FATURA, " +
                     "T.TIMBRADO, " +
                     "P.NOME, P.RUC " +
                     "FROM CON_NOTA_FATURADA NF " +
                     "INNER JOIN CON_TIMBRADO T ON T.ID_TIMBRADO = NF.ID_TIMBRADO " +
                     "INNER JOIN BS_PESSOA P ON P.ID_PESSOA = NF.ID_PESSOA " +
                     "WHERE DATE(NF.DT_FATURA) BETWEEN ? AND ? ");

        boolean hasBoca = bocaFiltro != null && !bocaFiltro.trim().isEmpty() && !bocaFiltro.equals("[TODAS]");
        if (hasBoca) {
            sql.append(" AND NF.NR_BOCA = ? ");
        }
        
        sql.append(" ORDER BY NF.NR_FATURA ASC");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            pstmt.setString(1, fechaInicio);
            pstmt.setString(2, fechaFin);
            if (hasBoca) {
                pstmt.setString(3, bocaFiltro);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LivroVendaDto dto = new LivroVendaDto();
                    dto.setMoeda(monedaBase);
                    dto.setCliente(rs.getString("NOME"));
                    dto.setRuc(rs.getString("RUC"));
                    dto.setDtVenda(rs.getDate("DT_FATURA"));
                    dto.setDtInicial(rs.getDate("DT_FATURA"));
                    
                    String timbrado = rs.getString("TIMBRADO");
                    int filial = rs.getInt("NR_FILIAL");
                    int boca = rs.getInt("NR_BOCA");
                    int facturaNum = rs.getInt("NR_FATURA");
                    
                    String nrFaturaStr = String.format("%03d-%03d-%07d", filial, boca, facturaNum);
                    dto.setNrFatura(nrFaturaStr);
                    dto.setTimbrado(timbrado);
                    dto.setNrDocumento(timbrado + " " + nrFaturaStr);

                    boolean cancelado = rs.getBoolean("BO_CANCELADO");
                    if (cancelado) {
                        dto.setTipoDocumento("ANULADO");
                        dto.setVlGravada10(BigDecimal.ZERO);
                        dto.setVlGravada5(BigDecimal.ZERO);
                        dto.setVlIva10(BigDecimal.ZERO);
                        dto.setVlIva5(BigDecimal.ZERO);
                        dto.setVlTotalExcento(BigDecimal.ZERO);
                    } else {
                        boolean contado = rs.getBoolean("BO_CONTADO");
                        dto.setTipoDocumento(contado ? "CONTADO" : "CREDITO");
                        
                        BigDecimal prodIva10 = rs.getBigDecimal("PRODUTOS_IVA_DEZ");
                        BigDecimal iva10 = rs.getBigDecimal("IVA_DEZ");
                        BigDecimal prodIva5 = rs.getBigDecimal("PRODUTOS_IVA_CINCO");
                        BigDecimal iva5 = rs.getBigDecimal("IVA_CINCO");
                        BigDecimal prodIva0 = rs.getBigDecimal("PRODUTOS_IVA_ZERO");

                        dto.setVlGravada10(prodIva10.subtract(iva10));
                        dto.setVlGravada5(prodIva5.subtract(iva5));
                        dto.setVlIva10(iva10);
                        dto.setVlIva5(iva5);
                        dto.setVlTotalExcento(prodIva0);
                    }
                    dtoList.add(dto);
                }
            }
        }
        return dtoList;
    }

    public ParametrizacaoEdisys getParametrizacaoEdisys() throws SQLException {
        ParametrizacaoEdisys p = new ParametrizacaoEdisys();
        String sql = "SELECT * FROM CON_PARAMETRIZACAO_EDISYS LIMIT 1";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                p.setId(rs.getLong("ID_PARAMETRIZACAO"));
                p.setImpuneIva(rs.getString("IMPUNE_IVA"));
                p.setImpuneIre(rs.getString("IMPUNE_IRE"));
                p.setImpuneIrpRps(rs.getString("IMPUNE_IRP_RSP"));
                // Add more fields if needed, but these are the ones used in RG90
            }
        }
        return p;
    }

    public List<IntegracaoVendaHechaukaDto> relatorioRG90(String fechaInicio, String fechaFin, String bocaFiltro) throws SQLException {
        List<IntegracaoVendaHechaukaDto> dtoList = new ArrayList<>();
        ParametrizacaoEdisys parametrosEdisys = getParametrizacaoEdisys();
        Long idConsumidorFinal = getClientePadrao();

        StringBuilder sql = new StringBuilder("SELECT " +
                     "NF.ID_NOTA_FATURADA, NF.DT_FATURA, NF.NR_FATURA, NF.NR_BOCA, NF.NR_FILIAL, NF.BO_CANCELADO, NF.BO_CONTADO, " +
                     "NF.PRODUTOS_IVA_DEZ, NF.PRODUTOS_IVA_CINCO, NF.PRODUTOS_IVA_ZERO, " +
                     "NF.IVA_DEZ, NF.IVA_CINCO, NF.VL_FATURA, " +
                     "T.TIMBRADO, " +
                     "P.ID_PESSOA, P.NOME, P.RUC, " +
                     "V.ID_CONTA_RECEBER " +
                     "FROM CON_NOTA_FATURADA NF " +
                     "INNER JOIN CON_TIMBRADO T ON T.ID_TIMBRADO = NF.ID_TIMBRADO " +
                     "INNER JOIN BS_PESSOA P ON P.ID_PESSOA = NF.ID_PESSOA " +
                     "LEFT JOIN ven_venda V ON V.ID_VENDA = NF.ID_VENDA " +
                     "WHERE DATE(NF.DT_FATURA) BETWEEN ? AND ? ");

        boolean hasBoca = bocaFiltro != null && !bocaFiltro.trim().isEmpty() && !bocaFiltro.equals("[TODAS]");
        if (hasBoca) {
            sql.append(" AND NF.NR_BOCA = ? ");
        }
        
        sql.append(" ORDER BY NF.NR_FATURA ASC");

        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            pstmt.setString(1, fechaInicio);
            pstmt.setString(2, fechaFin);
            if (hasBoca) {
                pstmt.setString(3, bocaFiltro);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    boolean cancelado = rs.getBoolean("BO_CANCELADO");
                    if (cancelado) continue; // RG90 code usually skips canceled or handles them specifically

                    IntegracaoVendaHechaukaDto dto = new IntegracaoVendaHechaukaDto();
                    dto.setTipoRegistro(1L);

                    Long idPessoa = rs.getLong("ID_PESSOA");
                    String ruc = rs.getString("RUC");
                    
                    if (idPessoa.equals(idConsumidorFinal)) {
                        dto.setCodigoIdentificacion(15L);
                        dto.setRuc("X");
                        dto.setNombreCliente("SIN NOMBRE");
                    } else {
                        if (ruc == null) ruc = "";
                        if (ruc.indexOf("-") <= 0) {
                            dto.setCodigoIdentificacion(12L);
                        } else {
                            dto.setCodigoIdentificacion(11L);
                        }
                        String[] array = ruc.split("-");
                        dto.setRuc(array[0]);
                        dto.setNombreCliente(rs.getString("NOME"));
                    }

                    dto.setCodigoComprobante(109L); // Factura
                    dto.setFecha(rs.getDate("DT_FATURA"));
                    dto.setNrTimbrado(rs.getString("TIMBRADO"));
                    
                    String nrFaturaStr = String.format("%03d-%03d-%07d", 
                        rs.getInt("NR_FILIAL"), rs.getInt("NR_BOCA"), rs.getInt("NR_FATURA"));
                    dto.setNroFactura(nrFaturaStr);

                    dto.setMontoIva10(rs.getBigDecimal("PRODUTOS_IVA_DEZ"));
                    dto.setMontoIva5(rs.getBigDecimal("PRODUTOS_IVA_CINCO"));
                    dto.setMontoExenta(rs.getBigDecimal("PRODUTOS_IVA_ZERO"));
                    dto.setTotalGeneral(rs.getBigDecimal("VL_FATURA"));

                    // Condicion Venta
                    Long idContaReceber = rs.getLong("ID_CONTA_RECEBER");
                    if (rs.wasNull()) {
                        // If no venda, use BO_CONTADO from NF
                        dto.setCodigoCondicionVenta(rs.getBoolean("BO_CONTADO") ? 1L : 2L);
                    } else {
                        dto.setCodigoCondicionVenta(2L); // Credito
                    }
                    // Re-check logic if it was actually Contado but has Venda
                    if (idContaReceber == 0) {
                         dto.setCodigoCondicionVenta(rs.getBoolean("BO_CONTADO") ? 1L : 2L);
                    }

                    dto.setMonedaExtranjera("N");
                    dto.setImputaIva(parametrosEdisys.getImpuneIva());
                    dto.setImputaIre(parametrosEdisys.getImpuneIre());
                    dto.setImputaIrpRsp(parametrosEdisys.getImpuneIrpRps());
                    dto.setNrComprobanteVenta(" ");
                    dto.setTimbradoComprobanteVenta(" ");

                    dtoList.add(dto);
                }
            }
        }
        return dtoList;
    }

    public void connect(String host, String user, String password, String database) throws SQLException {
        String url = "jdbc:mysql://" + host + ":3306/" + (database != null ? database : "") + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        connection = DriverManager.getConnection(url, user, password);
    }

    public List<String> listDatabases() throws SQLException {
        List<String> dbs = new ArrayList<>();
        ResultSet rs = connection.getMetaData().getCatalogs();
        while (rs.next()) {
            dbs.add(rs.getString(1));
        }
        return dbs;
    }

    public List<String> listBocas() throws SQLException {
        List<String> bocas = new ArrayList<>();
        String sql = "SELECT nro_boca FROM con_boca_faturacao ORDER BY nro_boca";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                bocas.add(rs.getString(1));
            }
        }
        return bocas;
    }

    public Long getClientePadrao() throws SQLException {
        String sql = "SELECT VALOR FROM sys_parametros WHERE PARAMETRO = 'CLIENTE_PADRAO'";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return Long.valueOf(rs.getString(1));
            }
        }
        return null;
    }

    public List<Venda> getVendasForSimulation(String fechaInicio, String fechaFin, String boca, java.util.function.BooleanSupplier isCancelled) throws SQLException, InterruptedException {
        List<Venda> vendas = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                     "SELECT V.ID_VENDA, V.ID_PESSOA, V.VL_TOTAL, V.VL_DESCONTO, V.BOCA " +
                     "FROM ven_venda V " +
                     "INNER JOIN con_nota_faturada NF ON NF.ID_VENDA = V.ID_VENDA " +
                     "WHERE DATE(NF.DT_FATURA) BETWEEN ? AND ? AND V.BO_CANCELADO = FALSE");

        boolean hasBoca = boca != null && !boca.trim().isEmpty() && !boca.equals("[TODAS]");
        if (hasBoca) {
            sql.append(" AND NF.NR_BOCA = ?");
        }
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            pstmt.setString(1, fechaInicio);
            pstmt.setString(2, fechaFin);
            if (hasBoca) {
                pstmt.setString(3, boca);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    if (isCancelled.getAsBoolean()) throw new InterruptedException("Descarga cancelada");
                    Venda v = new Venda();
                    v.setId(rs.getLong("ID_VENDA"));
                    v.setIdPessoa(rs.getLong("ID_PESSOA"));
                    v.setVlTotal(rs.getBigDecimal("VL_TOTAL"));
                    v.setVlDesconto(rs.getBigDecimal("VL_DESCONTO"));
                    v.setBoca(rs.getString("BOCA"));
                    vendas.add(v);
                }
            }
        }
        
        for (Venda v : vendas) {
            if (isCancelled.getAsBoolean()) throw new InterruptedException("Descarga cancelada");
            v.setItens(getItensVenda(v.getId()));
        }
        return vendas;
    }

    private List<ItemVenda> getItensVenda(Long idVenda) throws SQLException {
        List<ItemVenda> itens = new ArrayList<>();
        String sql = "SELECT IV.ID_ITEM_VENDA, IV.ID_PRODUTO, IV.VL_TOTAL, IV.IVA, IV.QUANTIDADE, IV.VL_PRECO_VENDA, IV.CODIGO_BARRA, P.BO_PROCESSAVEL " +
                     "FROM ven_item_venda IV " +
                     "INNER JOIN est_produto P ON P.ID_PRODUTO = IV.ID_PRODUTO " +
                     "WHERE IV.ID_VENDA = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, idVenda);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ItemVenda item = new ItemVenda();
                    item.setId(rs.getLong("ID_ITEM_VENDA"));
                    item.setIdProducto(rs.getLong("ID_PRODUTO"));
                    item.setVlTotal(rs.getBigDecimal("VL_TOTAL"));
                    item.setIva(rs.getLong("IVA"));
                    item.setQuantidade(rs.getBigDecimal("QUANTIDADE"));
                    item.setVlPrecoVenda(rs.getBigDecimal("VL_PRECO_VENDA"));
                    item.setCodigoBarra(rs.getString("CODIGO_BARRA"));
                    item.setProcessavel(rs.getBoolean("BO_PROCESSAVEL"));
                    itens.add(item);
                }
            }
        }
        return itens;
    }

    public void limpaTabelasAutoImpressao() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("DELETE FROM con_item_processamento_venda_auto_impressor");
            stmt.executeUpdate("DELETE FROM con_processamento_venda_auto_impressor");
        }
    }

    private Long getNextId(String tableName, String idColumn) throws SQLException {
        String sql = "SELECT COALESCE(MAX(" + idColumn + "), 0) + 1 FROM " + tableName;
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        }
        return 1L;
    }

    public void setAutoCommit(boolean value) throws SQLException {
        if (connection != null) connection.setAutoCommit(value);
    }

    public void commit() throws SQLException {
        if (connection != null) connection.commit();
    }

    public void rollback() throws SQLException {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void saveProcessamento(Venda v, List<ItemVenda> filteredItens, BigDecimal totalSimulado) throws SQLException {
        Long idProc = getNextId("con_processamento_venda_auto_impressor", "ID_PROCESSAMENTO");
        
        String sqlProc = "INSERT INTO con_processamento_venda_auto_impressor (ID_PROCESSAMENTO, CD_VENDA, DT_VENDA, ID_PESSOA, ID_VENDA, ID_CONTA_RECEBER, NR_FATURA, ID_TIMBRADO, FILIAL, BOCA, BO_CANCELADO, USUARIO, VL_COT_DOLAR, VL_COT_REAL, VL_COT_GUARANI, VL_TOTAL, ID_FILIAL) " +
                         "SELECT ?, CD_VENDA, DT_VENDA, ID_PESSOA, ID_VENDA, ID_CONTA_RECEBER, NR_FATURA, ID_TIMBRADO, FILIAL, BOCA, BO_CANCELADO, USUARIO, VL_COT_DOLAR, VL_COT_REAL, VL_COT_GUARANI, ?, ID_FILIAL " +
                         "FROM ven_venda WHERE ID_VENDA = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sqlProc)) {
            pstmt.setLong(1, idProc);
            pstmt.setBigDecimal(2, totalSimulado);
            pstmt.setLong(3, v.getId());
            pstmt.executeUpdate();
        }

        Long nextIdItem = getNextId("con_item_processamento_venda_auto_impressor", "ID_ITEM_PROCESSAMENTO");
        String sqlItem = "INSERT INTO con_item_processamento_venda_auto_impressor (ID_ITEM_PROCESSAMENTO, ID_PROCESSAMENTO, ID_PRODUTO, VL_TOTAL, QUANTIDADE, VL_PRECO_VENDA, IVA, CODIGO_BARRA) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sqlItem)) {
            for (ItemVenda item : filteredItens) {
                pstmt.setLong(1, nextIdItem++);
                pstmt.setLong(2, idProc);
                pstmt.setLong(3, item.getIdProducto()); 
                pstmt.setBigDecimal(4, item.getVlSimulado()); 
                pstmt.setBigDecimal(5, item.getQuantidade());
                pstmt.setBigDecimal(6, item.getVlPrecoVenda());
                pstmt.setLong(7, item.getIva());
                pstmt.setString(8, item.getCodigoBarra());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
    }

    public void finalizarFacturacion(String boca) throws SQLException {
        String sqlUpdate = "UPDATE con_nota_faturada nf " +
                          "INNER JOIN con_processamento_venda_auto_impressor p ON p.ID_VENDA = nf.ID_VENDA " +
                          "SET nf.VL_FATURA = p.VL_TOTAL, " +
                          "    nf.BO_PROCESSADO = 1, " +
                          "    nf.PRODUTOS_IVA_DEZ = (SELECT COALESCE(SUM(i.VL_TOTAL),0) FROM con_item_processamento_venda_auto_impressor i WHERE i.ID_PROCESSAMENTO = p.ID_PROCESSAMENTO AND i.IVA = 10), " +
                          "    nf.PRODUTOS_IVA_CINCO = (SELECT COALESCE(SUM(i.VL_TOTAL),0) FROM con_item_processamento_venda_auto_impressor i WHERE i.ID_PROCESSAMENTO = p.ID_PROCESSAMENTO AND i.IVA = 5), " +
                          "    nf.PRODUTOS_IVA_ZERO = (SELECT COALESCE(SUM(i.VL_TOTAL),0) FROM con_item_processamento_venda_auto_impressor i WHERE i.ID_PROCESSAMENTO = p.ID_PROCESSAMENTO AND i.IVA = 0) " +
                          "WHERE (p.BOCA = ? OR ? = '[TODAS]')";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sqlUpdate)) {
            pstmt.setString(1, boca);
            pstmt.setString(2, boca);
            pstmt.executeUpdate();
        }

        String sqlIva = "UPDATE con_nota_faturada SET " +
                        "IVA_DEZ = ROUND(PRODUTOS_IVA_DEZ / 11, 0), " +
                        "IVA_CINCO = ROUND(PRODUTOS_IVA_CINCO / 21, 0), " +
                        "TOTAL_IVA = ROUND(PRODUTOS_IVA_DEZ / 11, 0) + ROUND(PRODUTOS_IVA_CINCO / 21, 0) " +
                        "WHERE BO_PROCESSADO = 1 AND VL_FATURA > 0";
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(sqlIva);
        }
    }
}
