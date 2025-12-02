package org.API_Miel.Resena.services;

import org.API_Miel.Resena.models.CreateResenaRequest;
import org.API_Miel.Resena.models.Resena;
import org.API_Miel.Resena.repositories.ResenaRepository;
import java.sql.SQLException;
import java.util.List;

public class ResenaService {
    private final ResenaRepository repository;

    public ResenaService() {
        this.repository = new ResenaRepository();
    }

    public void crearResena(CreateResenaRequest request) throws Exception {
        if (request.getCalificacion() < 1 || request.getCalificacion() > 5) {
            throw new IllegalArgumentException("La calificación debe estar entre 1 y 5");
        }
        if (request.getComentario() == null || request.getComentario().trim().isEmpty()) {
            throw new IllegalArgumentException("El comentario no puede estar vacío");
        }

        Resena resena = new Resena();
        resena.setProductoId(request.getProductoId());
        resena.setUsuarioId(request.getUsuarioId());
        resena.setCalificacion(request.getCalificacion());
        resena.setComentario(request.getComentario());

        try {
            repository.crear(resena);
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                throw new Exception("Ya has dejado una reseña para este producto.");
            }
            throw e;
        }
    }

    public List<Resena> obtenerResenasPorProducto(Long productoId) throws SQLException {
        return repository.buscarPorProducto(productoId);
    }

    public void eliminarResena(Long resenaId, Long usuarioId) throws Exception {
        // Verificar que la reseña existe y pertenece al usuario
        Resena resena = repository.buscarPorId(resenaId);
        
        if (resena == null) {
            throw new IllegalArgumentException("La reseña no existe");
        }

        // Si se proporciona usuarioId, verificar que sea el dueño
        // (Esto es importante para seguridad - un usuario solo puede eliminar sus propias reseñas)
        if (usuarioId != null && !resena.getUsuarioId().equals(usuarioId)) {
            throw new IllegalArgumentException("No tienes permiso para eliminar esta reseña");
        }

        try {
            repository.eliminar(resenaId);
        } catch (SQLException e) {
            throw new Exception("Error al eliminar la reseña: " + e.getMessage());
        }
    }
}