package me.serbob.commons.utils.message;

import net.md_5.bungee.api.ChatColor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatUtil {
    public static final Pattern HEX_PATTERN = Pattern.compile("#([A-Fa-f0-9]{6})");
    private static Method COLOR_OF_METHOD;
    private static boolean HEX_SUPPORT = false;

    static {
        try {
            COLOR_OF_METHOD = ChatColor.class.getMethod("of", String.class);
            HEX_SUPPORT = true;
        } catch (NoSuchMethodException e) {
            HEX_SUPPORT = false;
        }
    }

    public static String c(String textToTranslate) {
        if (textToTranslate == null) return null;

        String result = textToTranslate;

        if (HEX_SUPPORT && COLOR_OF_METHOD != null) {
            Matcher matcher = HEX_PATTERN.matcher(result);
            StringBuffer buffer = new StringBuffer();

            while (matcher.find()) {
                try {
                    ChatColor hexColor = (ChatColor) COLOR_OF_METHOD.invoke(null, "#" + matcher.group(1));
                    matcher.appendReplacement(buffer, hexColor.toString());
                } catch (Exception e) {
                    matcher.appendReplacement(buffer, matcher.group(0));
                }
            }

            result = matcher.appendTail(buffer).toString();
        } else {
            result = result.replaceAll("#[A-Fa-f0-9]{6}", "");
        }

        return ChatColor.translateAlternateColorCodes('&', result);
    }

    public static List<String> c(List<String> stringList) {
        if (stringList == null) return null;
        return stringList.stream().map(ChatUtil::c).collect(Collectors.toList());
    }

    public static String[] c(String[] strings) {
        if (strings == null) return null;
        return Arrays.stream(strings).map(ChatUtil::c).toArray(String[]::new);
    }
}