package io.dallen.tokenizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LexerTable {
    private Node root;

    private Node currentNode;

    public LexerTable(String... defaultTypes) {
        root = new Node(null);
        currentNode = root;
        for(String typ : defaultTypes) {
            root.defineIdent(typ, Token.IdentifierType.TYPE);
        }
    }

    public void newChild() {
        Node newChild = new Node(currentNode);
        currentNode.children.add(newChild);
        currentNode = newChild;
    }

    public void toParent() {
        currentNode = currentNode.parent;
    }

    public void nextChild() {
        currentNode = currentNode.children.get(currentNode.current);
        currentNode.parent.current++;
    }

    public Node getCurrent() {
        return currentNode;
    }

    public Token.IdentifierType getIdent(String symbol) {
        return currentNode.getIdent(symbol);
    }

    public void defineIdent(String symbol, Token.IdentifierType typ) {
        currentNode.defineIdent(symbol, typ);
    }

    public static class Node {
        private final Map<String, Token.IdentifierType> symbolMap = new HashMap<>();
        private final Node parent;
        private final List<Node> children;
        private int current = 0;

        private Node(Node parent) {
            this.parent = parent;
            this.children = new ArrayList<>();
        }

        private Token.IdentifierType getIdent(String symbol) {
            Token.IdentifierType typ = symbolMap.get(symbol);
            if(typ != null) {
                return typ;
            }

            if(parent != null) {
                return parent.getIdent(symbol);
            }

            return null;
        }

        private void defineIdent(String symbol, Token.IdentifierType typ) {
            symbolMap.put(symbol, typ);
        }

        public Node getParent() {
            return parent;
        }
    }


}
