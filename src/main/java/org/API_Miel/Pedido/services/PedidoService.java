package org.API_Miel.Pedido.services;

import org.API_Miel.Pedido.models.CreatePedidoRequest;
import org.API_Miel.Pedido.models.Pedido;
import org.API_Miel.Pedido.models.PedidoDetalle;
import org.API_Miel.Pedido.models.PedidoResponse;
import org.API_Miel.Pedido.repositories.PedidoRepository;
import org.API_Miel.Carrito.models.CarritoDetalleDTO; 
import org.API_Miel.Carrito.repositories.CarritoRepository;
import org.API_Miel.Producto.models.Producto;
import org.API_Miel.Producto.repositories.ProductoRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private static final BigDecimal COSTO_ENVIO = new BigDecimal("100.00");

    public PedidoService() {
        this.pedidoRepository = new PedidoRepository();
        this.carritoRepository = new CarritoRepository();
        this.productoRepository = new ProductoRepository();
    }

    public PedidoResponse createFromCarrito(Long usuarioId, CreatePedidoRequest request) {
        // 1. Obtener carrito
        var carrito = carritoRepository.findByUsuarioId(usuarioId)
            .orElseThrow(() -> new RuntimeException("No hay carrito activo"));

        List<CarritoDetalleDTO> itemsCarrito = carritoRepository.findDetallesByCarritoId(carrito.getId());

        if (itemsCarrito.isEmpty()) {
            throw new RuntimeException("El carrito está vacío");
        }

        // 2. Pre-cálculo y Validación de Stock
        BigDecimal subtotal = BigDecimal.ZERO;
        List<PedidoDetalle> detallesParaGuardar = new ArrayList<>();

        for (CarritoDetalleDTO item : itemsCarrito) {
            Producto producto = productoRepository.findById(item.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProductoId()));

            if (producto.getStock() < item.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre());
            }

            BigDecimal itemSubtotal = producto.getPrecio().multiply(new BigDecimal(item.getCantidad()));
            subtotal = subtotal.add(itemSubtotal);

            PedidoDetalle detalle = new PedidoDetalle();
            detalle.setProductoId(producto.getId());
            detalle.setNombreProducto(producto.getNombre());
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(itemSubtotal);

            detallesParaGuardar.add(detalle);
        }

        // 3. Crear el objeto Pedido
        Pedido pedido = new Pedido();
        pedido.setNumeroPedido(pedidoRepository.generateNumeroPedido());
        pedido.setUsuarioId(usuarioId);
        pedido.setMetodoPagoId(request.getMetodoPagoId());
        pedido.setDireccionId(request.getDireccionId());
        pedido.setSubtotal(subtotal);
        pedido.setCostoEnvio(COSTO_ENVIO);
        pedido.setTotal(subtotal.add(COSTO_ENVIO));
        pedido.setEstado("pendiente");
        pedido.setNotasCliente(request.getNotasCliente());

        // 4. Guardar Pedido
        Pedido savedPedido = pedidoRepository.save(pedido);

        // 5. Guardar Detalles y Actualizar Stock
        for (PedidoDetalle detalle : detallesParaGuardar) {
            detalle.setPedidoId(savedPedido.getId());
            pedidoRepository.saveDetalle(detalle);

            Producto p = productoRepository.findById(detalle.getProductoId()).get();
            productoRepository.updateStock(p.getId(), p.getStock() - detalle.getCantidad());
        }

        // 6. Limpiar Carrito
        carritoRepository.clearCarrito(carrito.getId());

        return buildPedidoResponse(savedPedido);
    }

    public PedidoResponse getById(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));
        return buildPedidoResponse(pedido);
    }

    public List<PedidoResponse> getByUsuarioId(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId).stream()
            .map(this::buildPedidoResponse)
            .collect(Collectors.toList());
    }

    public List<PedidoResponse> getAll() {
        return pedidoRepository.findAll().stream()
            .map(this::buildPedidoResponse)
            .collect(Collectors.toList());
    }

    public void updateEstado(Long id, String estado) {
        Pedido pedido = pedidoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        List<String> estadosValidos = List.of("pendiente", "procesando", "enviado", "entregado", "cancelado");
        if (!estadosValidos.contains(estado)) {
            throw new RuntimeException("Estado inválido");
        }
        pedidoRepository.updateEstado(id, estado);
    }

    /**
     * Cancela un pedido y restaura el stock de los productos
     * Solo se pueden cancelar pedidos en estado 'pendiente' o 'procesando'
     */
    public PedidoResponse cancelarPedido(Long pedidoId, Long usuarioId) {
        // 1. Buscar el pedido
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // 2. Validar que el pedido pertenece al usuario
        if (!pedido.getUsuarioId().equals(usuarioId)) {
            throw new RuntimeException("No tienes permiso para cancelar este pedido");
        }

        // 3. Validar que el pedido se puede cancelar
        if (pedido.getEstado().equals("cancelado")) {
            throw new RuntimeException("El pedido ya está cancelado");
        }

        if (pedido.getEstado().equals("entregado")) {
            throw new RuntimeException("No se puede cancelar un pedido ya entregado");
        }

        if (pedido.getEstado().equals("enviado")) {
            throw new RuntimeException("No se puede cancelar un pedido en tránsito. Contacta a soporte");
        }

        // 4. Obtener los detalles del pedido
        List<PedidoDetalle> detalles = pedidoRepository.findDetallesByPedidoId(pedidoId);

        // 5. Restaurar el stock de cada producto
        for (PedidoDetalle detalle : detalles) {
            Producto producto = productoRepository.findById(detalle.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + detalle.getProductoId()));
            
            // Devolver la cantidad al stock
            int nuevoStock = producto.getStock() + detalle.getCantidad();
            productoRepository.updateStock(producto.getId(), nuevoStock);
        }

        // 6. Actualizar el estado del pedido a 'cancelado'
        pedidoRepository.updateEstado(pedidoId, "cancelado");

        // 7. Retornar el pedido actualizado
        return getById(pedidoId);
    }

    /**
     * Versión para administradores que pueden cancelar cualquier pedido
     */
    public PedidoResponse cancelarPedidoAdmin(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if (pedido.getEstado().equals("cancelado")) {
            throw new RuntimeException("El pedido ya está cancelado");
        }

        if (pedido.getEstado().equals("entregado")) {
            throw new RuntimeException("No se puede cancelar un pedido ya entregado");
        }

        // Restaurar stock
        List<PedidoDetalle> detalles = pedidoRepository.findDetallesByPedidoId(pedidoId);
        for (PedidoDetalle detalle : detalles) {
            Producto producto = productoRepository.findById(detalle.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + detalle.getProductoId()));
            
            int nuevoStock = producto.getStock() + detalle.getCantidad();
            productoRepository.updateStock(producto.getId(), nuevoStock);
        }

        pedidoRepository.updateEstado(pedidoId, "cancelado");
        return getById(pedidoId);
    }

    private PedidoResponse buildPedidoResponse(Pedido pedido) {
        PedidoResponse response = new PedidoResponse();
        response.setId(pedido.getId());
        response.setUsuarioId(pedido.getUsuarioId());
        response.setNumeroPedido(pedido.getNumeroPedido());
        response.setSubtotal(pedido.getSubtotal());
        response.setCostoEnvio(pedido.getCostoEnvio());
        response.setTotal(pedido.getTotal());
        response.setEstado(pedido.getEstado());
        response.setNotasCliente(pedido.getNotasCliente());
        response.setFechaPedido(pedido.getFechaPedido());
        response.setFechaEntrega(pedido.getFechaEntrega());

        List<PedidoDetalle> detalles = pedidoRepository.findDetallesByPedidoId(pedido.getId());
        response.setDetalles(detalles);

        return response;
    }
}