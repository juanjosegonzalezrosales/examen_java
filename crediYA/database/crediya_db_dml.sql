USE crediya_db;

INSERT INTO roles (nombre, descripcion) VALUES
('ASESOR', 'Atencion de clientes, radicacion de solicitudes y asesoramiento'),
('SUPERVISOR', 'Verificacion y aprobacion de solicitudes en cadena de responsabilidad'),
('GERENTE', 'Aprobacion de montos mayores, parametrizacion y administracion general')
ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion);
