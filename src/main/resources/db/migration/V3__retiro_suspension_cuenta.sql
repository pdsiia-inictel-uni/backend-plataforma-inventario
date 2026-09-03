-- ---------------------------------------------------------------------------
-- V3: la cuenta deja de tener suspension temporal (RF-22b, RN-34)
--
-- Hubo un tercer estado, SUSPENDIDA, para las ausencias con retorno:
-- vacaciones, un permiso, una licencia. Se retira porque una ausencia de dias
-- no es una decision sobre el acceso al sistema, y mantener dos formas
-- distintas de no entrar obligaba a cada pantalla a explicar cual era cual.
-- Lo que la institucion decide sobre una cuenta es si la persona sigue aqui o
-- ya no: la baja, y la reincorporacion de quien vuelve.
--
-- Esta migracion va aparte y no reescribe la linea base: cualquier instalacion
-- que ya haya aplicado V1 tiene su suma de comprobacion registrada, y cambiarla
-- dejaria la base sin poder migrar. Se corrige hacia adelante.
-- ---------------------------------------------------------------------------

-- Quien estaba suspendido vuelve a estar activo, con su puesto intacto: es lo
-- que la suspension prometia —conservarlo y devolverlo (RN-34)—, cumplido de
-- una vez. Convertirlos en bajas seria despedir a quien estaba de vacaciones,
-- y ademas les quitaria el puesto, que es lo que la baja hace.
UPDATE usuario SET estado = 'ACTIVA' WHERE estado = 'SUSPENDIDA';

ALTER TABLE usuario DROP CONSTRAINT IF EXISTS ck_usuario_estado;
ALTER TABLE usuario ADD CONSTRAINT ck_usuario_estado CHECK (estado IN ('ACTIVA', 'BAJA'));

COMMENT ON COLUMN usuario.estado IS
    'ACTIVA = trabaja y entra. BAJA = ya no pertenece a la institucion y no conserva puesto (RF-22b, RN-34). Los usuarios nunca se eliminan (RN-09).';
