package io.dallen.errors;

public class ErrorPrinter {

    public static String pointToPos(String code, int pos, String message) {
        int lineNum = 1;
        for(int i = 0; i < pos; i++) {
            if(code.charAt(i) == '\n') {
                lineNum++;
            }
        }

        int lineStart = pos;
        while(lineStart > 0 && code.charAt(lineStart) != '\n') {
            lineStart--;
        }

        int lineEnd = pos;
        while(lineEnd < code.length() && code.charAt(lineEnd) != '\n') {
            lineEnd++;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(lineNum).append(" |").append(code, lineStart+1, lineEnd).append("\n");
        int lineNumLen = String.valueOf(lineNum).length() + 2;
        int indentStart = lineStart - lineNumLen + 1;
        for(int i = indentStart; i < pos; i++) {
            sb.append(" ");
        }
        sb.append("^\n");
        for(int i = indentStart; i < pos; i++) {
            sb.append(" ");
        }
        sb.append(message);
        return sb.toString();
    }

}
