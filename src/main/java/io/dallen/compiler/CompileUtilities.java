package io.dallen.compiler;

public class CompileUtilities {

    public static String underscoreJoin(String... name) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < name.length; i++) {
            if(name[i].isEmpty()) {
                continue;
            }
            char[] n = name[i].toCharArray();
            for(int j = 0; j < n.length; j++) {
                if(Character.isUpperCase(n[j]) && j != 0) {
                    sb.append("_");
                }
                sb.append(Character.toLowerCase(n[j]));
            }
            if(i < name.length - 1) {
                sb.append("_");
            }
        }
        return sb.toString();
    }
}
