package config;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.regex.Pattern;


public class FileConfig {
    /**
     * TODO 更好的文件忽略机制
     */
    public static List<String> ignoreList = Lists.newArrayList(".idea", "build",
            ".gradle", "node_modules", ".project", ".classpath", ".settings", ".*.class");

    public static String DEFAULT_GRAMMAR = "Java";

    public static boolean isIgnored(String fileName) {
        return ignoreList.stream().anyMatch(eachStr -> Pattern.matches(eachStr, fileName));
    }
}
