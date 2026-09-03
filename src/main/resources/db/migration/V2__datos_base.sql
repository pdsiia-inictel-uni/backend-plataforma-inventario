-- ============================================================================
-- Sistema de Gestion de Inventarios - INICTEL-UNI
-- V2: Datos que el sistema trae puestos
--
--   a) Las dos DIRECCIONES de la institucion (RF-10, RN-30). Estan en su
--      reglamento de organizacion y funciones y no cambian con el uso del
--      sistema: pedirle al Administrador que las escriba su primer dia era
--      pedirle que transcribiera —y pudiera equivocarse en— algo que ya se
--      sabe. La aplicacion no ofrece darlas de alta ni desactivarlas; solo
--      corregir sus datos.
--
--   b) El CATALOGO DE CATEGORIAS de bienes (RF-31, RF-32), institucional y
--      compartido por todas las coordinaciones.
--
-- Lo que NO se precarga: las coordinaciones y sus laboratorios, que son el
-- trabajo del Administrador y varian con la institucion (RF-11), y la cuenta
-- administradora inicial, que la crea la aplicacion porque necesita el hash
-- BCrypt de su contrasena (CuentaInicialInitializer).
-- ============================================================================

INSERT INTO direccion (nombre, sigla, descripcion, activa) VALUES
    ('Dirección de Investigación y Desarrollo Tecnológico',
     'DIDT',
     'Encargada de planificar y ejecutar proyectos de investigación científica y aplicada en telecomunicaciones y TIC.',
     TRUE),
    ('Dirección de Capacitación y Transferencia Tecnológica',
     'DCTT',
     'Encargada de los servicios de asesoría, consultoría, transferencia tecnológica y programas de capacitación especializada.',
     TRUE);

INSERT INTO categoria (nombre, descripcion, activa) VALUES
    ('Equipos de cómputo',  'Computadoras de escritorio, laptops, servidores y periféricos', TRUE),
    ('Drones',              'Vehículos aéreos no tripulados y sus accesorios',               TRUE),
    ('Cámaras espectrales', 'Cámaras multiespectrales e hiperespectrales',                   TRUE),
    ('Instrumentación',     'Instrumentos de medición y laboratorio',                        TRUE),
    ('Mobiliario',          'Escritorios, sillas, armarios y mobiliario en general',         TRUE),
    ('PDI',                 'Pizarras digitales interactivas',                               TRUE),
    ('Impresoras',          'Impresoras, escáneres y equipos multifuncionales',              TRUE),
    ('Otros',               'Bienes que no corresponden a las categorías anteriores',        TRUE);
