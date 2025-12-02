package org.API_Miel.Resena.routes;

import io.javalin.Javalin;
import org.API_Miel.Resena.controllers.ResenaController;

public class ResenaRoutes {
    private final ResenaController controller;

    public ResenaRoutes() {
        this.controller = new ResenaController();
    }

    public void registerRoutes(Javalin app) {
        // Crear reseña
        app.post("/api/resenas", controller::crear);
        
        // Listar reseñas de un producto
        app.get("/api/productos/{productoId}/resenas", controller::listarPorProducto);
        
        // Eliminar reseña por ID
        app.delete("/api/resenas/{resenaId}", controller::eliminar);
    }
}