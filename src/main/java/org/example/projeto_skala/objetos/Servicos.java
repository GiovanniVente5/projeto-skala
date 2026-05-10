package org.example.projeto_skala.objetos;

public class Servicos {
    String nome;
    int num;
    Double valor;

    public Servicos(String nome, int num, Double valor) {
        this.nome = nome;
        this.num = num;
        this.valor = valor;
    }

    @Override
    public String toString() {
        return "Servicos{" +
               "nome='" + nome + '\'' +
               ", num=" + num +
               ", valor=" + valor +
               '}';
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }
}
