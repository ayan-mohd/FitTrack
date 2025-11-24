import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ColorExtractor {
    public static void main(String[] args) {
        try {
            File file = new File("src/main/resources/images/logo.png");
            if (!file.exists()) {
                System.out.println("File not found: " + file.getAbsolutePath());
                return;
            }
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                System.out.println("Could not read image.");
                return;
            }
            
            int width = image.getWidth();
            int height = image.getHeight();
            Map<Integer, Integer> colorCounts = new HashMap<>();
            int backgroundColor = image.getRGB(0, 0);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getRGB(x, y);
                    if (pixel != backgroundColor) {
                        colorCounts.put(pixel, colorCounts.getOrDefault(pixel, 0) + 1);
                    }
                }
            }

            int dominantColor = 0;
            int maxCount = 0;
            for (Map.Entry<Integer, Integer> entry : colorCounts.entrySet()) {
                if (entry.getValue() > maxCount) {
                    maxCount = entry.getValue();
                    dominantColor = entry.getKey();
                }
            }

            if (dominantColor != 0) {
                int red = (dominantColor >> 16) & 0xff;
                int green = (dominantColor >> 8) & 0xff;
                int blue = (dominantColor) & 0xff;
                String hex = String.format("#%02x%02x%02x", red, green, blue);
                System.out.println("Dominant Non-Background Color: " + hex);
            } else {
                System.out.println("No non-background color found.");
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
