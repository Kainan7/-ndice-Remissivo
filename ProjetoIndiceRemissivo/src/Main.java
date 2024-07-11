import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.Path;
import java.io.BufferedWriter;
import java.nio.file.StandardOpenOption;

public class Main {

    class Nodo {
        public String elemento;
        public Nodo esquerdo;
        public Nodo direito;

        public Nodo(String elemento) {
            this.elemento = elemento;
            this.esquerdo = null;
            this.direito = null;
        }
    }

    public class ArvoreBinariaBusca {
        public Nodo raiz;
        public int nElementos;

        public ArvoreBinariaBusca() {
            this.raiz = null;
            this.nElementos = 0;
        }

        public void insere(String elemento) {
            if (this.raiz == null) {
                this.raiz = new Nodo(elemento);
                this.nElementos++;
            } else {
                this.insere(elemento, this.raiz);
            }
        }

        public void insere(String elemento, Nodo nodo) {
            if (elemento.compareTo(nodo.elemento) < 0) {
                if (nodo.esquerdo == null) {
                    nodo.esquerdo = new Nodo(elemento);
                    this.nElementos++;
                } else {
                    this.insere(elemento, nodo.esquerdo);
                }
            } else if (elemento.compareTo(nodo.elemento) > 0) {
                if (nodo.direito == null) {
                    nodo.direito = new Nodo(elemento);
                    this.nElementos++;
                } else {
                    this.insere(elemento, nodo.direito);
                }
            }
        }

        public boolean busca(String elemento) {
            return busca(elemento, this.raiz);
        }

        public boolean busca(String elemento, Nodo nodo) {
            if (nodo == null) {
                return false;
            }
            if (elemento.compareTo(nodo.elemento) < 0) {
                return busca(elemento, nodo.esquerdo);
            } else if (elemento.compareTo(nodo.elemento) > 0) {
                return busca(elemento, nodo.direito);
            } else {
                return true;
            }
        }
    }

    public class ListaEncadeada {
        class Nodo {
            public int linha;
            public Nodo proximo;

            public Nodo(int linha) {
                this.linha = linha;
                this.proximo = null;
            }
        }

        private Nodo primeiro;
        private Nodo ultimo;

        public ListaEncadeada() {
            this.primeiro = null;
            this.ultimo = null;
        }

        public void insere(int linha) {
            Nodo novo = new Nodo(linha);
            if (this.ultimo == null) {
                this.primeiro = novo;
                this.ultimo = novo;
            } else {
                this.ultimo.proximo = novo;
                this.ultimo = novo;
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            Nodo atual = this.primeiro;
            while (atual != null) {
                sb.append(atual.linha).append(" ");
                atual = atual.proximo;
            }
            return sb.toString().trim();
        }
    }

    public class TabelaDispersao {
        private String[][] tabela;
        private int tamanho;

        public TabelaDispersao(int tamanho) {
            this.tamanho = tamanho;
            this.tabela = new String[tamanho][];
            for (int i = 0; i < tamanho; i++) {
                tabela[i] = new String[0];
            }
        }

        private int hash(String palavra) {
            int hash = 7;
            for (int i = 0; i < palavra.length(); i++) {
                hash = hash * 31 + palavra.charAt(i);
            }
            return Math.abs(hash % tamanho);
        }

        public void insere(String palavra) {
            int index = hash(palavra);
            if (!contem(palavra)) {
                String[] newBucket = new String[tabela[index].length + 1];
                System.arraycopy(tabela[index], 0, newBucket, 0, tabela[index].length);
                newBucket[tabela[index].length] = palavra;
                tabela[index] = newBucket;
            }
        }

        public boolean contem(String palavra) {
            int index = hash(palavra);
            for (String item : tabela[index]) {
                if (item.equals(palavra)) {
                    return true;
                }
            }
            return false;
        }
    }

    // Classe para leitura dos arquivos
    public static class LeitorArquivos {
        public static String[] lerArquivo(String caminho) throws IOException {
            Path path = Paths.get(caminho);
            return Files.readString(path).split("\\R");
        }
    }

    // Classe para o índice remissivo
    private ArvoreBinariaBusca arvore;
    private TabelaDispersao hash;
    private NodoIndice[] indice;

    // Classe Nodo para a Árvore de Índice
    public class NodoIndice {
        public String chave;
        public ListaEncadeada linhas;

        public NodoIndice(String chave) {
            this.chave = chave;
            this.linhas = new ListaEncadeada();
        }
    }

    public Main() {
        this.arvore = new ArvoreBinariaBusca();
        this.hash = new TabelaDispersao(100);
        this.indice = new NodoIndice[100];
    }

    public void processaArquivos(String caminhoPalavrasChave, String caminhoTexto) throws IOException {
        // Lê o arquivo de palavras-chave
        String[] palavrasChave = LeitorArquivos.lerArquivo(caminhoPalavrasChave);
        for (String palavra : palavrasChave) {
            palavra = palavra.toLowerCase();
            this.hash.insere(palavra);
            int index = this.hash.hash(palavra);
            this.indice[index] = new NodoIndice(palavra);
        }

        // Lê o arquivo de texto e identifica as linhas onde cada palavra-chave aparece
        String[] texto = LeitorArquivos.lerArquivo(caminhoTexto);
        for (int i = 0; i < texto.length; i++) {
            String[] palavras = texto[i].split("\\W+");
            for (String palavra : palavras) {
                palavra = palavra.toLowerCase();
                if (this.hash.contem(palavra)) {
                    int index = this.hash.hash(palavra);
                    this.indice[index].linhas.insere(i + 1);
                }
            }
        }
    }

    public void imprimeIndice(String caminhoArquivo) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(caminhoArquivo), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (NodoIndice nodo : this.indice) {
                if (nodo != null) {
                    String linha = nodo.chave + ": " + nodo.linhas.toString();
                    writer.write(linha);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método main para executar o programa
    public static void main(String[] args) {
        try {
            Main indice = new Main();
            // Processa os arquivos de palavras-chave e texto
            indice.processaArquivos("C:\\Users\\gpont\\IdeaProjects\\ProjetoIndiceRemissivo\\src\\palavras-chave.txt", "C:\\Users\\gpont\\IdeaProjects\\ProjetoIndiceRemissivo\\src\\texto.txt");
            // Imprime o índice remissivo
            indice.imprimeIndice("C:\\Users\\gpont\\IdeaProjects\\ProjetoIndiceRemissivo\\src\\linhas_palavras.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
