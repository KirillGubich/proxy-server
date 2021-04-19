package by.bsuir.poit.csan.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum TextParser {
    INSTANCE;

    public List<String> parseAbsolutePath(String textToParse, String regex) {
        List<String> components = new ArrayList<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(textToParse);
        while (matcher.find()) {
            components.add(matcher.group());
        }
        return components;
    }
}
