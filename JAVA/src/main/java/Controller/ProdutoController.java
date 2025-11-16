package Controller;


import DTO.ProdutoDTO;
import DTO.FavoritoDTO;
import Service.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @GetMapping
    public ResponseEntity<List<ProdutoDTO>> listarProdutos(
            @RequestParam(required = false) String grupo,
            @RequestParam(required = false) Integer perfilId) {
        List<ProdutoDTO> produtos = produtoService.listarProdutos(grupo, perfilId);
        return ResponseEntity.ok(produtos);
    }

    @PostMapping
    public ResponseEntity<?> inserirProduto(@RequestBody ProdutoDTO dto) {
        Integer id = produtoService.inserirProduto(dto);
        if (id > 0) {
            return ResponseEntity.ok("Produto criado com sucesso!");
        } else {
            return ResponseEntity.badRequest().body("Erro ao criar produto.");
        }
    }

    @PutMapping("/{cod}")
    public ResponseEntity<String> atualizarProduto(@PathVariable String cod, @RequestBody ProdutoDTO dto) {
        produtoService.atualizarProduto(cod, dto);
        return ResponseEntity.ok("Produto atualizado com sucesso.");
    }

    @DeleteMapping("/{cod}")
    public ResponseEntity<String> deletarProduto(@PathVariable String cod) {
        produtoService.deletarProduto(cod);
        return ResponseEntity.ok("Produto deletado com sucesso.");
    }

    // --- ENDPOINTS PARA FAVORITOS ---

    @PostMapping("/favoritos")
    public ResponseEntity<String> adicionarFavorito(@RequestBody FavoritoDTO dto) {
        produtoService.adicionarFavorito(dto);
        return ResponseEntity.ok("Produto adicionado aos favoritos.");
    }

    @GetMapping("/favoritos/{clienteId}")
    public ResponseEntity<List<ProdutoDTO>> listarFavoritos(@PathVariable Integer clienteId) {
        List<ProdutoDTO> favoritos = produtoService.listarFavoritos(clienteId);
        return ResponseEntity.ok(favoritos);
    }
}
