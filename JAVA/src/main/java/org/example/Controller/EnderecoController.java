package org.example.Controller;

import java.util.List;

import org.example.DTO.EnderecoDTO;
import org.example.Service.EnderecoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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