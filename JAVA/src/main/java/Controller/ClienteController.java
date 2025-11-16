package Controller;

import DTO.ClienteDTO;
import Service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listarClientes() {
        List<ClienteDTO> clientes = clienteService.listarClientes();
        return ResponseEntity.ok(clientes);
    }

    @PostMapping
    public ResponseEntity<?> inserirCliente(@RequestBody ClienteDTO dto) {
        Integer id = clienteService.inserirCliente(dto);
        if (id > 0) {
            return ResponseEntity.ok("Cliente criado com sucesso! ID: " + id);
        } else {
            return ResponseEntity.badRequest().body("Erro ao criar cliente.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> atualizarCliente(@PathVariable Integer id, @RequestBody ClienteDTO dto) {
        clienteService.atualizarCliente(id, dto);
        return ResponseEntity.ok("Cliente atualizado com sucesso.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletarCliente(@PathVariable Integer id) {
        clienteService.deletarCliente(id);
        return ResponseEntity.ok("Cliente deletado com sucesso.");
    }
}