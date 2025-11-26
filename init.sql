CREATE DATABASE `ecommerce_miel` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `ecommerce_miel`;

-- Tabla de roles
CREATE TABLE rol (
  id INT PRIMARY KEY AUTO_INCREMENT,
  nombre VARCHAR(50) NOT NULL UNIQUE,
  descripcion VARCHAR(255),
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Tabla de usuarios
CREATE TABLE usuario (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre_completo VARCHAR(255) NOT NULL,
  correo VARCHAR(255) NOT NULL UNIQUE,
  contrasena VARCHAR(255) NOT NULL,
  num_celular VARCHAR(20),
  rol_id INT NOT NULL DEFAULT 2,
  activo BOOLEAN DEFAULT TRUE,
  fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  ultima_conexion TIMESTAMP NULL,
  INDEX idx_correo (correo),
  INDEX idx_rol_id (rol_id),
  FOREIGN KEY (rol_id) REFERENCES rol(id)
) ENGINE=InnoDB;

-- Tabla de productos
CREATE TABLE producto (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL,
  descripcion TEXT,
  precio DECIMAL(10,2) NOT NULL CHECK (precio >= 0),
  stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
  imagen_url VARCHAR(500),
  activo BOOLEAN DEFAULT TRUE,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_nombre (nombre),
  INDEX idx_activo (activo),
  INDEX idx_precio (precio)
) ENGINE=InnoDB;

-- Tabla de carritos
CREATE TABLE carrito (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL UNIQUE,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Tabla de detalle del carrito
CREATE TABLE carrito_detalle (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  carrito_id BIGINT NOT NULL,
  producto_id BIGINT NOT NULL,
  cantidad INT NOT NULL CHECK (cantidad > 0),
  fecha_agregado TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_carrito_producto (carrito_id, producto_id),
  INDEX idx_carrito_id (carrito_id),
  INDEX idx_producto_id (producto_id),
  FOREIGN KEY (carrito_id) REFERENCES carrito(id) ON DELETE CASCADE,
  FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Tabla de direcciones de envío
CREATE TABLE direccion (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL,
  nombre_destinatario VARCHAR(255) NOT NULL,
  telefono_contacto VARCHAR(20),
  calle VARCHAR(255) NOT NULL,
  numero_exterior VARCHAR(20),
  numero_interior VARCHAR(20),
  colonia VARCHAR(255) NOT NULL,
  codigo_postal VARCHAR(10) NOT NULL,
  ciudad VARCHAR(100) NOT NULL,
  estado VARCHAR(100) NOT NULL,
  referencias TEXT,
  es_predeterminada BOOLEAN DEFAULT FALSE,
  activa BOOLEAN DEFAULT TRUE,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_usuario_id (usuario_id),
  INDEX idx_predeterminada (usuario_id, es_predeterminada),
  FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Tabla de métodos de pago
CREATE TABLE metodo_pago (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id BIGINT NOT NULL,
  tipo_tarjeta ENUM('debito', 'credito') NOT NULL,
  nombre_titular VARCHAR(255) NOT NULL,
  ultimos_digitos VARCHAR(4) NOT NULL,
  mes_expiracion TINYINT NOT NULL CHECK (mes_expiracion BETWEEN 1 AND 12),
  anio_expiracion SMALLINT NOT NULL,
  es_predeterminado BOOLEAN DEFAULT FALSE,
  activo BOOLEAN DEFAULT TRUE,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_usuario_id (usuario_id),
  INDEX idx_predeterminado (usuario_id, es_predeterminado),
  FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Tabla de pedidos
CREATE TABLE pedido (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  numero_pedido VARCHAR(50) NOT NULL UNIQUE,
  usuario_id BIGINT NOT NULL,
  metodo_pago_id BIGINT NOT NULL,
  direccion_id BIGINT NOT NULL,
  subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),
  costo_envio DECIMAL(10,2) NOT NULL DEFAULT 0 CHECK (costo_envio >= 0),
  total DECIMAL(10,2) NOT NULL CHECK (total >= 0),
  estado ENUM('pendiente', 'procesando', 'enviado', 'entregado', 'cancelado') NOT NULL DEFAULT 'pendiente',
  notas_cliente TEXT,
  fecha_pedido TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  fecha_entrega TIMESTAMP NULL,
  INDEX idx_usuario_id (usuario_id),
  INDEX idx_numero_pedido (numero_pedido),
  INDEX idx_estado (estado),
  INDEX idx_fecha_pedido (fecha_pedido),
  FOREIGN KEY (usuario_id) REFERENCES usuario(id),
  FOREIGN KEY (metodo_pago_id) REFERENCES metodo_pago(id),
  FOREIGN KEY (direccion_id) REFERENCES direccion(id)
) ENGINE=InnoDB;

-- Tabla de detalle del pedido
CREATE TABLE pedido_detalle (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  pedido_id BIGINT NOT NULL,
  producto_id BIGINT NOT NULL,
  nombre_producto VARCHAR(255) NOT NULL,
  cantidad INT NOT NULL CHECK (cantidad > 0),
  precio_unitario DECIMAL(10,2) NOT NULL CHECK (precio_unitario >= 0),
  subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),
  INDEX idx_pedido_id (pedido_id),
  INDEX idx_producto_id (producto_id),
  FOREIGN KEY (pedido_id) REFERENCES pedido(id) ON DELETE CASCADE,
  FOREIGN KEY (producto_id) REFERENCES producto(id)
) ENGINE=InnoDB;

-- Tabla de reseñas
CREATE TABLE resena (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  producto_id BIGINT NOT NULL,
  usuario_id BIGINT NOT NULL,
  calificacion TINYINT NOT NULL CHECK (calificacion BETWEEN 1 AND 5),
  comentario TEXT,
  fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  activa BOOLEAN DEFAULT TRUE,
  UNIQUE KEY uk_usuario_producto (usuario_id, producto_id),
  INDEX idx_producto_id (producto_id),
  INDEX idx_usuario_id (usuario_id),
  INDEX idx_calificacion (calificacion),
  INDEX idx_fecha_creacion (fecha_creacion),
  FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE,
  FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Insertar roles por defecto
INSERT INTO rol (id, nombre, descripcion) VALUES 
(1, 'admin', 'Administrador con acceso completo al sistema'),
(2, 'user', 'Usuario cliente con acceso a compras y reseñas');