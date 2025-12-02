package org.API_Miel.Pedido.routes;

import org.API_Miel.Pedido.controllers.PedidoController;
import io.javalin.Javalin;

public class PedidoRoutes {
    private final PedidoController pedidoController;
    
    public PedidoRoutes() {
        this.pedidoController = new PedidoController();
    }
    
    public void register(Javalin app) {
        // Crear pedido desde el carrito del usuario
        app.post("/api/usuarios/{usuarioId}/pedidos", pedidoController::create);
        
        // Obtener pedidos del usuario
        app.get("/api/usuarios/{usuarioId}/pedidos", pedidoController::getByUsuario);
        
        // Cancelar pedido (el usuario cancela su propio pedido)
        app.delete("/api/usuarios/{usuarioId}/pedidos/{id}/cancelar", pedidoController::cancelarPedido);
        
        // Listar todos los pedidos (admin)
        app.get("/api/pedidos", pedidoController::getAll);
        
        // Obtener un pedido específico
        app.get("/api/pedidos/{id}", pedidoController::getById);
        
        // Actualizar estado de pedido (admin)
        app.patch("/api/pedidos/{id}/estado", pedidoController::updateEstado);
        
        // Cancelar cualquier pedido (admin)
        app.delete("/api/pedidos/{id}/cancelar", pedidoController::cancelarPedidoAdmin);
    }
}