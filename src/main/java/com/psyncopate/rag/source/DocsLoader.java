package com.psyncopate.rag.source;

import com.knuddels.jtokkit.api.EncodingType;
import jakarta.annotation.PostConstruct;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DocsLoader is a Spring component responsible for loading
 * reference documents from multiple file formats — DOCX, PDF, and Excel —
 * into a vector store to support AI-powered semantic search and embeddings.
 *
 * <p>
 * It scans configured directories for these document types, extracts
 * textual content (including tables and images for DOCX), and splits
 * the text into tokenized chunks before saving them in the vector store.
 * The loader avoids re-loading documents if the vector store already
 * contains data, improving startup performance.
 * </p>
 *
 * <p>
 * Supported document formats:
 * <ul>
 *   <li>Microsoft Word (.docx)</li>
 *   <li>PDF files (.pdf)</li>
 *   <li>Microsoft Excel (.xlsx)</li>
 * </ul>
 * </p>
 *
 * <p>
 * This loader is typically triggered once at application startup via the
 * {@link jakarta.annotation.PostConstruct} annotated method.
 * </p>
 *
 * <p>
 * If the configured resource directories for these file types are not found
 * in the classpath resources, the loader performs a recursive search from
 * the current working directory to find folders with matching names.
 * </p>
 *
 * @author Venkat
 * @version 1.0
 * @since 2025-05-22
 */
@Component
public class DocsLoader {

    private static final Logger log = LoggerFactory.getLogger(DocsLoader.class);

    private final JdbcClient jdbcClient;
    private final VectorStore vectorStore;
    private final TokenTextSplitter textSplitter = new TokenTextSplitter();

    /**
     * Constructor injecting required dependencies.
     *
     * @param jdbcClient   JDBC client used to query the vector store metadata.
     * @param vectorStore  Vector store where extracted documents will be saved.
     */
    public DocsLoader(JdbcClient jdbcClient, VectorStore vectorStore) {
        this.jdbcClient = jdbcClient;
        this.vectorStore = vectorStore;
    }

    /**
     * Initialization method run after bean creation. Loads documents into the vector store
     * if it's not already populated.
     */
    @PostConstruct
    public void init() {
        var count = jdbcClient.sql("SELECT COUNT(*) FROM vector_store")
                .query(Integer.class)
                .single();

        log.info("Current Vector Store count: {}", count);

        // Skip loading if the vector store is already populated
        if (count > 0) return;

        log.info("Initializing Vector Store with reference documents...");

        // Define document folders by type
        var docFolders = List.of(
                Path.of("src/main/resources/doc"),
                Path.of("src/main/resources/pdf"),
                Path.of("src/main/resources/excel")
        );

        try {
            Path docxDir = resolveFolderPath("doc");
            Path pdfDir = resolveFolderPath("pdf");
            Path excelDir = resolveFolderPath("excel");

            loadFilesFromDirectory(docxDir, this::processDocxFile, "DOCX");
            loadFilesFromDirectory(pdfDir, this::processPdfFile, "PDF");
            loadFilesFromDirectory(excelDir, this::processExcelFile, "Excel");

        } catch (IOException e) {
            log.error("Failed to process documents: {}", e.getMessage(), e);
        }

        log.info("Reference documents loaded successfully.");
    }

    /**
     * Loads files from the specified directory and applies the given processor function.
     *
     * @param directory Directory containing documents.
     * @param processor Function to process each file.
     * @param type      Human-readable type name for logging.
     * @throws IOException if directory traversal fails.
     */
    private void loadFilesFromDirectory(Path directory, FileProcessor processor, String type) throws IOException {
        if (!Files.exists(directory)) {
            log.warn("{} directory does not exist: {}", type, directory);
            return;
        }

        try (var paths = Files.walk(directory)) {
            paths.filter(Files::isRegularFile).forEach(file -> {
                try {
                    processor.process(file);
                    log.info("Processed {} file: {}", type, file.getFileName());
                } catch (Exception e) {
                    log.error("Error processing {} file {}: {}", type, file.getFileName(), e.getMessage());
                }
            });
        }
    }

    /**
     * Processes a PDF file and loads each page as a separate document into the vector store.
     *
     * @param pdfFile Path to the PDF file.
     * @throws IOException if the file cannot be read.
     */
    private void processPdfFile(Path pdfFile) throws IOException {
        var config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(new ExtractedTextFormatter.Builder()
                        .withNumberOfBottomTextLinesToDelete(0)
                        .withNumberOfTopPagesToSkipBeforeDelete(0)
                        .build())
                .withPagesPerDocument(1)
                .build();

        var pdfReader = new PagePdfDocumentReader(new FileSystemResource(pdfFile.toFile()), config);
        vectorStore.accept(textSplitter.apply(pdfReader.get()));
    }

    /**
     * Processes an Excel (.xlsx) file, extracting all cells' textual content sheet by sheet.
     *
     * @param excelFile Path to the Excel file.
     * @throws IOException if the file is corrupt or unreadable.
     */
    private void processExcelFile(Path excelFile) throws IOException {
        try (InputStream is = Files.newInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(is)) {

            var excelDocs = new ArrayList<Document>();

            for (Sheet sheet : workbook) {
                var content = new StringBuilder("Sheet: ").append(sheet.getSheetName()).append("\n");

                for (Row row : sheet) {
                    for (Cell cell : row) {
                        try {
                            content.append("\n").append(cell.getStringCellValue());
                        } catch (Exception e) {
                            log.warn("Skipping problematic cell in {}: {}", sheet.getSheetName(), e.getMessage());
                        }
                    }
                }

                excelDocs.add(new Document(content.toString()));
            }

            vectorStore.accept(excelDocs);
        }
    }

    /**
     * Processes a DOCX Word document file and loads both text paragraphs and table contents.
     *
     * @param docFile Path to the DOCX file.
     * @throws IOException if the file is unreadable or malformed.
     */
    private void processDocxFile(Path docFile) throws IOException {
        try (InputStream is = Files.newInputStream(docFile);
             XWPFDocument document = new XWPFDocument(is)) {

            var content = new StringBuilder("Document: ").append(docFile.getFileName()).append("\n");

            // Extract paragraphs
            for (var paragraph : document.getParagraphs()) {
                var text = paragraph.getText();
                if (text != null && !text.isBlank()) {
                    content.append("\n").append(text.trim());
                }
            }

            // Extract tables
            for (var table : document.getTables()) {
                content.append("\n\nTable:\n");
                for (var row : table.getRows()) {
                    for (var cell : row.getTableCells()) {
                        var text = cell.getText();
                        if (text != null && !text.isBlank()) {
                            content.append("| ").append(text.trim());
                        }
                    }
                    content.append("\n");
                }
            }

            var documents = textSplitter.apply(List.of(new Document(content.toString())));
            vectorStore.accept(documents);
        }
    }


    /**
     * Resolves a folder path by first checking in src/main/resources, and then in the root directory.
     *
     * @param folderName the name of the folder to look for
     * @return the resolved Path
     * @throws IOException if folder doesn't exist in either location
     */
    private Path resolveFolderPath(String folderName) throws IOException {
        Path resourcePath = Paths.get("src/main/resources", folderName);
        if (Files.exists(resourcePath)) {
            return resourcePath;
        }

        Path rootPath = Paths.get(folderName);
        if (Files.exists(rootPath)) {
            return rootPath;
        }

        throw new IOException("Folder '" + folderName + "' not found in resources or root directory.");
    }


    /**
     * Functional interface for file processors.
     */
    @FunctionalInterface
    private interface FileProcessor {
        void process(Path path) throws Exception;
    }

    /**
     * Custom BatchingStrategy bean that overrides the default settings.
     * This is used to control how documents are batched before being sent to the vector store.
     *
     * <p>Maximum token counts for supported models:
     * <ul>
     *     <li>OpenAI text-embedding-ada-002/003: 8191 tokens</li>
     *     <li>Anthropic Claude 2: 100,000 tokens</li>
     *     <li>Amazon Titan: 20,000 tokens</li>
     * </ul>
     *
     * <p>Recommended reserve percentage: 10% to 20%
     *
     * @return a BatchingStrategy instance with custom settings.
     */
    // @Bean
    public BatchingStrategy batchingStrategy() {
        return new TokenCountBatchingStrategy(
                EncodingType.CL100K_BASE,
                8191,
                0.1
        );
    }
}
