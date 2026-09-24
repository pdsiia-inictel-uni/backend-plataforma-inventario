-- ============================================================================
-- Sistema de Gestion de Inventarios - INICTEL-UNI
-- V1: Esquema completo (ERS v1, seccion 5)
--
-- La institucion "INICTEL-UNI" es una constante de la aplicacion (RN-01);
-- no se persiste. La jerarquia comienza en DIRECCION.
--
-- La COORDINACION es la unidad de aislamiento del sistema: posee su propio
-- inventario, su unico Responsable y sus Operadores.
--
-- ----------------------------------------------------------------------------
-- Nota sobre la historia de este archivo
--
--   Hasta la v3.4 el esquema se construia en seis migraciones: la inicial de
--   la v2.0 y cinco que la iban corrigiendo —la baja de la bitacora, la
--   precarga de las direcciones, la tabla de asignaciones, el estrechamiento a
--   una coordinacion por persona—. Cada una tenia sentido mientras hubiera
--   instalaciones con datos que migrar; ninguna las tuvo. Con la base todavia
--   vacia, arrastrar ese historial obligaba a leer seis archivos para saber
--   como es una tabla, y dejaba en el esquema columnas que nacian y morian sin
--   haber guardado nunca un dato.
--
--   La v3.5 las funde en esta: el esquema se declara una vez, tal como es hoy.
--   Lo que aquellas migraciones decian sigue dicho en la ERS, que es donde se
--   explica por que el sistema es como es (seccion 5 y capitulo 0).
--
--   La v1 hace lo mismo con las cuatro que vinieron despues (la retirada de la
--   suspension de cuentas, los prestamos a personas registradas con la baja
--   definitiva de bienes, el codigo patrimonial alfanumerico y el PDF de la
--   baja): la base del servidor estaba vacia y ninguna tenia datos que migrar.
--   El sistema se instala desde cero con V1 (esquema) y V2 (datos base).
-- ----------------------------------------------------------------------------
-- ============================================================================

-- ---------------------------------------------------------------------------
-- DIRECCION (RF-10)
--
-- Son las dos del reglamento de organizacion y funciones de INICTEL-UNI. Se
-- precargan en V2 y la aplicacion no ofrece alta ni desactivacion (RN-30):
-- solo la correccion de su nombre y su sigla. Sin estado ni fechas: una
-- direccion existe siempre y nadie la apaga.
-- ---------------------------------------------------------------------------
CREATE TABLE direccion (
    id                   BIGSERIAL     PRIMARY KEY,
    nombre               VARCHAR(150)  NOT NULL,
    sigla                VARCHAR(20),
    CONSTRAINT ck_direccion_nombre CHECK (LENGTH(TRIM(nombre)) > 0)
);

CREATE UNIQUE INDEX uk_direccion_nombre ON direccion (LOWER(nombre));

COMMENT ON TABLE direccion IS
    'Direcciones de INICTEL-UNI (RF-10). Se precargan con el esquema: son fijas y no se crean ni se desactivan desde la aplicacion.';
COMMENT ON COLUMN direccion.sigla IS
    'DIDT y DCTT. La sigla de una direccion es institucional y conocida, al contrario que la de una coordinacion, que no existe (RF-11).';

-- ---------------------------------------------------------------------------
-- COORDINACION (RF-11) - unidad de aislamiento (RN-23)
--
-- No tiene sigla: en la institucion las coordinaciones se nombran por su
-- nombre, y el campo se quedaba vacio o se rellenaba con una abreviatura
-- inventada en el momento, distinta en cada tarjeta.
--
-- Tampoco tiene estado: una coordinacion es una unidad estable de la
-- institucion y no se desactiva (RF-13).
-- ---------------------------------------------------------------------------
CREATE TABLE coordinacion (
    id                   BIGSERIAL     PRIMARY KEY,
    direccion_id         BIGINT        NOT NULL REFERENCES direccion (id),
    nombre               VARCHAR(150)  NOT NULL,
    descripcion          VARCHAR(500),
    CONSTRAINT ck_coordinacion_nombre CHECK (LENGTH(TRIM(nombre)) > 0)
);

-- RN-02: el nombre es unico dentro de su Direccion
CREATE UNIQUE INDEX uk_coordinacion_nombre ON coordinacion (direccion_id, LOWER(nombre));
CREATE INDEX ix_coordinacion_direccion ON coordinacion (direccion_id);

-- Necesario para la llave foranea compuesta de LABORATORIO (RN-03)
CREATE UNIQUE INDEX uk_coordinacion_id_direccion ON coordinacion (id, direccion_id);

COMMENT ON TABLE coordinacion IS
    'Unidad de aislamiento: inventario, responsable unico y operadores propios (RN-23).';

-- ---------------------------------------------------------------------------
-- LABORATORIO (RF-12)
--
-- Sin estado: un laboratorio no se desactiva. Existe mientras la coordinacion
-- lo tenga registrado.
-- ---------------------------------------------------------------------------
CREATE TABLE laboratorio (
    id                   BIGSERIAL     PRIMARY KEY,
    coordinacion_id      BIGINT        NOT NULL REFERENCES coordinacion (id),
    nombre               VARCHAR(150)  NOT NULL,
    ubicacion            VARCHAR(200),
    CONSTRAINT ck_laboratorio_nombre CHECK (LENGTH(TRIM(nombre)) > 0)
);

CREATE UNIQUE INDEX uk_laboratorio_nombre ON laboratorio (coordinacion_id, LOWER(nombre));
CREATE INDEX ix_laboratorio_coordinacion  ON laboratorio (coordinacion_id);

-- Permite que EQUIPO referencie (laboratorio_id, coordinacion_id) y garantice
-- en la propia base que un bien nunca se ubica en un laboratorio ajeno (RN-12).
CREATE UNIQUE INDEX uk_laboratorio_id_coordinacion ON laboratorio (id, coordinacion_id);

COMMENT ON TABLE laboratorio IS
    'Ubicacion fisica dentro de una Coordinacion (RF-12). Toda Coordinacion nace con uno (RN-26).';

-- ---------------------------------------------------------------------------
-- USUARIO (RF-16 .. RF-30)
--
-- El rol admite NULL: es la persona registrada que todavia no tiene puesto
-- (RF-16b). Su coordinacion no vive aqui, sino en usuario_coordinacion: la
-- asignacion es una relacion, no un atributo del usuario.
-- ---------------------------------------------------------------------------
CREATE TABLE usuario (
    id                      BIGSERIAL     PRIMARY KEY,
    username                VARCHAR(50)   NOT NULL,
    nombres                 VARCHAR(100)  NOT NULL,
    primer_apellido         VARCHAR(100)  NOT NULL,
    segundo_apellido        VARCHAR(100)  NOT NULL,
    dni                     VARCHAR(8)    NOT NULL,
    cargo                   VARCHAR(150),
    correo                  VARCHAR(150)  NOT NULL,
    password_hash           VARCHAR(100)  NOT NULL,
    rol                     VARCHAR(20),
    estado                  VARCHAR(20)   NOT NULL DEFAULT 'ACTIVA',
    debe_cambiar_password   BOOLEAN       NOT NULL DEFAULT FALSE,
    intentos_fallidos       INTEGER       NOT NULL DEFAULT 0,
    bloqueado_hasta         TIMESTAMP,
    ultimo_acceso           TIMESTAMP,
    fecha_creacion          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion     TIMESTAMP,
    -- RF-23b: el puesto que tenia la persona al darse de baja. La baja libera
    -- el puesto (RN-34) —borra la asignacion y deja el rol en NULL— y sin este
    -- rastro la persona desaparecia de la lista de su Responsable y de los
    -- filtros por rol y por coordinacion. Se limpian al reincorporarla.
    ultimo_rol              VARCHAR(20),
    ultima_coordinacion_id  BIGINT        REFERENCES coordinacion (id),
    CONSTRAINT ck_usuario_rol CHECK (rol IN ('ADMIN', 'RESPONSABLE', 'OPERADOR')),
    -- RF-22b: la persona sigue en la institucion o ya no. No hay suspension
    -- temporal (RF-22c).
    CONSTRAINT ck_usuario_estado CHECK (estado IN ('ACTIVA', 'BAJA')),
    CONSTRAINT ck_usuario_dni CHECK (dni ~ '^[0-9]{8}$'),
    CONSTRAINT ck_usuario_ultimo_rol
        CHECK (ultimo_rol IS NULL OR ultimo_rol IN ('ADMIN', 'RESPONSABLE', 'OPERADOR'))
);

CREATE UNIQUE INDEX uk_usuario_username ON usuario (LOWER(username));
CREATE UNIQUE INDEX uk_usuario_correo   ON usuario (LOWER(correo));
CREATE UNIQUE INDEX uk_usuario_dni      ON usuario (dni);

CREATE INDEX ix_usuario_rol_estado ON usuario (rol, estado);
CREATE INDEX ix_usuario_ultima_coordinacion ON usuario (ultima_coordinacion_id)
    WHERE ultima_coordinacion_id IS NOT NULL;

COMMENT ON COLUMN usuario.estado IS
    'ACTIVA = trabaja y entra. BAJA = ya no pertenece a la institucion y no conserva puesto (RF-22b, RN-34). Los usuarios nunca se eliminan (RN-09).';
COMMENT ON COLUMN usuario.rol IS
    'ADMIN | RESPONSABLE | OPERADOR. NULL = registro previo: la persona existe pero aun no tiene puesto ni puede entrar (RF-16b).';
COMMENT ON COLUMN usuario.segundo_apellido IS
    'Obligatorio: la identidad de una persona en la institucion son sus dos apellidos (RF-16).';

-- ---------------------------------------------------------------------------
-- USUARIO_COORDINACION - el puesto de una persona (RF-28d)
--
-- Una fila por persona como maximo (RN-05) y una sola con rol RESPONSABLE por
-- coordinacion (RN-06). Dar de baja un puesto es borrar su fila; la persona
-- conserva su cuenta y su historial (RF-26).
-- ---------------------------------------------------------------------------
CREATE TABLE usuario_coordinacion (
    usuario_id        BIGINT      NOT NULL REFERENCES usuario (id),
    coordinacion_id   BIGINT      NOT NULL REFERENCES coordinacion (id),
    rol               VARCHAR(20) NOT NULL,
    fecha_asignacion  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (usuario_id, coordinacion_id),
    -- El ADMIN no se asigna a ninguna coordinacion: su ambito es la institucion
    CONSTRAINT ck_asignacion_rol CHECK (rol IN ('RESPONSABLE', 'OPERADOR'))
);

-- RN-05: una persona, una coordinacion. Vale para los dos roles operativos.
CREATE UNIQUE INDEX uk_persona_en_una_coordinacion
    ON usuario_coordinacion (usuario_id);

-- RN-06: una coordinacion tiene como maximo un RESPONSABLE. La regla vive
-- tambien en el dominio; este indice la hace imposible de violar incluso ante
-- dos peticiones concurrentes.
CREATE UNIQUE INDEX uk_responsable_por_coordinacion
    ON usuario_coordinacion (coordinacion_id)
    WHERE rol = 'RESPONSABLE';

CREATE INDEX ix_asignacion_coordinacion ON usuario_coordinacion (coordinacion_id, rol);

COMMENT ON TABLE usuario_coordinacion IS
    'Puesto de una persona (RF-28d). Una persona pertenece a una sola coordinacion (RN-05) y una coordinacion tiene un solo responsable (RN-06).';

-- ---------------------------------------------------------------------------
-- CATEGORIA (RF-31 .. RF-33) - catalogo institucional compartido
-- ---------------------------------------------------------------------------
CREATE TABLE categoria (
    id              BIGSERIAL     PRIMARY KEY,
    nombre          VARCHAR(100)  NOT NULL,
    descripcion     VARCHAR(500),
    activa          BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_categoria_nombre ON categoria (LOWER(nombre));

-- ---------------------------------------------------------------------------
-- EQUIPO / BIEN (RF-34 .. RF-52)
--
-- Dos columnas cambian en la v3.10, con la base todavia vacia:
--
--   fecha_adquisicion  sustituye a anio_adquisicion. El formato de registro de
--                      uso pide la fecha de la orden de compra (RF-78), y de un
--                      anio no se saca una fecha. Guardar menos de lo que el
--                      documento necesita obliga a rellenarlo a mano con un
--                      dato que el sistema ya deberia conocer (RF-34).
--
--   responsable_equipo_id  el Operador que tiene el bien a su cargo (RF-83).
--                      NULL no es un hueco: significa que lo lleva el
--                      Responsable de la Coordinacion, sea quien sea en cada
--                      momento. Asi la baja de un Responsable, que deja la
--                      Coordinacion parada pero no borra sus bienes, no deja
--                      ninguna referencia colgando (RN-37).
-- ---------------------------------------------------------------------------
CREATE TABLE equipo (
    id                     BIGSERIAL      PRIMARY KEY,
    coordinacion_id        BIGINT         NOT NULL REFERENCES coordinacion (id),
    laboratorio_id         BIGINT,
    nombre                 VARCHAR(150)   NOT NULL,
    marca                  VARCHAR(100)   NOT NULL,
    modelo                 VARCHAR(100)   NOT NULL,
    numero_serie           VARCHAR(100)   NOT NULL,
    codigo_inventario      VARCHAR(60)    NOT NULL,
    codigo_patrimonial     VARCHAR(60)    NOT NULL,
    categoria_id           BIGINT         NOT NULL REFERENCES categoria (id),
    condicion              VARCHAR(20)    NOT NULL DEFAULT 'OPERATIVO',
    fecha_adquisicion      DATE           NOT NULL,
    costo                  NUMERIC(12, 2) NOT NULL,
    observaciones          VARCHAR(1000),
    foto_url               VARCHAR(300),
    revision_pendiente     BOOLEAN        NOT NULL DEFAULT FALSE,
    motivo_baja            VARCHAR(500),
    fecha_baja             TIMESTAMP,
    responsable_id         BIGINT         REFERENCES usuario (id),
    responsable_equipo_id  BIGINT,
    usuario_registro_id    BIGINT         NOT NULL REFERENCES usuario (id),
    fecha_registro         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion    TIMESTAMP,
    activo                 BOOLEAN        NOT NULL DEFAULT TRUE,
    -- RF-42: ruta del PDF que sustenta la baja (carpeta archivos/).
    documento_baja_url     VARCHAR(300),
    CONSTRAINT ck_equipo_condicion CHECK (condicion IN ('OPERATIVO', 'PRESTADO', 'MANTENIMIENTO', 'BAJA')),
    -- El limite superior —que la fecha no sea futura— no cabe en un CHECK:
    -- exigiria CURRENT_DATE, que no es inmutable. Vive en el dominio (RN-25).
    CONSTRAINT ck_equipo_fecha_adq CHECK (fecha_adquisicion >= DATE '1980-01-01'),
    CONSTRAINT ck_equipo_costo     CHECK (costo >= 0),
    -- RF-34: exactamente 12 letras y/o numeros; la aplicacion lo guarda en
    -- mayusculas.
    CONSTRAINT ck_equipo_codigo_patrimonial CHECK (codigo_patrimonial ~ '^[A-Z0-9]{12}$'),
    -- RN-12: el laboratorio, si existe, pertenece a la misma Coordinacion que el bien
    CONSTRAINT fk_equipo_laboratorio FOREIGN KEY (laboratorio_id, coordinacion_id)
        REFERENCES laboratorio (id, coordinacion_id),
    -- RN-37, RN-38: quien tiene el bien a su cargo trabaja en la Coordinacion
    -- del bien. La llave apunta a la asignacion, no al usuario, y con ello la
    -- base sostiene por si misma las dos mitades de la regla: no se puede
    -- poner a cargo a alguien de otra Coordinacion, y no se puede retirar de
    -- su puesto a quien tiene bienes a su nombre, porque su fila de
    -- usuario_coordinacion esta referenciada.
    --
    -- Se declara aplazable porque la asignacion se mapea como coleccion y
    -- cambiar el rol de una persona se traduce en un borrado y un alta de su
    -- fila dentro de la misma transaccion: comprobada al vuelo, la regla
    -- rechazaria un estado intermedio que al confirmar ya es valido.
    CONSTRAINT fk_equipo_responsable_equipo FOREIGN KEY (responsable_equipo_id, coordinacion_id)
        REFERENCES usuario_coordinacion (usuario_id, coordinacion_id)
        DEFERRABLE INITIALLY DEFERRED
);

-- RN-14: unicidad institucional, sin distincion de Coordinacion.
-- El comodin 'S/N' queda exento porque el mobiliario carece de serie.
CREATE UNIQUE INDEX uk_equipo_numero_serie
    ON equipo (UPPER(numero_serie))
    WHERE UPPER(numero_serie) <> 'S/N';

CREATE UNIQUE INDEX uk_equipo_codigo_inventario  ON equipo (UPPER(codigo_inventario));
CREATE UNIQUE INDEX uk_equipo_codigo_patrimonial ON equipo (UPPER(codigo_patrimonial));

-- Necesario para la llave foranea compuesta de PRESTAMO
CREATE UNIQUE INDEX uk_equipo_id_coordinacion ON equipo (id, coordinacion_id);

-- RNF-14: indices de apoyo. La consulta dominante del sistema es
-- "bienes de una Coordinacion filtrados por condicion" (RF-47).
CREATE INDEX ix_equipo_coord_condicion ON equipo (coordinacion_id, condicion);
CREATE INDEX ix_equipo_coord_categoria ON equipo (coordinacion_id, categoria_id);
CREATE INDEX ix_equipo_laboratorio     ON equipo (laboratorio_id);
CREATE INDEX ix_equipo_responsable     ON equipo (responsable_id);
CREATE INDEX ix_equipo_nombre          ON equipo (LOWER(nombre));
CREATE INDEX ix_equipo_marca           ON equipo (LOWER(marca));
CREATE INDEX ix_equipo_revision        ON equipo (coordinacion_id) WHERE revision_pendiente = TRUE;

-- RN-38: "que bienes tiene a su cargo esta persona" se pregunta antes de cada
-- baja y de cada cambio de puesto, asi que conviene que sea barata.
CREATE INDEX ix_equipo_responsable_equipo ON equipo (responsable_equipo_id)
    WHERE responsable_equipo_id IS NOT NULL;

COMMENT ON COLUMN equipo.condicion IS 'OPERATIVO | PRESTADO | MANTENIMIENTO | BAJA. Todo bien nace OPERATIVO (RN-13).';
COMMENT ON COLUMN equipo.fecha_adquisicion IS
    'Fecha de la orden de compra (RF-34). Sustituye al anio desde la v3.10: el formato de registro de uso la pide entera (RF-78).';
COMMENT ON COLUMN equipo.responsable_id IS 'Responsable vigente de la Coordinacion al momento del alta (RF-36).';
COMMENT ON COLUMN equipo.responsable_equipo_id IS
    'Operador que tiene el bien a su cargo (RF-83). NULL = lo lleva el Responsable de la Coordinacion, sea quien sea en cada momento (RN-37).';
COMMENT ON COLUMN equipo.revision_pendiente IS 'Devuelto con dano; el Responsable debe confirmar mantenimiento (RN-19).';
COMMENT ON COLUMN equipo.activo IS 'FALSE = dado de baja. Baja logica, nunca fisica (RNF-47).';
COMMENT ON COLUMN equipo.documento_baja_url IS
    'Ruta del PDF que sustenta la baja (carpeta archivos/, RF-42). motivo_baja queda para una baja sin documento.';

-- ---------------------------------------------------------------------------
-- MOVIMIENTO_EQUIPO - historial inmutable del bien (RF-53 .. RF-57)
-- ---------------------------------------------------------------------------
CREATE TABLE movimiento_equipo (
    id                  BIGSERIAL     PRIMARY KEY,
    equipo_id           BIGINT        NOT NULL REFERENCES equipo (id),
    tipo                VARCHAR(20)   NOT NULL,
    condicion_anterior  VARCHAR(20),
    condicion_nueva     VARCHAR(20),
    detalle             VARCHAR(1000),
    usuario_id          BIGINT        REFERENCES usuario (id),
    usuario_nombre      VARCHAR(200),
    rol_usuario         VARCHAR(20),
    fecha_hora          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_movimiento_tipo CHECK (tipo IN (
        'ALTA', 'PRESTAMO', 'DEVOLUCION', 'MANTENIMIENTO', 'OPERATIVO',
        'BAJA', 'REINCORPORACION', 'EDICION', 'REUBICACION', 'RESPONSABLE'))
);

CREATE INDEX ix_movimiento_equipo ON movimiento_equipo (equipo_id, fecha_hora DESC);

COMMENT ON TABLE movimiento_equipo IS 'Linea de tiempo del bien. Solo INSERT: ver trigger de inmutabilidad (RN-20, RN-21).';

-- ---------------------------------------------------------------------------
-- PRESTAMO (RF-58 .. RF-68)
-- ---------------------------------------------------------------------------
CREATE TABLE prestamo (
    id                         BIGSERIAL     PRIMARY KEY,
    equipo_id                  BIGINT        NOT NULL,
    coordinacion_id            BIGINT        NOT NULL REFERENCES coordinacion (id),
    nombre_persona             VARCHAR(200)  NOT NULL,
    dni_persona                VARCHAR(8)    NOT NULL,
    destino                    VARCHAR(200),
    fecha_prestamo             TIMESTAMP     NOT NULL,
    fecha_estimada_devolucion  DATE,
    observaciones_salida       VARCHAR(1000),
    fecha_devolucion           TIMESTAMP,
    conforme                   BOOLEAN,
    reporta_dano               BOOLEAN       NOT NULL DEFAULT FALSE,
    observaciones_retorno      VARCHAR(1000),
    usuario_presta_id          BIGINT        NOT NULL REFERENCES usuario (id),
    usuario_recibe_id          BIGINT        REFERENCES usuario (id),
    estado                     VARCHAR(20)   NOT NULL DEFAULT 'ACTIVO',
    -- RF-59: el equipo sale a nombre de un Responsable u Operador registrado,
    -- de la coordinacion de destino que se elige primero. nombre_persona y
    -- dni_persona son la fotografia del momento de la salida: el historial no
    -- cambia si la persona corrige despues sus datos.
    persona_usuario_id         BIGINT        REFERENCES usuario (id),
    coordinacion_destino_id    BIGINT        REFERENCES coordinacion (id),
    CONSTRAINT ck_prestamo_estado CHECK (estado IN ('ACTIVO', 'DEVUELTO')),
    CONSTRAINT ck_prestamo_dni    CHECK (dni_persona ~ '^[0-9]{8}$'),
    -- El prestamo hereda la Coordinacion del bien; la base lo garantiza (RN-23)
    CONSTRAINT fk_prestamo_equipo FOREIGN KEY (equipo_id, coordinacion_id)
        REFERENCES equipo (id, coordinacion_id)
);

-- RN-16: un bien no puede tener dos prestamos activos simultaneos
CREATE UNIQUE INDEX uk_prestamo_activo_por_equipo
    ON prestamo (equipo_id)
    WHERE estado = 'ACTIVO';

CREATE INDEX ix_prestamo_coord_estado ON prestamo (coordinacion_id, estado);
CREATE INDEX ix_prestamo_equipo       ON prestamo (equipo_id, fecha_prestamo DESC);
CREATE INDEX ix_prestamo_dni          ON prestamo (dni_persona);
CREATE INDEX ix_prestamo_vencidos     ON prestamo (fecha_estimada_devolucion)
    WHERE estado = 'ACTIVO';
CREATE INDEX ix_prestamo_persona_usuario ON prestamo (persona_usuario_id);
CREATE INDEX ix_prestamo_coord_destino   ON prestamo (coordinacion_destino_id);

COMMENT ON COLUMN prestamo.persona_usuario_id IS
    'Usuario registrado (Responsable u Operador) que se lleva el bien.';
COMMENT ON COLUMN prestamo.coordinacion_destino_id IS
    'Coordinacion de destino del bien: la de la persona que lo lleva.';

-- ---------------------------------------------------------------------------
-- USO EXTERNO (RF-78): prestamo de un equipo para que lo use, dentro de la
-- institucion, personal de otra institucion (UPC, UTEC...) con su encargado.
-- El punto 1 conserva el formato en papel: el responsable del equipamiento es
-- el Coordinador, y se anotan el investigador encargado, su correo y celular.
--
-- Es el "Formato de registro de uso de equipos de investigacion", guardado.
-- Se registra en dos partes:
--   * apertura (puntos 1 a 5): encargado, equipo, usuario, proyecto y uso.
--     El inicio lo pone el servidor y el equipo pasa a PRESTADO.
--   * cierre (puntos 6 a 10): conformidad de entrega y de devolucion,
--     incidentes y observaciones. El equipo vuelve a OPERATIVO, con revision
--     pendiente si volvio en malas condiciones.
-- El punto 9 son las firmas: se ponen a mano sobre el PDF impreso.
-- ---------------------------------------------------------------------------
CREATE TABLE uso_externo (
    id                     BIGSERIAL     PRIMARY KEY,
    equipo_id              BIGINT        NOT NULL,
    coordinacion_id        BIGINT        NOT NULL REFERENCES coordinacion (id),

    -- 1. Responsable del equipamiento (Coordinador): investigador encargado
    encargado_nombre       VARCHAR(150)  NOT NULL,
    encargado_correo       VARCHAR(150),
    encargado_celular      VARCHAR(30),

    -- 2. Condicion del equipo al abrirse el uso (el resto sale del bien)
    estado_equipo_inicio   VARCHAR(40)   NOT NULL,

    -- 3. Datos del usuario
    usuario_nombre         VARCHAR(150)  NOT NULL,
    usuario_correo         VARCHAR(150),
    usuario_telefono       VARCHAR(30),

    -- 4. Proyecto asociado y actividad
    proyecto               VARCHAR(1000),

    -- 5. Registro de uso
    fecha_inicio           TIMESTAMP     NOT NULL,
    fecha_fin_prevista     DATE,
    hora_fin_prevista      TIME,
    actividad              VARCHAR(1000),
    usuario_registra_id    BIGINT        NOT NULL REFERENCES usuario (id),

    -- 6 a 10. Cierre
    entregado_operativo    BOOLEAN,
    devuelto_operativo     BOOLEAN,
    incidente              VARCHAR(1000),
    accion_correctiva      VARCHAR(1000),
    observaciones          VARCHAR(1000),
    fecha_cierre           TIMESTAMP,
    usuario_cierra_id      BIGINT        REFERENCES usuario (id),

    estado                 VARCHAR(20)   NOT NULL DEFAULT 'ABIERTO',
    CONSTRAINT ck_uso_externo_estado CHECK (estado IN ('ABIERTO', 'CERRADO')),
    -- Un uso cerrado tiene su cierre completo; uno abierto, ninguno.
    CONSTRAINT ck_uso_externo_cierre CHECK (
        (estado = 'ABIERTO' AND fecha_cierre IS NULL AND usuario_cierra_id IS NULL)
        OR (estado = 'CERRADO' AND fecha_cierre IS NOT NULL AND usuario_cierra_id IS NOT NULL
            AND entregado_operativo IS NOT NULL AND devuelto_operativo IS NOT NULL)),
    -- El uso hereda la Coordinacion del bien; la base lo garantiza (RN-23)
    CONSTRAINT fk_uso_externo_equipo FOREIGN KEY (equipo_id, coordinacion_id)
        REFERENCES equipo (id, coordinacion_id)
);

-- Un equipo no puede estar en dos usos externos abiertos a la vez.
CREATE UNIQUE INDEX uk_uso_externo_abierto_por_equipo
    ON uso_externo (equipo_id)
    WHERE estado = 'ABIERTO';

CREATE INDEX ix_uso_externo_equipo      ON uso_externo (equipo_id, fecha_inicio DESC);
CREATE INDEX ix_uso_externo_coord_estado ON uso_externo (coordinacion_id, estado);

-- ---------------------------------------------------------------------------
-- INMUTABILIDAD DEL HISTORIAL (RN-21, RF-55)
--
-- Defensa en profundidad: aunque el codigo de la aplicacion nunca emite
-- UPDATE ni DELETE sobre el historial del bien, la base lo impide por si
-- misma. Ni siquiera un error de programacion futuro puede alterar una fecha
-- ya registrada.
--
-- El sistema no lleva bitacora de acciones de las personas (RN-28): la
-- trazabilidad que la normativa de bienes estatales exige es la del bien, y es
-- esta.
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_registro_inmutable() RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'El registro % es inmutable: no admite % (RN-21).',
        TG_TABLE_NAME, TG_OP;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_movimiento_equipo_inmutable
    BEFORE UPDATE OR DELETE ON movimiento_equipo
    FOR EACH ROW EXECUTE FUNCTION fn_registro_inmutable();

-- ---------------------------------------------------------------------------
-- BAJA DEFINITIVA (RF-43)
--
-- Un bien dado de baja no vuelve al inventario. La aplicacion no ofrece la
-- reincorporacion; la base lo sostiene por su cuenta, igual que sostiene la
-- inmutabilidad del historial (defensa en profundidad).
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_equipo_baja_definitiva() RETURNS trigger AS $$
BEGIN
    IF OLD.activo = FALSE AND (NEW.activo = TRUE OR NEW.condicion <> 'BAJA') THEN
        RAISE EXCEPTION 'Un bien dado de baja no puede reincorporarse (equipo %).', OLD.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tg_equipo_baja_definitiva
    BEFORE UPDATE ON equipo
    FOR EACH ROW EXECUTE FUNCTION fn_equipo_baja_definitiva();
