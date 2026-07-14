package org.example.projeto_skala.objetos;

public class ReciboCampoConfig {
    private double x;
    private double y;
    private double largura;
    private double altura;

    public ReciboCampoConfig() {
    }

    public ReciboCampoConfig(double x, double y, double largura, double altura) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
    }

    public ReciboCampoConfig copiar() {
        return new ReciboCampoConfig(x, y, largura, altura);
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getLargura() {
        return largura;
    }

    public void setLargura(double largura) {
        this.largura = largura;
    }

    public double getAltura() {
        return altura;
    }

    public void setAltura(double altura) {
        this.altura = altura;
    }
}
