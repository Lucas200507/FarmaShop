package Controller;

import DTO.EnderecoDTO;
import Service.EnderecoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enderecos")
public class EnderecoController {

    @Autowired
    private EnderecoService enderecoService;

    @GetMapping
    public ResponseEntity<List<EnderecoDTO>> listarEnderecos() {
        List<EnderecoDTO> enderecos = enderecoService.listarEnderecos();
        return ResponseEntity.ok(enderecos);
    }

    @PostMapping
    public ResponseEntity<?> inserirEndereco(@RequestBody EnderecoDTO dto) {
        String id = enderecoService.inserirEndereco(dto);
        if (id != null && !id.equals("0")) {
            return ResponseEntity.ok("Endereço criado com sucesso! ID: " + id);
        } else {
            return ResponseEntity.badRequest().body("Erro ao criar endereço.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> atualizarEndereco(@PathVariable String id, @RequestBody EnderecoDTO dto) {
        enderecoService.atualizarEndereco(id, dto);
        return ResponseEntity.ok("Endereço atualizado com sucesso.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarEndereco(@PathVariable String id) {
        enderecoService.deletarEndereco(id);
        return ResponseEntity.ok("Endereço deletado com sucesso.");
    }
}