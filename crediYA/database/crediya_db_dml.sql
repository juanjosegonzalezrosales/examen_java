-- =============================================================================
-- CREDIYA S.A.S. - SCRIPT DML DE DATOS DE PRUEBA (IDEMPOTENTE Y RESTAURABLE)
-- Archivo: crediya_db_dml.sql
-- Ubicacion: crediYA/database/crediya_db_dml.sql
-- Motor: MySQL 8.0+
-- Descripcion: Poblado integral de datos de prueba para validacion de modulos,
--              roles de usuario, autorizacion por montos ($200.000.000),
--              amortizaciones francesa/alemana, pagos y reportes.
--              Disenado para ejecutarse multiples veces de forma segura.
-- =============================================================================

USE crediya_db;

-- -----------------------------------------------------------------------------
-- LIMPIEZA PREVIA PARA RESTAURACION SEGURA E IDEMPOTENTE
-- -----------------------------------------------------------------------------
DELETE FROM pagos;
DELETE FROM cuotas;
DELETE FROM prestamos;
DELETE FROM solicitudes;
DELETE FROM credenciales;
DELETE FROM empleados;
DELETE FROM clientes;

ALTER TABLE pagos AUTO_INCREMENT = 1;
ALTER TABLE cuotas AUTO_INCREMENT = 1;
ALTER TABLE prestamos AUTO_INCREMENT = 1;
ALTER TABLE solicitudes AUTO_INCREMENT = 1;
ALTER TABLE credenciales AUTO_INCREMENT = 1;
ALTER TABLE empleados AUTO_INCREMENT = 1;
ALTER TABLE clientes AUTO_INCREMENT = 1;

START TRANSACTION;

-- -----------------------------------------------------------------------------
-- 0. ROLES ESTRUCTURALES DEL SISTEMA
-- -----------------------------------------------------------------------------
INSERT INTO roles (id, nombre, descripcion) VALUES
(1, 'ASESOR', 'Atencion de clientes, radicacion de solicitudes y asesoramiento'),
(2, 'SUPERVISOR', 'Verificacion y aprobacion de solicitudes en cadena de responsabilidad'),
(3, 'GERENTE', 'Aprobacion de montos mayores, parametrizacion y administracion general')
ON DUPLICATE KEY UPDATE descripcion = VALUES(descripcion);

-- -----------------------------------------------------------------------------
-- 1. EMPLEADOS (Exactamente 6 empleados: 2 Asesores, 2 Supervisores, 2 Gerentes)
-- -----------------------------------------------------------------------------
INSERT INTO empleados (id, nombre, documento, rol_id, correo, salario) VALUES
(1, 'Carlos Andres Mendoza Gomez', '1001', 1, 'carlos.mendoza@crediya.com.co', 1800000.00),
(2, 'Laura Sofia Restrepo Diaz', '1002', 1, 'laura.restrepo@crediya.com.co', 1850000.00),
(3, 'Andres Felipe Vargas Mora', '2001', 2, 'andres.vargas@crediya.com.co', 3500000.00),
(4, 'Diana Patricia Ospina Jaramillo', '2002', 2, 'diana.ospina@crediya.com.co', 3600000.00),
(5, 'Juan Jose Gonzalez Rosales', '3001', 3, 'juan.gonzalez@crediya.com.co', 6500000.00),
(6, 'Roberto Carlos Silva Camacho', '3002', 3, 'roberto.silva@crediya.com.co', 6800000.00);

-- -----------------------------------------------------------------------------
-- 2. CREDENCIALES DE ACCESO DE PRUEBA (PBKDF2WithHmacSHA256, 65536 iteraciones)
-- -----------------------------------------------------------------------------
-- Empleado 1 (Carlos - ASESOR):     Doc: 1001 | Pass: Asesor123*
-- Empleado 2 (Laura - ASESOR):      Doc: 1002 | Pass: Asesor123*
-- Empleado 3 (Andres - SUPERVISOR): Doc: 2001 | Pass: Super123*
-- Empleado 4 (Diana - SUPERVISOR):  Doc: 2002 | Pass: Super123*
-- Empleado 5 (Juan - GERENTE):      Doc: 3001 | Pass: Gerente123*
-- Empleado 6 (Roberto - GERENTE):   Doc: 3002 | Pass: Gerente123*

INSERT INTO credenciales (id, empleado_id, password_hash, salt, iteraciones) VALUES
(1, 1, '6cad19ba93cc6036dca68291fdfe849a54f5d656de31ed49fc9a40463883317d', '56254fb830d19bff066c06b51fe291f3', 65536),
(2, 2, '6acd42ad9ab0e542e06b60349c6929e7e72a4795bcb7dad6be788f7edf51603e', 'df64df9f38a7d4342b2ce57144b6d69a', 65536),
(3, 3, '971fd201b3a085f19710b451dd39374b6ce7fa67c2e1daf7b05d431440a3daf2', '65d3704b07a52be468f20325faacbe41', 65536),
(4, 4, '9326a145f91d799bf7419e33022b07f5d55bd56e829271a04c85c30087e3dbc3', '4d24a0129b24f384255cdc5be0c23068', 65536),
(5, 5, '0d26925f1f0b8acc11cc91d4f49a0b2116c32cee7e274a05459b6e28b944ee16', '5d9f6ce755b7b3f1bee9b1df294f74f3', 65536),
(6, 6, 'efa11e693f758b7f25fba83d00b84ceb1d22c85d85412d86ca79b088513964a1', '84068e60342cbf5dca381937edb4de10', 65536);

-- -----------------------------------------------------------------------------
-- 3. CLIENTES (Exactamente 10 clientes con escenarios diversos)
-- -----------------------------------------------------------------------------
-- Clientes 1 y 3: Con multiples solicitudes
-- Clientes 2, 4, 5, 6, 7, 8: Con 1 solicitud cada uno
-- Clientes 9 y 10: Sin solicitudes (para probar clientes nuevos sin historial)

INSERT INTO clientes (id, nombre, documento, correo, telefono) VALUES
(1,  'Camila Andrea Gutierrez Rios',    '101001001', 'camila.gutierrez@email.com',  '3104567890'),
(2,  'Mateo Alejandro Morales Castro',  '101001002', 'mateo.morales@email.com',    '3115678901'),
(3,  'Valentina Lopez Martinez',        '101001003', 'valentina.lopez@email.com',  '3126789012'),
(4,  'David Esteban Ramirez Herrera',   '101001004', 'david.ramirez@email.com',     '3137890123'),
(5,  'Sofia Helena Cardona Benitez',    '101001005', 'sofia.cardona@email.com',    '3148901234'),
(6,  'Juan Pablo Torres Navarro',       '101001006', 'juan.torres@email.com',       '3159012345'),
(7,  'Mariana Isabel Mejia Duque',      '101001007', 'mariana.mejia@email.com',     '3160123456'),
(8,  'Santiago Andres Zuluaga Botero',  '101001008', 'santiago.zuluaga@email.com',  '3171234567'),
(9,  'Daniela Gomez Ospina',            '101001009', 'daniela.gomez@email.com',     '3182345678'),
(10, 'Nicolas Eduardo Salazar Pineda',  '101001010', 'nicolas.salazar@email.com',   '3193456789');

-- -----------------------------------------------------------------------------
-- 4. SOLICITUDES DE CREDITO (10 solicitudes que cubren todos los estados y reglas)
-- -----------------------------------------------------------------------------
-- Estados: PENDIENTE (3), APROBADA (2), RECHAZADA (2), DESEMBOLSADO (3)
-- Montos: Menores a 200M, iguales a 200M y mayores a 200M
-- Modalidades: FRANCES y ALEMAN
-- Creadores: Asesores (1, 2), Supervisores (3, 4), Gerentes (5)

INSERT INTO solicitudes (id, cliente_id, empleado_id, monto_solicitado, plazo_meses, modalidad, fecha_solicitud, estado, observaciones, fecha_resolucion) VALUES
-- Solicitud 1: Desembolsada, francesa, monto menor a 200M (Origina Prestamo #1)
(1, 1, 1, 12000000.00, 12, 'FRANCES', '2026-08-01', 'DESEMBOLSADO',
 '[2026-08-03] Aprobada por Supervisor Andres Vargas. | [2026-08-03] Desembolso completado por GERENTE Juan Jose Gonzalez Rosales (ID: 5). Prestamo originado: #1', '2026-08-03'),

-- Solicitud 2: Pendiente, alemana, monto menor a 200M (Permite probar evaluacion/modificacion)
(2, 1, 2, 5000000.00, 6, 'ALEMAN', '2026-11-05', 'PENDIENTE',
 'Radicada por Asesor Laura Restrepo. En evaluacion de cartera.', NULL),

-- Solicitud 3: Desembolsada, alemana, monto menor a 200M (Origina Prestamo #2)
(3, 2, 1, 24000000.00, 24, 'ALEMAN', '2026-07-15', 'DESEMBOLSADO',
 '[2026-07-16] Aprobada por Supervisor Diana Ospina. | [2026-07-16] Desembolso completado por SUPERVISOR Diana Patricia Ospina Jaramillo (ID: 4). Prestamo originado: #2', '2026-07-16'),

-- Solicitud 4: Aprobada, francesa, $50M (Lista para probar desembolso desde menu)
(4, 3, 3, 50000000.00, 36, 'FRANCES', '2026-11-01', 'APROBADA',
 '[2026-11-02] Aprobacion autorizada para Supervisor. Aprobada por Supervisor Andres Vargas. Documentacion completa.', '2026-11-02'),

-- Solicitud 5: Rechazada definitiva, francesa, $30M (Prueba de inmutabilidad de rechazo)
(5, 3, 2, 30000000.00, 18, 'FRANCES', '2026-10-10', 'RECHAZADA',
 '[2026-10-12] Rechazo autorizado para Supervisor. Capacidad de endeudamiento insuficiente segun politicas de riesgo.', '2026-10-12'),

-- Solicitud 6: Pendiente, monto mayor a $200M ($250M) (Prueba limite: Supervisor denegado, Gerente autorizado)
(6, 4, 1, 250000000.00, 60, 'FRANCES', '2026-11-08', 'PENDIENTE',
 'Radicada por Asesor Carlos Mendoza. Credito corporativo que supera los $200.000.000. Requiere decision de Gerencia.', NULL),

-- Solicitud 7: Aprobada por Gerencia, monto mayor a $200M ($300M) (Lista para desembolso de gran monto)
(7, 5, 5, 300000000.00, 48, 'ALEMAN', '2026-11-02', 'APROBADA',
 '[2026-11-03] Aprobacion autorizada para Gerente (sin limite de monto). Resolucion de Junta y Gerencia General.', '2026-11-03'),

-- Solicitud 8: Rechazada por Gerencia, monto mayor a $200M ($280M) (Prueba rechazo en alta gerencia)
(8, 6, 4, 280000000.00, 36, 'ALEMAN', '2026-10-01', 'RECHAZADA',
 '[2026-10-03] Rechazo autorizado para Gerente. Garantias colaterales no satisfacen requerimientos minimos.', '2026-10-03'),

-- Solicitud 9: Pendiente en el umbral exacto de $200M ($200.000.000) (Prueba frontera del Supervisor)
(9, 7, 2, 200000000.00, 24, 'FRANCES', '2026-11-09', 'PENDIENTE',
 'Radicada por Asesor Laura Restrepo. En el umbral maximo autorizado para aprobacion de nivel Supervisor.', NULL),

-- Solicitud 10: Desembolsada, francesa, $18M (Origina Prestamo #3 con cuotas pagadas y en mora)
(10, 8, 3, 18000000.00, 12, 'FRANCES', '2026-05-10', 'DESEMBOLSADO',
 '[2026-05-12] Aprobada por Supervisor Andres Vargas. | [2026-05-12] Desembolso completado por GERENTE Roberto Carlos Silva Camacho (ID: 6). Prestamo originado: #3', '2026-05-12');

-- -----------------------------------------------------------------------------
-- 5. PRESTAMOS (Asociados estrictamente a solicitudes DESEMBOLSADO)
-- -----------------------------------------------------------------------------
-- Prestamo 1: Solicitud 1 | Frances | $12M | 24% | Saldo actual: $10.192.675,38 (2 cuotas pagadas)
-- Prestamo 2: Solicitud 3 | Aleman  | $24M | 18% | Saldo actual: $22.000.000,00 (2 cuotas pagadas)
-- Prestamo 3: Solicitud 10| Frances | $18M | 24% | Saldo actual: $13.892.720,59 (3 cuotas pagadas, 2 en mora)

INSERT INTO prestamos (id, solicitud_id, monto_capital, tasa_interes_anual, plazo_cuotas, modalidad, fecha_inicio, monto_total, valor_cuota_referencia, saldo_pendiente, estado) VALUES
(1, 1,  12000000.00, 24.00, 12, 'FRANCES', '2026-08-01', 13616581.92, 1134715.16, 10192675.38, 'ACTIVO'),
(2, 3,  24000000.00, 18.00, 24, 'ALEMAN',  '2026-07-15', 28500000.00, 1360000.00, 22000000.00, 'ACTIVO'),
(3, 10, 18000000.00, 24.00, 12, 'FRANCES', '2026-05-10', 20424872.88, 1702072.74, 13892720.59, 'ACTIVO');

-- -----------------------------------------------------------------------------
-- 6. CUOTAS DE AMORTIZACION (Generadas mediante las formulas financieras exactas)
-- -----------------------------------------------------------------------------

-- Cuotas del Prestamo 1 (Modalidad Francesa, 12 cuotas):
-- Cuotas 1 y 2 pagadas; Cuotas 3 a 12 pendientes.
INSERT INTO cuotas (id, prestamo_id, version_plan, numero_cuota, fecha_vencimiento, valor_cuota, valor_capital, valor_interes, saldo_restante_capital, valor_pagado, capital_pagado, interes_pagado, vigente, estado) VALUES
(1,  1, 1, 1,  '2026-09-01', 1134715.16, 894715.16,  240000.00, 11105284.84, 1134715.16, 894715.16,  240000.00, 1, 'PAGADA'),
(2,  1, 1, 2,  '2026-10-01', 1134715.16, 912609.46,  222105.70, 10192675.38, 1134715.16, 912609.46,  222105.70, 1, 'PAGADA'),
(3,  1, 1, 3,  '2026-11-01', 1134715.16, 930861.65,  203853.51,  9261813.73,       0.00,      0.00,       0.00, 1, 'PENDIENTE'),
(4,  1, 1, 4,  '2026-12-01', 1134715.16, 949478.89,  185236.27,  8312334.84,       0.00,      0.00,       0.00, 1, 'PENDIENTE'),
(5,  1, 1, 5,  '2027-01-01', 1134715.16, 968468.46,  166246.70,  7343866.38,       0.00,      0.00,       0.00, 1, 'PENDIENTE'),
(6,  1, 1, 6,  '2027-02-01', 1134715.16, 987837.83,  146877.33,  6356028.55,       0.00,      0.00,       0.00, 1, 'PENDIENTE'),
(7,  1, 1, 7,  '2027-03-01', 1134715.16, 1007594.59, 127120.57,  5348433.96,       0.00,      0.00,       0.00, 1, 'PENDIENTE'),
(8,  1, 1, 8,  '2027-04-01', 1134715.16, 1027746.48, 106968.68,  4320687.48,       0.00,      0.00,       0.00, 1, 'PENDIENTE'),
(9,  1, 1, 9,  '2027-05-01', 1134715.16, 1048301.41,  86413.75,  3272386.07,       0.00,      0.00,       0.00, 1, 'PENDIENTE'),
(10, 1, 1, 10, '2027-06-01', 1134715.16, 1069267.44,  65447.72,  2203118.63,       0.00,      0.00,       0.00, 1, 'PENDIENTE'),
(11, 1, 1, 11, '2027-07-01', 1134715.16, 1090652.79,  44062.37,  1112465.84,       0.00,      0.00,       0.00, 1, 'PENDIENTE'),
(12, 1, 1, 12, '2027-08-01', 1134715.16, 1112465.84,  22249.32,        0.00,       0.00,      0.00,       0.00, 1, 'PENDIENTE');

-- Cuotas del Prestamo 2 (Modalidad Alemana, 24 cuotas):
-- Cuotas 1 y 2 pagadas; Cuotas 3 a 24 pendientes.
INSERT INTO cuotas (id, prestamo_id, version_plan, numero_cuota, fecha_vencimiento, valor_cuota, valor_capital, valor_interes, saldo_restante_capital, valor_pagado, capital_pagado, interes_pagado, vigente, estado) VALUES
(13, 2, 1, 1,  '2026-08-15', 1360000.00, 1000000.00, 360000.00, 23000000.00, 1360000.00, 1000000.00, 360000.00, 1, 'PAGADA'),
(14, 2, 1, 2,  '2026-09-15', 1345000.00, 1000000.00, 345000.00, 22000000.00, 1345000.00, 1000000.00, 345000.00, 1, 'PAGADA'),
(15, 2, 1, 3,  '2026-10-15', 1330000.00, 1000000.00, 330000.00, 21000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(16, 2, 1, 4,  '2026-11-15', 1315000.00, 1000000.00, 315000.00, 20000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(17, 2, 1, 5,  '2026-12-15', 1300000.00, 1000000.00, 300000.00, 19000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(18, 2, 1, 6,  '2027-01-15', 1285000.00, 1000000.00, 285000.00, 18000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(19, 2, 1, 7,  '2027-02-15', 1270000.00, 1000000.00, 270000.00, 17000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(20, 2, 1, 8,  '2027-03-15', 1255000.00, 1000000.00, 255000.00, 16000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(21, 2, 1, 9,  '2027-04-15', 1240000.00, 1000000.00, 240000.00, 15000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(22, 2, 1, 10, '2027-05-15', 1225000.00, 1000000.00, 225000.00, 14000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(23, 2, 1, 11, '2027-06-15', 1210000.00, 1000000.00, 210000.00, 13000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(24, 2, 1, 12, '2027-07-15', 1195000.00, 1000000.00, 195000.00, 12000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(25, 2, 1, 13, '2027-08-15', 1180000.00, 1000000.00, 180000.00, 11000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(26, 2, 1, 14, '2027-09-15', 1165000.00, 1000000.00, 165000.00, 10000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(27, 2, 1, 15, '2027-10-15', 1150000.00, 1000000.00, 150000.00,  9000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(28, 2, 1, 16, '2027-11-15', 1135000.00, 1000000.00, 135000.00,  8000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(29, 2, 1, 17, '2027-12-15', 1120000.00, 1000000.00, 120000.00,  7000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(30, 2, 1, 18, '2028-01-15', 1105000.00, 1000000.00, 105000.00,  6000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(31, 2, 1, 19, '2028-02-15', 1090000.00, 1000000.00,  90000.00,  5000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(32, 2, 1, 20, '2028-03-15', 1075000.00, 1000000.00,  75000.00,  4000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(33, 2, 1, 21, '2028-04-15', 1060000.00, 1000000.00,  60000.00,  3000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(34, 2, 1, 22, '2028-05-15', 1045000.00, 1000000.00,  45000.00,  2000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(35, 2, 1, 23, '2028-06-15', 1030000.00, 1000000.00,  30000.00,  1000000.00,       0.00,       0.00,      0.00, 1, 'PENDIENTE'),
(36, 2, 1, 24, '2028-07-15', 1015000.00, 1000000.00,  15000.00,        0.00,       0.00,      0.00,      0.00, 1, 'PENDIENTE');

-- Cuotas del Prestamo 3 (Modalidad Francesa, 12 cuotas):
-- Cuotas 1, 2 y 3 pagadas; Cuotas 4 y 5 VENCIDAS (mora); Cuota 6 a 12 pendientes.
INSERT INTO cuotas (id, prestamo_id, version_plan, numero_cuota, fecha_vencimiento, valor_cuota, valor_capital, valor_interes, saldo_restante_capital, valor_pagado, capital_pagado, interes_pagado, vigente, estado) VALUES
(37, 3, 1, 1,  '2026-06-10', 1702072.74, 1342072.74, 360000.00, 16657927.26, 1702072.74, 1342072.74, 360000.00, 1, 'PAGADA'),
(38, 3, 1, 2,  '2026-07-10', 1702072.74, 1368914.19, 333158.55, 15289013.07, 1702072.74, 1368914.19, 333158.55, 1, 'PAGADA'),
(39, 3, 1, 3,  '2026-08-10', 1702072.74, 1396292.48, 305780.26, 13892720.59, 1702072.74, 1396292.48, 305780.26, 1, 'PAGADA'),
(40, 3, 1, 4,  '2026-09-10', 1702072.74, 1424218.33, 277854.41, 12468502.26,       0.00,        0.00,      0.00, 1, 'PENDIENTE'),
(41, 3, 1, 5,  '2026-10-10', 1702072.74, 1452702.69, 249370.05, 11015799.57,       0.00,        0.00,      0.00, 1, 'PENDIENTE'),
(42, 3, 1, 6,  '2026-11-10', 1702072.74, 1481756.75, 220315.99,  9534042.82,       0.00,        0.00,      0.00, 1, 'PENDIENTE'),
(43, 3, 1, 7,  '2026-12-10', 1702072.74, 1511391.88, 190680.86,  8022650.94,       0.00,        0.00,      0.00, 1, 'PENDIENTE'),
(44, 3, 1, 8,  '2027-01-10', 1702072.74, 1541619.72, 160453.02,  6481031.22,       0.00,        0.00,      0.00, 1, 'PENDIENTE'),
(45, 3, 1, 9,  '2027-02-10', 1702072.74, 1572452.12, 129620.62,  4908579.10,       0.00,        0.00,      0.00, 1, 'PENDIENTE'),
(46, 3, 1, 10, '2027-03-10', 1702072.74, 1603901.16,  98171.58,  3304677.94,       0.00,        0.00,      0.00, 1, 'PENDIENTE'),
(47, 3, 1, 11, '2027-04-10', 1702072.74, 1635979.18,  66093.56,  1668698.76,       0.00,        0.00,      0.00, 1, 'PENDIENTE'),
(48, 3, 1, 12, '2027-05-10', 1702072.74, 1668698.76,  33373.98,        0.00,       0.00,        0.00,      0.00, 1, 'PENDIENTE');

-- -----------------------------------------------------------------------------
-- 7. PAGOS (Historial de abonos con consistencia de capital e interes)
-- -----------------------------------------------------------------------------
-- Restriccion de consistencia: monto = monto_mora + monto_interes + monto_capital
-- Prestamo 1: 2 pagos (Cuotas 1 y 2)
-- Prestamo 2: 2 pagos (Cuotas 1 y 2)
-- Prestamo 3: 3 pagos (Cuotas 1, 2 y 3)

INSERT INTO pagos (id, prestamo_id, cuota_id, fecha_pago, monto, monto_mora, monto_interes, monto_capital, tipo_pago, opcion_abono, observaciones) VALUES
-- Pagos Prestamo 1 (Cliente Camila Gutierrez)
(1, 1, 1,  '2026-08-30', 1134715.16, 0.00, 240000.00, 894715.16, 'CUOTA_PROGRAMADA', NULL, 'Pago puntual cuota #1 en ventanilla bancaria.'),
(2, 1, 2,  '2026-09-28', 1134715.16, 0.00, 222105.70, 912609.46, 'CUOTA_PROGRAMADA', NULL, 'Pago puntual cuota #2 por transferencia PSE.'),

-- Pagos Prestamo 2 (Cliente Mateo Morales)
(3, 2, 13, '2026-08-14', 1360000.00, 0.00, 360000.00, 1000000.00, 'CUOTA_PROGRAMADA', NULL, 'Pago cuota #1 amortizacion alemana.'),
(4, 2, 14, '2026-09-15', 1345000.00, 0.00, 345000.00, 1000000.00, 'CUOTA_PROGRAMADA', NULL, 'Pago cuota #2 amortizacion alemana.'),

-- Pagos Prestamo 3 (Cliente Santiago Zuluaga)
(5, 3, 37, '2026-06-08', 1702072.74, 0.00, 360000.00, 1342072.74, 'CUOTA_PROGRAMADA', NULL, 'Pago cuota #1.'),
(6, 3, 38, '2026-07-10', 1702072.74, 0.00, 333158.55, 1368914.19, 'CUOTA_PROGRAMADA', NULL, 'Pago cuota #2.'),
(7, 3, 39, '2026-08-15', 1702072.74, 0.00, 305780.26, 1396292.48, 'CUOTA_PROGRAMADA', NULL, 'Pago cuota #3 con leve retraso.');

COMMIT;

-- -----------------------------------------------------------------------------
-- 8. REAJUSTE DE CONTADORES AUTO_INCREMENT
-- -----------------------------------------------------------------------------
ALTER TABLE empleados    AUTO_INCREMENT = 7;
ALTER TABLE credenciales AUTO_INCREMENT = 7;
ALTER TABLE clientes     AUTO_INCREMENT = 11;
ALTER TABLE solicitudes  AUTO_INCREMENT = 11;
ALTER TABLE prestamos    AUTO_INCREMENT = 4;
ALTER TABLE cuotas       AUTO_INCREMENT = 49;
ALTER TABLE pagos        AUTO_INCREMENT = 8;

-- =============================================================================
-- 9. 20 CLIENTES DE PRUEBA Y CREDENCIALES (DOCUMENTOS 10000000 - 10000019)
-- =============================================================================
-- Password comun: 123456
INSERT INTO clientes (id, nombre, documento, correo, telefono) VALUES
(11, 'Alejandro Morales Vega', '10000000', 'alejandro.morales@test.com', '3001000000'),
(12, 'Beatriz Helena Castro', '10000001', 'beatriz.castro@test.com', '3001000001'),
(13, 'Camilo Andres Pardo', '10000002', 'camilo.pardo@test.com', '3001000002'),
(14, 'Daniela Maria Rios', '10000003', 'daniela.rios@test.com', '3001000003'),
(15, 'Esteban Jose Valencia', '10000004', 'esteban.valencia@test.com', '3001000004'),
(16, 'Fernanda Lucia Montoya', '10000005', 'fernanda.montoya@test.com', '3001000005'),
(17, 'Gabriel Antonio Ortiz', '10000006', 'gabriel.ortiz@test.com', '3001000006'),
(18, 'Helena Patricia Munoz', '10000007', 'helena.munoz@test.com', '3001000007'),
(19, 'Ignacio David Salcedo', '10000008', 'ignacio.salcedo@test.com', '3001000008'),
(20, 'Juliana Marcela Cardenas', '10000009', 'juliana.cardenas@test.com', '3001000009'),
(21, 'Kevin Mauricio Duarte', '10000010', 'kevin.duarte@test.com', '3001000010'),
(22, 'Lorena Isabel Bermudez', '10000011', 'lorena.bermudez@test.com', '3001000011'),
(23, 'Manuel Fernando Quintero', '10000012', 'manuel.quintero@test.com', '3001000012'),
(24, 'Natalia Sofia Herrera', '10000013', 'natalia.herrera@test.com', '3001000013'),
(25, 'Oscar Eduardo Salazar', '10000014', 'oscar.salazar@test.com', '3001000014'),
(26, 'Paula Andrea Giraldo', '10000015', 'paula.giraldo@test.com', '3001000015'),
(27, 'Quirino Alberto Mejia', '10000016', 'quirino.mejia@test.com', '3001000016'),
(28, 'Rosa Maria Villamizar', '10000017', 'rosa.villamizar@test.com', '3001000017'),
(29, 'Sergio Daniel Restrepo', '10000018', 'sergio.restrepo@test.com', '3001000018'),
(30, 'Tatiana Giselle Guzman', '10000019', 'tatiana.guzman@test.com', '3001000019')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);

INSERT INTO credenciales (id, cliente_id, password_hash, salt, iteraciones) VALUES
(8, 11, '28b69755104b0446b6907cdc6308248adb7f7a190d311595d3d83ea6df127363', 'f460ca653bd35196f42f1ee92573e1c2', 65536),
(9, 12, 'e5aa280e54738380cba23e7034ad5461a5e16bb781cf06832bdc1f580a08e001', 'd231bd06c7eaa967a0951b651cf99cbd', 65536),
(10, 13, 'e9eb40a28e74c2b7733cd42cf083d11a0ef2be5ed2c449edc42829ffa5d88fb3', '4f78c7a8402c65520e50c6bbb18bbfe7', 65536),
(11, 14, '4e38495e4ce455bbcb75b3a6cc862724cc4f894b15e247c816f859517a8583c5', '8d6b11cb9d80cf1967160c3a87434cd3', 65536),
(12, 15, '236fd977bb81352240dc90331d29b49f296f3a4eaf3172e12ed5c5ddc729af37', '8c6340ff5de391f04c7af1fc6ae6699b', 65536),
(13, 16, 'aad0064713ff474d513058d57abb55b866b490572388ddfd31f63f92a45710a6', 'c73aa1a49b803236ebaab377b088e583', 65536),
(14, 17, 'd481498310d33a58d7afc6480dca518c44051260f3623765955ccf176444e9c7', '378720f02d692edbb59bcc9617a833e9', 65536),
(15, 18, 'bebfacf95bde6211647dccea8169f10247797e51a9744cf5606a34d6a5bc38a0', '67dea6fa455b86c8948d99b1e67f3d55', 65536),
(16, 19, 'b985aadaee5593a4c8d0f902beb9d8ba7ebdbe766c5b374384ea7bc609142ff9', '7c8a1a8824a43d00adf8d71424d7d992', 65536),
(17, 20, '3349cd332de703737069c4ffde577e14907c56f9e64658be8e23fc50736a50a8', '1c6c527019d5d42f935fce465ccf4ae7', 65536),
(18, 21, '652b6e2cbba5ebb80397088c7d819cf0976f225deaafab2d68e382690b055180', 'b938f7198a7e13ad761c7eceded2c376', 65536),
(19, 22, '8eaa4c99ed77704c793501d4c1df34fa978d631a6f961b731f877b701796bfd3', '59ba78886501318552956da6e0276fbf', 65536),
(20, 23, '18148e616c977f408d545134a38465da7999707f924fb8c3204075c59812d568', '8e77708162dba7fff7ab0896f19aab9a', 65536),
(21, 24, 'a99aa9dff755c1a2caaaa34e348677fb4050feb4bcd1e05d139d1599dddd374b', '21a8a226b39e18046c1261366476274c', 65536),
(22, 25, '6e9612c643afa53ed41e429a0bdbd4a1099bd24dab7fc9c8024b689ac4fc0133', '48aac98d47a50cca2c35956572a2ccd7', 65536),
(23, 26, 'a3ca6b7c86ae0680c345b5273369d323f7070ebb43b1c8a6ee7f83f425cd829d', '1c48343b4b68c5d454bd9b51ba19feb6', 65536),
(24, 27, 'b628e0fc0c052061ce4b60f2be9b397b942eec4550cc000b2ad67fd302a1510c', 'e0241a8810f3a0e46dc065524a296a91', 65536),
(25, 28, '4a262d70bb3f8235570778f532de1c60c5d89382aaaee898f4f89e862703e41e', '0dca8ef117ed3e9e4ef151c438537aee', 65536),
(26, 29, '2ad7d41aeb59f7b5e0ad27add3ab0045ca81e5a196152bb5a982e42da978c415', 'cee75be37becaf3023875cc02de5d1df', 65536),
(27, 30, '579df49983b91ffcfecbac7b021f5096b6a01ee266cab3b04b3eb17eb3a164f5', '06f84db9b5e3ed1f0f8614ead0da8e2c', 65536)
ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash);
