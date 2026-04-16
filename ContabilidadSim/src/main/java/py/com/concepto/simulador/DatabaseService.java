package py.com.concepto.simulador;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import py.com.concepto.simulador.model.*;
import java.math.BigDecimal;

public class DatabaseService {
    private Connection connection;

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
