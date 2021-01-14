package com.company;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.imgscalr.Scalr;

public class ImageConfig {
    BufferedImage image;
    int originalHeight;
    int originalWidth;
    private static final int TARGET_WIDTH = 200;
    private static final int FONT_SIZE = 15;

    public ImageConfig(File originalImage) throws IOException {
        this.image = ImageIO.read(originalImage);
        this.originalHeight = image.getHeight();
        this.originalWidth = image.getWidth();
    }

    public BufferedImage resizeImage(BufferedImage originalImage, int targetWidth) {
        return Scalr.resize(originalImage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC, targetWidth,
                Scalr.OP_ANTIALIAS);
    }

    public double[][] getPixelMatrix() {
        BufferedImage resizedOriginalImage = resizeImage(this.image, TARGET_WIDTH);
        final int[] pixels = ((DataBufferInt) resizedOriginalImage.getRaster().getDataBuffer()).getData();
        final int width = resizedOriginalImage.getWidth();
        final int height = resizedOriginalImage.getHeight();
        final boolean hasAlphaChannel = resizedOriginalImage.getAlphaRaster() != null;

        double[][] result = new double[height][width];
        if (hasAlphaChannel) {
            final int pixelLength = 4;
            for (int pixel = 0, row = 0, col = 0; pixel + 3 < pixels.length; pixel += pixelLength) {
                int alpha = pixels[pixel] & 0xff; // alpha 255
                int blue = pixels[pixel + 1] & 0xff; // blue
                int green = pixels[pixel + 2] & 0xff; // green
                int red = pixels[pixel + 3] & 0xff; // red
                double intensity = ((0.21 * red) + (0.72 * green) + (0.07 * blue)) * (alpha / 255.0);
                result[row][col] = intensity;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            final int pixelLength = 3;
            for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel++) {
                int red = pixels[pixel] >> 16 & 0xff; // red
                int blue = pixels[pixel] >> 8 & 0xff; // green
                int green = pixels[pixel] & 0xff; // blue
                double intensity = (0.21 * red) + (0.72 * green) + (0.07 * blue);
                result[row][col] = intensity;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }
        return result;
    }

    public ArrayList<String> getAsciiMatrix() {
        double[][] pixels = getPixelMatrix();
        final String asciiCharacters = "`.^\",:;Il!i~+_-?][}{1)(|\\/tfjrxnuvczXYUJCLQ0OZmwqpdbkhao*#MW&8%B@$";
        final int MAX_PIXEL_BRIGHTNESS = 255;
        ArrayList<String> asciiMatrix = new ArrayList<String>();
        for (double[] rowData : pixels) {
            StringBuilder asciiRow = new StringBuilder();
            for (double colData : rowData) {
                int index = (int)(colData * asciiCharacters.length() + 1) / MAX_PIXEL_BRIGHTNESS;
                asciiRow.append(index >= asciiCharacters.length() ? " " : String.valueOf(asciiCharacters.charAt(index))
                                                                            + (asciiCharacters.charAt(index)));
            }
            asciiMatrix.add(asciiRow.toString());
        }
        return asciiMatrix;
    }

    public Rectangle2D getTextDimensions(Font font, String text) {
        FontMetrics metrics = new FontMetrics(font) {};
        return metrics.getStringBounds(text, null);
    }


    public void saveAsciiImage() {
        ArrayList<String> asciiText = getAsciiMatrix();
        Font font = new Font("Lucida sans Typewriter", Font.BOLD, FONT_SIZE);
        Rectangle2D bounds = getTextDimensions(font, asciiText.get(0));
        BufferedImage asciiImage = new BufferedImage((int)bounds.getWidth(), asciiText.size() *
                (int)bounds.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = asciiImage.createGraphics();
        g2d.setFont(font);
        FontMetrics fontMetrics = g2d.getFontMetrics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, asciiImage.getWidth(), asciiImage.getHeight());

        g2d.setColor(Color.WHITE);
        int lineHeight = fontMetrics.getAscent();
        for (int i = 0; i < asciiText.size(); i++) {
            g2d.drawString(asciiText.get(i), 0, lineHeight);
            lineHeight += fontMetrics.getAscent() + 2;
        }
        g2d.dispose();

        try {
            System.out.println(ImageIO.write(resizeImage(asciiImage, TARGET_WIDTH * 8), "jpg", new File("ascii-image.jpg")));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
