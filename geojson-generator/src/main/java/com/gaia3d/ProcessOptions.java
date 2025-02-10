package com.gaia3d;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProcessOptions {
    // Default Options
    HELP("help", "h", "help", false, "Print Gelp"),
    VERSION("version", "v", "version", false, "Print Version Info"),
    QUIET("quiet", "q", "quiet", false, "Quiet mode/Silent mode"),
    VERBOSE("verbose", "V", "verbose", false, "Verbose mode"),
    // Path Options
    INPUT("input", "i", "input", true, "Input directory path"),
    OUTPUT("output", "o", "output", true, "Output directory file path");

    private final String longName;
    private final String shortName;
    private final String argName;
    private final boolean argRequired;
    private final String description;
}
