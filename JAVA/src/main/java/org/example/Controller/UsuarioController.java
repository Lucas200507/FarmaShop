package org.example.Controller;

import java.util.List;

import org.example.DTO.UsuarioDTO;
import org.example.Service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> listarUsuarios(@RequestParam(required = false) String tipo) {
        List<UsuarioDTO> usuarios = usuarioService.listarUsuarios(tipo != null ? tipo : "todos");
        return ResponseEntity.ok(usuarios);
    }

    @PostMapping
    public ResponseEntity<?> inserirUsuario(@RequestBody UsuarioDTO dto) {
        Integer id = usuarioService.inserirUsuario(dto);
        if (id > 0) {
            return ResponseEntity.ok("Usuário criado com sucesso! ID: " + id);
        } else {
            return ResponseEntity.badRequest().body("Erro ao criar usuário.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> atualizarUsuario(@PathVariable Integer id, @RequestBody UsuarioDTO dto) {
        usuarioService.atualizarUsuario(id, dto);
        return ResponseEntity.ok("Usuário atualizado com sucesso.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> desativarUsuario(@PathVariable Integer id) {
        usuarioService.desativarUsuario(id);
        return ResponseEntity.ok("Usuário desativado com sucesso.");
    }
}