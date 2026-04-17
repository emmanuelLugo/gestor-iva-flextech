package py.com.concepto.simulador;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRCsvExporterParameter;
import py.com.concepto.model.entity.Filial;
import py.com.concepto.simulador.model.IntegracaoVendaHechaukaDto;
import py.com.concepto.simulador.model.LivroVendaDto;

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

    public void generarRG90Pdf(List<IntegracaoVendaHechaukaDto> datos, String rutaDestino, String moneda, String filialNombre, String usuario) throws JRException {
        JasperPrint jasperPrint = fillRG90(datos, moneda, filialNombre, usuario);
        JasperExportManager.exportReportToPdfFile(jasperPrint, rutaDestino);
    }

    public void generarRG90Csv(List<IntegracaoVendaHechaukaDto> datos, String rutaDestino, String moneda, String filialNombre, String usuario) throws JRException {
        JasperPrint jasperPrint = fillRG90(datos, moneda, filialNombre, usuario);

        JRCsvExporter exporter = new JRCsvExporter();
        
        // Usamos solo setParameter para evitar el error de mezcla de APIs
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
        exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, rutaDestino);
        exporter.setParameter(JRCsvExporterParameter.FIELD_DELIMITER, ";");

        exporter.exportReport();
    }

    private JasperPrint fillRG90(List<IntegracaoVendaHechaukaDto> datos, String moneda, String filialNombre, String usuario) throws JRException {
        // Cargar el diseño .jrxml desde el classpath (recursos)
        InputStream jrxmlStream = getClass().getResourceAsStream("/reports/ExportacaoVendaRg90.jrxml");
        if (jrxmlStream == null) {
            throw new JRException("No se pudo encontrar el archivo /reports/ExportacaoVendaRg90.jrxml en los recursos.");
        }

        // Compilar el reporte en tiempo de ejecución
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

        // Crear el DataSource a partir de la lista de DTOs
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(datos);

        // Parámetros del reporte
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("P_RESUMIDO", false);
        parameters.put("P_MOEDA_FATURAMENTO", moneda);
        parameters.put("P_FILIAL", filialNombre);
        parameters.put("P_USUARIO", usuario);
        parameters.put(JRParameter.REPORT_LOCALE, new java.util.Locale("es", "PY"));

        // Llenar el reporte
        return JasperFillManager.fillReport(jasperReport, parameters, dataSource);
    }
}
