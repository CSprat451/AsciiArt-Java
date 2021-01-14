package com.company;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        ImageConfig image = new ImageConfig(new File("spider_man2.jpg"));
        image.saveAsciiImage();

    }
}
