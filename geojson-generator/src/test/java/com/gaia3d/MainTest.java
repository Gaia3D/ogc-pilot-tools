package com.gaia3d;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private final String INPUT_DIRECTORY = "D:\\data\\ogc-pilot\\input";
    private final String OUTPUT_DIRECTORY = "D:\\data\\ogc-pilot\\output";

    @Test
    void help() {
        Main.main(new String[]{
                "--help"
        });
    }

    @Test
    void buildings() {
        String name = "buildings";
        Main.main(new String[]{
                "--input", createInputPath(INPUT_DIRECTORY, name),
                "--output", createInputPath(OUTPUT_DIRECTORY, name)
        });
    }

    @Test
    void noiseSource() {
        String name = "noise-source";
        Main.main(new String[]{
                "--input", createInputPath(INPUT_DIRECTORY, name),
                "--output", createInputPath(OUTPUT_DIRECTORY, name)
        });
    }

    @Test
    void both() {
        String name = "";
        Main.main(new String[]{
                "--input", createInputPath(INPUT_DIRECTORY, name),
                "--output", createInputPath(OUTPUT_DIRECTORY, name)
        });
    }

    private String createInputPath(String parent, String path) {
        File file = new File(parent, path);
        return file.getAbsolutePath();
    }
}