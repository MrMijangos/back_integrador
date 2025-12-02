package org.API_Miel.Resena.repositories;

import org.API_Miel.ConfigDB.DatabaseConfig;
import org.API_Miel.Resena.models.Resena;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ResenaRepository {

    public void crear(Resena resena) throws SQLException {
        String sql = "INSERT INTO resena (producto_id, usuario_id, calificacion, comentario) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, resena.getProductoId());
            stmt.setLong(2, resena.getUsuarioId());
            stmt.setInt(3, resena.getCalificacion());
            stmt.setString(4, resena.getComentario());
            
            stmt.executeUpdate();
        }
    }

    public List<Resena> buscarPorProducto(Long productoId) throws SQLException {
        List<Resena> lista = new ArrayList<>();
        String sql = "SELECT * FROM resena WHERE producto_id = ? AND activa = TRUE ORDER BY fecha_creacion DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, productoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapResultSet(rs));
                }
            }
        }
        return lista;
    }

    public Resena buscarPorId(Long resenaId) throws SQLException {
        String sql = "SELECT * FROM resena WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, resenaId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    public void eliminar(Long resenaId) throws SQLException {
        // Opción 1: Eliminación lógica (recomendada - marca como inactiva)
        String sql = "UPDATE resena SET activa = FALSE WHERE id = ?";
        
        // Opción 2: Eliminación física (descomentar si prefieres borrar completamente)
        // String sql = "DELETE FROM resena WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, resenaId);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new SQLException("No se encontró la reseña con ID: " + resenaId);
            }
        }
    }

    private Resena mapResultSet(ResultSet rs) throws SQLException {
        return new Resena(
            rs.getLong("id"),
            rs.getLong("producto_id"),
            rs.getLong("usuario_id"),
            rs.getInt("calificacion"),
            rs.getString("comentario"),
            rs.getBoolean("activa"),
            rs.getTimestamp("fecha_creacion")
        );
    }
}