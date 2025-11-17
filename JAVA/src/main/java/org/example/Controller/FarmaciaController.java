package org.example.Controller;

import java.util.List;

import org.example.DTO.FarmaciaDTO;
import org.example.Service.FarmaciaService;
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
@RequestMapping("/api/farmacias")
public class FarmaciaController {

    @Autowired
    private FarmaciaService farmaciaService;

    @GetMapping
    public ResponseEntity<List<FarmaciaDTO>> listarFarmacias() {
        List<FarmaciaDTO> farmacias = farmaciaService.listarFarmacias();
        return ResponseEntity.ok(farmacias);
    }

    @PostMapping
    public ResponseEntity<?> inserirFarmacia(@RequestBody FarmaciaDTO dto) {
        Integer id = farmaciaService.inserirFarmacia(dto);
        if (id > 0) {
            return ResponseEntity.ok("Farmácia criada com sucesso! ID: " + id);
        } else {
            return ResponseEntity.badRequest().body("Erro ao criar farmácia.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> atualizarFarmacia(@PathVariable Integer id, @RequestBody FarmaciaDTO dto) {
        farmaciaService.atualizarFarmacia(id, dto);
        return ResponseEntity.ok("Farmácia atualizada com sucesso.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> desativarFarmacia(@PathVariable Integer id) {
        farmaciaService.desativarFarmacia(id);
        return ResponseEntity.ok("Farmácia desativada com sucesso (usuário associado também foi desativado).");
    }
}
