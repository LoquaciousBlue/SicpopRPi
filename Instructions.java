import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

public class Instructions implements Iterable<Pair<PrintCommands, Integer>> {
    private List<Pair<PrintCommands, Integer>> cmds;   
    private static final int partitionFactor = 256/13;

    private Instructions(){
        cmds = new LinkedList<>();
    }

    private Instructions(File pdf) throws IOException {
        this();
        Bitmap<Integer> bmImg = convertColorImageToBitmap(convertPdfToBufferedImage(pdf));
        this.generateInstructionsFromBitmap(bmImg);
    }

    public static Instructions generateInstructions(File pdf) throws IOException {
        return new Instructions(pdf);
    }

    private static BufferedImage convertPdfToBufferedImage(File pdf) throws IOException {
        PDDocument doc = PDDocument.load(pdf);
        PDFRenderer rend = new PDFRenderer(doc);
        if (doc.getNumberOfPages() != 1) throw new RuntimeException("PDF was corrupted in transit; retry printing.");
        return rend.renderImageWithDPI(0, 96, ImageType.RGB);
    }

    private Bitmap<Integer> convertColorImageToBitmap(BufferedImage img){
        int width = img.getWidth(), height = img.getHeight();
        Bitmap<Integer> bm = new Bitmap<>(width, height);
        for (int i = 0; i < width; i++)
            for(int j = 0; j < height; j++)
                bm.setValueAt(i, j, convertRgbToDecimalGrayscale(img.getRGB(i, j)));
        return bm;
    }

    private void generateInstructionsFromBitmap(Bitmap<Integer> bm){
        int width = bm.getWidth(), height = bm.getHeight(); 
        for (int i = 0; i < height; i++){
            for(int j = ((i%2==0) ? 0 : width - 1); j != ((i%2==0) ? width : -1); j += (i%2==0) ? 1 : -1){
                int nozCount = bm.getValueAt(j, i);
                Pair<PrintCommands, Integer> newCmd = new Pair<>(PrintCommands.ADVANCE, nozCount);
                if (j == ((i%2==0)? 0 : width - 1)){
                    newCmd.setFirst(PrintCommands.FEED);
                }
                this.cmds.add(newCmd);
            }
        }
        this.cmds.get(0).setFirst(PrintCommands.WAIT); // Don't move on the first dot!
    }

    private static int convertGrayscaleToDecimalGrayscale(int grayscale8Bit){
        return grayscale8Bit/partitionFactor;
    }

    private static int convertRgbToGrayscale(int rgb){
        long rgbCopy = rgb;
        double red, green, blue;
        blue = (0xff & rgbCopy) * 0.114; // Weighted for human eyes
        rgbCopy /= 0x100;
        green = (0xff & rgbCopy) * 0.587; // Weighted for human eyes
        rgbCopy /= 0x100;
        red = (0xff & rgbCopy) * 0.299; // Weighted for human eyes
        rgbCopy /= 0x100;
        return (int)(red + green + blue);
    }

    private static int convertRgbToDecimalGrayscale(int rgb){
        return convertGrayscaleToDecimalGrayscale(convertRgbToGrayscale(rgb));
    }

    public static void reduceScale(String fileName) throws IOException {
        File in = new File(fileName);
        BufferedImage imageToPrint = ImageIO.read(in);
        for (int i = 0; i < 96*3; i++){ // Width
            for (int j = 0; j < 96*5; j++){ // Height
                int gray = convertRgbToGrayscale(imageToPrint.getRGB(i, j));
                gray = convertGrayscaleToDecimalGrayscale(gray) * partitionFactor;
                imageToPrint.setRGB(i, j, (gray*0x10000) + (gray*0x100) + gray);
            }
        }
    }

    @Override
    public Iterator<Pair<PrintCommands, Integer>> iterator() {
        return this.cmds.iterator();
    }
    
}