package org.API_Miel.Resena.controllers;

import io.javalin.http.Context;
import org.API_Miel.Resena.models.CreateResenaRequest;
import org.API_Miel.Resena.services.ResenaService;

public class ResenaController {
    private final ResenaService service;

    public ResenaController() {
        this.service = new ResenaService();
    }

    public void crear(Context ctx) {
        try {
            System.out.println("📥 Body recibido: " + ctx.body());
            
            CreateResenaRequest request = ctx.bodyAsClass(CreateResenaRequest.class);
            
            System.out.println("📦 Request parseado:");
            System.out.println("   - productoId: " + request.getProductoId());
            System.out.println("   - usuarioId: " + request.getUsuarioId());
            System.out.println("   - calificacion: " + request.getCalificacion());
            System.out.println("   - comentario: " + request.getComentario());

            service.crearResena(request);
            ctx.status(201).json("Reseña creada exitosamente");
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json("Error al crear la reseña: " + e.getMessage());
        }
    }

    public void listarPorProducto(Context ctx) {
        try {
            Long productoId = Long.parseLong(ctx.pathParam("productoId"));
            ctx.json(service.obtenerResenasPorProducto(productoId));
        } catch (Exception e) {
            ctx.status(500).json("Error al obtener reseñas");
        }
    }

    public void eliminar(Context ctx) {
        try {
            Long resenaId = Long.parseLong(ctx.pathParam("resenaId"));
            
            // Si tienes autenticación, deberías obtener el usuarioId del token/sesión
            // Para este ejemplo, lo obtendré del query param o body
            Long usuarioId = ctx.queryParam("usuarioId") != null 
                ? Long.parseLong(ctx.queryParam("usuarioId")) 
                : null;

            System.out.println("🗑️ Intentando eliminar reseña:");
            System.out.println("   - resenaId: " + resenaId);
            System.out.println("   - usuarioId: " + usuarioId);

            service.eliminarResena(resenaId, usuarioId);
            ctx.status(200).json("Reseña eliminada exitosamente");
        } catch (IllegalArgumentException e) {
            ctx.status(403).json(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json("Error al eliminar la reseña: " + e.getMessage());
        }
    }
}