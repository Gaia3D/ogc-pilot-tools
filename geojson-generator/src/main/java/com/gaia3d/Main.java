package com.gaia3d;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;

@Slf4j
public class Main {
    public static void main(String[] args) {
        Configurator.initConsoleLogger();
        log.info("Hello, World!");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine command = parser.parse(Configurator.createOptions(), args);

            /* check input directory */
            if (!command.hasOption(ProcessOptions.INPUT.getArgName())) {
                log.error("Input directory is required");
                return;
            } else {
                log.info("Input directory is: " + command.getOptionValue("input"));
            }
            File inputDirectory = new File(command.getOptionValue("input"));
            if (!inputDirectory.exists()) {
                log.error("Input directory does not exist");
                return;
            }

            /* check output directory */
            if (!command.hasOption(ProcessOptions.OUTPUT.getArgName())) {
                log.error("Output directory is required");
                return;
            } else {
                log.info("Output directory is: " + command.getOptionValue("output"));
            }
            File outputDirectory = new File(command.getOptionValue("output"));
            if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
                log.error("Failed to create output directory");
                return;
            }

            String[] extensions = new String[]{"gml"};
            List<File> inputFiles = (List<File>) FileUtils.listFiles(inputDirectory, extensions, true);


            GaiaGMLReader reader = new GaiaGMLReader();
            GeojsonWriter writer = new GeojsonWriter();
            for (File file : inputFiles) {
                GaiaGMLObject gaiaGMLObject = reader.read(file);
                writer.write(gaiaGMLObject, new File(outputDirectory, file.getName().replace(".gml", ".geojson")));
            }

        } catch (ParseException e) {
            log.error("Failed to parse command line options, Please check the arguments.", e);
            throw new RuntimeException(e);
        }
    }
}