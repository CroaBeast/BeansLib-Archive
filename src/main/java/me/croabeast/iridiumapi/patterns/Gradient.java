package me.croabeast.iridiumapi.patterns;

import me.croabeast.iridiumapi.IridiumAPI;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Gradient extends BasePattern {

    private Color getColor(String line) {
        return new Color(Integer.parseInt(line, 16));
    }

    @Override
    public String process(String string, boolean useRGB) {
        Matcher matcher = gradientPattern().matcher(string);
        while (matcher.find()) {
            String start = matcher.group(1);
            String end = matcher.group(3);
            String content = matcher.group(2);
            string = string.replace(matcher.group(),
                    IridiumAPI.color(content, getColor(start), getColor(end), useRGB));
        }
        return string;
    }
}