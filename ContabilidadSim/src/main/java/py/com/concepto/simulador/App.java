package py.com.concepto.simulador;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import py.com.concepto.simulador.model.SimulationResult;
import py.com.concepto.simulador.model.Venda;

public class App extends JFrame {
    private DatabaseService dbService = new DatabaseService();
    private SimulationService simService = new SimulationService();
    private SimulationResult lastResult;
    private List<Venda> lastVendas;

    // GUI Components
    private JTextField txtHost = new JTextField("localhost", 15);
    private JTextField txtUser = new JTextField("root", 15);
    private JPasswordField txtPass = new JPasswordField("84125497", 15);
    private JComboBox<String> cbDatabase = new JComboBox<>();
    private JComboBox<String> cbBoca = new JComboBox<>();
    
    private DatePicker datePickerInicio;
    private DatePicker datePickerFin;
    private JTextField txtMontoMax = new JTextField("0", 10);

    private JLabel lblStatus = new JLabel("Status: Desconectado");
    private JProgressBar progressBar = new JProgressBar();
    private JLabel lblResVentas = new JLabel("Ventas: -");
    private JLabel lblResOriginal = new JLabel("Total Original: -");
    private JLabel lblResSimulado = new JLabel("Total Simulado: -");
    private JLabel lblResDiferencia = new JLabel("Diferencia: -");

    private JTextArea txtLog = new JTextArea();
    private JScrollPane scrollLog = new JScrollPane(txtLog);

    private JButton btnSimular = new JButton("Correr Simulación");
    private JButton btnFacturar = new JButton("PROCESAR Y FACTURAR (REAL)");
    private JButton btnConnect = new JButton("Conectar y Listar BD");
    private JButton btnExport = new JButton("edisys (xls)");
    private JButton btnReportePdf = new JButton("Generar LIBRO VENTA (PDF)");
    private JButton btnRG90 = new JButton("Generar RG90 (PDF)");
    private JButton btnRG90Csv = new JButton("Generar RG90 (CSV)");
    private JButton btnCancelar = new JButton("DETENER PROCESO");

    private ReportService reportService = new ReportService();
    private volatile boolean cancelled = false;

    public App() {
        setTitle("Flextech - Simulador Auto Impressor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 950);
        setLayout(new BorderLayout());

        btnCancelar.setBackground(Color.DARK_GRAY);
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setEnabled(false);

        txtLog.setEditable(false);
        txtLog.setBackground(new Color(245, 245, 245));
        txtLog.setFont(new Font("Monospaced", Font.PLAIN, 12));
        scrollLog.setBorder(BorderFactory.createTitledBorder("Registro de Actividad"));

        DatePickerSettings settingsInicio = new DatePickerSettings(new Locale("es", "PY"));
        settingsInicio.setFormatForDatesCommonEra("dd/MM/yyyy");
        datePickerInicio = new DatePicker(settingsInicio);
        datePickerInicio.setDate(LocalDate.now());

        DatePickerSettings settingsFin = new DatePickerSettings(new Locale("es", "PY"));
        settingsFin.setFormatForDatesCommonEra("dd/MM/yyyy");
        datePickerFin = new DatePicker(settingsFin);
        datePickerFin.setDate(LocalDate.now());

        JPanel pnlConn = new JPanel(new GridLayout(0, 2, 5, 5));
        pnlConn.setBorder(BorderFactory.createTitledBorder("Conexión"));
        pnlConn.add(new JLabel("Host:")); pnlConn.add(txtHost);
        pnlConn.add(new JLabel("Usuario:")); pnlConn.add(txtUser);
        pnlConn.add(new JLabel("Contraseña:")); pnlConn.add(txtPass);
        pnlConn.add(btnConnect);
        pnlConn.add(cbDatabase);

        JPanel pnlFiltros = new JPanel(new GridLayout(0, 2, 5, 5));
        pnlFiltros.setBorder(BorderFactory.createTitledBorder("Filtros y Reportes"));
        pnlFiltros.add(new JLabel("Boca:")); pnlFiltros.add(cbBoca);
        pnlFiltros.add(new JLabel("Fecha Inicio (DD/MM/AAAA):")); pnlFiltros.add(datePickerInicio);
        pnlFiltros.add(new JLabel("Fecha Fin (DD/MM/AAAA):")); pnlFiltros.add(datePickerFin);
        pnlFiltros.add(new JLabel("Monto Máx Item:")); pnlFiltros.add(txtMontoMax);
        pnlFiltros.add(btnSimular);
        pnlFiltros.add(btnExport);
        pnlFiltros.add(new JLabel("Reporte Legal:")); pnlFiltros.add(btnReportePdf);
        pnlFiltros.add(new JLabel("Reporte RG90:")); 
        JPanel pnlRG90Buttons = new JPanel(new GridLayout(1, 2, 5, 5));
        pnlRG90Buttons.add(btnRG90);
        pnlRG90Buttons.add(btnRG90Csv);
        pnlFiltros.add(pnlRG90Buttons);

        btnFacturar.setBackground(new Color(255, 100, 100));
        btnFacturar.setForeground(Color.WHITE);
        pnlFiltros.add(btnFacturar);
        pnlFiltros.add(btnCancelar);

        JPanel pnlResult = new JPanel(new GridLayout(0, 1, 5, 5));
        pnlResult.setBorder(BorderFactory.createTitledBorder("Resultados Simulación"));
        pnlResult.add(lblResVentas);
        pnlResult.add(lblResOriginal);
        pnlResult.add(lblResSimulado);
        pnlResult.add(lblResDiferencia);

        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.add(pnlResult, BorderLayout.NORTH);
        pnlCenter.add(scrollLog, BorderLayout.CENTER);

        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.add(pnlConn, BorderLayout.NORTH);
        pnlNorth.add(pnlFiltros, BorderLayout.CENTER);

        JPanel pnlSouth = new JPanel(new BorderLayout());
        progressBar.setVisible(false);
        pnlSouth.add(progressBar, BorderLayout.NORTH);
        pnlSouth.add(lblStatus, BorderLayout.SOUTH);

        add(pnlNorth, BorderLayout.NORTH);
        add(pnlCenter, BorderLayout.CENTER);
        add(pnlSouth, BorderLayout.SOUTH);

        setLocationRelativeTo(null);

        btnConnect.addActionListener(e -> connect());
        cbDatabase.addActionListener(e -> initDatabase());
        btnSimular.addActionListener(e -> runSimulation(false, null));
        btnExport.addActionListener(e -> exportarEdisysXls());
        btnReportePdf.addActionListener(e -> generarReportePdf());
        btnRG90.addActionListener(e -> generarReporteRG90(false));
        btnRG90Csv.addActionListener(e -> generarReporteRG90(true));
        btnFacturar.addActionListener(e -> runRealExecution());
        btnCancelar.addActionListener(e -> {
            cancelled = true;
            btnCancelar.setEnabled(false);
            log("!!! Solicitando detención del proceso...");
        });
    }

    private void generarReportePdf() {
        LocalDate inicio = datePickerInicio.getDate();
        LocalDate fin = datePickerFin.getDate();
        if (inicio == null || fin == null) {
            JOptionPane.showMessageDialog(this, "Seleccione fechas válidas");
            return;
        }

        setLoading(true, "Status: Generando Reporte PDF...");
        new Thread(() -> {
            try {
                log("--- Generando Reporte Libro Venta PDF ---");
                log("Periodo: " + inicio + " al " + fin);
                
                String bocaStr = (String) cbBoca.getSelectedItem();
                List<py.com.concepto.simulador.model.LivroVendaDto> datos = dbService.relatorioLivroVenda(inicio.toString(), fin.toString(), bocaStr);
                
                if (datos.isEmpty()) {
                    log("!!! No se encontraron datos para el periodo seleccionado.");
                    setLoading(false, "Status: Sin datos.");
                    JOptionPane.showMessageDialog(this, "No hay datos para reportar en esas fechas.");
                    return;
                }

                // Formatear fechas a DD-MM-YYYY para el filtro
                java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
                String filtros = inicio.format(dtf) + " al " + fin.format(dtf);
                
                // Formatear fecha para el nombre del archivo (DDMMYYYY)
                java.time.format.DateTimeFormatter dtfFile = java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy");
                String fechaFile = inicio.format(dtfFile);
                
                String bocaSuffix = "TODAS";
                if (bocaStr != null && !bocaStr.equals("[TODAS]")) {
                    filtros += " - Boca: " + bocaStr;
                    bocaSuffix = bocaStr;
                }

                String filename = "LibroVenta_" + fechaFile + "_" + bocaSuffix + ".pdf";
                String rutaPdf = getOutputPath(filename);
                
                String moneda = datos.get(0).getMoeda().getNome();
                
                // Obtener datos reales de la Filial/Empresa
                py.com.concepto.model.entity.Filial filial = dbService.getFilialData();
                
                reportService.generarLibroVentaPdf(datos, rutaPdf, filtros, moneda, filial);
                
                log("--- Reporte Generado Exitosamente ---");
                log("Archivo: " + rutaPdf);
                setLoading(false, "Status: PDF Generado.");
                JOptionPane.showMessageDialog(this, "Reporte generado con éxito:\n" + rutaPdf);
            } catch (Exception ex) {
                log("ERROR al generar reporte: " + ex.getMessage());
                setLoading(false, "Error en Reporte.");
                JOptionPane.showMessageDialog(this, "Error al generar reporte: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }

    private String getOutputPath(String filename) {
        String dbName = (String) cbDatabase.getSelectedItem();
        if (dbName == null || dbName.isEmpty()) {
            return filename;
        }

        // Formatear nombre de base de datos (comercial_manuel -> Comercial Manuel)
        StringBuilder formattedName = new StringBuilder();
        String[] parts = dbName.split("_");
        for (String part : parts) {
            if (!part.isEmpty()) {
                formattedName.append(Character.toUpperCase(part.charAt(0)))
                             .append(part.substring(1).toLowerCase())
                             .append(" ");
            }
        }
        String folderName = formattedName.toString().trim();

        java.io.File directory = new java.io.File(folderName);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        return folderName + java.io.File.separator + filename;
    }

    private void generarReporteRG90(boolean isCsv) {
        LocalDate inicio = datePickerInicio.getDate();
        LocalDate fin = datePickerFin.getDate();
        if (inicio == null || fin == null) {
            JOptionPane.showMessageDialog(this, "Seleccione fechas válidas");
            return;
        }

        setLoading(true, "Status: Generando Reporte RG90 (" + (isCsv ? "CSV" : "PDF") + ")...");
        new Thread(() -> {
            try {
                log("--- Generando Reporte RG90 " + (isCsv ? "CSV" : "PDF") + " ---");
                log("Periodo: " + inicio + " al " + fin);
                
                String bocaStr = (String) cbBoca.getSelectedItem();
                List<py.com.concepto.simulador.model.IntegracaoVendaHechaukaDto> datos = dbService.relatorioRG90(inicio.toString(), fin.toString(), bocaStr);
                
                if (datos.isEmpty()) {
                    log("!!! No se encontraron datos para el periodo seleccionado.");
                    setLoading(false, "Status: Sin datos.");
                    JOptionPane.showMessageDialog(this, "No hay datos para reportar en esas fechas.");
                    return;
                }

                // Formatear fechas para el nombre del archivo (DDMMYYYY)
                java.time.format.DateTimeFormatter dtfFile = java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy");
                String fechaFile = inicio.format(dtfFile);
                
                String bocaSuffix = "TODAS";
                if (bocaStr != null && !bocaStr.equals("[TODAS]")) {
                    bocaSuffix = bocaStr;
                }

                String ext = isCsv ? ".csv" : ".pdf";
                String filename = "RG90_" + fechaFile + "_" + bocaSuffix + ext;
                String rutaDestino = getOutputPath(filename);
                
                py.com.concepto.model.entity.Filial filial = dbService.getFilialData();
                String moneda = "GUARANI";
                String usuario = txtUser.getText();

                if (isCsv) {
                    reportService.generarRG90Csv(datos, rutaDestino, moneda, filial.getDescricao(), usuario);
                } else {
                    reportService.generarRG90Pdf(datos, rutaDestino, moneda, filial.getDescricao(), usuario);
                }
                
                log("--- Reporte RG90 Generado Exitosamente ---");
                log("Archivo: " + rutaDestino);
                setLoading(false, "Status: Reporte Generado.");
                JOptionPane.showMessageDialog(this, "Reporte generado con éxito:\n" + rutaDestino);
            } catch (Exception ex) {
                log("ERROR al generar reporte RG90: " + ex.getMessage());
                setLoading(false, "Error en Reporte RG90.");
                JOptionPane.showMessageDialog(this, "Error al generar reporte RG90: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(message + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }

    private void setLoading(boolean loading, String statusText) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setIndeterminate(loading);
            progressBar.setVisible(loading);
            lblStatus.setText(statusText);
            btnSimular.setEnabled(!loading);
            btnFacturar.setEnabled(!loading);
            btnConnect.setEnabled(!loading);
            btnExport.setEnabled(!loading);
            btnCancelar.setEnabled(loading);
            setCursor(loading ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) : Cursor.getDefaultCursor());
        });
    }

    private void connect() {
        setLoading(true, "Status: Conectando...");
        new Thread(() -> {
            try {
                dbService.connect(txtHost.getText(), txtUser.getText(), new String(txtPass.getPassword()), null);
                List<String> dbs = dbService.listDatabases();
                SwingUtilities.invokeLater(() -> {
                    cbDatabase.removeAllItems();
                    for (String db : dbs) cbDatabase.addItem(db);
                });
                setLoading(false, "Status: Conectado. Seleccione BD.");
            } catch (Exception ex) {
                setLoading(false, "Status: Error conexión.");
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }).start();
    }

    private void initDatabase() {
        String db = (String) cbDatabase.getSelectedItem();
        if (db == null) return;
        setLoading(true, "Status: Inicializando BD " + db + "...");
        new Thread(() -> {
            try {
                dbService.connect(txtHost.getText(), txtUser.getText(), new String(txtPass.getPassword()), db);
                List<String> bocas = dbService.listBocas();
                SwingUtilities.invokeLater(() -> {
                    cbBoca.removeAllItems();
                    cbBoca.addItem("[TODAS]");
                    for (String b : bocas) cbBoca.addItem(b);
                });
                setLoading(false, "Status: BD " + db + " lista.");
            } catch (Exception ex) {
                setLoading(false, "Error init DB: " + ex.getMessage());
            }
        }).start();
    }

    private void runSimulation(boolean silent, Runnable onDone) {
        LocalDate inicio = datePickerInicio.getDate();
        LocalDate fin = datePickerFin.getDate();
        if (inicio == null || fin == null) {
            if (!silent) JOptionPane.showMessageDialog(this, "Seleccione fechas válidas");
            return;
        }

        if (!silent) {
            txtLog.setText("");
            log("--- Iniciando Simulación ---");
            log("Periodo: " + inicio + " al " + fin);
            log("Boca: " + cbBoca.getSelectedItem());
        }

        cancelled = false;
        setLoading(true, "Status: Corriendo simulación...");
        new Thread(() -> {
            try {
                String boca = (String) cbBoca.getSelectedItem();
                BigDecimal montoMax = new BigDecimal(txtMontoMax.getText().replace(",", "."));
                Long clientePadrao = dbService.getClientePadrao();
                
                java.util.function.Consumer<String> logger = (msg) -> log(msg);
                java.util.function.BooleanSupplier checkCancelled = () -> cancelled;

                lastVendas = dbService.getVendasForSimulation(inicio.toString(), fin.toString(), boca, checkCancelled);
                
                lastResult = simService.runSimulation(lastVendas, clientePadrao, montoMax, logger, checkCancelled);
                
                SwingUtilities.invokeLater(() -> {
                    updateResultsUI();
                    if (!silent) {
                        lblStatus.setText("Status: Simulación completada.");
                        log("--- Simulación Finalizada ---");
                        log("Ventas procesadas: " + lastResult.getCountVendas());
                    }
                    if (onDone != null) onDone.run();
                });
            } catch (InterruptedException iex) {
                log("!!! PROCESO CANCELADO POR EL USUARIO.");
                setLoading(false, "Status: Cancelado.");
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    if (!silent) {
                        log("ERROR: " + ex.getMessage());
                        JOptionPane.showMessageDialog(this, "Error simulación: " + ex.getMessage());
                    }
                });
                ex.printStackTrace();
            } finally {
                if (onDone == null) setLoading(false, lblStatus.getText());
            }
        }).start();
    }

    private void runRealExecution() {
        String boca = (String) cbBoca.getSelectedItem();
        if (boca == null || boca.equals("[TODAS]")) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar una BOCA específica para facturar.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿ESTÁ SEGURO?\nEsta acción modificará los totales de las facturas en la base de datos legal.\nBoca: " + boca,
            "Confirmación de Facturación Real", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) return;

        runSimulation(true, () -> {
            log("\n--- INICIANDO EJECUCIÓN REAL ---");
            cancelled = false;
            setLoading(true, "Status: Ejecutando cambios reales en base de datos...");
            new Thread(() -> {
                try {
                    dbService.setAutoCommit(false); // Iniciar Transacción
                    log("Limpiando tablas temporales...");
                    dbService.limpaTabelasAutoImpressao();
                    
                    int count = 0;
                    for (Venda v : lastVendas) {
                        if (cancelled) {
                            log("!!! CANCELACIÓN DETECTADA. Realizando Rollback...");
                            dbService.rollback();
                            throw new InterruptedException("Proceso abortado por el usuario");
                        }
                        dbService.saveProcessamento(v, v.getItens(), v.getVlTotal());
                        count++;
                        if (count % 10 == 0) log("Guardando procesos... (" + count + "/" + lastVendas.size() + ")");
                    }
                    
                    log("Finalizando facturación y recalculando IVAs...");
                    dbService.finalizarFacturacion(boca);
                    
                    dbService.commit(); // Todo salió bien, confirmar cambios
                    log("--- PROCESO COMPLETADO ---");
                    setLoading(false, "Status: ¡FACTURACIÓN COMPLETADA CON ÉXITO!");
                    JOptionPane.showMessageDialog(this, "Proceso finalizado correctamente.");
                } catch (InterruptedException iex) {
                    log("!!! PROCESO DETENIDO. No se aplicaron cambios a la base de datos.");
                    setLoading(false, "Status: Detenido.");
                } catch (Exception ex) {
                    try { dbService.rollback(); } catch (Exception e) {}
                    log("ERROR CRÍTICO: " + ex.getMessage());
                    setLoading(false, "ERROR en ejecución real.");
                    JOptionPane.showMessageDialog(this, "ERROR en ejecución real: " + ex.getMessage());
                    ex.printStackTrace();
                } finally {
                    try { dbService.setAutoCommit(true); } catch (Exception e) {}
                }
            }).start();
        });
    }

    private void updateResultsUI() {
        DecimalFormat df = new DecimalFormat("#,##0");
        lblResVentas.setText("Ventas: " + lastResult.getCountVendas());
        lblResOriginal.setText("Total Original: " + df.format(lastResult.getTotalOriginal()));
        lblResSimulado.setText("Total Simulado: " + df.format(lastResult.getTotalSimulado()));
        lblResDiferencia.setText("Diferencia: " + df.format(lastResult.getDiferenca()));
    }

    private void exportarEdisysXls() {
        LocalDate inicio = datePickerInicio.getDate();
        LocalDate fin = datePickerFin.getDate();
        if (inicio == null || fin == null) {
            JOptionPane.showMessageDialog(this, "Seleccione fechas válidas");
            return;
        }

        setLoading(true, "Status: Generando Edisys XLS Detallado...");
        new Thread(() -> {
            try {
                log("--- Generando Reporte Edisys XLS Detallado (Datos Legales) ---");
                
                String bocaStr = (String) cbBoca.getSelectedItem();
                // Llamamos a la lógica real de Edisys (desglose por tasas de IVA)
                List<py.com.concepto.simulador.model.IntegracaoVendaEdisysDto> datos = dbService.relatorioEdisys(inicio.toString(), fin.toString(), bocaStr);
                
                if (datos.isEmpty()) {
                    log("!!! No se encontraron datos para exportar.");
                    setLoading(false, "Status: Sin datos.");
                    JOptionPane.showMessageDialog(this, "No hay datos para exportar.");
                    return;
                }

                java.time.format.DateTimeFormatter dtfFile = java.time.format.DateTimeFormatter.ofPattern("ddMMyyyy");
                String fechaFile = inicio.format(dtfFile);
                String bocaSuffix = (bocaStr != null && !bocaStr.equals("[TODAS]")) ? bocaStr : "TODAS";

                String filename = "Edisys_" + fechaFile + "_" + bocaSuffix + ".xls";
                String rutaXls = getOutputPath(filename);
                
                py.com.concepto.model.entity.Filial filial = dbService.getFilialData();
                String usuario = txtUser.getText();

                reportService.generarEdisysXls(datos, rutaXls, "GUARANI", filial.getDescricao(), usuario);
                
                log("--- Exportación Exitosa ---");
                log("Archivo: " + rutaXls);
                setLoading(false, "Status: XLS Generado.");
                JOptionPane.showMessageDialog(this, "Reporte Edisys generado con éxito:\n" + rutaXls);
            } catch (Exception ex) {
                log("ERROR al exportar Edisys: " + ex.getMessage());
                setLoading(false, "Error en Exportación.");
                JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage());
                ex.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        Locale.setDefault(new Locale("es", "PY"));
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}
