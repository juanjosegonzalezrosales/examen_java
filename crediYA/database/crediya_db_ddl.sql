CREATE DATABASE IF NOT EXISTS crediya_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE crediya_db;

CREATE TABLE IF NOT EXISTS roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(30) NOT NULL UNIQUE,
    descripcion VARCHAR(150) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS empleados (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    documento VARCHAR(30) NOT NULL UNIQUE,
    rol_id INT NOT NULL,
    correo VARCHAR(80) NOT NULL,
    salario DECIMAL(10,2) NOT NULL,
    CONSTRAINT chk_empleados_salario CHECK (salario >= 0),
    CONSTRAINT fk_empleados_rol FOREIGN KEY (rol_id)
        REFERENCES roles(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS clientes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    documento VARCHAR(30) NOT NULL UNIQUE,
    correo VARCHAR(80) NOT NULL,
    telefono VARCHAR(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS solicitudes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    empleado_id INT NOT NULL,
    monto_solicitado DECIMAL(12,2) NOT NULL,
    plazo_meses INT NOT NULL,
    modalidad VARCHAR(20) NOT NULL,
    fecha_solicitud DATE NOT NULL,
    estado VARCHAR(20) NOT NULL,
    observaciones TEXT NULL,
    fecha_resolucion DATE NULL,
    CONSTRAINT chk_solicitud_monto CHECK (monto_solicitado > 0),
    CONSTRAINT chk_solicitud_plazo CHECK (plazo_meses > 0),
    CONSTRAINT chk_solicitud_estado CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA')),
    CONSTRAINT chk_solicitud_modalidad CHECK (modalidad IN ('FRANCES', 'ALEMAN')),
    CONSTRAINT fk_solicitudes_cliente FOREIGN KEY (cliente_id)
        REFERENCES clientes(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_solicitudes_empleado FOREIGN KEY (empleado_id)
        REFERENCES empleados(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS prestamos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    solicitud_id INT NOT NULL UNIQUE,
    monto_capital DECIMAL(12,2) NOT NULL,
    tasa_interes_anual DECIMAL(5,2) NOT NULL,
    plazo_cuotas INT NOT NULL,
    modalidad VARCHAR(20) NOT NULL,
    fecha_inicio DATE NOT NULL,
    monto_total DECIMAL(12,2) NOT NULL,
    valor_cuota_referencia DECIMAL(12,2) NOT NULL,
    saldo_pendiente DECIMAL(12,2) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    CONSTRAINT chk_prestamo_capital CHECK (monto_capital > 0),
    CONSTRAINT chk_prestamo_tasa CHECK (tasa_interes_anual >= 0),
    CONSTRAINT chk_prestamo_plazo CHECK (plazo_cuotas > 0),
    CONSTRAINT chk_prestamo_saldo CHECK (saldo_pendiente >= 0),
    CONSTRAINT chk_prestamo_estado CHECK (estado IN ('ACTIVO', 'PAGADO')),
    CONSTRAINT chk_prestamo_modalidad CHECK (modalidad IN ('FRANCES', 'ALEMAN')),
    CONSTRAINT fk_prestamos_solicitud FOREIGN KEY (solicitud_id)
        REFERENCES solicitudes(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS cuotas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    prestamo_id INT NOT NULL,
    version_plan INT NOT NULL DEFAULT 1,
    numero_cuota INT NOT NULL,
    fecha_vencimiento DATE NOT NULL,
    valor_cuota DECIMAL(12,2) NOT NULL,
    valor_capital DECIMAL(12,2) NOT NULL,
    valor_interes DECIMAL(12,2) NOT NULL,
    saldo_restante_capital DECIMAL(12,2) NOT NULL,
    valor_pagado DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    capital_pagado DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    interes_pagado DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    vigente BOOLEAN NOT NULL DEFAULT TRUE,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT chk_cuota_numero CHECK (numero_cuota > 0),
    CONSTRAINT chk_cuota_version CHECK (version_plan > 0),
    CONSTRAINT chk_cuota_valor CHECK (valor_cuota >= 0),
    CONSTRAINT chk_cuota_capital CHECK (valor_capital >= 0),
    CONSTRAINT chk_cuota_interes CHECK (valor_interes >= 0),
    CONSTRAINT chk_cuota_saldo CHECK (saldo_restante_capital >= 0),
    CONSTRAINT chk_cuota_pagado CHECK (valor_pagado >= 0),
    CONSTRAINT chk_cuota_cap_pagado CHECK (capital_pagado >= 0),
    CONSTRAINT chk_cuota_int_pagado CHECK (interes_pagado >= 0),
    CONSTRAINT chk_cuota_estado CHECK (estado IN ('PENDIENTE', 'PAGADA')),
    CONSTRAINT uk_cuota_plan UNIQUE (prestamo_id, version_plan, numero_cuota),
    CONSTRAINT uk_cuota_prestamo UNIQUE (id, prestamo_id),
    CONSTRAINT fk_cuotas_prestamo FOREIGN KEY (prestamo_id)
        REFERENCES prestamos(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS pagos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    prestamo_id INT NOT NULL,
    cuota_id INT NULL,
    fecha_pago DATE NOT NULL,
    monto DECIMAL(12,2) NOT NULL,
    monto_mora DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    monto_interes DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    monto_capital DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    tipo_pago VARCHAR(25) NOT NULL,
    opcion_abono VARCHAR(20) NULL,
    observaciones VARCHAR(150) NULL,
    CONSTRAINT chk_pago_monto CHECK (monto > 0),
    CONSTRAINT chk_pago_mora CHECK (monto_mora >= 0),
    CONSTRAINT chk_pago_interes CHECK (monto_interes >= 0),
    CONSTRAINT chk_pago_capital CHECK (monto_capital >= 0),
    CONSTRAINT chk_pago_tipo CHECK (tipo_pago IN ('CUOTA_PROGRAMADA', 'ABONO_EXTRAORDINARIO', 'PAGO_TOTAL')),
    CONSTRAINT chk_pago_opcion CHECK (opcion_abono IS NULL OR opcion_abono IN ('REDUCIR_CUOTA', 'REDUCIR_PLAZO')),
    CONSTRAINT chk_pago_desglose CHECK (monto = monto_mora + monto_interes + monto_capital),
    CONSTRAINT chk_pago_tipo_coherencia CHECK (
        (tipo_pago = 'CUOTA_PROGRAMADA' AND cuota_id IS NOT NULL AND opcion_abono IS NULL)
        OR
        (tipo_pago = 'ABONO_EXTRAORDINARIO' AND cuota_id IS NULL AND opcion_abono IN ('REDUCIR_CUOTA', 'REDUCIR_PLAZO') AND monto_mora = 0.00 AND monto_interes = 0.00 AND monto_capital = monto)
        OR
        (tipo_pago = 'PAGO_TOTAL' AND cuota_id IS NULL AND opcion_abono IS NULL)
    ),
    CONSTRAINT fk_pagos_prestamo FOREIGN KEY (prestamo_id)
        REFERENCES prestamos(id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT,
    CONSTRAINT fk_pagos_cuota_compuesta FOREIGN KEY (cuota_id, prestamo_id)
        REFERENCES cuotas(id, prestamo_id)
        ON UPDATE RESTRICT
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
