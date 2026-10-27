package com.example.simuladoac2;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseFirestore db;

    private EditText edtNome, edtAno, edtNota;
    private Spinner spTipo, spGenero, spFiltroTipo;
    private CheckBox chkAssistido, chkSomenteAssistidos;
    private Button btnSalvar;
    private RecyclerView recyclerFilmes;

    private List<Filme> listaFilmes = new ArrayList<>();
    private List<Filme> listaFiltrada = new ArrayList<>();

    private FilmeAdapter adapter;
    private Filme filmeEditando = null;

    private boolean operacaoEmAndamento = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    Insets teclado = insets.getInsets(
                            WindowInsetsCompat.Type.ime()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            Math.max(systemBars.bottom, teclado.bottom)
                    );

                    return insets;
                }
        );

        db = FirebaseFirestore.getInstance();

        edtNome = findViewById(R.id.edtNome);
        edtAno = findViewById(R.id.edtAno);
        edtNota = findViewById(R.id.edtNota);

        spTipo = findViewById(R.id.spTipo);
        spGenero = findViewById(R.id.spGenero);
        spFiltroTipo = findViewById(R.id.spFiltroTipo);

        chkAssistido = findViewById(R.id.chkAssistido);
        chkSomenteAssistidos = findViewById(R.id.chkSomenteAssistidos);

        btnSalvar = findViewById(R.id.btnSalvar);
        recyclerFilmes = findViewById(R.id.recyclerFilmes);

        configurarSpinner(spTipo, R.array.tipos);
        configurarSpinner(spGenero, R.array.generos);
        configurarSpinner(spFiltroTipo, R.array.filtro_tipos);

        recyclerFilmes.setLayoutManager(new LinearLayoutManager(this));

        adapter = new FilmeAdapter(listaFiltrada);
        recyclerFilmes.setAdapter(adapter);

        adapter.setOnItemClickListener(filme -> editarFilme(filme));
        adapter.setOnItemLongClickListener(filme -> confirmarExclusao(filme));

        btnSalvar.setOnClickListener(v -> salvarFilme());

        spFiltroTipo.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            AdapterView<?> parent,
                            View view,
                            int position,
                            long id
                    ) {
                        filtrarFilmes();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                }
        );

        chkSomenteAssistidos.setOnCheckedChangeListener(
                (buttonView, isChecked) -> filtrarFilmes()
        );

        carregarFilmes();
    }

    private void configurarSpinner(Spinner spinner, int opcoes) {
        ArrayAdapter<CharSequence> opcoesAdapter =
                ArrayAdapter.createFromResource(
                        this,
                        opcoes,
                        android.R.layout.simple_spinner_item
                );

        opcoesAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinner.setAdapter(opcoesAdapter);
    }

    private void salvarFilme() {
        if (operacaoEmAndamento) return;

        String nome = edtNome.getText().toString().trim();
        String textoAno = edtAno.getText().toString().trim();
        String textoNota = edtNota.getText().toString().trim();

        if (nome.isEmpty()) {
            edtNome.setError("Informe o nome.");
            edtNome.requestFocus();
            return;
        }

        if (spTipo.getSelectedItemPosition() <= 0) {
            Toast.makeText(
                    this,
                    "Selecione o tipo.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (spGenero.getSelectedItemPosition() <= 0) {
            Toast.makeText(
                    this,
                    "Selecione o gênero.",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (textoAno.isEmpty()) {
            edtAno.setError("Informe o ano de lançamento.");
            edtAno.requestFocus();
            return;
        }

        int ano;
        Double nota = null;

        try {
            ano = Integer.parseInt(textoAno);
        } catch (NumberFormatException e) {
            edtAno.setError("Informe um ano numérico válido.");
            edtAno.requestFocus();
            return;
        }

        // Se o campo estiver vazio, a nota permanece null.
        edtNota.setError(null);

        if (!textoNota.isEmpty()) {
            try {
                nota = Double.parseDouble(textoNota.replace(',', '.'));

                if (Double.isNaN(nota) || Double.isInfinite(nota)) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                edtNota.setError("Informe uma nota numérica válida.");
                edtNota.requestFocus();
                return;
            }
        }

        String id = filmeEditando == null
                ? null
                : filmeEditando.getId();

        Filme filme = new Filme(
                id,
                nome,
                spTipo.getSelectedItem().toString(),
                spGenero.getSelectedItem().toString(),
                ano,
                nota,
                chkAssistido.isChecked()
        );

        operacaoEmAndamento = true;
        btnSalvar.setEnabled(false);

        if (filmeEditando == null) {
            db.collection("filmes")
                    .add(filme)
                    .addOnSuccessListener(doc -> {
                        filme.setId(doc.getId());
                        finalizarSalvamento("Registro salvo!");
                    })
                    .addOnFailureListener(e -> mostrarErro("salvar", e));
        } else {
            db.collection("filmes")
                    .document(filme.getId())
                    .set(filme)
                    .addOnSuccessListener(aVoid ->
                            finalizarSalvamento("Registro atualizado!")
                    )
                    .addOnFailureListener(e -> mostrarErro("atualizar", e));
        }
    }

    private void finalizarSalvamento(String mensagem) {
        operacaoEmAndamento = false;
        btnSalvar.setEnabled(true);

        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();

        limparCampos();
        carregarFilmes();
    }

    private void limparCampos() {
        edtNome.setText("");
        edtAno.setText("");
        edtNota.setText("");

        edtNome.setError(null);
        edtAno.setError(null);
        edtNota.setError(null);

        spTipo.setSelection(0);
        spGenero.setSelection(0);

        chkAssistido.setChecked(false);

        filmeEditando = null;
        btnSalvar.setText("Salvar");
    }

    private void carregarFilmes() {
        db.collection("filmes")
                .get()
                .addOnSuccessListener(query -> {
                    listaFilmes.clear();

                    for (QueryDocumentSnapshot doc : query) {
                        Filme filme = doc.toObject(Filme.class);
                        filme.setId(doc.getId());
                        listaFilmes.add(filme);
                    }

                    filtrarFilmes();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Erro ao carregar: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    private void filtrarFilmes() {
        listaFiltrada.clear();

        String tipo = spFiltroTipo.getSelectedItem().toString();

        for (Filme filme : listaFilmes) {
            boolean tipoCorreto =
                    spFiltroTipo.getSelectedItemPosition() == 0
                            || tipo.equals(filme.getTipo());

            boolean assistidoCorreto =
                    !chkSomenteAssistidos.isChecked()
                            || filme.isAssistido();

            if (tipoCorreto && assistidoCorreto) {
                listaFiltrada.add(filme);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private void editarFilme(Filme filme) {
        if (operacaoEmAndamento) return;

        limparCampos();

        edtNome.setText(filme.getNome());
        edtAno.setText(String.valueOf(filme.getAno()));

        edtNota.setText(
                filme.getNota() == null
                        ? ""
                        : String.valueOf(filme.getNota())
        );

        selecionarOpcao(spTipo, filme.getTipo());
        selecionarOpcao(spGenero, filme.getGenero());

        chkAssistido.setChecked(filme.isAssistido());

        filmeEditando = filme;
        btnSalvar.setText("Atualizar");

        ScrollView scrollFormulario = findViewById(R.id.scrollFormulario);
        scrollFormulario.smoothScrollTo(0, 0);
    }

    private void selecionarOpcao(Spinner spinner, String valor) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(valor)) {
                spinner.setSelection(i);
                return;
            }
        }

        spinner.setSelection(0);
    }

    private void confirmarExclusao(Filme filme) {
        if (operacaoEmAndamento) return;

        new AlertDialog.Builder(this)
                .setTitle("Excluir registro")
                .setMessage("Deseja excluir " + filme.getNome() + "?")
                .setPositiveButton(
                        "Excluir",
                        (dialog, which) -> excluirFilme(filme)
                )
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirFilme(Filme filme) {
        if (operacaoEmAndamento) return;

        operacaoEmAndamento = true;
        btnSalvar.setEnabled(false);

        db.collection("filmes")
                .document(filme.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    operacaoEmAndamento = false;
                    btnSalvar.setEnabled(true);

                    if (filmeEditando != null
                            && filmeEditando.getId().equals(filme.getId())) {
                        limparCampos();
                    }

                    Toast.makeText(
                            this,
                            "Registro excluído!",
                            Toast.LENGTH_SHORT
                    ).show();

                    carregarFilmes();
                })
                .addOnFailureListener(e -> mostrarErro("excluir", e));
    }

    private void mostrarErro(String acao, Exception erro) {
        operacaoEmAndamento = false;
        btnSalvar.setEnabled(true);

        Toast.makeText(
                this,
                "Erro ao " + acao + ": " + erro.getMessage(),
                Toast.LENGTH_LONG
        ).show();
    }
}