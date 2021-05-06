import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Bitmap <T extends Comparable<T>> {
    private List<T> pixelList;
    private int width, height;

    public Bitmap(){
        this(288, 480); // 3x5 index card
    }

    public Bitmap(int widthPx, int heightPx){
        this.width = widthPx;
        this.height = heightPx;
        this.pixelList = new ArrayList<>(width * height);
    }

    public void setValueAt(int xPos, int yPos, T value){
        pixelList.set(yPos*width + xPos, value);
    }

    public T getValueAt(int xPos, int yPos){
        return pixelList.get(yPos*width + xPos);
    }

    public int getWidth(){
        return this.width;
    }

    public int getHeight(){
        return this.height;
    }

}
