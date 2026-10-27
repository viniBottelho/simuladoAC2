package com.example.simuladoac2;

public class Filme {
    private String id;
    private String nome;
    private String tipo;
    private String genero;
    private int ano;
    private Double nota;
    private boolean assistido;

    public Filme() {
        // Necessário para o Firestore.
    }

    public Filme(String id, String nome, String tipo, String genero,
                 int ano, Double nota, boolean assistido) {
        this.id = id;
        this.nome = nome;
        this.tipo = tipo;
        this.genero = genero;
        this.ano = ano;
        this.nota = nota;
        this.assistido = assistido;
    }

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public String getGenero() { return genero; }
    public int getAno() { return ano; }
    public Double getNota() { return nota; }
    public boolean isAssistido() { return assistido; }

    public void setId(String id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setGenero(String genero) { this.genero = genero; }
    public void setAno(int ano) { this.ano = ano; }
    public void setNota(Double nota) { this.nota = nota; }
    public void setAssistido(boolean assistido) {
        this.assistido = assistido;
    }
}