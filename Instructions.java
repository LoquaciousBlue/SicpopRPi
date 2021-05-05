import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.Double;

import javax.imageio.ImageIO;

public class Instructions {

    private Instructions(PdfFile pdf){

    }

    public static Instructions generateInstructions(PdfFile pdf){
        return new Instructions(pdf);
    }

    private Bitmap<Integer> convertColorImageToBitmap(BufferedImage img){
        int width = img.getWidth(), height = img.getHeight();
        Bitmap<Integer> bm = new Bitmap<>(width, height);
        for (int i = 0; i < width; i++)
            for(int j = 0; j < height; j++)
                bm.setValueAt(i, j, this.convertRgbToDecimalGrayscale(img.getRGB(i, j)));
        return bm;
    }

    private static int convertGrayscaleToDecimalGrayscale(int grayscale8Bit){
        return grayscale8Bit/23;
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
        /*
        File in = new File(fileName), 
            out1 = new File(String.format("%s%x.%s", "FullGray", System.currentTimeMillis(), "png")), 
            out2 = new File(String.format("%s%x.%s", "SicpopGray", System.currentTimeMillis(), "png")),
            out3 = new File(String.format("%s%x.%s", "BinaryGray", System.currentTimeMillis(), "png"));
        */
        File in = new File(fileName), 
            out1 = new File("FullGray.png"), 
            out2 = new File("SicpopGray.png"),
            out3 = new File("BinaryGray.png");
        if (    (out1.exists() ? (out1.delete() && out1.createNewFile()) : out1.createNewFile()) 
            &&  (out2.exists() ? (out2.delete() && out2.createNewFile()) : out2.createNewFile())
            &&  (out3.exists() ? (out3.delete() && out3.createNewFile()) : out3.createNewFile())){
            BufferedImage   oldBm = ImageIO.read(in), newBm1 = ImageIO.read(in), 
                newBm2 = ImageIO.read(in), newBm3 = ImageIO.read(in);
            int threshold = getThresholdWithOtsu(generateOtsuHistogram(oldBm));
            for (int i = 0; i < oldBm.getWidth(); i++){
                for (int j = 0; j < oldBm.getHeight(); j++){
                    int gray = convertRgbToGrayscale(oldBm.getRGB(i, j));
                    newBm1.setRGB(i, j, (gray*0x10000) + (gray*0x100) + gray);
                    newBm3.setRGB(i, j, (gray < threshold ? 0xffffff : 0x000000));
                    gray = convertGrayscaleToDecimalGrayscale(gray) * 23;
                    newBm2.setRGB(i, j, (gray*0x10000) + (gray*0x100) + gray);
                }
            }
            ImageIO.write(newBm1, "png", out1);
            ImageIO.write(newBm2, "png", out2);
        } else {
            System.err.println("Oh");
            System.out.println("Shit");
        }
    }

    private static int[] generateOtsuHistogram(BufferedImage buffImg){
        int[] freq = new int[256];
        for(int i = 0; i < buffImg.getWidth(); i++)
            for(int j = 0; j < buffImg.getHeight(); j++){ 
                int val = buffImg.getRGB(i,j);
                if ((val & 0xff0000)/0x10000 != (val & 0xff) || (val & 0x00ff00)/0x100 != (val & 0xff))
                    val = convertRgbToDecimalGrayscale(val);
                else
                    val &= 0xff;
                freq[val]++;
            }
                
        return freq;
    }

    private static int getThresholdWithOtsu(int[] freq, int numOfPx){
        int backgroundWeight = 0, foregroundWeight = 0, threshold = 0;
        float dotSum = 0, backgroundSum = 0, foregroundSum, maximumVariance = 0, 
            targetVariance, backgroundMean, foregroundMean, preVariance;

        for(int i = 0; i < freq.length; i++) dotSum += i * freq[i];

        for(int i = 0; i < freq.length; i++){
            backgroundWeight += freq[i];
            if (backgroundWeight == 0) continue;

            foregroundWeight += (numOfPx - backgroundWeight);
            if (foregroundWeight == 0) break;

            backgroundSum += i*freq[i];
            foregroundSum = dotSum - backgroundSum;

            backgroundMean = backgroundSum/backgroundWeight;
            foregroundMean = foregroundSum/foregroundWeight;

            preVariance = (backgroundMean - foregroundMean) * (backgroundMean - foregroundMean);

            targetVariance = (backgroundWeight * foregroundWeight) * preVariance;

            if (maximumVariance < targetVariance){
                maximumVariance = targetVariance;
                threshold = i;
            }
        }

        return threshold;
    }

    private static int getThresholdWithOtsu(int[] freq){
        int totalPx = 0;
        for (Integer i : freq) totalPx += i;
        return getThresholdWithOtsu(freq, totalPx);
    }
}