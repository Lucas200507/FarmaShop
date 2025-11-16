package Controller;

import DTO.FarmaciaDTO;
import Service.FarmaciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
