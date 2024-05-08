package com.jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }
    
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    
    private int start = 0;
    private int current = 0;
    private int line = 1;




    Scanner(String source) {
        this.source = source;
    }

    private char nextChar() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type){
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal){
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek(){
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekTwice(){
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }
    
    private void parseString(){
        while(peek() != '"' && !isAtEnd()){
            if (peek() == '\n') line++;
            nextChar();
        }

        if(isAtEnd()){
            Lox.error(line, "Unterminated string.");
            return;
        }

        nextChar();

        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private boolean isDigit(char c){
        return c >= '0' && c <= '9';
    }

    private void parseNumber(){
        // check to see if next char is a number, then consume it
        while (isDigit(peek())) nextChar();

        // if we reach decimal
        if(peek() == '.' && isDigit(peekTwice())){
            nextChar();

            while (isDigit(peek())) nextChar();
        }

        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private boolean isAlpha(char c){
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c){
        return isAlpha(c) || isDigit(c);
    }
    private void parseIdentifier(){
        while(isAlphaNumeric(peek())) nextChar();

        String text = source.substring(start, current);
        System.out.println(text);
        TokenType type = keywords.get(text);
        if(type == null) type = TokenType.IDENTIFIER;

        addToken(type);
    }

    private void scanToken() {
        char c = nextChar();
        switch(c) {
            case '(' : addToken(TokenType.LEFT_PAREN); break;
            case ')' : addToken(TokenType.RIGHT_PAREN); break;
            case '{' : addToken(TokenType.LEFT_BRACE); break;
            case '}' : addToken(TokenType.RIGHT_BRACE); break;
            case ',' : addToken(TokenType.COMMA); break;
            case '.' : addToken(TokenType.DOT); break;
            case '-' : addToken(TokenType.MINUS); break;
            case '+' : addToken(TokenType.PLUS); break;
            case ';' : addToken(TokenType.SEMICOLON); break;
            case '*' : addToken(TokenType.STAR); break;
            case '!' :
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=' :
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<' :
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '?' :
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '/' :
            // if the next char is a slash, consume untill end of line
            if (match('/')) {
                while (peek() != '\n' && !isAtEnd()) nextChar();   
            } else {
                // else treat it as a division operator
                addToken(TokenType.SLASH);
            }
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++;
                break;
            case '"':
                parseString();
                break;                
            default : 
                
                // number case
                if (isDigit(c)){
                    parseNumber();
                } else if(isAlpha(c)){ 
                    parseIdentifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    List<Token> scanTokens() {
        while (!isAtEnd()){
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }
}
