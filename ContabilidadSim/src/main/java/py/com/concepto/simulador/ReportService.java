package py.com.concepto.simulador;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import py.com.concepto.simulador.model.LivroVendaDto;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import py.com.concepto.model.entity.Filial;

public class ReportService {

    public void generarLibroVentaPdf(List<LivroVendaDto> datos, String rutaDestino, String filtros, String moneda, Filial filial) throws JRException {
        // Cargar el diseño .jrxml desde el classpath (recursos)
        InputStream jrxmlStream = getClass().getResourceAsStream("/reports/LibroVenta.jrxml");
        if (jrxmlStream == null) {
            throw new JRException("No se pudo encontrar el archivo /reports/LibroVenta.jrxml en los recursos.");
        }

        // Compilar el reporte en tiempo de ejecución
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

        // Crear el DataSource a partir de la lista de DTOs
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(datos);

        // Parámetros del reporte
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("P_FILTROS", filtros);
        parameters.put("P_MOEDA_FATURAMENTO", moneda);
        parameters.put("P_REMOVE_CABECALHO", false);
        parameters.put("P_RESUMIDO", false);
        parameters.put("P_OBJECT_FILIAL", filial);
        parameters.put(JRParameter.REPORT_LOCALE, new java.util.Locale("es", "PY"));

        // Llenar el reporte
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // Exportar a PDF
        JasperExportManager.exportReportToPdfFile(jasperPrint, rutaDestino);
    }
}
