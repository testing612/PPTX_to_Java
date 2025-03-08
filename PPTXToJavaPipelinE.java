import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PPTXToJavaPipeline {
    private static final String LIBRE_OFFICE_CMD = "libreoffice --headless --convert-to png --outdir %s %s";
    
    public static void main(String[] args) throws Exception {
        Path uploadDir = Paths.get("uploads");
        Path outputDir = Paths.get("converted");
        String pptxFile = "presentation.pptx"; // Replace with uploaded file
        
        // 1. Convert PPTX to PNG
        convertToPNG(pptxFile, outputDir);
        
        // 2. Generate Java Code
        int slideCount = countSlides(outputDir);
        generateJavaCode(outputDir, slideCount);
        
        // 3. Package into ZIP
        createZipBundle(outputDir, slideCount);
    }
    
    private static void convertToPNG(String pptxPath, Path outputDir) throws Exception {
        Process process = Runtime.getRuntime().exec(
            String.format(LIBRE_OFFICE_CMD, outputDir, pptxPath)
        );
        if (process.waitFor() != 0) throw new RuntimeException("Conversion failed");
    }
    
    private static int countSlides(Path dir) throws IOException {
        return (int) Files.list(dir)
            .filter(p -> p.toString().endsWith(".png"))
            .count();
    }
    
    private static void generateJavaCode(Path dir, int slideCount) throws IOException {
        String code = String.format("""
            import javax.swing.*;
            import java.awt.*;
            import java.awt.event.*;
            public class SlideViewer extends JFrame {
                private int currentSlide = 0;
                private JLabel label = new JLabel();
                public SlideViewer() {
                    setLayout(new BorderLayout());
                    add(label, BorderLayout.CENTER);
                    addKeyListener(new KeyAdapter() {
                        public void keyPressed(KeyEvent e) {
                            if (e.getKeyCode() == KeyEvent.VK_RIGHT && currentSlide < %d) currentSlide++;
                            if (e.getKeyCode() == KeyEvent.VK_LEFT && currentSlide > 0) currentSlide--;
                            updateSlide();
                        }
                    });
                    setSize(1024, 768);
                    setVisible(true);
                }
                private void updateSlide() {
                    label.setIcon(new ImageIcon("slides/slide" + (currentSlide + 1) + ".png"));
                }
                public static void main(String[] args) { new SlideViewer(); }
            }
            """, slideCount - 1);
        Files.write(dir.resolve("SlideViewer.java"), code.getBytes());
    }
    
    private static void createZipBundle(Path sourceDir, int slides) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream("output.zip"))) {
            // Add Java file
            zos.putNextEntry(new ZipEntry("SlideViewer.java"));
            Files.copy(sourceDir.resolve("SlideViewer.java"), zos);
            
            // Add slides
            for (int i = 1; i <= slides; i++) {
                zos.putNextEntry(new ZipEntry("slides/slide" + i + ".png"));
                Files.copy(sourceDir.resolve("slide" + i + ".png"), zos);
            }
        }
    }
}