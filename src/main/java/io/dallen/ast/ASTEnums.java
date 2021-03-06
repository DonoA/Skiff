package io.dallen.ast;

/**
 * A collection of enums referenced by AST.java
 */
public class ASTEnums {
    public interface HasRaw {
        String getRawOp();
    }

    public enum MathOp implements HasRaw {
        PLUS("add", "+"), MINUS("sub", "-"), MUL("mul", "*"), DIV("div", "/"), XOR("xor", "^");

        private final String rawOp;
        private final String symbol;

        MathOp(String rawOp, String symbol) {
            this.symbol = symbol;
            this.rawOp = rawOp;
        }

        public String getRawOp() {
            return rawOp;
        }

        public String getSymbol() {
            return symbol;
        }
    }

    public enum CompareOp implements HasRaw {
        LT("<"), GT(">"), LE("<="), GE(">="), EQ("=="), NE("!=");

        private final String rawOp;

        CompareOp(String rawOp) {
            this.rawOp = rawOp;
        }

        public String getRawOp() {
            return rawOp;
        }
    }

    public enum BoolOp implements HasRaw {
        AND("&&"), OR("||");

        private final String rawOp;

        BoolOp(String rawOp) {
            this.rawOp = rawOp;
        }

        public String getRawOp() {
            return rawOp;
        }
    }

    public enum ImportType implements HasRaw {
        NORMAL("normal"), NATIVE("native");

        private final String rawOp;

        ImportType(String rawOp) {
            this.rawOp = rawOp;
        }

        public String getRawOp() {
            return rawOp;
        }
    }

    public enum SelfModTime implements HasRaw {
        PRE("pre"), POST("post");

        private final String rawOp;

        SelfModTime(String rawOp) {
            this.rawOp = rawOp;
        }

        public String getRawOp() {
            return rawOp;
        }
    }

    public enum DecModType implements HasRaw {
        PRIVATE("private"), STATIC("static"), NATIVE("native");

        private final String rawOp;

        DecModType(String rawOp) {
            this.rawOp = rawOp;
        }

        public String getRawOp() {
            return rawOp;
        }
    }
}
