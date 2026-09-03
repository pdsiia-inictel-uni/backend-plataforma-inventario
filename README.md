# Especificación de Requisitos de Software (ERS / SRS)

## Sistema de Gestión de Inventarios — INICTEL-UNI

**Alcance institucional:** Direcciones, Coordinaciones y Laboratorios

| Campo | Valor |
| --- | --- |
| Versión | 3.11 |
| Fecha | Septiembre 2026 |
| Reemplaza a | Versión 3.10 (la fotografía del bien al alcance del Operador y fuera de la edición, los reportes en color y sin identificar, y el símbolo S/ montado sobre el importe) |
| Estándar base | IEEE 830 / ISO/IEC/IEEE 29148 (adaptado) |
| Arquitectura | Domain-Driven Design — monolito modular por contextos delimitados |

---

## 0. Resumen de cambios respecto a la versión 3.10

La versión 3.11 no toca el esquema de la base, ni los préstamos, ni el
historial, ni el responsable del equipo que estrenó la 3.10. Cambia **quién
puede poner la fotografía de un bien y desde dónde**, **cómo se ven los
reportes impresos** y corrige un fallo de estilo que la propia 3.10 había
introducido.

Tres observaciones la motivan. La fotografía era la única escritura que un
Operador podía hacer sobre un equipo **ya registrado**: se subía desde la ficha,
suelta y sin confirmar nada, mientras que todos los demás datos del bien —hasta
el más pequeño— pasan por *Editar* y por su revisión. Los reportes salían en
azul corporativo con cabeceras de fondo oscuro y filas alternas grisáceas, y se
imprimen en impresoras comunes para archivarse en papel, donde el color no
aporta jerarquía y sí gasta tóner; y además no decían de quién eran: un listado
de bienes sin la institución ni la Dirección no se le puede presentar a nadie. Y
el símbolo **S/** del campo *Costo en soles* se había montado encima del importe.

| Id | Cambio |
| --- | --- |
| V311-01 | **La fotografía de un equipo registrado la pone solo el Responsable.** `POST /api/equipos/{id}/foto` pasa de admitir `RESPONSABLE` y `OPERADOR` a admitir solo al primero, y el servicio comprueba lo mismo con el guardián que ya usaban editar, mantenimiento y baja. Era la última rendija: sobre un bien ya inventariado el Operador no escribe nada (RF-45), y una fotografía es un dato del bien como el modelo o el laboratorio (RF-51c). |
| V311-02 | **La fotografía se cambia desde *Editar*, con el resto de los datos.** Deja de subirse desde la ficha —donde iba suelta, se guardaba al elegir el archivo y no pasaba por ninguna revisión— y pasa a ser una sección del formulario de edición, con la cámara del dispositivo o un archivo, y se confirma junto a todo lo demás en el paso de resumen (RF-51c, RNF-26). La ficha la sigue enseñando y ampliando, y dice dónde se cambia. |
| V311-03 | **En el alta, la fotografía la ofrece el formulario solo a quien puede ponerla.** El Operador registra el equipo sin ella y el Responsable la adjunta después desde *Editar*. El rótulo del paso cambia con el rol —"Adquisición, ubicación y fotografía" o "Adquisición y ubicación"—, porque anunciar un paso con algo que no va a aparecer es prometer lo que no se cumple (RNF-23). |
| V311-04 | **Los tres reportes del inventario salen en blanco y negro.** Excel, CSV y PDF pierden el azul de marca, el fondo oscuro de las cabeceras y el sombreado alterno de las filas. La jerarquía la marcan ahora el tamaño, la negrita y las líneas: en Excel la cabecera lleva borde y línea inferior gruesa; en el PDF, cada celda su recuadro. Se imprimen para archivarse, y un reporte que depende del color no sobrevive a una impresora en blanco y negro (RF-52). |
| V311-05 | **El formato de registro de salida también.** Sus bandas de sección, que iban en azul con texto blanco, pasan a texto negro con una línea inferior gruesa; las etiquetas pierden su fondo gris y las líneas pasan a negro. Es un papel que se imprime cada vez que sale un equipo, se firma a mano y se archiva (RF-78). |
| V311-06 | **Los reportes dicen de quién son.** Encabezan con **Sede Central INICTEL-UNI**, con el nombre completo de la **Dirección** y con el de la **Coordinación** —las tres cosas, en ese orden—, seguidas de qué es el documento y de cuándo se generó. Cuando el Administrador exporta la institución entera, la línea lo dice con esas palabras en lugar de dejar el hueco vacío. El formato de registro de salida gana también la línea de la Dirección, que es la que le faltaba para identificar la dependencia del equipo que sale (RF-52b). |
| V311-07 | **El símbolo S/ vuelve a su sitio.** La v3.10 reescribió la regla de estilo de los campos como `input:not(...):not(...)`, y eso, además de cubrir los tipos que faltaban, **multiplicó su especificidad**: la regla base pasó a ganarle a todas las que la matizan y las anuló en silencio. El importe perdió su sangría izquierda y se montó bajo el "S/"; el campo inválido perdió su borde rojo y el foco su anillo, aunque nadie había llegado a verlo todavía. La exclusión se envuelve en `:where()`, que no suma especificidad, de modo que la regla pesa lo mínimo y cada matiz vuelve a ganarle (RNF-22). |

**Lo que no cambia.** El esquema de la base queda exactamente como lo dejó la
v3.10: ni una columna. La fotografía sigue siendo opcional y sigue sin
condicionar el alta (RN-29); el visor a tamaño completo de la ficha, con su
cierre por teclado, queda intacto (RNF-32). Los datos de los reportes son los
mismos y en el mismo orden: lo que cambia es cómo se ven y qué dicen de sí
mismos.

---

## 0.1 Resumen de cambios de la versión 3.10 respecto a la 3.9

La versión 3.10 no toca la jerarquía organizacional, ni los préstamos, ni el
historial inmutable, ni el formato de registro de uso, ni la CSP. Es la
primera desde la 3.7 que **cambia el esquema de la base**, y lo hace en la
tabla del bien: una columna se sustituye y otra se añade. La base sigue vacía,
de modo que la línea base se rehace en lugar de parchearse, como permite
RNF-43 mientras el sistema no esté instalado (sección 5).

Tres observaciones la motivan. El formato de registro de uso pide la **fecha
de adquisición** —la de la orden de compra— y el sistema solo guardaba el
año: el documento salía con "2024" donde debía decir "18/11/2024", y guardar
menos de lo que el documento necesita obliga a rellenarlo a mano con un dato
que el sistema ya debería conocer. El inventario decía a nombre de quién
estaba cada equipo —del Responsable, todos— pero no **quién lo tiene**, que es
la pregunta que se hace cuando hay que ir a buscar uno; y en un laboratorio
los equipos los custodian los operadores, cada uno los suyos. Y los campos de
teléfono y de hora salían con otra altura, otro borde y otro ancho que los de
al lado, porque la hoja de estilos enumeraba los tipos de campo uno a uno y a
esos dos no los nombraba.

| Id | Cambio |
| --- | --- |
| V310-01 | **El bien guarda la fecha de adquisición, no el año.** `equipo.anio_adquisicion INTEGER` pasa a ser `equipo.fecha_adquisicion DATE`, y con ella el formato de registro de uso imprime la fecha de la orden de compra tal como el papel la pide (RF-78). El formulario de alta cambia el campo numérico por un calendario acotado entre el 1 de enero de 1980 y hoy; el listado ordena por ella y la muestra en `dd/MM/yyyy`. La regla no cambia de fondo: la adquisición sigue sin poder ser futura (RN-25), solo que ahora se comprueba al día y no al año. |
| V310-02 | **Cada equipo tiene un responsable de equipo, y es un Operador.** Nueva columna `equipo.responsable_equipo_id`. Responde a "quién lo tiene", que no es lo mismo que "quién responde por el inventario": el segundo es el Responsable de la Coordinación y ya estaba (RF-36); el primero es la persona que lo custodia día a día (RF-83). **`NULL` no es un hueco**: significa que lo lleva el Responsable, sea quien sea en cada momento. Así nace todo bien —también el que registra un Operador, que registrarlo no es quedárselo— y ahí vuelve cuando se le retira a alguien (RN-37). |
| V310-03 | **Solo el Responsable reparte los equipos de su Coordinación.** `PUT /api/equipos/{id}/responsable-equipo` es suyo y de nadie más: es quien reparte el trabajo y quien responde por el inventario entero. Un Operador no se asigna equipos a sí mismo ni se los pasa a un compañero. Se entrega únicamente a un **Operador activo de la misma Coordinación** —a un suspendido o a un dado de baja no se le entrega nada— y el propio Responsable puede quedárselo con un clic, que es lo que la pantalla llama *dejarlo a mi cargo* (RF-83, RN-37). |
| V310-04 | **Quien tiene equipos a su cargo no deja su puesto.** Un Operador con bienes a su nombre no se puede dar de baja ni retirar de su Coordinación —y por tanto tampoco mover a otra, que es retirarlo y volverlo a asignar—: antes, el Responsable tiene que entregarlos a otro Operador o quedárselos. El mensaje dice cuántos son y cuál es la salida. La **suspensión sí se permite**: conserva el puesto y la persona vuelve a él, así que sus equipos siguen siendo suyos (RN-38, RF-22b). |
| V310-05 | **La regla la sostiene también la base.** `equipo.responsable_equipo_id` no apunta al usuario sino a su **asignación**: la llave foránea es `(responsable_equipo_id, coordinacion_id) → usuario_coordinacion (usuario_id, coordinacion_id)`. Con eso la base garantiza por sí misma las dos mitades de la regla —no se puede poner a cargo a alguien de otra Coordinación, y no se puede borrar la asignación de quien tiene bienes a su nombre— igual que ya hacía con el laboratorio del bien (RN-12) y con el responsable único de cada Coordinación (RN-06). Se declara aplazable porque cambiar el rol de una persona reescribe su fila dentro de la misma transacción (sección 5). |
| V310-06 | **Dos sueltas automáticas, y solo dos.** La **baja de un bien** lo suelta: un equipo fuera de servicio no está a cargo de nadie, y si contara ataría a su operador a un equipo que ya no existe. Y el **ascenso de un Operador a Responsable** de su propia Coordinación (RF-26b) suelta los suyos: los sigue llevando la misma persona, pero pasa a llevarlos como Responsable, que es justamente lo que significa un bien sin asignación. Las dos quedan registradas en el historial del bien. |
| V310-07 | **El cambio de responsable deja rastro.** Nuevo tipo de movimiento `RESPONSABLE` en el historial inmutable del bien, con la frase de quién a quién. La línea de tiempo de la ficha sí lo muestra —al contrario que las ediciones, retiradas en la v3.9—: quién custodia un equipo es parte de su historia, y en un inventario patrimonial es de las cosas que hay que poder reconstruir (RF-53, RN-21). |
| V310-08 | **Todo campo de escritura se ve igual, sea del tipo que sea.** La regla de estilo enumeraba los tipos uno a uno —`text`, `password`, `email`, `number`, `date`, `search`— y bastaba usar uno que no estuviera en la lista para que el campo saliera con el aspecto por defecto del navegador: otra altura, otro borde, otra tipografía y sin ocupar el ancho de su columna. Le pasaba a **Teléfono de contacto**, **Celular** y **Hora** del formato de registro de uso, que son `tel` y `time`. Enumerar no escala: cada campo nuevo de un tipo distinto nacía descolocado. La regla se invierte —todos menos los que no son cajas de texto— y el problema no puede repetirse. Fecha y hora reciben además la normalización que WebKit necesita para no quedar más bajos que sus vecinos (RNF-22). |

**Lo que no cambia.** El bien sigue perteneciendo a una sola Coordinación y no
sale de ella (RN-11); `equipo.responsable_id` sigue siendo lo que era, el
Responsable vigente **al dar de alta** el bien (RF-36), y en la ficha se rotula
así para no confundirlo con el nuevo. El préstamo no se toca: prestar un
equipo no cambia quién lo tiene a su cargo, porque la salida es temporal y la
custodia no. El formato de registro de uso sigue sin guardarse (RN-36) y la
CSP sigue sin `'unsafe-inline'` ni `'unsafe-eval'`.

---

## 0.2 Resumen de cambios de la versión 3.9 respecto a la 3.8

La versión 3.9 no toca la jerarquía organizacional, ni el modelo del bien, ni
el préstamo, ni el historial inmutable, ni la matriz de permisos, ni una sola
regla de negocio de las que ya estaban. El esquema de la base **no cambia**:
ni una columna, ni una tabla, ni una migración nueva. Cambia **cómo se da de
alta a un operador**, **por dónde abren los dos listados**, **qué se ve y qué
no se ve en la ficha de un equipo** y añade **un documento que el laboratorio
imprime y firma**.

Cuatro observaciones del uso real la motivan. El Responsable que registraba a
un operador acertaba casi siempre, y cuando no acertaba se quedaba sin salida:
el alta eran dos peticiones encadenadas desde el navegador y, si la segunda
fallaba, la persona quedaba registrada **sin puesto**, invisible en su
pantalla y con el DNI ya ocupado, de modo que no podía ni corregirla ni
repetirla. El inventario y los préstamos abrían por una vista parcial
—"Operativos", "Activos"— sin decirlo, así que quien no encontraba un equipo
concluía que no estaba registrado cuando lo único que pasaba es que estaba
prestado. La ficha del bien contaba dos veces la misma salida, en la tabla de
préstamos y en la línea de tiempo, y ninguna de las dos veces enseñaba lo que
de verdad se consulta: qué se dijo al entregarlo y qué se dijo al recibirlo. Y
el laboratorio seguía rellenando a mano, en un documento aparte, un formato de
uso cuyos datos de equipo ya estaban en el sistema.

| Id | Cambio |
| --- | --- |
| V39-01 | **El alta de un operador es un solo acto también para el servidor.** `POST /api/usuarios/con-puesto` registra a la persona y le da su puesto **en una transacción**: o queda el operador entero, con su contraseña, o no queda nada. Hasta la v3.8 el cliente encadenaba `POST /api/usuarios` y `POST /api/usuarios/{id}/asignaciones`, y las dos podían fallar por separado. Cuando fallaba la segunda —la coordinación desactivada, un corte de red, la sesión caducada entre una y otra— la persona quedaba registrada sin rol y sin coordinación, y el Responsable **no podía verla** (su lista está acotada a su Coordinación, y quien no tiene ninguna no aparece en ella), **no podía editarla** (solo gestiona a los operadores de la suya, y aquello no era operador de nadie) y **no podía volver a registrarla**, porque su DNI, su correo y su usuario ya estaban tomados. El alta se convertía en un callejón sin salida que solo el Administrador podía deshacer (RF-16e, RNF-17b). |
| V39-02 | **El error del alta se señala en el campo que hay que corregir.** El formulario del Responsable entrega los datos y es la pantalla de fuera quien los envía, así que la respuesta del servidor llegaba a otro sitio: un DNI repetido se anunciaba en un aviso flotante genérico —"No se pudo registrar al operador"— y ninguno de los siete campos quedaba marcado. Ahora los errores por campo vuelven al formulario, que sigue abierto con lo escrito (RNF-25). La ventana de confirmación dice además, antes de aceptar, qué va a pasar además de guardar los datos: que la persona queda como operador de esta coordinación y que el sistema va a generar su contraseña (RNF-26). |
| V39-03 | **"Todos" es la primera pestaña y la que viene activa, en inventario y en préstamos.** La pantalla empieza enseñando todo lo que hay y el usuario acota desde ahí; el resto de pestañas conserva su orden. Abrir por "Operativos" respondía a "qué tengo disponible", que es una buena pregunta pero no es la que trae aquí: quien busca un equipo concreto y no lo encuentra no concluye que está prestado, concluye que no está registrado. Lo mismo en préstamos con "Activos", que escondía justamente el histórico que se viene a consultar (RF-47, RF-67). |
| V39-04 | **La ficha del bien deja de contar dos veces la misma salida.** La línea de tiempo del equipo ya no repite los préstamos ni las devoluciones —tienen su propia tabla, en esa misma ficha y con más datos— ni muestra las **ediciones**: quién corrigió un modelo mal escrito no es parte de la historia del equipo, que es dónde estuvo, en qué condición y con quién. El movimiento se sigue registrando en el historial inmutable, que no se toca (RN-21, RF-56): lo que cambia es qué enseña la pantalla. |
| V39-05 | **Cada préstamo de la ficha se abre.** Un botón **Ver detalle** por fila muestra lo que no cabía en ella y es justamente lo que se consulta cuando algo no cuadra: **quién entregó el equipo y a quién**, las **observaciones de entrega**, y del otro lado **quién recibió la devolución**, en qué estado volvió y las **observaciones de retorno**. Esas dos observaciones son la única constancia de cómo salió y cómo volvió el bien (RN-18) y hasta la v3.8 no se veían en ninguna pantalla de la ficha (RF-66b). |
| V39-06 | **El Responsable puede emitir el formato de registro de uso de un equipo, en PDF.** Es el papel que el laboratorio hace firmar cuando un equipo sale a trabajar, con sus diez puntos y en su orden. El sistema rellena lo que ya sabe —descripción, código patrimonial, código de inventario, marca, modelo, número de serie, valor de compra, año de adquisición, área y **la condición en que el bien está ahora mismo**— y deja en blanco lo que solo saben las personas. El documento **se ve entero antes de descargarse**: un papel que se va a firmar no se descarga a ciegas (RF-78, RF-79). |
| V39-07 | **El formato genera un documento y nada más.** No se guarda en ninguna tabla, no deja movimiento en el historial del bien, no crea un préstamo y **no cambia la condición del equipo**: después de emitirlo el bien sigue Operativo, si lo estaba. Es deliberado y es una regla nueva del sistema (RN-36). Por eso la pantalla lo recuerda dos veces —al enseñar el documento y al descargarlo— y el propio PDF lo lleva impreso al pie: el papel firmado y el registro del préstamo son dos cosas distintas, y hay que hacer las dos (RF-59, RF-79). |
| V39-08 | **La CSP gana una directiva y no pierde ninguna.** `frame-src 'self' blob:`, que es lo que permite mostrar en un marco el PDF que el servidor acaba de devolver. Ni `script-src` ni `style-src` admiten nada nuevo, sigue sin haber `'unsafe-inline'` ni `'unsafe-eval'` en ninguna directiva, y `object-src` sigue en `'none'`. La prueba de regresión comprueba ahora también esta directiva (RNF-07, csp.spec.ts). |

**Lo que no cambia.** El alta en dos actos del Administrador sigue siendo dos
actos, porque en su pantalla el puesto es justamente lo que está por decidir
(RF-16b, RF-28d): el acto único es del Responsable, que solo puede crear
operadores y solo en su Coordinación. El préstamo se registra como siempre y
con la misma pantalla (RF-59). El historial del bien conserva todos sus
movimientos, incluidos los que la ficha ya no muestra. Y el esquema de la base
queda exactamente como estaba: la v3.9 no ha exigido ninguna migración.

---

## 0.3 Resumen de cambios de la versión 3.8 respecto a la 3.7

La versión 3.8 no toca la jerarquía organizacional, ni el modelo del bien, ni
los préstamos, ni el historial, ni los estados de cuenta, ni una sola regla de
negocio. Ningún rol gana ni pierde una capacidad y el esquema de la base no
cambia. Cambia **cómo se lee la interfaz**, **cuánto texto la acompaña**,
**cuántas pantallas cuesta registrar un equipo** y, por fin, **cómo se
instala**.

Cuatro observaciones la motivan. La aplicación entera estaba escrita sin
tildes —"Contrasena", "Gestion", "Coordinacion"— salvo unas pocas etiquetas
sueltas que sí las llevaban, que es peor que no llevarlas ninguna: para una
institución peruana esa es la primera cosa que resta seriedad, antes que
cualquier decisión de color o de espaciado. Cada pantalla se explicaba a sí
misma con recuadros de prosa que repetían lo que ya se veía, hasta el punto de
que un formulario de cuatro campos llevaba tres párrafos alrededor. Registrar
un equipo pedía seis pantallas para once datos, la mitad de ellas con dos
campos. Y el sistema, pese a que RNF-44 lo exigía desde la 2.0, no tenía
Dockerfile ni forma de desplegarse.

| Id | Cambio |
| --- | --- |
| V38-01 | **La interfaz se escribe en español correcto.** Tildes y eñes en todo el texto que lee el usuario: las plantillas, las cadenas de los componentes y los datos que el sistema trae puestos —las dos Direcciones y las ocho categorías de `V2`—. No se tocó ningún identificador: `equipo.condicion` sigue llamándose así, y lo que cambió es la cabecera que lo rotula, `CONDICIÓN`. Quedan fuera a propósito las palabras que cambian de significado con la tilde (*esta/está*, *si/sí*, *que/qué*), porque acertar en el 95% y estropear el 5% es peor que no tocar (RNF-21, RNF-29). |
| V38-02 | **Desaparecen los recuadros de aviso.** Los 36 bloques de prosa explicativa que encabezaban listados y formularios se retiran, y con ellos la costumbre de contar en un párrafo lo que la pantalla ya enseña. Sobreviven dos: los que muestran el error que devuelve el servidor, sin los cuales un rechazo de credenciales no se vería en ninguna parte. La validación junto al campo, los estados vacíos explicativos y las ventanas de confirmación quedan intactos (RNF-24, RNF-25, RNF-26). |
| V38-03 | **Registrar un equipo son tres pantallas, no seis.** *Identificación y códigos* juntos —son la misma pregunta, cuál es este equipo, y se responden con el aparato delante y su etiqueta a la vista—; *adquisición, ubicación y fotografía* juntos —tres grupos cortos que no llenan una pantalla—; y el *resumen*, solo (RNF-28, ERS 8.4). |
| V38-04 | **Las barras de los dos paneles vuelven a decir algo.** Llevaban vacías desde que existen: el relleno es un `<span>` dentro de un contenedor que no es flex, de modo que quedaba en flujo *inline*, y un elemento inline ignora la anchura. El navegador recibía `width: 33%` y lo pintaba a 0 px. Una línea de CSS (RF-76). |
| V38-05 | **La acción principal deja de moverse.** Una descripción larga la empujaba al renglón siguiente y aparecía abajo a la izquierda, que es donde ya nadie la busca; ocurría en Personas y entre una Dirección y la otra. Las descripciones se limitan además a la medida de lectura (RNF-27). |
| V38-06 | **La tabla del inventario se lee.** Los códigos dejan de partirse a mitad —"INV-2024-" en una línea y "0102" en la siguiente no es un código—, la fila baja de 150 a 92 px y la columna de acciones queda fijada al borde derecho, de modo que sus botones no se van fuera de la pantalla (RF-47, RNF-22). |
| V38-07 | **El sistema se despliega.** Dos imágenes en dos etapas, `docker compose`, nginx sirviendo la aplicación y reenviando `/api` al backend, y toda la configuración por variables de entorno. Nginx emite además las cabeceras de endurecimiento, porque Spring solo las pone en lo que él sirve —la API— y quien entrega el HTML es nginx (RNF-07, RNF-09, RNF-44, RNF-45). |

**Lo que no cambia.** Ni una regla de negocio, ni un endpoint, ni una columna.
Las 40 comprobaciones de las reglas documentadas se ejecutaron contra la imagen
de producción y todas se cumplen. La CSP sigue sin `'unsafe-inline'` ni
`'unsafe-eval'` en ninguna directiva, y su prueba de regresión sigue en verde.

---

## 0.4 Resumen de cambios de la versión 3.7 respecto a la 3.6

La versión 3.7 no toca la jerarquía organizacional, ni el modelo del bien, ni
los préstamos, ni el historial, ni la CSP. Cambia **qué le puede pasar a la
cuenta de una persona** y **cómo se cambia de puesto dentro de la
institución**.

Dos observaciones del uso real la motivan. La cuenta tenía un solo interruptor
—activa o no—, y con él se resolvían dos cosas que no son la misma: quien se va
de vacaciones y quien se va de la institución acababan en el mismo sitio, de
modo que mirando la lista no había forma de saber a cuál de los dos se esperaba
de vuelta, ni si su puesto seguía siendo suyo. Y cambiar a alguien de puesto
—ascender a un operador, traer a alguien de otra coordinación, relevar a un
responsable— exigía dos actos separados con la coordinación **parada** entre
uno y otro: la baja del que estaba y, cuando se decidiera, el nombramiento del
que entraba. Entre los dos, sus operadores no podían registrar ni un equipo
(RN-07).

| Id | Cambio |
| --- | --- |
| V37-01 | **La cuenta tiene tres estados, y dos de ellos son los que faltaban.** **Suspender** es temporal —vacaciones, un permiso, una licencia—: la persona no entra por ahora y **conserva su puesto**, porque va a volver a él. **Dar de baja** es la salida de la institución: la persona deja de entrar y **deja también su puesto**, porque quien ya no trabaja aquí no responde por un inventario ni figura entre los operadores de una Coordinación. Ninguno de los dos la borra: su nombre sigue en el historial de los equipos y préstamos que pasaron por sus manos (RF-22b, RN-34, RN-09). Se puede **reincorporar** a quien volvió, y vuelve como quien acaba de registrarse: con su cuenta y sin cargo. |
| V37-02 | **El Administrador ejerce las dos sobre cualquiera** —responsables, operadores y otros administradores—, con dos límites que el servidor impone: no sobre sí mismo, y **nunca sobre el último Administrador activo**, ni suspendiéndolo ni dándolo de baja ni nombrándolo responsable de una Coordinación. Sin esa regla el sistema admite quedarse sin nadie que pueda repartir puestos (RF-25). El **Responsable** las ejerce sobre los **Operadores de su Coordinación**, y solo sobre ellos (RF-29). |
| V37-03 | **Cambiar de puesto se hace en la Coordinación, con "Cambiar responsable", y es un solo acto.** Allí se elige entre quienes pueden tomarlo: una persona **sin puesto**, un **Operador de esa misma Coordinación** —que asciende sin cambiar de sitio— o un **Administrador** que baja al terreno. Nunca quien ya es Responsable de otra: nadie responde por dos inventarios a la vez (RF-26b, RN-35). El **saliente queda libre en la misma transacción**: sin rol y sin Coordinación, con su cuenta activa y su historial intacto. La Coordinación no pasa ni un instante sin responsable, que es lo que la partición en dos actos no podía evitar (RN-07). |
| V37-04 | **La misma ventana nombra al primer responsable.** Una Coordinación recién creada ya no manda a otra pantalla a buscar a la persona: se elige aquí, entre los mismos candidatos, con la Coordinación delante. La ruta con encargo de `/personas` sigue funcionando para quien la tenga guardada, pero ninguna pantalla la genera (RF-26b, RF-28b). |
| V37-05 | **El esquema declara el estado en lugar del interruptor.** `usuario.activo` desaparece y en su lugar está `usuario.estado` —`ACTIVA`, `SUSPENDIDA`, `BAJA`— con su restricción. La base no tenía datos, así que la línea base se rehace en lugar de parchearse, como permite RNF-43 mientras el sistema no esté instalado. |

**Lo que no cambia.** El alta sigue partida en registro previo y asignación
(RF-16b, RF-28d), y la asignación sigue dándose solo a quien no tiene puesto
(RN-33): "Cambiar responsable" no es una excepción a esa regla sino la
operación que sí sabe qué hacer con el puesto que la persona deja. El
aislamiento por Coordinación, el historial del bien, la fotografía en el alta y
la CSP estricta quedan intactos.

---

## 0.5 Resumen de cambios de la versión 3.6 respecto a la 3.5

La versión 3.6 no toca la jerarquía organizacional, ni el modelo de datos del
bien, ni los préstamos, ni el historial, ni la CSP. Ningún rol gana ni pierde
una capacidad. Cambia **a quién se le puede dar un puesto** y **cuándo se entera
la pantalla de que los datos que muestra ya no son los que hay**.

Dos observaciones del uso real la motivan. La v3.4 dejó dicho que el puesto de
Responsable ocupado no se sustituye al asignar (RN-06), pero lo comprobaba
mirando la Coordinación y no a la persona: quien ya era **administrador** no
ocupaba ninguna, así que se colaba por esa rendija y podía convertirse en
responsable u operador de un golpe —y quien lo hacía podía ser él mismo, con lo
que se dejaba fuera de su propia pantalla—. Y las pantallas pedían sus datos una
sola vez, al abrirse: un equipo prestado por el compañero de al lado, una
coordinación que acababa de quedarse sin responsable o un puesto recién asignado
seguían viéndose como estaban hasta que alguien pulsaba F5, cosa que solo hace
quien ya sospecha que hay algo que ver.

| Id | Cambio |
| --- | --- |
| V36-01 | **El puesto se asigna solo a quien no tiene ninguno, y nunca a uno mismo.** Vale para los tres roles: quien ya es administrador, responsable u operador no cambia de puesto por el hecho de que se le asigne otro, porque eso deja su Coordinación parada o su inventario sin quien lo registre sin que nadie lo haya decidido. Moverla sigue siendo lo que era desde la v3.4: la baja del puesto que tiene y, después, la asignación del nuevo (RN-33, RF-28d, RN-08). Y quien reparte los puestos no es quien los recibe: el Administrador no se asigna un puesto a sí mismo, como tampoco se desactiva su propia cuenta (RF-25). |
| V36-02 | **Las pantallas se mantienen al día solas.** Cada listado, ficha y panel vuelve a preguntar al servidor cada 30 segundos mientras la pestaña está a la vista, y de inmediato al volver a ella; el armazón hace lo propio con el puesto de quien lo usa, de modo que un cambio de rol se ve en el menú sin cerrar sesión (RF-02, RNF-10). La recarga es silenciosa —sin indicador de carga ni avisos de error— y no ocurre mientras hay una ventana de confirmación abierta ni con la pestaña oculta (RNF-48). |
| V36-03 | **La pantalla de personas deja de encabezarse con el panel "Coordinaciones sin responsable".** La coordinación parada ya se anuncia donde se administra —su cuadro en rojo en Direcciones, con su botón **Asignar responsable**, que sigue trayendo hasta aquí el encargo planteado (RF-15b, RF-28b)—. Tenerlo en dos sitios era decir dos veces lo mismo, y las dos versiones se contradicen en cuanto una envejece. La lista de personas queda con lo suyo: buscar a alguien y darle su puesto (RF-28). |
| V36-04 | **El nombre de usuario y el correo institucional se escriben enteros, a mano.** El formulario los componía a partir del nombre real —`jperez`, `juan.perez@inictel-uni.edu.pe`— y acertaba a menudo, que es justo lo que lo hacía peligroso: las veces que no acertaba, el dato equivocado ya estaba escrito, con aspecto de correcto, y nadie relee lo que el propio formulario acaba de rellenar. Un campo vacío se ve; uno mal relleno, no. Además ninguno de los dos los decide el sistema: son los que la institución dio a esa persona (RF-16). |
| V36-05 | **La lista de coordinaciones se pide al abrir la ventana de asignación, y un fallo deja de ser definitivo.** El cliente memorizaba la lista para no repetir la consulta en cada pantalla, pero memorizaba también el error: bastaba una petición rechazada —la que sale mientras el primer ingreso tiene pendiente el cambio de contraseña, cuando el servidor responde 403 a todo lo que no sea cambiarla (RF-06)— para que la lista quedara vacía en toda la sesión, sin decirlo. Entonces no se podía nombrar a ningún responsable ni operador, porque no había coordinación que elegir, y lo único que se dejaba asignar era el puesto de Administrador, que es el que no necesita ninguna. Ahora el fallo borra la memoria y la ventana vuelve a preguntar cada vez que se abre, así que una coordinación recién creada aparece sin recargar el navegador (RF-28d, RNF-48). |
| V36-06 | **La persona sin puesto llega del servidor sin el campo `rol`, y el cliente lo completa al entrar.** La API serializa sin los campos nulos, así que quien está registrado sin puesto no trae `rol: null`: no trae `rol`. Para TypeScript son lo mismo, para una comparación no —`undefined !== null` es cierto—, y bastó eso para que la ventana de asignación creyera que toda persona recién registrada ya tenía puesto y no llegara a ofrecer ni el rol ni la Coordinación. Se normaliza en el adaptador, que es quien conoce el formato de la respuesta, y no en cada pantalla que la mira (RF-16b). |
| V36-07 | **"Mi equipo humano" muestra el equipo, y solo el equipo.** Desaparecen la tarjeta con los datos del propio Responsable —quien abre esa pantalla es esa misma persona, y sus datos están en *Mi cuenta*— y el encabezado "Operadores" sobre la tabla de operadores, que repetía dos renglones más abajo lo que el título de la pantalla ya decía. Se conserva el recuento, que informa de algo (RF-29, RNF-22). |
| V36-08 | **El Administrador vuelve a ver el inventario de la Coordinación que elige.** RF-46 y RF-49 nunca cambiaron —consulta todas las Coordinaciones, en solo lectura, eligiendo una cada vez—, pero la pantalla no llegaba a pedir el listado: la comprobación "¿ya eligió Coordinación?" era una señal calculada sobre un campo del formulario, que no es una señal, así que se quedaba con su primer valor —"todavía no"— y no volvía a calcularse nunca. El Administrador elegía la Coordinación, el selector cambiaba y la tabla seguía vacía, tuviera los equipos que tuviera. Le pasaba lo mismo a la pantalla de préstamos (RF-68). |
| V36-09 | **La pantalla de acceso lleva de fondo una fotografía de INICTEL-UNI** (`assets/inictel.jpg`), con un velo suave del degradado de marca —un tercio de opacidad, lo justo para unificar el tono sin tapar la imagen—. El contraste del pie no lo sostiene el velo sino su propia sombra de texto, que funciona sobre cualquier zona de la fotografía, clara u oscura, y no obliga a oscurecerla entera (RNF-32). La imagen se declara en `styles.css` y se sirve desde el propio origen, así que la CSP no se relaja en ninguna directiva (RNF-07). |

**Lo que no cambia.** Los dos actos del alta —registro previo y asignación— y,
dentro de la asignación, la elección de **rol y Coordinación** tal como estaba
(RF-28d): tres opciones de rol con su explicación al lado y, si el rol es
operativo, el selector de Coordinación. La contraseña sigue naciendo con el
puesto, la baja del Responsable sigue en la ficha de su Coordinación, y el
aislamiento por Coordinación, el historial del bien y el esquema de la base
quedan intactos. La v3.6 no ha exigido ninguna migración, ningún endpoint nuevo
ni ninguno retirado.

---

## 0.6 Resumen de cambios de la versión 3.5 respecto a la 3.4

La versión 3.5 no toca el modelo de permisos, ni el aislamiento por
Coordinación, ni el historial del bien, ni la CSP. Cambia **dónde se trabaja
sobre una Coordinación**, **qué identifica a una persona** y **qué ocurre
cuando una Coordinación se queda sin Responsable**. Y, aprovechando que la base
está vacía, deja el esquema declarado de una vez en lugar de construido por
parches.

Cuatro observaciones del uso real la motivan. El cuadro de cada Coordinación se
había ido llenando de controles —seis, en un espacio pensado para leerse de un
vistazo— mientras que para ver sus laboratorios había que abrir otra ventana.
La sigla de la Coordinación se quedaba vacía o se rellenaba con una abreviatura
inventada en el momento, distinta en cada tarjeta. El segundo apellido, siendo
opcional, se omitía por prisa, y quedaba media identidad registrada en el
sistema que existe justamente para no confundir a dos personas. Y la baja del
Responsable dejaba a la Coordinación parada solo a medias: sus Operadores no
podían registrar equipos ni salidas, pero sí devoluciones.

| Id | Cambio |
| --- | --- |
| V35-01 | **El cuadro de cada Coordinación se abre.** Un clic sobre él lleva a su **ficha**, donde están sus laboratorios —con su alta, su edición y su activación—, sus cifras y todas sus acciones: editar datos, ver inventario, activar o desactivar, y el puesto de Responsable. El cuadro conserva lo que sirve para comparar de un vistazo: responsable, tres cifras y estado (RF-15b). Desaparece la ventana suelta de laboratorios, que era un segundo camino al mismo sitio. |
| V35-02 | **La Coordinación deja de tener sigla, también en la base de datos.** No se pide al crearla, no se muestra y no se guarda: la columna `coordinacion.sigla` no existe. Las Direcciones sí conservan la suya —DIDT y DCTT son como se las nombra en la institución— (RF-11, RF-10). |
| V35-03 | **En la lista de personas, desactivar y activar salen de la fila.** Viven en la ficha, con el resto de lo que se hace sobre la cuenta de alguien. La fila queda con lo que la lista necesita: **Ver detalle** y **Asignar** (RF-28f). |
| V35-04 | **El segundo apellido es obligatorio en toda alta y edición**, para los tres roles y también en el primer ingreso del Administrador. La identidad de una persona en la institución son sus dos apellidos, y quien la registra tiene su DNI delante (RF-16, RF-06b, RN-32). |
| V35-05 | **Una Coordinación sin Responsable está parada del todo.** Sus Operadores no registran equipos, ni salidas, **ni devoluciones**: todas se rechazan explicando por qué. Su panel lo dice al entrar y sus dos botones grandes aparecen apagados, en lugar de responder con un error (RN-07, RF-35c, RF-59b, RF-77b). |
| V35-06 | **Una ventana a la vez.** Ninguna confirmación se abre encima de otra ventana: el formulario que la pidió se aparta mientras se decide y vuelve al cerrarla. Vale para el alta de personas y para la ficha de la Coordinación (RNF-26). |
| V35-07 | **Al entrar, el sistema saluda y dice dónde se ha entrado**: nombre de quien accede, su rol, su Coordinación y la Dirección a la que pertenece, con un único botón **Cerrar**. Una vez por ingreso, no por recarga (RF-01b). |
| V35-08 | **El esquema se declara una sola vez.** Las seis migraciones —la inicial y las cinco que la corregían— se funden en `V1` y `V2`, que describen la base tal como es hoy. Ninguna instalación tenía datos que migrar, y leer seis archivos para saber cómo es una tabla no ayudaba a nadie (sección 5). |

**Lo que no cambia.** El alta sigue partida en registro previo y asignación; la
baja del Responsable sigue siendo un acto explícito y suyo, y sigue haciendo
falta antes de nombrar a un sucesor; cada persona sigue perteneciendo a una
sola Coordinación. Las dos Direcciones siguen precargadas. La matriz de
permisos no gana ni pierde una capacidad.

---

## 0.7 Resumen de cambios de la versión 3.4 respecto a la 3.3

La versión 3.4 no toca el inventario, ni los préstamos, ni el historial del
bien, ni la CSP. Cambia **cuántas coordinaciones lleva una persona**, **cómo
se sustituye a un Responsable** y **cuánto se le dice al usuario antes de que
algo ocurra**.

Tres observaciones del uso real la motivan. La 3.3 admitió que un Responsable
llevara varias Coordinaciones pensando en el coordinador a cargo de dos
laboratorios; en la práctica, quien responde por un inventario responde por
ese inventario, y repartir el cargo reparte también a quién hay que preguntar
por un equipo. El relevo automático —asignar el puesto de una Coordinación
ocupada sustituía al vigente en el mismo acto— dejaba a alguien fuera de su
cargo como efecto colateral de nombrar a otro, sin que nadie hubiera decidido
darlo de baja. Y la lista de personas mostraba de golpe el DNI, el correo y la
coordinación de cada uno: siete columnas para encontrar a alguien, cuando para
encontrarlo basta su nombre.

| Id | Cambio |
| --- | --- |
| V34-01 | **Cada persona pertenece a una sola Coordinación.** Vale igual para el RESPONSABLE y para el OPERADOR (RN-05). Mover a alguien deja de ser sumarle un destino: se le da de baja del puesto que tiene y se le asigna después el nuevo. Con ello desaparecen el selector de ámbito del encabezado, la cabecera `X-Coordinacion` y la noción de *coordinación en curso*: el ámbito de una operación ya no es algo que el cliente elija, porque no hay entre qué elegir (RNF-10). |
| V34-02 | **Asignar el puesto de Responsable de una Coordinación que ya lo tiene se rechaza**, nombrando a quien lo ocupa y diciendo dónde se le da de baja. En el selector, esas Coordinaciones se ven pero no se eligen (RF-19b, RN-06). |
| V34-03 | **La baja del Responsable es un acto propio, y vive en la tarjeta de su Coordinación.** El botón **Dar baja al responsable** de la pantalla de Direcciones es lo que ocurre cuando alguien renuncia al cargo: el saliente queda como persona registrada, sin coordinación y con su cuenta activa; la tarjeta pasa a rojo y pide un sucesor (RF-26). Solo el Administrador puede hacerlo. Es también la única manera de sustituir a un Responsable: primero la baja, después el nombramiento. |
| V34-04 | **Una Coordinación sin Responsable no registra ni presta equipos, y ahora el servidor lo impide.** Hasta la 3.3 la tarjeta lo anunciaba pero nada lo comprobaba: un bien podía quedar registrado sin responsable a su nombre. La baja del vigente ya no exige que la Coordinación esté vacía —si lo exigiera, una Coordinación con equipos no podría cambiar nunca de Responsable—; lo que hace es dejarla parada hasta que se nombre al sucesor (RN-07). |
| V34-05 | **La lista de personas se lee para encontrar a alguien; su ficha, para saber quién es.** Cada fila ofrece **Ver detalle**, y allí viven el DNI, el correo, la coordinación, las fechas y el botón **Editar**, junto a las acciones sobre sus credenciales. La tabla queda con lo que sirve para buscar: persona, rol, usuario y estado de la cuenta (RF-28f). |
| V34-06 | **Ninguna acción importante ocurre sin una confirmación flotante que diga qué va a pasar.** El alta y la edición de una persona terminan en una ventana con los datos tal como van a quedar, y con **Cancelar** —que cancela el registro— y **Aceptar**. Se suman las confirmaciones que faltaban: asignar un puesto, dar de baja un puesto, restablecer una contraseña, desbloquear una cuenta y activar o desactivar una Coordinación o un Laboratorio (RNF-26). |

**Lo que no cambia.** El alta sigue partida en dos: registro previo primero
(RF-16b) y asignación después, que es cuando nace la contraseña (RF-28d). Las
dos Direcciones siguen precargadas y sin alta ni desactivación. El aislamiento
por Coordinación, el historial del bien, la fotografía en el alta y la CSP
estricta quedan intactos. La matriz de permisos gana una sola fila —la baja del
Responsable, exclusiva del Administrador— y no pierde ninguna.

---

## 0.8 Resumen de cambios de la versión 3.3 respecto a la 3.2

La versión 3.3 no toca el inventario, ni los préstamos, ni el historial del
bien, ni el aislamiento por Coordinación. Cambia **cómo entra una persona al
sistema** y **cuántas Coordinaciones puede llevar**.

Tres observaciones del uso real la motivan. Cuando llega alguien nuevo no
siempre se sabe todavía dónde va a trabajar, y el formulario de alta obligaba
a decidirlo el primer día: rol y Coordinación, o no había alta. La contraseña
nacía con ese alta, para una cuenta que aún no podía hacer nada con ella. Y un
coordinador de la casa puede estar a cargo de más de un laboratorio, cosa que
el modelo de una sola adscripción no admitía: había que elegir cuál de sus dos
puestos se registraba.

| Id | Cambio |
| --- | --- |
| V33-01 | **El alta de una persona es un registro previo.** Pide quién es —nombres, apellidos, DNI, cargo, correo y nombre de usuario— y nada más: sin rol, sin Coordinación y sin contraseña utilizable. La persona existe en el sistema, aparece en la lista marcada como **Sin asignar** y todavía no puede entrar (RF-16b). El alta se cierra ahí: confirma lo ocurrido y dice que hay que asignarle una Coordinación y su rol, sin abrir esa ventana ni ningún otro paso (RF-16c). |
| V33-02 | **La asignación de puesto es un acto aparte, y es donde nace la contraseña.** Desde la lista de personas, el botón **Asignar** da rol y —si es operativo— Coordinación. La contraseña temporal, aleatoria, se genera en ese momento y se muestra una sola vez (RF-28d). Quien ya tenía credenciales de otro puesto conserva las suyas. |
| V33-03 | **Un RESPONSABLE puede estar a cargo de varias Coordinaciones.** Se le suman de una en una y elige en cuál trabaja desde el encabezado. Un OPERADOR sigue perteneciendo a una sola: registra el día a día de un inventario concreto y repartirlo sería repartir su atención (RN-05). La adscripción deja de ser una columna del usuario y pasa a ser la tabla de asignaciones que siempre fue: una relación, no un atributo. |
| V33-04 | **El asistente de cambio de responsable desaparece, con su ruta y su endpoint.** Se retiran `/responsables/:id/relevo`, `POST /api/usuarios/coordinacion/{id}/responsable` y `GET /api/usuarios/por-dni/{dni}`. El relevo no se pierde: **es** lo que ocurre al asignar el puesto de Responsable de una Coordinación que ya lo tiene, en la misma transacción y con el aviso previo de a quién sustituye (RF-26, RN-08). Un asistente de tres pasos para lo que ahora es un botón obligaba a elegir la Coordinación antes de poder buscar a la persona, que es justo al revés de como se busca a la gente. |
| V33-05 | **Crear una Coordinación ya no arrastra a otra pantalla.** La coordinación recién creada se queda a la vista como una tarjeta más de su Dirección, marcada en rojo y con su botón **Asignar responsable**, que lleva a `/personas` con la tarea planteada. Hasta la 3.2 el alta saltaba sola al asistente de relevo: encadenaba dos tareas que no siempre van juntas y sacaba al Administrador de la pantalla que acababa de cambiar, sin haberla visto cambiar (RF-11c). |

**Revertido por la v3.4.** V33-03 y la segunda mitad de V33-04 ya no describen
el sistema: cada persona vuelve a pertenecer a una sola Coordinación (V34-01) y
asignar el puesto de un Responsable vigente dejó de relevarlo —se rechaza, y la
sustitución pasa por la baja explícita del que está (V34-02, V34-03)—. Lo que sí
sigue vigente de V33-04 es la desaparición del asistente y de sus tres
endpoints: no han vuelto.

**Lo que no cambió entonces.** La matriz de permisos no ganó ni perdió una capacidad: el
Administrador sigue repartiendo todos los puestos y el Responsable sigue
pudiendo dar de alta a los Operadores de su Coordinación —y allí el alta y la
asignación se resuelven de un tirón, porque el puesto no está en duda. Las dos
Direcciones siguen precargadas y sin alta ni desactivación. El aislamiento por
Coordinación, el historial del bien y la CSP estricta quedan intactos.

---

## 0.9 Resumen de cambios de la versión 3.2 respecto a la 3.1

La versión 3.2 no toca el modelo de datos del bien, ni el aislamiento por
Coordinación, ni el modelo de permisos: ningún rol gana ni pierde una sola
capacidad. Lo que cambia es **dónde encuentra cada cosa el Administrador** y
qué deja de tener que escribir a mano.

Tres observaciones del uso real la motivan. Las dos Direcciones de INICTEL-UNI
están en su reglamento y no cambian: pedir que alguien las transcriba el primer
día es pedir una oportunidad de equivocarse en un dato que ya se sabe. El
catálogo de categorías se echa en falta registrando un equipo, no navegando un
menú institucional. Y las personas se buscan por su nombre, no por el rol que
tienen: separar Responsables de Operadores obligaba a acertar la pantalla antes
de poder buscar, y dejaba a los Administradores sin ninguna.

| Id | Cambio |
| --- | --- |
| V32-01 | **Las dos Direcciones de la institución se precargan con el sistema.** La migración de datos base —entonces `V4`, hoy `V2` (V35-08)— registra la Dirección de Investigación y Desarrollo Tecnológico (DIDT) y la Dirección de Capacitación y Transferencia Tecnológica (DCTT) con su descripción. El Administrador entra a un sistema que ya sabe cómo se organiza la casa (RF-10, RN-30). |
| V32-02 | **No existe el alta de Direcciones, ni en la pantalla ni en la API.** Desaparece el botón "Nueva dirección" y con él `POST /api/organizacion/direcciones`. Se conserva la corrección de sus datos —nombre, sigla y descripción— porque un nombre mal escrito hay que poder arreglarlo (RF-10). |
| V32-03 | **Las Direcciones no se desactivan.** Se retiran el control "Mostrar desactivadas" de la pantalla, el botón "Desactivar" de cada Dirección y `PATCH /api/organizacion/direcciones/{id}/estado`. Como la pantalla ya no filtra por estado, el árbol se pide entero: las Coordinaciones desactivadas —que sí pueden desactivarse— siguen a la vista con su distintivo y su botón de activar, en lugar de desaparecer sin retorno. |
| V32-04 | **La opción del menú "Estructura" pasa a llamarse "Direcciones"** y su ruta pasa de `/estructura` a `/direcciones`. La ruta anterior redirige a la nueva. **El catálogo de categorías sale del menú** y se administra desde el Inventario, con el botón "Gestionar categorías" (RF-31). |
| V32-05 | **Las personas del sistema se gestionan en una sola pantalla, `/personas`, con filtro por rol.** Sustituye a las secciones separadas de Responsables y Operadores, que redirigen a ella. La lista muestra el rol de cada persona como un distintivo con color y texto, incluye a los Administradores —que antes no aparecían en ninguna lista— y el alta permite elegir cualquiera de los tres roles, como RF-16 siempre previó (RF-28). |

**Lo que no cambió entonces.** El relevo de Responsable conservaba su asistente
y su ruta `/responsables/:id/relevo`, que la v3.3 retiró (V33-04). El panel
"Coordinaciones sin responsable" encabezaba la pantalla de personas (RF-28b);
la v3.6 lo retiró, porque la Coordinación parada ya se anuncia en su cuadro de
Direcciones (V36-03). El Responsable sigue viendo a su gente en "Mi equipo
humano". Las reglas de negocio, la matriz de permisos y el aislamiento por
Coordinación quedaron intactos.

---

## 0.10 Resumen de cambios de la versión 3.1 respecto a la 3.0


La versión 3.1 no toca la jerarquía organizacional, el aislamiento por
Coordinación ni el modelo de permisos. Cambia una sola cosa, y en un solo
punto: **cómo llega la fotografía del bien al sistema**.

Hasta la 3.0 la fotografía solo podía adjuntarse después, desde la ficha del
bien ya registrado, y siempre eligiendo un archivo del dispositivo. Quien
inventaría no trabaja así: recorre el laboratorio con una tablet en la mano y
tiene el equipo delante justo mientras lo registra. Obligarlo a fotografiar
con la aplicación de cámara, guardar la imagen, terminar el alta, buscar el
bien en el listado, abrir su ficha y recién ahí subir el archivo son seis
pasos para lo que el dispositivo resuelve en uno.

| Id | Cambio |
| --- | --- |
| V31-01 | **El registro de un bien termina con un paso de fotografía, opcional.** Es el último paso antes del resumen y se puede saltar sin escribir nada: un bien sin foto se registra igual que antes (RF-51b). El paso solo existe en el alta; en la edición la fotografía se sigue cambiando desde la ficha, que es donde ya vivía, para no tener dos sitios que hagan lo mismo. |
| V31-02 | **Quien registra puede tomar la foto del equipo con la cámara del propio dispositivo**, sin salir del formulario: la aplicación abre un visor en vivo, congela la imagen y la adjunta. Es la vía pensada para la tablet. La alternativa de siempre —elegir una imagen ya guardada— se conserva íntegra y con el mismo peso visual. |
| V31-03 | **La cámara se habilita solo para el propio origen.** La cabecera `Permissions-Policy` pasa de `camera=()` a `camera=(self)`. Ninguna otra capacidad se abre y la Content-Security-Policy no se relaja en ninguna directiva: el vídeo del visor se enlaza por código sobre el elemento, no por URL. |
| V31-04 | **Una fotografía que falla nunca tumba un alta.** El bien se registra primero y la imagen se adjunta sobre él; si el envío de la imagen falla, el equipo queda registrado y el sistema avisa que la fotografía quedó pendiente y dónde completarla. |

---

## 0.11 Resumen de cambios de la versión 3.0 respecto a la 2.0

La versión 3.0 conserva íntegra la jerarquía organizacional y el aislamiento
por Coordinación que introdujo la 2.0. Lo que cambia es qué se registra de las
personas, qué le falta a una Coordinación para poder trabajar y cómo se
presenta todo ello al Administrador.

| Id | Cambio |
| --- | --- |
| V3-01 | **Desaparece la bitácora de acciones por Coordinación.** El sistema deja de registrar quién hizo cada cosa. Se eliminan el módulo 8 completo (RF-69 a RF-74), la ruta `/historial`, la API `/api/historial`, la tabla `auditoria` y el contexto `auditoria` del backend y del frontend. La trazabilidad **del bien** (módulo 6) se conserva intacta: es historial patrimonial, no vigilancia de personas. |
| V3-02 | **Primer ingreso del Administrador ampliado.** Además de la contraseña, confirma su identidad real: nombres, apellidos y DNI. La cuenta inicial nace con datos de relleno y el DNI es el identificador de una persona en todo el sistema. |
| V3-03 | **La contraseña se representa siempre con un candado.** El botón de restablecer contraseña de las listas de personas, y el cambio de contraseña propia, llevan el icono de candado en lugar del icono de reinicio. |
| V3-04 | **Toda Coordinación nace con su primer Laboratorio**, en la misma transacción, y nunca se queda sin ninguno activo. |
| V3-05 | **Sin laboratorios no se registran bienes.** Un bien sin un lugar donde estar no se puede encontrar después. |
| V3-06 | **Toda Coordinación recién creada lleva directo a asignar su Responsable**, en la sección de Responsables, eligiendo a alguien que ya existe (buscado por DNI) o dándolo de alta en el mismo acto. |
| V3-07 | **Las Coordinaciones se presentan en cuadros y no en franjas horizontales.** Cada Coordinación se lee entera de un vistazo y varias se comparan de golpe. |
| V3-08 | **La adscripción a una sola Coordinación se hace explícita en la interfaz.** El alta de personas dice "Asignar a una coordinación" y advierte que es una y solo una; al dar de alta un Responsable no se ofrecen las Coordinaciones que ya lo tienen. |

---

## 0.12 Resumen de cambios de la versión 2.0 respecto a la 1.0

La versión 2.0 reemplazó el modelo plano de una sola área por una jerarquía
organizacional real y un modelo de permisos multi-inquilino (multi-tenant)
donde la COORDINACIÓN es la unidad de aislamiento.

| Id | Cambio                                                                                                                                |
| --- |---------------------------------------------------------------------------------------------------------------------------------------|
| C-01 | Se incorpora la jerarquía Institución > Dirección > Coordinación > Laboratorio. El campo libre "area" desaparece.                     |
| C-02 | Los roles son: ADMIN, RESPONSABLE y OPERADOR.                                                                  |
| C-03 | El inventario deja de ser único y global: cada Coordinación posee su propio inventario, aislado de las demás.                         |
| C-04 | El Administrador pierde toda capacidad de escritura sobre los bienes. Su rol es estructural y de supervisión, no operativo.           |
| C-05 | Se eliminan los atributos dinámicos (modelo EAV). El bien pasa a tener un conjunto de campos fijos, ampliado con fecha de adquisición y costo. |
| C-06 | El campo "estado" del bien se renombra a "condicion" y su valor inicial pasa de DISPONIBLE a OPERATIVO.                               |
| C-07 | Se incorpora el historial inmutable de movimientos por bien: toda transición de condición queda registrada con su fecha y hora.       |
| C-08 | Se incorpora el caso de uso "Cambio de Responsable" con relevo obligatorio: una Coordinación nunca puede quedar sin Responsable.      |
| C-09 | Se incorpora la bitácora de auditoría por Coordinación (suprimida en la v3.0).                                                        |
| C-10 | La capacidad objetivo sube de 50 a 300 usuarios concurrentes.                                                                         |

---

## 1. Introducción

### 1.1 Propósito

Este documento especifica los requisitos funcionales y no funcionales del
Sistema de Gestión de Inventarios de INICTEL-UNI. Sirve como base
contractual y técnica para el diseño, desarrollo, pruebas y despliegue del
sistema, y es la referencia normativa a la que debe ajustarse toda
implementación.

### 1.2 Alcance

El sistema permite:

a) Modelar la estructura organizacional de la institución en tres niveles
   anidados: Direcciones, Coordinaciones y Laboratorios.
b) Gestionar usuarios con tres roles diferenciados, asignados a una
   Coordinación cuando corresponde.
c) Registrar, consultar, editar y dar de baja los bienes de cada Coordinación
   (equipos de cómputo, drones, cámaras espectrales, instrumentación,
   mobiliario, pizarras digitales interactivas, impresoras, entre otros),
   clasificados por categoría.
d) Controlar el préstamo y la devolución de equipos con trazabilidad completa.
e) Conservar el historial inmutable de cada bien: su alta, sus préstamos, sus
   devoluciones, sus mantenimientos y su baja.

El inventario de cada Coordinación es independiente. Ningún usuario operativo
ve ni modifica los bienes de una Coordinación distinta a la suya.

### 1.3 Definiciones y acrónimos

| Término | Significado |
| --- | --- |
| ERS | Especificación de Requisitos de Software |
| DDD | Domain-Driven Design |
| PDI | Pizarra Digital Interactiva |
| JWT | JSON Web Token |
| DNI | Documento Nacional de Identidad (8 dígitos, Perú) |
| CSP | Content-Security-Policy |
| RF | Requisito Funcional |
| RNF | Requisito No Funcional |
| RN | Regla de Negocio invariante |
| Registro previo | Alta de una persona sin puesto: existe en el sistema con sus datos, pero sin rol, sin Coordinación y sin contraseña, de modo que todavía no puede entrar (RF-16b) |
| Asignación | Acto que le da a una persona registrada un rol y, si es operativo, la Coordinación —una sola— donde lo ejerce; es cuando nace su contraseña (RF-28d). Solo la recibe quien no tiene ningún puesto, y nadie se la da a sí mismo (RN-33) |
| Baja de un puesto | Acto que retira a una persona de su Coordinación y la devuelve al estado de registrada sin puesto, con su cuenta activa (RF-28d). No confundir con la **baja de la persona**, que es su salida de la institución (RF-22b) |
| Suspensión | Ausencia temporal —vacaciones, permiso, licencia—: la persona no entra al sistema y **conserva su puesto**, porque va a volver a él (RF-22b) |
| Baja de una persona | Salida de la institución: la persona deja de entrar y **deja su puesto**, que queda libre. Su historial se conserva y puede reincorporarse, pero vuelve sin cargo (RF-22b, RN-34) |
| Libre | Persona con su cuenta activa y **sin puesto**: ni rol ni Coordinación. Es como queda quien acaba de registrarse, quien deja un cargo y quien es relevado (RF-16b, RF-26b) |
| Ficha de la persona | Ventana con todos sus datos —DNI, correo, coordinación, fechas— y con las acciones que se ejercen sobre ellos, empezando por **Editar**. Se abre desde su fila en la lista (RF-28f) |
| Condición | Situación actual de un bien: Operativo, Prestado, En mantenimiento o Dado de baja |
| Movimiento | Registro inmutable de una transición de condición de un bien |
| Relevo | Sustitución de un Responsable por otro. Desde la v3.7 es **una sola operación** —Cambiar responsable, en la ficha de la Coordinación—: el entrante toma el puesto y el saliente queda libre en la misma transacción, sin que la Coordinación se pare entre medias (RF-26b, RN-35) |
| Primer ingreso | Acto en que un usuario estrena su cuenta: define su contraseña definitiva y, si es el Administrador de la cuenta inicial, confirma también su identidad real |

### 1.4 Tecnologías definidas

| Área | Tecnología |
| --- | --- |
| Backend | Java 21, Spring Boot 3.5.x (Web, Data JPA, Security, Validation) |
| Frontend | Angular 21 (TypeScript strict, Signals), CSS propio sin librerías de terceros |
| Base de datos | PostgreSQL 16 o superior |
| Migraciones | Flyway (esquema versionado) |
| Autenticación | JWT firmado con HMAC-SHA512 |
| Documentación | OpenAPI / Swagger |
| Contenedores | Docker y Docker Compose |
| Versionado | Git / GitHub |

---

## 2. Descripción general

### 2.1 Perspectiva del producto

Aplicación web cliente-servidor. El frontend Angular consume una API REST
expuesta por el backend Spring Boot, que persiste en PostgreSQL. El backend se
organiza como monolito modular con contextos delimitados (DDD): cada contexto
tiene dominio puro, aplicación, infraestructura y presentación, y se comunica
con los demás únicamente a través de puertos declarados en su propio dominio
(capa anticorrupción).

**Contextos delimitados del sistema:**

| Contexto | Contenido |
| --- | --- |
| `organizacion` | Dirección, Coordinación, Laboratorio |
| `iam` | Personas, roles, asignaciones a coordinaciones, autenticación |
| `inventario` | Categorías, bienes y su historial de movimientos |
| `prestamos` | Salidas, devoluciones y conformidad |
| `reportes` | Panel de control y exportación |
| `compartido` | Paginación, errores, eventos, contexto de seguridad |

### 2.2 Jerarquía organizacional

```
INSTITUCION (fija, no editable)
  Nombre: "INICTEL-UNI"
  Es un valor constante del sistema. No existe pantalla de alta,
  edicion ni baja de instituciones.
    |
    +-- DIRECCION            (2)      Fijas: DIDT y DCTT, precargadas.
          |                      No se crean ni se desactivan (RN-30)
          |
          +-- COORDINACION   (1..N)   Solo el Administrador la crea
                |                     Unidad de aislamiento del sistema
                |                     Posee: 1 Responsable, N Operadores
                |                            y su propio inventario. Ni el
                |                            Responsable ni los Operadores
                |                            pertenecen a otra: una persona,
                |                            una coordinacion (RN-05)
                |
                +-- LABORATORIO (1..N) Solo el Administrador lo crea
                      Ubicacion fisica de un bien, opcional en el bien
                      pero obligatoria en la Coordinacion: nace con el
                      primero y nunca se queda sin ninguno activo
```

Un bien **pertenece** a una Coordinación (obligatorio) y se **ubica** en un
Laboratorio de esa misma Coordinación (opcional). Ningún bien puede ubicarse en
un Laboratorio que pertenezca a otra Coordinación.

### 2.3 Segmentos objetivo (usuarios del sistema)

#### a) ADMINISTRADOR — "el que organiza"

**Perfil:** personal de dirección o de patrimonio institucional. No es un
usuario operativo del inventario.

**Puede:**

- Crear y editar Coordinaciones y Laboratorios, y corregir los datos de las
  dos Direcciones de la institución, que vienen dadas (RN-30).
- Registrar personas y asignarles después su puesto —cualquiera de los tres
  roles— desde la pantalla de Personas.
- Nombrar al Responsable de cada Coordinación, y **darlo de baja** cuando
  renuncia al cargo. Son dos actos distintos, y en ese orden se sustituye a un
  Responsable: primero la baja, después el nombramiento (RF-26).
- Activar y desactivar usuarios.
- Consultar el inventario de TODAS las Coordinaciones, en modo solo lectura.
- Administrar el catálogo de categorías.
- Exportar reportes y ver el panel institucional.

**No puede:**

- Registrar, editar, dar de baja, prestar ni recibir bienes.
- Eliminar físicamente ningún usuario ni ningún bien.

#### b) RESPONSABLE — "el dueño del inventario"

**Perfil:** coordinador o jefe de laboratorio a cargo de los bienes.
Exactamente uno por Coordinación, ni más ni menos, y de una sola Coordinación:
quien responde por un inventario responde por ese inventario (RN-05). Deja el
cargo cuando el Administrador le da de baja, y entonces vuelve a ser una
persona registrada sin puesto (RF-26).

**Puede, únicamente dentro de SU Coordinación:**

- Registrar, editar y dar de baja bienes.
- Cambiar la condición de un bien a "En mantenimiento" o "Dado de baja", y
  reincorporarlo.
- Registrar préstamos y devoluciones.
- Ver y gestionar a los Operadores de su Coordinación.
- Consultar el historial completo de cada bien.
- Exportar el inventario de su Coordinación.

**No puede:**

- Ver ni tocar nada de otra Coordinación.
- Crear Direcciones, Coordinaciones ni Laboratorios.
- Crear otros Responsables ni Administradores.

#### c) OPERADOR — "el que registra el día a día"

**Perfil:** técnico, asistente o practicante. Es el usuario con menos
experiencia informática del sistema; la interfaz debe diseñarse pensando
primero en él.

**Puede, únicamente dentro de SU Coordinación:**

- Registrar (agregar) bienes nuevos.
- Consultar el inventario de su Coordinación.
- Registrar préstamos de bienes operativos.
- Registrar devoluciones con checklist de conformidad.

**No puede:**

- Editar ni dar de baja bienes.
- Cambiar la condición de un bien a mantenimiento o baja.
- Ver ni gestionar usuarios.

### 2.4 Restricciones

- El nombre de la institución es la constante "INICTEL-UNI".
- El DNI es de 8 dígitos numéricos (formato peruano) y es el identificador
  único de una persona en el sistema.
- Los bienes con código patrimonial se rigen por la normativa de bienes
  estatales: no se eliminan físicamente, se dan de baja (eliminación lógica).
- Los usuarios nunca se eliminan: se desactivan, para preservar la coherencia
  de los bienes y préstamos que llevan su nombre.
- Las fechas y horas de los hechos del sistema las genera el servidor y son
  inmutables: ningún rol, por ningún medio, puede alterarlas.
- Idioma de la interfaz: español.

---

## 3. Requisitos funcionales

### Módulo 1: Autenticación y seguridad

| Id | Requisito |
| --- | --- |
| RF-01 | El sistema permitirá el inicio de sesión mediante nombre de usuario o correo institucional, y contraseña. |
| RF-01b | **Todo ingreso satisfactorio abre un saludo de bienvenida**: nombra a quien entra, dice con qué rol lo hace y **en qué Coordinación y Dirección trabaja**, con un único botón **Cerrar**. Es la primera vez que el sistema puede decírselo —antes del acceso no sabe quién es— y responde a la pregunta con la que se entra a una aplicación de inventario: dónde estoy parado (ERS 8.1). El Administrador ve su ámbito institucional; quien todavía no tiene puesto ve el saludo con su rol y nada más, porque no hay Coordinación que nombrar —hasta la v3.7 llevaba además un aviso diciendo que su cuenta aún no podía trabajar, retirado con los demás recuadros (V38-02, RF-16b)—. Se muestra una vez por ingreso, no en cada recarga: repetirlo lo convertiría en un estorbo. |
| RF-02 | El sistema autenticará mediante JWT firmado; el token expirará tras un periodo configurable (por defecto 60 minutos) y transportará el identificador del usuario, su rol y la Coordinación que tiene asignada. El servidor relee ambos de la base en cada petición, de modo que una asignación o una baja surten efecto en la siguiente sin esperar a que el token expire. |
| RF-03 | El sistema cerrará la sesión automáticamente al expirar el token y permitirá el cierre de sesión manual. |
| RF-04 | El sistema restringirá el acceso según el rol y la Coordinación del usuario autenticado, tanto en el frontend (ocultamiento de opciones) como en el backend (validación obligatoria por endpoint). La validación del backend es la única autoritativa. |
| RF-05 | Las contraseñas se almacenarán cifradas con BCrypt; nunca en texto plano. |
| RF-06 | El Administrador podrá restablecer la contraseña de cualquier usuario, generando una contraseña temporal que obligará al cambio en el siguiente ingreso. |
| RF-06b | En su primer ingreso el Administrador completará, además de su contraseña definitiva, sus datos personales: nombres, primer apellido, segundo apellido y DNI, los cuatro obligatorios. Es la única identidad que él mismo declara, y se declara entera. El **cargo no se pregunta**: quien estrena la plataforma es su Administrador, de modo que el sistema lo fija en "Administrador del sistema" y se lo muestra ya resuelto. La cuenta administradora inicial nace con datos de relleno y el DNI identifica a la persona en todo el sistema, de modo que la operación es obligatoria y se resuelve en una sola transacción con el cambio de contraseña. Los demás roles llegan con sus datos ya cargados por quien los dio de alta y solo cambian la contraseña. Desde la v3.5 el segundo apellido es igual de obligatorio en el alta que el Administrador hace de otras personas (RF-16, RN-32). |
| RF-06c | Toda acción de contraseña se representará con el icono de un candado de seguridad: el restablecimiento desde las listas de personas y el cambio de la contraseña propia. El icono de reinicio queda reservado para las acciones que rehacen un proceso. |
| RF-07 | Un usuario con la cuenta desactivada no podrá autenticarse. El sistema responderá con el mensaje exacto: "Su cuenta se encuentra inactiva. Comuniquese con el administrador del sistema." No se revelará ninguna otra información. |
| RF-08 | El sistema bloqueará temporalmente la cuenta tras 5 intentos fallidos consecutivos, informando los minutos restantes. |

### Módulo 2: Estructura organizacional (solo Administrador)

| Id | Requisito |
| --- | --- |
| RF-09 | El sistema mostrará la institución "INICTEL-UNI" como raíz fija de la jerarquía, sin opciones de creación ni edición. |
| RF-10 | Las Direcciones de la institución **se precargan con el sistema y no se crean ni se desactivan desde la aplicación**: son las dos del reglamento de organización y funciones de INICTEL-UNI, no un dato que varíe con el uso (RN-30). La migración `V2` registra la **Dirección de Investigación y Desarrollo Tecnológico (DIDT)** y la **Dirección de Capacitación y Transferencia Tecnológica (DCTT)**, cada una con su descripción. No existe pantalla ni endpoint de alta ni de desactivación de Direcciones. El Administrador sí podrá **corregir** el nombre (único), la sigla y la descripción de cada una. |
| RF-11 | El Administrador podrá crear, editar y desactivar Coordinaciones dentro de una Dirección. Cada Coordinación tendrá nombre (único dentro de su Dirección) y descripción opcional. **No tiene sigla**: en la institución las coordinaciones se nombran por su nombre, y el campo se quedaba vacío o se rellenaba con una abreviatura inventada en el momento, distinta en cada tarjeta. Las Direcciones sí la conservan, porque DIDT y DCTT son como se las nombra (RF-10). |
| RF-11b | El alta de una Coordinación exigirá el nombre de su **primer Laboratorio**, con ubicación física opcional. Coordinación y Laboratorio se crearán en la misma transacción: una Coordinación sin laboratorios no puede registrar bienes (RN-26), de modo que crearla "para completarla después" dejaría una unidad inservible. |
| RF-11c | Creada la Coordinación, **se queda a la vista** como una tarjeta más de su Dirección, marcada en rojo y con su botón **Asignar responsable**, que es lo único que le falta para poder operar (RN-07). El sistema no salta solo a otra pantalla: encadenar las dos tareas sacaba al Administrador de la vista que acababa de cambiar sin dejarle verla cambiar, y daba por supuesto que ya tenía decidido a quién poner, cosa que no siempre ocurre. |
| RF-12 | El Administrador podrá crear, editar y desactivar Laboratorios dentro de una Coordinación. Cada Laboratorio tendrá nombre (único dentro de su Coordinación) y ubicación opcional. |
| RF-13 | El sistema impedirá desactivar una Coordinación que tenga bienes activos o préstamos vigentes, e informará el motivo. |
| RF-14 | El sistema impedirá desactivar un Laboratorio que tenga bienes ubicados en él; solicitará reubicarlos primero. |
| RF-14b | El sistema impedirá desactivar el **único Laboratorio activo** de una Coordinación, y pedirá crear otro antes (RN-26). |
| RF-15 | La vista de Direcciones mostrará, por cada Coordinación, su resumen completo: Responsable vigente, número de Operadores, número de Laboratorios y número de bienes por condición. La v3.0 lo presenta en tarjetas dispuestas en rejilla (ERS 8.2). |
| RF-15b | **Cada tarjeta se abre y lleva a la ficha de su Coordinación.** La rejilla sirve para comparar unidades de un vistazo, así que la tarjeta lleva solo lo que se compara —responsable, las tres cifras y su estado— y anuncia que se abre. Dentro está todo lo demás: la **lista de sus laboratorios**, con **Nuevo laboratorio**, edición y activación de cada uno; sus cifras con el desglose por condición; y sus acciones —**Editar datos**, **Ver inventario**, **Activar o desactivar** y el puesto de Responsable (asignarlo o darlo de baja, RF-26)—. Hasta la v3.4 esas acciones se apretaban en el pie del cuadro, seis controles en un espacio pensado para leerse de un vistazo, y los laboratorios vivían en una ventana aparte. La tarjeta es un control alcanzable con el teclado, no un bloque con un clic encima (RNF-32). |

### Módulo 3: Gestión de usuarios y roles

| Id | Requisito |
| --- | --- |
| RF-16 | El Administrador podrá registrar personas con los campos: nombres, primer apellido, **segundo apellido**, DNI (8 dígitos, único), cargo, correo institucional (único) y nombre de usuario (único). Todos son obligatorios salvo el cargo. El **nombre de usuario y el correo se escriben completos, a mano**: el sistema no los compone a partir del nombre —lo hizo hasta la v3.5, y las veces que no acertaba dejaba escrito un dato verosímil que ya nadie releía—. Los **dos apellidos** lo son desde la v3.5: siendo opcional, el segundo se omitía por prisa y quedaba media identidad registrada en el sistema que existe justamente para no confundir a dos personas ni duplicar personal (RN-32). |
| RF-16b | **El alta es un registro previo.** No pide rol, ni Coordinación, ni contraseña: responde a *quién es* la persona, y *qué hace y dónde* se decide después, al asignarla (RF-28d). Quien queda registrado existe en el sistema, aparece en la lista de personas marcado como **Sin asignar** y todavía no puede entrar: su cuenta no tiene ninguna capacidad y su contraseña aún no existe. Es deliberado: cuando llega alguien no siempre se sabe todavía dónde va a trabajar, y una contraseña emitida el primer día es una contraseña que se pierde antes de servir para nada. |
| RF-16c | **El alta termina en el alta.** Al guardar, el sistema confirmará que la persona quedó registrada y enunciará en una frase lo que le falta —asignarle una Coordinación y su rol para que pueda entrar—, y **no abrirá ninguna otra ventana ni encadenará ningún paso**. Encadenar la asignación daba por hecho que quien registra ya tiene decidido el puesto, que es precisamente lo que RF-16b dejó de dar por hecho; y convertía en un trámite obligatorio lo que debe poder aplazarse. La acción sigue disponible, con el mismo nombre y en el mismo sitio, en la fila de esa persona (RF-28d). |
| RF-16d | **Ni el alta ni la edición guardan sin confirmarlo.** Al enviar el formulario, el sistema abrirá una ventana flotante con los datos tal como van a quedar —nombre completo, DNI, cargo, correo y nombre de usuario— y, en el alta, con la frase que dice qué le faltará a la persona. La ventana ofrece dos salidas: **Aceptar**, que guarda, y **Cancelar**, que cancela el registro y cierra el formulario. Si el servidor rechaza algún dato, la ventana se cierra y el formulario vuelve con el error junto a su campo (RNF-25, RNF-26). |
| RF-16e | **El alta del Responsable a un Operador suyo es un solo acto, y una sola transacción.** `POST /api/usuarios/con-puesto` registra a la persona y le da su puesto a la vez: o queda registrada como operador de esa Coordinación, con su contraseña recién generada, o no queda nada. Es el único caso en que el puesto se sabe de antemano —el Responsable solo puede crear operadores, y solo en la Coordinación que administra (RF-29)—, de modo que preguntarlo sería preguntar lo que ya se sabe. Hasta la v3.8 el cliente encadenaba las dos peticiones y un fallo en la segunda dejaba hecha la primera: la persona quedaba **registrada sin puesto**, fuera del alcance de quien la había dado de alta —su lista está acotada a su Coordinación (RN-23) y quien no tiene ninguna no aparece en ella— y con su DNI, su correo y su usuario ya ocupados, de modo que el alta no se podía ni deshacer ni repetir. El Administrador conserva los dos actos separados (RF-16b, RF-28d), porque en su pantalla el puesto es lo que está por decidir. |
| RF-17 | El registro previo de una persona no exige que exista ninguna Coordinación: solo la exige su asignación (RF-28d). Si no hay ninguna registrada, la pantalla de asignación lo explicará y guiará al Administrador a crearla. |
| RF-18 | Todo usuario con rol operativo —RESPONSABLE u OPERADOR— está asignado a exactamente una Coordinación. El rol ADMIN no se asigna a ninguna (RN-05). Cambiar a alguien de Coordinación son dos actos: la baja del puesto que tiene y la asignación del nuevo. |
| RF-18b | La asignación advertirá, con todas las letras, que la Coordinación elegida será la única en la que esa persona trabaje: el único inventario que vea y pueda modificar. Si la persona ya tiene puesto, la ventana no ofrece asignarle otro: muestra el que tiene, con su botón de baja, y explica por qué (RN-05). |
| RF-19 | Una Coordinación tendrá como máximo un Responsable activo. La **asignación de puesto** (RF-28d) se rechaza si el puesto ya está ocupado, nombrando a quien lo ocupa y remitiendo a **Cambiar responsable**, que es la operación que sí sabe qué hacer con el que sale (RF-26b, RN-06). |
| RF-19c | La asignación **se rechaza también cuando es la persona la que ya está ocupada**: solo recibe puesto quien está registrado sin ninguno (RN-33). Es la misma regla mirada desde el otro lado —RF-19 mira la Coordinación, RF-19c mira a la persona— y por el mismo motivo: nadie deja su puesto como efecto colateral de que le den otro. El mensaje nombra el puesto que tiene y dice por dónde se cambia: **Cambiar responsable** en la ficha de la Coordinación, que traslada el puesto y libera al saliente en un solo acto (RF-26b), o la baja del puesto si lo que procede es dejarlo libre (RF-28d). |
| RF-19b | El selector de Coordinación de una asignación de RESPONSABLE mostrará las que ya tienen responsable junto al nombre de quien lo es, y no permitirá elegirlas: para esas, el camino es **Cambiar responsable** desde su ficha (RF-26b). No se ocultan, porque saber quién está a cargo es justamente lo que hace falta para decidirlo (RNF-26). |
| RF-20 | Una Coordinación podrá tener cualquier número de Operadores. |
| RF-21 | El Administrador podrá editar los datos de cualquier usuario. |
| RF-22 | El Administrador podrá **suspender**, **dar de baja**, **reactivar** y **reincorporar** cuentas. Son las únicas formas de retirar a una persona: el sistema no ofrece eliminación física de usuarios en ninguna pantalla ni endpoint (RN-09). |
| RF-22b | **La cuenta tiene tres estados, y la diferencia entre dos de ellos es de fondo.** **Activa**: la persona trabaja y entra. **Suspendida**: no entra *por ahora* —vacaciones, un permiso, una licencia— y **conserva su puesto**, porque va a volver a él; su Coordinación sigue teniendo Responsable si lo era. **De baja**: ya no pertenece a la institución, y por eso **deja también su puesto**: quien se fue no responde por un inventario ni figura entre los operadores de una Coordinación (RN-34). Si era el Responsable, la suya queda parada hasta que se nombre a otro (RN-07). Ningún estado borra a la persona ni su historial. |
| RF-23 | Al **reactivar** una cuenta suspendida, la persona recupera su puesto tal como lo dejó y vuelve a operar con normalidad. Al **reincorporar** a quien estaba de baja, recupera su cuenta **pero no su puesto**: el que tenía se cubrió cuando se fue, y dárselo otra vez es una decisión aparte (RF-28d). |
| RF-24 | El sistema validará la unicidad del DNI, del correo y del nombre de usuario, y el formato del DNI (exactamente 8 dígitos numéricos). |
| RF-25 | **Siempre queda al menos un Administrador activo, y nadie decide sobre su propia cuenta.** El sistema impedirá que un Administrador se suspenda o se dé de baja a sí mismo, y que el último Administrador activo quede fuera por cualquiera de las tres vías que pueden quitarlo de en medio: la suspensión, la baja y el nombramiento como Responsable de una Coordinación, que también le quita el cargo (RF-26b). Sin esta regla el sistema admite quedarse sin nadie que reparta los puestos, y la única salida sería entrar en la base de datos a mano. |

**RF-26 — CAMBIO DE RESPONSABLE.** Poner a otra persona al frente del
inventario de una Coordinación es **un solo acto**, y ocurre **en la
Coordinación**: es su puesto el que cambia de manos, y lo que hay que mirar
antes de decidirlo son sus equipos y su gente, no la ficha de una persona.

Hasta la v3.6 eran dos actos separados —la baja del que estaba y, cuando se
decidiera, el nombramiento del que entraba— y entre uno y otro la Coordinación
quedaba **parada**: sus operadores no podían registrar ni un equipo ni una
salida (RN-07). Se partió así para que nadie perdiera el cargo como efecto
colateral de nombrar a otro; la v3.7 conserva ese cuidado sin el precio,
porque la decisión que se toma ya nombra a los dos: a quien entra y a quien
sale.

**a) Dónde.** La ficha de cada Coordinación ofrece **Cambiar responsable** si
lo tiene, y **Asignar responsable** si no, y solo al Administrador. Las dos
abren la misma ventana: la pregunta es la misma.

**b) Quién puede tomarlo (RN-35).** La ventana lista a quienes pueden, y solo
a ellos:

- una persona **sin puesto**, que estrena cargo —y entonces nace su contraseña,
  que se muestra una sola vez (RF-06)—;
- un **Operador de esa misma Coordinación**, que asciende sin cambiar de sitio;
- un **Administrador**, que baja al terreno y deja de serlo —siempre que no sea
  el último activo (RF-25)—.

No aparece, y el servidor lo rechaza si llega por otra vía, **quien ya es
Responsable** de cualquier Coordinación: nadie responde por dos inventarios, y
soltar el suyo de paso dejaría otra Coordinación parada sin que nadie lo
hubiera decidido. Tampoco quien no tiene la cuenta activa. Para mover a un
Operador de otra Coordinación hay que darle antes de baja su puesto (RF-28d):
la excepción es el de la propia, porque ahí no cambia de sitio, solo de
responsabilidad.

**c) Qué ocurre.** En una sola transacción:

1. el **saliente queda libre**: sin rol y sin Coordinación, con su cuenta activa
   y su historial intacto —dejar un cargo no es dejar la institución (RNF-47)—;
2. el **entrante** queda como Responsable de esa Coordinación, dejando el puesto
   que tuviera;
3. la Coordinación **no pasa ni un instante sin responsable**, de modo que sus
   operadores no dejan de trabajar (RN-07);
4. no se exige que la Coordinación esté vacía de equipos: exigirlo dejaría a las
   que tienen inventario sin manera de cambiar de Responsable, que es justo
   cuando hace falta.

**d) Antes de ejecutar**, una confirmación flotante nombra a quien entra, a
quien sale y lo que le pasa a cada uno (RNF-26), y al terminar el sistema lo
confirma con las mismas palabras (RNF-31).

**e) La otra salida del cargo es la baja de la persona** (RF-22b): quien deja
la institución deja su puesto, y ahí sí la Coordinación queda parada hasta que
se nombre a alguien, porque nadie ha entrado a ocuparlo.

| Id | Requisito |
| --- | --- |
| RF-27 | La lista de personas se busca por texto libre —nombre, DNI, usuario o correo—, de modo que antes de registrar a alguien puede comprobarse si ya está, y no se duplica personal. |
| RF-28 | El Administrador visualizará a **todas las personas del sistema en una sola pantalla**, con filtros por rol, por Coordinación, por estado de cuenta y por texto libre, y un único botón de alta. El filtro de rol permitirá ver a los Administradores, a los Responsables, a los Operadores o a todos a la vez; cada fila mostrará el rol con un distintivo de color **y** texto (RNF-30). Hasta la v3.1 eran dos listas separadas —Responsables y Operadores— que obligaban a acertar la pantalla antes de poder buscar a alguien y dejaban a los Administradores sin ninguna. |
| RF-28b | **La Coordinación sin Responsable se anuncia en Direcciones, no en Personas.** Su cuadro se marca en rojo y ofrece **Asignar responsable**, que lleva a la lista de personas con la tarea planteada (RF-15b). La pantalla de personas ya no encabeza su contenido con el panel *"Coordinaciones sin responsable"* que tuvo hasta la v3.5: la misma alerta en dos sitios se contradice en cuanto uno de los dos envejece, y el sitio donde se administra una Coordinación es su cuadro. Al llegar con el encargo, la pantalla queda en **modo encargo** y las filas de las personas **sin asignar** —las únicas que pueden recibir el puesto (RN-33)— ofrecen **Asignar aquí** en lugar del botón genérico. Hasta la v3.7 una barra recordaba además qué puesto se estaba cubriendo, con su botón de cancelar; se retiró con los demás recuadros (V38-02), de modo que el modo se reconoce por ese botón de las filas y se abandona saliendo de la pantalla. |
| RF-28c | La lista mostrará el rol de cada persona como un distintivo con color **y** texto, incluido el estado **Sin asignar** de quien está registrado sin puesto (RNF-30). Un filtro de puesto acota la lista a las personas sin asignar: es la cola de trabajo del Administrador. |
| RF-28e | La Coordinación de cada persona se muestra en su ficha (RF-28f). Un Administrador figura como "Toda la institución"; alguien sin puesto, como "Todavía ninguna". |
| RF-28f | **Cada fila ofrece "Ver detalle", que abre la ficha de esa persona.** La tabla conserva lo que sirve para encontrarla —nombre, cargo, rol, nombre de usuario y estado de la cuenta—; el **DNI**, el **correo institucional**, la **Coordinación**, el último acceso y la fecha de registro viven en la ficha, junto a las acciones que se ejercen sobre esos datos: **Editar**, **Restablecer contraseña**, **Desbloquear** (si la cuenta lo está), **Suspender o reactivar**, **Dar de baja o reincorporar** (RF-22b) y **Asignar puesto**. La edición se abre desde ahí y no desde la fila: se corrigen los datos de alguien mirándolos, no recorriendo una tabla (RNF-22). En la fila quedan solo **Ver detalle** y **Asignar**: activar y desactivar salieron de ella en la v3.5, porque eran un segundo camino al mismo sitio. |
| RF-29 | El Responsable visualizará en una única sección a **los Operadores** de su Coordinación, y podrá darlos de alta, editarlos, **suspenderlos y darlos de baja** —solo a ellos, y solo los de su Coordinación (RF-22b)—. La pantalla no muestra los datos del propio Responsable: quien la mira es esa misma persona, y su ficha está en *Mi cuenta*. En esa pantalla el registro y la asignación se resuelven **en un solo acto**, al contrario que en la de Personas: el puesto no está en duda —el Responsable solo puede crear Operadores, y solo en la Coordinación que administra—, así que preguntárselo sería preguntar lo que ya se sabe (RNF-22). |
| RF-30 | El Operador no tendrá acceso a ninguna pantalla ni endpoint de gestión de usuarios. |

**RF-28d — ASIGNACIÓN DE UN PUESTO.** Es el segundo de los dos actos en que se
reparte la incorporación de una persona, y el que la pone a trabajar. Desde la
lista de personas, el botón **Asignar** abre una ventana que:

a) muestra el puesto que la persona tiene ahora mismo, si tiene alguno, con su
   botón **Dar de baja**, antes de proponer nada. Mientras lo tenga, la ventana
   no ofrece asignarle otro: explica que cada persona pertenece a una sola
   Coordinación y que para moverla hay que darle de baja primero (RN-05);
b) pide el **rol** —Responsable, Operador o Administrador— presentado como tres
   opciones con su explicación al lado, no como un desplegable: es la decisión
   de la que depende todo lo demás;
c) pide la **Coordinación** cuando el rol es operativo, y no la pide para
   ADMINISTRADOR, que no pertenece a ninguna (RN-05). El selector se llena con
   **las Coordinaciones que existen en ese momento**: la lista se pide al abrir
   la ventana, no una sola vez al entrar a la pantalla, de modo que una
   Coordinación recién creada puede recibir a su gente sin recargar el
   navegador. Mientras la lista llega, el selector lo dice; que no haya
   ninguna solo se anuncia cuando de verdad no la hay (RNF-24). Las
   Coordinaciones que ya tienen Responsable aparecen con el nombre de quien lo
   es y **no pueden elegirse** (RF-19b);
d) **confirma antes de ejecutar**, en una ventana flotante que dice quién
   quedará como qué y de qué Coordinación, y si va a generarse una contraseña
   (RNF-26);
e) **genera la contraseña**, aleatoria y distinta cada vez, cuando la persona
   estrena puesto, y la muestra una sola vez para entregarla en mano (RF-06).
   Quien ya tenía credenciales de un puesto anterior conserva las suyas: darle
   otras sería cerrarle la sesión sin avisar;
f) rechaza, explicándolo, **asignar a alguien que ya tiene puesto** —el que
   sea, incluido el de Administrador, que no ocupa Coordinación (RN-33)—,
   nombrar Responsable de una Coordinación que ya lo tiene (RN-05, RN-06) y
   asignarse un puesto a uno mismo. En la lista, la fila de quien ya tiene
   puesto ofrece **Asignar** en gris y lleva a su puesto actual y a su baja;
   solo la de quien está **Sin asignar** ofrece la acción principal, y la del
   propio Administrador no la ofrece (RNF-23).

Dar de baja el puesto de una persona **no desactiva su cuenta**: vuelve a quedar
registrada y sin puesto, exactamente como recién dada de alta, y su historial en
bienes y préstamos sigue nombrándola (RNF-47). La baja también se confirma antes
de ejecutarse, y cuando quien la recibe es un Responsable, la confirmación dice
que su Coordinación quedará parada (RF-26).

Puede asignar el Administrador, sin límite. El Responsable solo puede dar
puestos de Operador en su Coordinación, y dar de baja a esos mismos Operadores:
nombrar Responsables y darles de baja es decisión del Administrador, y nombrar
Administradores, más todavía.

### Módulo 4: Catálogo de categorías (solo Administrador)

| Id | Requisito |
| --- | --- |
| RF-31 | El Administrador podrá crear, editar y desactivar categorías de bienes. El catálogo es institucional y compartido por todas las Coordinaciones. Se administra **desde el Inventario**, con el botón "Gestionar categorías": es allí donde se echa en falta una categoría, al registrar un equipo, y no en un menú institucional aparte. La pantalla ofrece el regreso al inventario, que es de donde se viene. |
| RF-32 | Las categorías existentes se conservan: Equipos de cómputo, Drones, Cámaras espectrales, Instrumentación, Mobiliario, PDI, Impresoras y Otros. |
| RF-33 | El sistema impedirá desactivar una categoría que tenga bienes activos asociados. |

### Módulo 5: Inventario de bienes

**RF-34** — El sistema permitirá registrar un bien con los siguientes campos:

| Campo | Obligatorio | Origen |
| --- | --- | --- |
| Nombre del equipo | Sí | Usuario |
| Marca | Sí | Usuario |
| Modelo | Sí | Usuario |
| Número de serie | Sí | Usuario (admite "S/N") |
| Código de inventario | Sí | Usuario (único institucional) |
| Código patrimonial | Sí | Usuario (único institucional) |
| Categoría | Sí | Usuario (selección) |
| Fecha de adquisición | Sí | Usuario (calendario, del 01/01/1980 a hoy) |
| Costo | Sí | Usuario (>= 0, soles) |
| Laboratorio | No | Usuario (selección) |
| Observaciones | No | Usuario |
| Fotografía | No | Usuario (cámara del dispositivo o archivo, RF-51b). En el alta solo la ofrece el formulario al Responsable (RF-51j) |
| Condición | Sí | SISTEMA: siempre OPERATIVO |
| Coordinación | Sí | SISTEMA: la del usuario |
| Responsable al registrarlo | Sí | SISTEMA: el Responsable vigente de la Coordinación (RF-36) |
| Responsable del equipo | Sí | SISTEMA: nace a cargo del Responsable; después lo reparte él (RF-83) |
| Usuario que registra | Sí | SISTEMA: el autenticado |
| Fecha y hora de alta | Sí | SISTEMA: reloj del servidor |

| Id | Requisito |
| --- | --- |
| RF-35 | Todo bien nace con la condición OPERATIVO. Ningún rol puede registrar un bien directamente como prestado, en mantenimiento o dado de baja. |
| RF-35b | El sistema impedirá registrar un bien en una Coordinación que no tenga ningún Laboratorio activo, e informará que debe pedirse al Administrador que registre uno (RN-26). El rechazo lo emite el servidor al enviar el formulario. Hasta la v3.7 la pantalla lo advertía además antes de empezar; ese aviso se retiró con los demás recuadros de prosa (V38-02), de modo que la comprobación sigue siendo firme pero el usuario la encuentra al guardar y no al abrir. |
| RF-35c | El sistema impedirá registrar un bien en una Coordinación que **no tenga Responsable vigente**, y lo explicará: no hay quien responda por el equipo que se registre, y hay que pedir al Administrador que nombre uno (RN-07). Es la situación en que queda la Coordinación tras la baja de su Responsable (RF-26), y la que anuncia su tarjeta en rojo. |
| RF-36 | Al registrar un bien, el sistema le asignará automáticamente como responsable al Responsable vigente de la Coordinación del usuario que lo registra, sea quien sea quien complete el formulario. Este dato queda fijado como referencia histórica del alta, y por eso el alta exige que ese Responsable exista (RF-35c). |
| RF-37 | El bien pertenece a la Coordinación del usuario que lo registra. El cliente no envía la Coordinación: la deduce el servidor del token. |
| RF-38 | Si se indica un Laboratorio, el sistema validará que pertenezca a la Coordinación del bien. |
| RF-39 | El número de serie, el código de inventario y el código patrimonial serán únicos en toda la institución, sin distinción de Coordinación. El valor convencional "S/N" queda exento de la unicidad de serie. |
| RF-40 | El Responsable podrá editar todos los campos de un bien de su Coordinación, excepto la condición, las fechas y los campos generados por el sistema. |
| RF-41 | El Responsable podrá cambiar la condición de un bien de su Coordinación a "En mantenimiento" y devolverlo a "Operativo". |
| RF-42 | El Responsable podrá dar de baja un bien de su Coordinación registrando el motivo. La baja es lógica: el bien se conserva en el histórico (RNF-22 de la versión 1.0 sigue vigente). |
| RF-43 | El Responsable podrá reincorporar al inventario un bien dado de baja, registrando el motivo; el bien regresa a la condición OPERATIVO. |
| RF-44 | El sistema impedirá dar de baja o enviar a mantenimiento un bien que se encuentre prestado; exigirá registrar antes su devolución. |
| RF-45 | El Operador solo podrá registrar bienes. Cualquier intento de editar, cambiar de condición, dar de baja, **adjuntar o cambiar la fotografía de un bien ya registrado** o **repartir responsables de equipo** recibirá error 403. |
| RF-46 | El Administrador podrá consultar los bienes de cualquier Coordinación en modo solo lectura. Ningún endpoint de escritura de inventario aceptará al rol ADMIN. |

**RF-83 — RESPONSABLE DEL EQUIPO.** Cada bien está **a cargo de una persona
concreta**, y esa persona es un **Operador** de su Coordinación o el
**Responsable** de esa Coordinación. No es lo mismo que RF-36: aquél sella el
bien con el Responsable vigente **al darlo de alta**, que es un hecho del
pasado y no cambia; éste dice **quién lo tiene hoy**, que es lo que se
pregunta cuando hay que ir a buscarlo.

| Id | Requisito |
| --- | --- |
| RF-83b | **Todo bien nace a cargo del Responsable de su Coordinación**, lo registre quien lo registre. Que un Operador complete el formulario no lo pone a su nombre: quedárselo es una decisión aparte y la toma el Responsable (RN-37). |
| RF-83c | **Solo el Responsable asigna y cambia el responsable de un equipo de su Coordinación.** Es quien reparte el trabajo dentro de ella y quien responde por el inventario entero. Un Operador no se asigna equipos a sí mismo ni se los pasa a un compañero, y el Administrador no reparte equipos de nadie, porque no opera sobre ninguna Coordinación (RN-22, RN-23). |
| RF-83d | **Se entrega únicamente a un Operador activo de la misma Coordinación.** A quien está suspendido o dado de baja no se le entrega un equipo, y la ventana no lo ofrece siquiera (RNF-23). El servidor comprueba las tres condiciones —que sea Operador, que esté activo y que sea de esa Coordinación— y la base la última por su cuenta (sección 5). |
| RF-83e | **El Responsable puede quedarse el equipo con un clic.** La ventana lo llama *dejarlo a mi cargo* y es lo que en la base significa no tener asignación. No deja el equipo huérfano: responde por él quien responde por la Coordinación. Es también lo que hay que hacer antes de que un Operador con equipos a su nombre pueda dejar su puesto (RN-38). |
| RF-83f | **Dos sueltas ocurren solas.** La **baja de un bien** lo suelta —un equipo fuera de servicio no está a cargo de nadie, y si contara ataría a su operador a un equipo que ya no existe—; y el **ascenso de un Operador a Responsable** de su propia Coordinación suelta los suyos, porque pasa a llevarlos con otro título (RF-26b, RN-35). Ninguna de las dos cambia de manos el equipo. |
| RF-83g | **Cada cambio deja un movimiento en el historial del bien**, del tipo `RESPONSABLE`, con la frase de quién a quién. La línea de tiempo de la ficha sí lo muestra, al contrario que las ediciones: quién custodia un equipo es parte de su historia (RF-53, RF-56, RN-21). |
| RF-83h | **La ficha del bien lo enseña y lo cambia en el mismo sitio.** El nombre de quien lo tiene aparece entre los datos del equipo, con la marca *Responsable de la coordinación* cuando no hay operador asignado, y con el botón **Cambiar** al lado si quien mira es el Responsable. Un bien dado de baja no ofrece el botón: no hay nada que repartir. |


**RF-47 — LISTADO DE INVENTARIO.** La ventana de inventario mostrará por
defecto **todos** los bienes de la Coordinación, sea cual sea su condición. Un
control de filtro permitirá alternar entre, y en este orden:

- Todos (por defecto)
- Operativos
- En mantenimiento
- Dados de baja
- Prestados

Hasta la v3.8 la pestaña por defecto era "Operativos", que responde a "qué
tengo disponible". Es una buena pregunta, pero no es la que trae a esta
pantalla: quien busca un equipo concreto y no lo encuentra no concluye que
está prestado, concluye que no está registrado. La vista completa es la que no
esconde nada, y las demás pestañas acotan desde ella (V39-03).

| Id | Requisito |
| --- | --- |
| RF-48 | El listado permitirá además buscar por nombre, marca, modelo, número de serie, código de inventario, código patrimonial y categoría, y filtrar por Laboratorio. |
| RF-49 | Para el Administrador, el listado exigirá además seleccionar la Coordinación cuyo inventario se desea consultar; los filtros de condición se aplican dentro de esa Coordinación. |
| RF-50 | El listado será paginado y ordenable por columna. |
| RF-51 | El sistema permitirá adjuntar opcionalmente una fotografía del bien: en el momento del registro (RF-51b) y después, desde el formulario de **Editar** (RF-51c). La ficha del bien la muestra y la amplía, pero no la cambia. |
| RF-52 | El sistema permitirá exportar el inventario filtrado a Excel, CSV y PDF. El Administrador exporta por Coordinación; el Responsable y el Operador exportan la suya. Los tres formatos salen **en blanco y negro** y encabezados con la dependencia a la que pertenecen (RF-52b). |

**RF-51b — FOTOGRAFÍA EN EL REGISTRO DEL BIEN.** El formulario de alta de un
bien ofrecerá la fotografía, **opcional**, como última sección del segundo
paso, después de la ubicación y antes del resumen (V38-03). Hasta la v3.7 era
un paso propio; adjuntar una imagen que la mayoría de las altas no lleva no
merecía una pantalla entera. En ella, quien registra podrá:

a) **Tomar la foto en ese momento con la cámara del dispositivo.** El sistema
   abrirá un visor en vivo dentro de la propia pantalla —sin salir del
   formulario ni abrir otra aplicación—, pedirá preferentemente la cámara
   trasera, que es la que apunta al equipo, y congelará la imagen al pulsar
   "Capturar". La captura se reduce a un lado máximo de 1600 px y viaja como
   JPEG, de modo que entra sin problema en el límite de 5 MB del servidor.
b) **Elegir una imagen ya guardada** en el dispositivo, como hasta la versión
   3.0.
c) **No hacer ninguna de las dos cosas.** La sección se deja en blanco, se
   continúa al resumen y el bien se registra sin fotografía (RN-29).

La fotografía elegida se muestra como vista previa antes de guardar, puede
retirarse o repetirse, y aparece nombrada en el resumen final junto al resto de
los datos. Volver atrás desde el resumen conserva la fotografía ya tomada: no
se pide dos veces.

| Id | Requisito |
| --- | --- |
| RF-51c | Si el navegador no expone cámara, no hay ninguna disponible o el usuario deniega el permiso, el sistema lo explicará en lenguaje natural y ofrecerá inmediatamente la vía alternativa: el selector de archivos con captura del sistema, que en tablet y móvil abre la aplicación de cámara del dispositivo. En ningún caso el paso queda sin salida (RNF-25, RNF-24). |
| RF-51d | El alta del bien y el envío de su fotografía son dos operaciones: el bien no puede recibir una imagen antes de existir. El sistema registrará primero el bien y adjuntará después la imagen. Si el envío de la imagen falla, el alta se conserva y el sistema informará que el equipo quedó registrado, que la fotografía quedó pendiente y que puede adjuntarse desde la ficha del bien (RNF-31). |
| RF-51e | El sistema validará en el cliente el formato (JPG, PNG o WEBP) y el tamaño (5 MB) antes de enviar la imagen, con mensaje en línea (RNF-25). La validación del servidor sigue siendo la única autoritativa (RF-04). |

**RF-51f — LA FOTOGRAFÍA DE UN BIEN YA REGISTRADO.** Sobre un equipo que ya
está en el inventario, **la fotografía la pone y la sustituye solo el
Responsable**, y lo hace desde **Editar**, no desde la ficha.

| Id | Requisito |
| --- | --- |
| RF-51g | El endpoint `POST /api/equipos/{id}/foto` es exclusivo del Responsable de la Coordinación del bien. Era la última escritura que un Operador podía hacer sobre un equipo ya registrado, y contradecía RF-45: una fotografía es un dato del bien como el modelo o el laboratorio. |
| RF-51h | La fotografía es una sección del **formulario de edición**, con las dos vías de siempre —la cámara del dispositivo o un archivo del equipo (RF-51b)— y **se confirma con el resto de los datos** en el paso de resumen (RNF-26). Antes se subía desde la ficha en cuanto se elegía el archivo, sin revisión ni confirmación de ninguna clase, que era el único cambio del sistema que ocurría sin que nadie lo aceptara. |
| RF-51i | En la edición, **no elegir nada conserva la que el equipo ya tiene**: el resumen lo dice con esas palabras, y la sección lo advierte antes de que nadie toque la cámara. No existe operación de borrar la fotografía. |
| RF-51j | En el **alta**, el formulario ofrece la fotografía solo a quien puede ponerla —el Responsable—, y el rótulo del paso cambia en consecuencia. El Operador registra el equipo sin ella; el Responsable la adjunta después (RNF-23). |
| RF-51k | La **ficha del bien** muestra la fotografía y la amplía a tamaño completo, con su cierre por teclado (RNF-32), y ya no la cambia. Al Responsable le indica dónde hacerlo. |

**RF-52b — ENCABEZADO INSTITUCIONAL DE LOS REPORTES.** Todo reporte de
inventario —Excel, CSV y PDF— y el formato de registro de salida se encabezan
diciendo **de quién son**, en cuatro líneas y en este orden:

1. **Sede Central INICTEL-UNI**, constante del sistema (RN-01).
2. El nombre completo de la **Dirección** y el de la **Coordinación**
   exportadas, separados. Si el Administrador exporta la institución entera,
   una sola línea lo dice: *Todas las direcciones y coordinaciones*.
3. Qué es el documento: *Reporte de inventario de bienes*.
4. Cuándo se generó y cuántos registros trae.

| Id | Requisito |
| --- | --- |
| RF-52c | Los tres formatos salen **en blanco y negro**. Ni color de marca, ni cabeceras con fondo oscuro, ni filas alternas sombreadas: la jerarquía la marcan el tamaño, la negrita y las líneas. Los reportes se imprimen para archivarse en papel, y lo que depende del color no sobrevive a una impresora en blanco y negro. Vale igual para el formato de registro de salida (RF-78), que además se firma a mano. |


### Módulo 6: Historial del bien (trazabilidad de condición)

**RF-53** — El sistema registrará un movimiento inmutable cada vez que un bien
cambie de condición o sufra un hecho relevante. Los tipos de movimiento son:

| Tipo | Significado |
| --- | --- |
| ALTA | registro inicial del bien |
| PRESTAMO | salida del bien |
| DEVOLUCION | retorno del bien |
| MANTENIMIENTO | ingreso a mantenimiento |
| OPERATIVO | retorno a condición operativa |
| BAJA | baja lógica |
| REINCORPORACION | reingreso al inventario tras una baja |
| EDICION | modificación de datos del bien |
| REUBICACION | cambio de Laboratorio |
| RESPONSABLE | cambio del operador que tiene el bien a su cargo (RF-83) |

| Id | Requisito |
| --- | --- |
| RF-54 | Cada movimiento registrará: tipo, condición anterior, condición nueva, motivo u observación, usuario que lo provocó, rol de ese usuario y fecha y hora del servidor. |
| RF-55 | Los movimientos son de solo escritura por parte del sistema. No existe endpoint ni pantalla de edición o eliminación de movimientos. Las fechas y horas de los movimientos son inmutables. |
| RF-56 | La ficha del bien mostrará su línea de tiempo en orden cronológico inverso y con lenguaje claro, del alta al último movimiento. **Desde la v3.9 no repite en ella los préstamos ni las devoluciones**, que tienen su propia tabla en esa misma ficha y con más datos (RF-66b), **ni muestra las ediciones**: quién corrigió un modelo mal escrito no es parte de la historia del equipo, que es dónde estuvo, en qué condición y con quién. Los movimientos ocultos se siguen registrando y conservando: el historial es inmutable y completo (RF-53, RN-21); lo que cambia es qué enseña la pantalla, que hasta la v3.8 contaba dos veces la misma salida y ninguna de las dos entera (V39-04). |
| RF-57 | El historial de un bien será visible para el Administrador, para el Responsable de su Coordinación y para los Operadores de esa Coordinación. |

### Módulo 7: Préstamos y devoluciones

| Id | Requisito |
| --- | --- |
| RF-58 | La ventana de préstamos listará únicamente los bienes de la Coordinación del usuario que se encuentren en condición OPERATIVO, presentados con la etiqueta "Disponible". |

**RF-59** — El Responsable y el Operador podrán registrar un préstamo con:

- bien a prestar (selección entre los disponibles)
- nombre completo y DNI de la persona que lo lleva
- destino o área de uso (opcional)
- fecha estimada de devolución (opcional, no anterior a hoy)
- observaciones de salida
- fecha y hora de salida: SISTEMA
- usuario que entrega: SISTEMA

| Id | Requisito |
| --- | --- |
| RF-59b | El sistema impedirá registrar **salidas y devoluciones** en una Coordinación **sin Responsable vigente**, y lo explicará: un equipo sale y vuelve a nombre de quien responde por el inventario, y ese cargo está vacante hasta que el Administrador nombre a alguien (RN-07, RF-26). Mientras dure, la Coordinación está parada del todo: sus Operadores no pueden ejecutar ninguna operación sobre ella. La consulta sigue abierta, porque mirar no cambia nada. |
| RF-60 | Al registrar el préstamo, el bien pasará automáticamente a condición PRESTADO y no podrá prestarse nuevamente hasta su devolución. |
| RF-61 | El Responsable y el Operador podrán registrar la devolución mediante un checklist de conformidad: se confirma que el equipo está completo y en buen estado, o se detalla la observación en caso contrario. |
| RF-62 | La fecha y hora de devolución las genera el servidor al confirmar. |
| RF-63 | Si la devolución es conforme, el bien regresa a condición OPERATIVO. |
| RF-64 | Si la devolución reporta daño o falta de piezas, el bien regresa a condición OPERATIVO pero queda marcado con una alerta de revisión pendiente. Solo el Responsable puede confirmar el paso a MANTENIMIENTO. Un Operador nunca cambia la condición a mantenimiento por sí mismo. |
| RF-65 | Una devolución no conforme exigirá obligatoriamente detallar la observación de retorno. |
| RF-66 | El sistema mostrará el historial de préstamos por bien y por persona (DNI), dentro del alcance de la Coordinación del usuario. |
| RF-66b | **Cada préstamo del historial de un bien se abre.** Desde la ficha del equipo, un botón **Ver detalle** por fila mostrará lo que no cabe en la tabla y es justamente lo que se consulta cuando algo no cuadra: **quién entregó el equipo y a quién** (con su DNI), el destino, la fecha comprometida y las **observaciones de entrega**; y del lado de la vuelta, **quién recibió la devolución**, cuándo, si fue conforme y las **observaciones de retorno**. Esas dos observaciones son la única constancia del estado en que el bien salió y volvió (RN-18), y hasta la v3.8 no se veían en ninguna pantalla de la ficha. Un préstamo todavía abierto lo dice en lugar de fingir una devolución vacía. |
| RF-67 | El sistema listará los préstamos con la pestaña **Todos** activa por defecto, y permitirá acotar a **Activos**, **Vencidos** y **Devueltos**, en ese orden. Los préstamos que hayan superado la fecha estimada de devolución se resaltarán indicando los días de atraso. Abrir por "Activos" escondía justamente el histórico que se viene a consultar (V39-03, RF-47). |
| RF-68 | El Administrador consultará los préstamos de cualquier Coordinación en modo solo lectura; no puede registrar salidas ni devoluciones. |

### Módulo 8: SUPRIMIDO EN LA VERSIÓN 3.0

El módulo de **Bitácora de acciones por Coordinación** (RF-69 a RF-74) queda
eliminado. El sistema no registra ni muestra quién realizó cada operación, ni
para el Administrador ni para el Responsable.

Se suprimen con él la ruta `/historial` del frontend, la API
`/api/historial/**`, la tabla `auditoria` y el contexto `auditoria` del
backend, junto al mecanismo de eventos que lo alimentaba.

**No debe confundirse con el módulo 6**, que sigue vigente. La trazabilidad del
**bien** —alta, préstamo, devolución, mantenimiento, baja, reincorporación— es
una exigencia patrimonial de la normativa de bienes estatales y se conserva
íntegra, con su inmutabilidad garantizada en la base de datos.

La numeración de los requisitos posteriores no se reajusta: RF-75 a RF-77
mantienen su identificador para no invalidar las referencias del código y de
las pruebas.

### Módulo 9: Panel de control

| Id | Requisito |
| --- | --- |
| RF-75 | El Administrador verá un panel institucional con: número de Direcciones, Coordinaciones y Laboratorios; total de bienes por condición; Coordinaciones sin Responsable asignado (alerta crítica); préstamos activos y vencidos por Coordinación. |
| RF-76 | El Responsable verá el panel de su Coordinación: bienes por condición, bienes por categoría, préstamos activos, préstamos vencidos, bienes con revisión pendiente y número de Operadores. |
| RF-77 | El Operador verá un panel simplificado y orientado a la acción: cuántos bienes hay disponibles, cuántos están prestados, cuántos préstamos registró él mismo, y accesos directos grandes a "Registrar equipo" y "Registrar prestamo". |
| RF-77b | Si su Coordinación **no tiene Responsable**, los dos accesos directos del panel del Operador aparecerán **apagados**: un botón que responde con un error es peor que un botón que no responde (RNF-23, RN-07). Hasta la v3.7 los precedía además un aviso que nombraba la consecuencia y a quién avisar; se retiró con los demás recuadros (V38-02), así que los botones quedan apagados sin decir por qué. El servidor rechaza igualmente la operación si llega por otra vía (RF-04, RF-35c, RF-59b). |

### Módulo 10: Formato de registro de uso de equipos (v3.9)

**RF-78 — FORMATO DE REGISTRO DE USO.** El **Responsable** podrá emitir, desde
la ficha de un equipo de su Coordinación y con el botón **Registrar salida**,
el *Formato de registro de uso de equipos de investigación* del laboratorio,
en PDF. El documento conserva los diez puntos del formato en papel y en su
orden, y reparte sus campos en tres clases:

| Punto | Contenido | Quién lo pone |
| --- | --- | --- |
| 1 | Responsable del equipamiento | El cargo, **Coordinador**, lo fija el sistema; el investigador encargado, su correo y su celular se escriben |
| 2 | Identificación del equipo | **El sistema**, a partir del bien: descripción, código patrimonial, código de inventario, marca, modelo, número de serie, valor de compra, **fecha de adquisición** (la de la orden de compra, desde la v3.10), área o línea de investigación y **el estado en que el equipo está ahora mismo** |
| 3 | Datos del usuario | Se escriben: nombre, correo institucional y teléfono |
| 4 | Proyecto asociado y actividad (PAO – Prociencia u otro) | Se escribe, en texto largo |
| 5 | Registro de uso | **La fecha y la hora de inicio las pone el servidor** en el momento de emitir el documento; la fecha y la hora de fin, y la actividad realizada, se escriben |
| 6 | ¿El equipo fue entregado en condiciones operativas? | Se marca Sí o No |
| 7 | ¿El equipo fue devuelto en condiciones operativas? | Se marca Sí o No; si el equipo aún no ha vuelto, las dos casillas salen en blanco para marcarlas a mano |
| 8 | Incidentes, fallas o daños | Se escriben la descripción y la acción correctiva, en texto largo |
| 9 | Autorizaciones y firmas | El PDF imprime el nombre del **Coordinador** que lo emite bajo su línea de firma, y deja en blanco la del investigador. Las dos se firman sobre el papel |
| 10 | Observaciones generales | Se escriben, en texto largo |

Ningún campo escrito es obligatorio: el formato se imprime y se termina de
rellenar a mano, delante del equipo, y exigir en pantalla una hora de fin que
todavía no se sabe obliga a inventarla.

| Id | Requisito |
| --- | --- |
| RF-78b | **El documento se ve antes de descargarse.** Generado el PDF, la pantalla lo muestra entero en la propia ventana; desde ahí se descarga o se vuelve al formulario, que conserva lo escrito. Un papel que se va a firmar no se descarga a ciegas. |
| RF-79 | **El formato no registra el préstamo, y el sistema lo dice.** Al mostrar el documento y otra vez al descargarlo, la pantalla recuerda que la salida del equipo no consta en ninguna parte hasta que se registre el préstamo (RF-59), y ofrece ir a registrarlo. El propio PDF lo lleva impreso al pie. Son dos cosas distintas —el papel que se firma y el registro del sistema— y hay que hacer las dos. |
| RF-80 | **Solo lo emite el Responsable, y solo sobre bienes de su Coordinación.** Es quien firma como coordinador y quien responde por el equipo que sale. El Administrador no lo emite —no opera sobre ninguna Coordinación (RN-22)— y el Operador tampoco. El servidor comprueba el rol y la Coordinación del bien, además de la anotación del endpoint (RF-04, RN-23). |
| RF-81 | El formato se emite **sea cual sea la condición del bien**, y el documento imprime la que tenga: si el equipo está prestado o en mantenimiento, el papel lo dice y quien lo firma se entera antes de llevárselo. Impedirlo obligaría a mentir en el punto 2 para poder imprimir. |
| RF-82 | El documento **no se guarda**: no hay tabla, ni endpoint de consulta, ni lista de formatos emitidos, ni fichero en el servidor. Se genera, viaja al navegador y ahí termina (RN-36). |

---

## 4. Requisitos no funcionales

### 4.1 Seguridad

| Id | Requisito |
| --- | --- |
| RNF-01 | Toda comunicación en producción se realizará sobre HTTPS. |
| RNF-02 | Autenticación basada en JWT firmado; autorización por rol Y por Coordinación validada en cada endpoint del backend. |
| RNF-03 | Contraseñas cifradas con BCrypt (factor de costo >= 10). |
| RNF-04 | Política de contraseñas: mínimo 8 caracteres, con letras, números y al menos un carácter especial. |
| RNF-05 | Bloqueo temporal de cuenta tras 5 intentos fallidos. |
| RNF-06 | Protección contra inyección SQL mediante JPA y consultas parametrizadas; sin concatenación de SQL en ningún punto. |

**RNF-07 — CONTENT-SECURITY-POLICY ESTRICTA AL 100%.** La política de la
aplicación no contendrá `'unsafe-inline'` ni `'unsafe-eval'` en ninguna
directiva. En concreto:

```
default-src 'self'
script-src 'self'
script-src-attr 'none'
style-src 'self'
style-src-attr 'none'
img-src 'self' data: blob:
font-src 'self'
connect-src 'self'
media-src 'self'
worker-src 'self' blob:
manifest-src 'self'
object-src 'none'
base-uri 'self'
form-action 'self'
frame-ancestors 'none'
upgrade-insecure-requests
```

Consecuencias vinculantes para el frontend: prohibidos los manejadores de
evento en atributos HTML, los atributos style en línea, las etiquetas
`<script>` embebidas y cualquier recurso servido desde un CDN. Toda la interfaz
se sirve desde el propio origen. Se mantendrá una prueba automática de
regresión que falle si reaparece un estilo o un manejador en línea.

La captura de fotografías (RF-51b) **no obliga a relajar ninguna directiva**.
El flujo de la cámara se asigna por código a la propiedad `srcObject` del
elemento de vídeo, que no es una descarga de un recurso y por tanto no pasa por
`media-src`; la vista previa de la imagen es un `blob:`, ya autorizado en
`img-src` para las fotografías que se descargan con JWT.

| Id | Requisito |
| --- | --- |
| RNF-08 | La documentación Swagger, que requiere estilos en línea propios, se aislará en su propia cadena de seguridad para no relajar la política de la aplicación, y podrá desactivarse en producción. |
| RNF-09 | Cabeceras de endurecimiento obligatorias: X-Frame-Options DENY, Referrer-Policy same-origin, Permissions-Policy restrictiva y HSTS con includeSubDomains. |
| RNF-09b | La `Permissions-Policy` habilitará la cámara **únicamente para el propio origen** —`camera=(self)`—, porque el registro de un bien la usa para fotografiarlo (RF-51b). Micrófono, geolocalización, pagos, USB y cohortes de interés siguen denegados por completo. Abrirla a `self` no expone a terceros: la aplicación no puede viajar dentro de un marco ajeno (`frame-ancestors 'none'` y X-Frame-Options DENY), y el navegador solo entrega la cámara en contexto seguro, que RNF-01 garantiza en producción. |
| RNF-10 | El aislamiento por Coordinación se validará en el servidor en todas las operaciones de lectura y escritura. Un identificador de otra Coordinación enviado por el cliente responderá 403 o 404, nunca los datos. **El cliente no declara nada sobre su ámbito**: cada persona pertenece a una sola Coordinación (RN-05) y el servidor la relee de la base en cada petición, de modo que una baja o una asignación surten efecto en la siguiente sin esperar a que el token expire. La cabecera `X-Coordinacion` de la v3.3, que servía para elegir entre varias, se retiró junto con el motivo que la justificaba. |

### 4.2 Rendimiento y escalabilidad

| Id | Requisito |
| --- | --- |
| RNF-11 | El sistema soportará al menos 300 usuarios concurrentes sin degradación perceptible. |
| RNF-12 | Las consultas de listado paginado responderán en menos de 2 segundos con un inventario de hasta 50,000 bienes por Coordinación. |
| RNF-13 | Todas las consultas de listado serán paginadas del lado del servidor. Ningún endpoint devolverá colecciones sin límite. |
| RNF-14 | Toda columna usada en filtro, ordenamiento o llave foránea contará con índice. En particular: `coordinacion_id`, `condicion`, `categoria_id` y `laboratorio_id` en la tabla de bienes. |
| RNF-15 | El pool de conexiones se dimensionará y parametrizará por variable de entorno; el valor por defecto debe sostener la concurrencia objetivo sin agotar las conexiones de PostgreSQL. |
| RNF-16 | Se evitará el problema N+1: las consultas de listado resolverán sus asociaciones por proyección o por carga por lotes. |
| RNF-17 | Las transacciones de solo lectura se marcarán como tales. |
| RNF-17b | **Una operación que cambia varias cosas las cambia todas o ninguna, y lo hace en el servidor.** Ninguna operación con más de un efecto se compone encadenando peticiones desde el navegador: allí cada eslabón puede fallar por su cuenta —la red, la sesión que caduca entre una y otra, una regla que solo se comprueba en la segunda— y el usuario se queda con la mitad hecha y sin manera de deshacerla. El alta con puesto (RF-16e) y el relevo de responsable (RF-26b) son las dos operaciones de esta clase, y las dos viven en una sola transacción del servidor. |
| RNF-18 | Las respuestas HTTP se servirán comprimidas. |

### 4.3 Disponibilidad y respaldo

| Id | Requisito |
| --- | --- |
| RNF-19 | Disponibilidad objetivo de 99% en horario laboral. |
| RNF-20 | Respaldo automático diario de la base de datos con retención mínima de 30 días. |

### 4.4 Usabilidad (segmento objetivo con cero experiencia previa)

| Id | Requisito |
| --- | --- |
| RNF-21 | Interfaz en español, responsiva (escritorio, tablet y móvil). |
| RNF-21b | **En español escrito con sus tildes y sus eñes**, en todo lo que lee el usuario: los rótulos, las ayudas, los mensajes y los datos que el sistema trae puestos. Una aplicación institucional que rotula "Contrasena" o "Gestion" se lee como un borrador, y hasta la v3.7 lo hacía en todas sus pantallas salvo en unas pocas etiquetas sueltas, que es peor: la mezcla delata el descuido más que la ausencia. Los identificadores del código quedan fuera —`equipo.condicion` es un nombre de campo, no una palabra—, y también las palabras cuyo significado depende de la tilde (*esta/está*, *si/sí*, *que/qué*, *aun/aún*), donde una sustitución automática acierta casi siempre y estropea el resto. |
| RNF-22 | PRINCIPIO RECTOR: la pantalla debe poder usarse sin capacitación previa. Cada vista responde a la pregunta "que puedo hacer aqui" antes de mostrar datos. |
| RNF-23 | Cada usuario verá únicamente las opciones que su rol le permite. No se muestran opciones deshabilitadas ni menús que terminen en un error de permisos. |
| RNF-24 | Toda pantalla de listado vacía mostrará un estado vacío explicativo con el siguiente paso concreto, nunca una tabla en blanco. |
| RNF-25 | Los mensajes de validación serán en línea, junto al campo, en lenguaje natural y sin terminología técnica. Prohibidos los mensajes del tipo "constraint violation" o "error 500". |
| RNF-26 | **Toda acción importante se confirma antes de ocurrir**, en una ventana flotante que describe la consecuencia en palabras concretas y nombra a quien la sufre: quién deja de ser responsable y qué coordinación queda parada, qué equipo deja de aparecer en el inventario, qué contraseña deja de servir. Son, al menos: el registro y la edición de una persona (RF-16d), la asignación de un puesto y su baja (RF-28d), la baja del Responsable (RF-26), la desactivación y reactivación de una cuenta, el restablecimiento de una contraseña, el desbloqueo de una cuenta, la activación y desactivación de una Coordinación o un Laboratorio, y la baja o reincorporación de un bien. La ventana ofrece siempre dos salidas explícitas, y la que no ejecuta se llama **Cancelar**. |
| RNF-27 | La acción principal de cada pantalla será visualmente dominante, única y estará siempre en la misma posición: arriba a la derecha del encabezado. **Y en la misma pantalla tras pantalla**: hasta la v3.7 una descripción larga la empujaba al renglón siguiente y aparecía abajo a la izquierda —ocurría en Personas y entre una Dirección y la otra—, de modo que el usuario tenía que buscarla en cada vista. Las descripciones se limitan a la medida de lectura para dejarle sitio (V38-05). |
| RNF-27b | **La pantalla no se explica a sí misma con párrafos.** Desde la v3.8 no hay recuadros de prosa que cuenten lo que la vista ya enseña: si un formulario necesita un párrafo para entenderse, lo que hay que arreglar es el formulario. Se conservan la validación junto al campo (RNF-25), los estados vacíos con su siguiente paso (RNF-24), las ventanas de confirmación (RNF-26) y el error que devuelve el servidor, que es lo único que el usuario no puede deducir mirando (V38-02). |
| RNF-28 | Los formularios largos se dividirán en pasos cortos con progreso visible. El registro de un bien no superará un paso por pantalla en móvil. |
| RNF-29 | Se usará vocabulario del dominio, no del sistema: "Equipo" y no "entidad", "Dado de baja" y no "inactivo", "Disponible" y no "operativo" en el contexto de préstamos. |
| RNF-30 | Los estados y condiciones se distinguirán por color Y por texto, nunca solo por color (accesibilidad). |
| RNF-31 | Toda operación que modifique datos dará realimentación inmediata confirmando que ocurrió y qué cambió. |
| RNF-32 | Contraste mínimo AA (WCAG 2.1), navegación completa por teclado y etiquetas asociadas a todos los campos. |
| RNF-48 | **Ninguna pantalla obliga a recargar el navegador para ver lo que ha cambiado.** Todo listado, ficha y panel vuelve a pedir sus datos cada 30 segundos mientras la pestaña está a la vista, y de inmediato al volver a ella. El armazón hace lo mismo con el puesto de quien la usa, de modo que una asignación o una baja se reflejan en su menú y en su ámbito sin cerrar sesión (RF-02, RNF-10). La recarga es **silenciosa**: no enciende el indicador de carga ni avisa de sus errores —un giro cada medio minuto sobre una tabla que se está leyendo molesta más de lo que informa—, y **no ocurre mientras hay una ventana de confirmación o un formulario abiertos**, porque cambiar lo que hay detrás es cambiarle la pregunta a quien está a punto de responderla (RNF-26). Con la pestaña oculta no se pide nada: nadie está leyendo, y sostener una consulta por pestaña abierta cuesta al servidor lo mismo que si la leyera alguien (RNF-11). |

### 4.5 Navegación y rutas

| Id | Requisito |
| --- | --- |
| RNF-33 | Las rutas serán cortas, en español, en minúsculas y semánticas. Sin segmentos técnicos ni identificadores innecesarios. |
| RNF-34 | Cualquier ruta inexistente mostrará una página 404 propia, en español, con explicación clara y un botón primario de regreso a la página principal del usuario. |
| RNF-35 | Una ruta existente pero no autorizada para el rol del usuario redirigirá a su página principal con un aviso, sin filtrar la existencia del recurso. |
| RNF-36 | La página principal depende del rol: el Administrador entra a la estructura organizacional, el Responsable y el Operador a su panel. |

### 4.6 Mantenibilidad y arquitectura

| Id | Requisito |
| --- | --- |
| RNF-37 | El backend mantendrá el enfoque Domain-Driven Design con contextos delimitados. Cada contexto conserva sus cuatro capas: dominio puro (sin Spring ni JPA), aplicación, infraestructura y presentación. |
| RNF-38 | El dominio no dependerá de ningún framework. Las reglas de negocio serán comprobables sin levantar Spring ni base de datos. |
| RNF-39 | La comunicación entre contextos se hará exclusivamente por puertos declarados en el dominio del contexto consumidor, implementados por adaptadores anticorrupción en su infraestructura. Prohibido que un contexto importe entidades JPA o servicios de otro. |
| RNF-40 | Las invariantes del negocio vivirán en el dominio, no en el controlador ni en el cliente. |
| RNF-41 | Cobertura mínima de pruebas unitarias del 60% en la capa de dominio y servicios. |
| RNF-42 | Documentación de la API con OpenAPI/Swagger. |
| RNF-43 | Migraciones de base de datos versionadas con Flyway. El esquema nunca se genera desde Hibernate: este solo valida. Mientras el sistema no esté instalado en producción, una versión puede **rehacer la línea base** en lugar de añadir un parche encima: es lo que hizo la v3.5, que fundió seis migraciones en dos (V35-08). A partir de la primera instalación con datos reales, cada cambio del esquema es una migración nueva y las anteriores no se tocan. |

### 4.7 Portabilidad y despliegue

| Id | Requisito |
| --- | --- |
| RNF-44 | **Los tres servicios se despliegan con Docker Compose**: base de datos, backend y frontend. Las dos imágenes propias se construyen en dos etapas, de modo que la que se publica no arrastra el compilador ni el código fuente: el backend compila con el JDK y se ejecuta sobre el JRE; el frontend compila con Node y se sirve con nginx. El backend corre con un usuario sin privilegios, expone una sonda de estado en `/actuator/health` —solo `health`, y sin detalle— y el arranque queda ordenado: el backend espera a que la base acepte conexiones y el frontend a que el backend responda sano. Las fotografías de los bienes viven en un volumen, de modo que un redespliegue no se las lleva por delante (RF-51). |
| RNF-44b | **Solo el frontend publica puerto.** La base de datos y el backend se alcanzan únicamente por la red interna: la aplicación y la API comparten origen detrás de nginx, que reenvía `/api` al backend. Es lo que permite que `connect-src 'self'` baste y que no haya que abrir CORS a nadie (RNF-07, RNF-10). |
| RNF-44c | **Nginx emite las cabeceras de endurecimiento además de Spring.** Spring solo las pone en lo que él sirve, que es la API; quien entrega el HTML y el JavaScript de la aplicación es nginx, y sin ellas la pantalla que abre el usuario viajaría sin Content-Security-Policy. La política es la misma cadena que `CSP_ESTRICTA`, y las cabeceras se declaran en un fragmento aparte que se incluye en **cada** `location`: nginx no las hereda allí donde el `location` declara cabeceras propias, así que un simple `Cache-Control` deja al bloque sin CSP (RNF-07, RNF-09). |
| RNF-45 | Toda la configuración por variables de entorno; ninguna credencial ni secreto en el código fuente. El despliegue lee un `.env` que no se versiona, con `.env.example` como plantilla; las tres variables sin valor por defecto —la clave de la base, la del JWT y la de la cuenta inicial— detienen el arranque si faltan, con el nombre de la que falta, en lugar de levantar el sistema con una clave de ejemplo. |
| RNF-45b | **El origen autorizado incluye el puerto.** El navegador envía cabecera `Origin` en el POST del acceso aunque la aplicación y la API compartan origen, de modo que `APP_CORS_ORIGENES_PERMITIDOS` debe coincidir exactamente con la dirección que se escribe en el navegador —`http://localhost:8090` si se publica en ese puerto, `http://localhost` si es el 80, que el navegador omite—. Si no coincide, Spring responde `403 Invalid CORS request` y nadie puede entrar, mientras la API sigue respondiendo con normalidad a las herramientas de línea de comandos, que no envían `Origin`: el síntoma engaña, y por eso queda escrito. |

### 4.8 Legales y normativos

| Id | Requisito |
| --- | --- |
| RNF-46 | Tratamiento de datos personales (DNI, nombres) conforme a la Ley N° 29733 — Ley de Protección de Datos Personales del Perú. |
| RNF-47 | Conservación del histórico de bienes patrimoniales y de las acciones de los usuarios; prohibida la eliminación física. |

---

## 5. Modelo de datos

La institución "INICTEL-UNI" es una constante de la aplicación y no se
persiste como tabla.

**El esquema se declara una sola vez.** Desde la v3.5 hay dos migraciones y no
seis: `V1` crea la base tal como es hoy y `V2` inserta lo que el sistema trae
puesto —las dos Direcciones y el catálogo de categorías—. Las cinco
migraciones que corregían la inicial (baja de la bitácora, precarga de
Direcciones, tabla de asignaciones, una coordinación por persona) existían para
llevar de una versión a otra instalaciones con datos, y ninguna llegó a
tenerlos; mientras tanto obligaban a leer seis archivos para saber cómo era una
tabla, y dejaban en el esquema columnas que nacían y morían sin haber guardado
nunca un dato. Lo que aquellas migraciones decían sigue dicho aquí y en el
capítulo 0, que es donde se explica por qué el sistema es como es.

### DIRECCION

```
id, nombre (unico), sigla, descripcion, activa, fecha_creacion
Precargada por V2 con las dos direcciones de la institucion.
La sigla si existe aqui —DIDT y DCTT son como se las nombra— y no en la
coordinacion (RF-11).
La columna "activa" se conserva por simetria y vale siempre TRUE:
ninguna operacion del sistema desactiva una direccion (RN-30).
```

### COORDINACION

```
id, direccion_id (FK), nombre, descripcion, activa, fecha_creacion
UNIQUE (direccion_id, LOWER(nombre))
Sin columna "sigla": la coordinacion no la tiene (RF-11).
```

### LABORATORIO

```
id, coordinacion_id (FK), nombre, ubicacion, activo, fecha_creacion
UNIQUE (coordinacion_id, LOWER(nombre))
```

### USUARIO

```
id, username (unico), nombres, primer_apellido,
segundo_apellido (NOT NULL, RN-32),
dni (unico, 8 digitos), cargo, correo (unico), password_hash,
rol (ADMIN | RESPONSABLE | OPERADOR, NULL = registro previo),
estado (ACTIVA | SUSPENDIDA | BAJA), debe_cambiar_password, intentos_fallidos,
bloqueado_hasta, ultimo_acceso, fecha_creacion, fecha_actualizacion

El usuario no tiene columna de coordinacion: la asignacion es una
relacion, no un atributo, y vive en usuario_coordinacion.
rol admite NULL: es la persona registrada que aun no tiene puesto
(RF-16b, RN-31).
estado sustituye al booleano "activo" desde la v3.7: irse de vacaciones
y dejar la institucion no son la misma cosa, y un interruptor no sabe
decir cual de las dos es (RF-22b, RN-34). La BAJA borra ademas la fila
de usuario_coordinacion: quien se fue no conserva puesto.
```

### USUARIO_COORDINACION (asignaciones)

```
usuario_id (FK), coordinacion_id (FK), rol (RESPONSABLE | OPERADOR),
fecha_asignacion
PRIMARY KEY (usuario_id, coordinacion_id)
CHECK: el ADMIN no se asigna a ninguna coordinacion (RN-05)
Indice unico parcial por coordinacion_id  WHERE rol = 'RESPONSABLE':
  RN-06, un solo responsable por coordinacion.
Indice unico por usuario_id, sin condicion:
  RN-05, una persona en una sola coordinacion, sea cual sea su rol.
El rol se guarda tambien aqui, y no solo en el usuario, porque es lo que
permite a la base sostener por si misma la unicidad del responsable.
La baja de un puesto (RF-26) es el borrado de su fila: la persona conserva
su cuenta y su historial, y el rol del usuario queda en NULL.
```

### CATEGORIA

```
id, nombre (unico), descripcion, activa, fecha_creacion
```

### EQUIPO (BIEN)

```
id, coordinacion_id (FK, obligatorio), laboratorio_id (FK, opcional),
nombre, marca, modelo, numero_serie (unico salvo 'S/N'),
codigo_inventario (unico), codigo_patrimonial (unico),
categoria_id (FK), condicion (OPERATIVO | PRESTADO | MANTENIMIENTO |
BAJA), fecha_adquisicion (DATE), costo, observaciones, foto_url,
revision_pendiente (booleano), motivo_baja, fecha_baja,
responsable_id (FK usuario, Responsable vigente al momento del alta),
responsable_equipo_id (FK usuario_coordinacion, NULL = el Responsable),
usuario_registro_id (FK usuario), fecha_registro, fecha_actualizacion,
activo

fecha_adquisicion sustituye a anio_adquisicion desde la v3.10: el formato
de registro de uso pide la fecha de la orden de compra (RF-78), y de un
anio no se saca una fecha. El CHECK exige que no sea anterior al
1980-01-01; el limite superior —que no sea futura— vive en el dominio,
porque un CHECK con CURRENT_DATE no seria inmutable (RN-25).

responsable_equipo_id es el Operador que tiene el bien a su cargo (RF-83).
NULL no es un hueco sin rellenar: significa que lo lleva el Responsable de
la Coordinacion, sea quien sea en cada momento (RN-37). Es como nace todo
bien y donde vuelve cuando se le retira a un Operador, de modo que la baja
de un Responsable —que deja la Coordinacion parada pero no borra sus
bienes— no deja ninguna referencia colgando.

La llave foranea no apunta al usuario sino a su asignacion:
  FOREIGN KEY (responsable_equipo_id, coordinacion_id)
      REFERENCES usuario_coordinacion (usuario_id, coordinacion_id)
      DEFERRABLE INITIALLY DEFERRED
Con ella la base sostiene por si misma las dos mitades de RN-37 y RN-38:
no se puede poner a cargo a alguien de otra Coordinacion, y no se puede
borrar la asignacion de quien tiene bienes a su nombre. Es aplazable
porque la asignacion se mapea como coleccion y cambiar el rol de una
persona se traduce en un borrado y un alta de su fila dentro de la misma
transaccion: comprobada al vuelo, la regla rechazaria un estado intermedio
que al confirmar ya es valido.
```

### MOVIMIENTO_EQUIPO (historial inmutable del bien)

```
id, equipo_id (FK), tipo, condicion_anterior, condicion_nueva,
detalle, usuario_id (FK), usuario_nombre, rol_usuario, fecha_hora
Sin UPDATE ni DELETE en toda la aplicacion.
Desde la v3.10 el CHECK de "tipo" admite un valor mas, RESPONSABLE: el
cambio de quien tiene el bien a su cargo (RF-83).
```

### PRESTAMO

```
id, equipo_id (FK), coordinacion_id (FK), nombre_persona,
dni_persona, destino, fecha_prestamo, fecha_estimada_devolucion,
observaciones_salida, fecha_devolucion, conforme,
reporta_dano, observaciones_retorno, usuario_presta_id (FK),
usuario_recibe_id (FK), estado (ACTIVO | DEVUELTO)
Indice unico parcial: un solo prestamo ACTIVO por equipo.
```

### AUDITORIA — SUPRIMIDA EN LA VERSIÓN 3.0

La tabla `auditoria` no existe: el sistema no registra quién hizo cada cosa
fuera del historial del bien (RN-28). Desde la v3.5 tampoco se crea para
borrarla después — el esquema se declara ya sin ella (sección 5)—, de modo que
el único disparador de inmutabilidad es el de `movimiento_equipo`.

---

## 6. Matriz de permisos por rol

**Ámbito:** "Suya" significa únicamente la Coordinación a la que el usuario
pertenece, que es una sola (RN-05). "Todas (L)" significa todas las
Coordinaciones, solo lectura.

| Funcionalidad | ADMIN | RESPONSABLE | OPERADOR |
| --- | --- | --- | --- |
| Editar Direcciones (no se crean ni desactivan) | SI | NO | NO |
| Crear/editar Coordinaciones | SI | NO | NO |
| Crear/editar Laboratorios | SI | NO | NO |
| Crear categorías | SI | NO | NO |
| Registrar personas (registro previo) | SI | SI | NO |
| **Registrar persona y puesto en un solo acto** (RF-16e) | SI | SI (operadores, suya) | NO |
| Asignar el puesto de ADMIN | SI | NO | NO |
| Nombrar al RESPONSABLE de una coordinación sin él | SI | NO | NO |
| **Cambiar el RESPONSABLE de una coordinación** | SI | NO | NO |
| Asignar el puesto de OPERADOR | SI | SI (suya) | NO |
| Dar de baja el puesto de un OPERADOR | SI | Operadores (suya) | NO |
| Editar usuarios | SI | Operadores (suya) | NO |
| Ver la ficha completa de una persona | Todas | Los suyos | NO |
| Suspender y reactivar cuentas | SI (salvo la propia) | Operadores (suya) | NO |
| Dar de baja y reincorporar personas | SI (salvo la propia) | Operadores (suya) | NO |
| Ver la lista de personas | Todas | Los suyos | NO |
| Registrar bienes | NO | SI (suya) | SI (suya) |
| Editar bienes | NO | SI (suya) | NO |
| **Adjuntar o cambiar la fotografía de un bien registrado** (RF-51f) | NO | SI (suya) | NO |
| Cambiar condición a mantenimiento | NO | SI (suya) | NO |
| Dar de baja / reincorporar bienes | NO | SI (suya) | NO |
| Consultar inventario | Todas (L) | SI (suya) | SI (suya) |
| Ver historial de un bien | Todas (L) | SI (suya) | SI (suya) |
| Registrar préstamos | NO | SI (suya) | SI (suya) |
| Registrar devoluciones | NO | SI (suya) | SI (suya) |
| Consultar préstamos | Todas (L) | SI (suya) | SI (suya) |
| Ver el detalle de un préstamo del bien (RF-66b) | Todas (L) | SI (suya) | SI (suya) |
| Exportar reportes | Todas (L) | SI (suya) | SI (suya) |
| **Emitir el formato de registro de uso** (RF-78) | NO | SI (suya) | NO |
| **Asignar o cambiar el responsable de un equipo** (RF-83) | NO | SI (suya) | NO |
| Ver panel de control | Institucional | Suya | Suya (simplificado) |

---

## 7. Reglas de negocio invariantes

Estas reglas se implementan en el dominio y se verifican con pruebas.

| Id | Regla |
| --- | --- |
| RN-01 | El nombre de la institución es siempre "INICTEL-UNI". |
| RN-02 | Una Coordinación pertenece a exactamente una Dirección. |
| RN-03 | Un Laboratorio pertenece a exactamente una Coordinación. |
| RN-04 | No puede asignarse el puesto de RESPONSABLE u OPERADOR si no existe al menos una Coordinación activa. El registro previo de la persona sí puede hacerse antes (RF-16b). |
| RN-05 | Toda persona con rol operativo —RESPONSABLE u OPERADOR— está asignada a exactamente una Coordinación. Un ADMIN no está asignado a ninguna. Una persona sin rol no está asignada a ninguna y no puede operar. |
| RN-05b | La Coordinación en la que se ejecuta una operación es la del usuario, y la determina el servidor leyéndola de la base. El cliente no la elige ni la declara (RNF-10). |
| RN-06 | Una Coordinación tiene como máximo un RESPONSABLE activo. Asignar ese puesto cuando ya está ocupado se rechaza: no se sustituye a nadie sin darle de baja antes (RF-26). |
| RN-07 | Una Coordinación sin RESPONSABLE está **parada**: no admite ninguna operación de escritura de sus Operadores —ni altas de bienes, ni salidas, ni devoluciones—, y el servidor lo impide en todas ellas. La consulta sigue abierta. Puede quedarse sin él —es lo que ocurre cuando se le da de baja (RF-26)— y queda marcada en rojo hasta que se nombre a su sucesor. |
| RN-08 | La sustitución de un Responsable son dos operaciones consecutivas y explícitas: la baja del vigente y el nombramiento del entrante. Ninguna operación del sistema hace las dos a la vez. |
| RN-09 | Un usuario nunca se elimina. Se **suspende** —temporalmente, conservando su puesto—, se **da de baja** —definitivamente, liberándolo— y se reactiva o se reincorpora (RF-22b, RN-34). |
| RN-10 | Solo la cuenta **activa** autentica. Ni la suspendida ni la de baja pueden entrar, y el sistema responde a las dos con el mismo mensaje: cuál de los dos casos es no se revela en la pantalla de acceso (RF-07). |
| RN-11 | Un bien pertenece a exactamente una Coordinación, fijada en el alta a partir de la Coordinación del usuario que lo registra. |
| RN-12 | El Laboratorio de un bien, si existe, pertenece a la misma Coordinación que el bien. |
| RN-13 | Todo bien nace en condición OPERATIVO. |
| RN-14 | Número de serie, código de inventario y código patrimonial son únicos en toda la institución. "S/N" está exento en la serie. |
| RN-15 | Solo se puede prestar un bien en condición OPERATIVO. |
| RN-16 | Un bien no puede tener dos préstamos activos simultáneos. |
| RN-17 | Un bien PRESTADO no puede darse de baja ni enviarse a mantenimiento; primero se registra su devolución. |
| RN-18 | La devolución exige declarar la conformidad. Si no es conforme, la observación de retorno es obligatoria. |
| RN-19 | Una devolución con daño marca el bien para revisión; solo el RESPONSABLE confirma el paso a MANTENIMIENTO. |
| RN-20 | Toda transición de condición genera un movimiento inmutable. |
| RN-21 | Las fechas y horas de altas, préstamos, devoluciones, bajas y movimientos las genera el servidor y no son modificables por ningún rol ni por ningún endpoint. |
| RN-22 | El rol ADMIN no tiene ninguna operación de escritura sobre bienes ni préstamos. |
| RN-23 | Ninguna operación puede leer ni escribir datos de una Coordinación distinta a la del usuario, salvo el ADMIN en lectura. |
| RN-24 | El OPERADOR no accede a la gestión de usuarios. |
| RN-25 | La **fecha** de adquisición no puede ser futura ni anterior al 1 de enero de 1980; el costo no puede ser negativo. Desde la v3.10 el bien guarda la fecha entera y no solo el año, porque el formato de registro de uso pide la de la orden de compra (RF-78, V310-01). |
| RN-26 | Toda Coordinación tiene al menos un Laboratorio activo: nace con el primero y no puede desactivarse el único que le queda. Sin Laboratorios activos no se registran bienes. |
| RN-27 | No existe operación, pantalla ni endpoint que asigne a una persona a dos Coordinaciones, sea cual sea su rol. Moverla es darle de baja del puesto que tiene y asignarle después el nuevo, en dos actos separados y confirmados. |
| RN-31 | El rol y la contraseña de una persona nacen con su asignación, no con su alta. Quien está registrado sin puesto no tiene ninguna capacidad en el sistema ni credenciales con las que entrar. |
| RN-32 | La identidad de una persona son sus nombres y sus **dos apellidos**, los tres obligatorios en toda alta, edición y primer ingreso. El dominio los exige y la base los declara NOT NULL. |
| RN-34 | La **suspensión** conserva el puesto y la **baja** lo libera. Quien deja la institución no responde por un inventario ni figura entre los operadores de una Coordinación: la baja retira su rol y su asignación en el mismo acto, y si era el Responsable su Coordinación queda parada (RN-07). Ninguna de las dos borra a la persona ni su historial (RN-09, RNF-47). |
| RN-35 | El puesto de Responsable de una Coordinación solo lo toma quien está **activo** y **no es ya Responsable** de ninguna: una persona sin puesto, un Operador de esa misma Coordinación o un Administrador que no sea el último activo (RF-25). El relevo es un solo acto y en él el saliente **queda libre** —sin rol y sin Coordinación—, de modo que la Coordinación no pasa ni un instante sin responsable (RF-26b, RN-07). |
| RN-33 | Un puesto solo se **asigna** (RF-28d) a quien no tiene ninguno, sea cual sea el rol que se le vaya a dar y el que ya tenga —ADMIN incluido, que no ocupa Coordinación—. Y nadie se asigna un puesto a sí mismo. Para cambiar de puesto a alguien está **Cambiar responsable** (RN-35), que es la operación que sabe qué hacer con el que deja: liberarlo en el mismo acto. |
| RN-28 | El sistema no conserva registro de qué usuario ejecutó cada operación fuera del historial del bien, que nombra al autor de cada movimiento por exigencia patrimonial. |
| RN-29 | La fotografía de un bien es opcional y nunca condiciona su alta: ni su ausencia, ni un permiso de cámara denegado, ni un fallo al enviar la imagen impiden registrar el bien. |
| RN-30 | Las Direcciones de la institución son las dos de su reglamento: se precargan con el sistema y ninguna operación las crea ni las desactiva. Solo se corrigen sus datos. |
| RN-37 | **Todo bien está a cargo de alguien, y ese alguien es de su Coordinación.** El responsable del equipo es un **Operador** de la Coordinación del bien o el **Responsable** de esa Coordinación, y nadie más. Se representa con `equipo.responsable_equipo_id`, donde `NULL` significa *lo lleva el Responsable, sea quien sea en cada momento*: por eso el bien nunca queda sin nadie que responda por él, ni siquiera mientras la Coordinación está sin Responsable (RN-07). Todo bien nace así, también el que registra un Operador —registrar un equipo no es quedárselo—, y ahí vuelve cuando se le retira a alguien o cuando se da de baja el bien, porque un equipo fuera de servicio no está a cargo de nadie. Solo el Responsable lo asigna y lo cambia (RF-83). |
| RN-38 | **Quien tiene bienes a su cargo no deja su puesto.** Un Operador con equipos a su nombre no se puede dar de baja ni retirar de su Coordinación —y por tanto tampoco mover a otra, que es retirarlo y volverlo a asignar (RN-27)—: antes, el Responsable tiene que entregarlos a otro Operador o quedárselos. Un equipo a nombre de alguien que ya no trabaja allí es un equipo del que nadie responde, y el inventario diría lo contrario. La **suspensión** no lo exige, porque conserva el puesto y la persona vuelve a él (RN-34). La base lo sostiene por su cuenta: la fila de `usuario_coordinacion` de esa persona está referenciada por sus bienes (sección 5). |
| RN-36 | **Emitir un documento no es registrar un hecho.** El formato de registro de uso (RF-78) genera un PDF y no escribe nada: no se guarda en ninguna tabla, no deja movimiento en el historial del bien, no crea un préstamo y **no cambia la condición del equipo**, que queda como estaba. La salida de un bien solo consta cuando se registra el préstamo (RF-59, RN-15), y el sistema lo recuerda al emitir el formato (RF-79). Es la regla que separa el trámite en papel del registro patrimonial: confundirlos dejaría equipos "prestados" en el inventario sin ningún préstamo detrás, o fuera del laboratorio sin que el inventario lo supiera. |

---

## 8. Diseño de interfaz por segmento objetivo

### 8.1 Principios transversales

- Una acción principal por pantalla, siempre arriba a la derecha del
  encabezado, con el mismo aspecto en todas las vistas.
- El encabezado de cada vista indica siempre el ámbito en el que el usuario
  está trabajando: "Coordinacion de Redes". El usuario nunca debe dudar de
  dónde está parado. Es un dato, no un control: cada persona pertenece a una
  sola Coordinación y no hay nada que elegir (RN-05).
- Toda acción importante se confirma en una ventana flotante que dice qué va a
  ocurrir antes de que ocurra, y su salida de escape se llama siempre
  **Cancelar** (RNF-26).
- **Una ventana a la vez.** Ninguna ventana se abre encima de otra: la que
  estaba se aparta mientras se decide y vuelve al cerrarla. Dos modales
  superpuestos dejan al usuario sin saber cuál está atendiendo, y el de abajo
  se lee a medias por debajo del de arriba.
- Al entrar, el sistema **saluda y dice dónde se ha entrado** —rol,
  Coordinación y Dirección— con un único botón **Cerrar** (RF-01b).
- La **pantalla de acceso** lleva de fondo una fotografía de la institución
  (`assets/inictel.jpg`) bajo un velo suave con el degradado de marca, y la
  caja del formulario encima. El velo unifica el tono sin tapar la imagen; el
  contraste del pie lo asegura su sombra de texto, no el oscurecimiento del
  fondo (RNF-32). La imagen se sirve desde el propio origen, de modo que la CSP
  no se relaja (RNF-07).
- Condiciones representadas con distintivo de color + texto + icono:

| Condición | Color | Nota |
| --- | --- | --- |
| Operativo | verde | "Disponible" en el contexto de préstamos |
| Prestado | ámbar | |
| En mantenimiento | azul | |
| Dado de baja | gris | |
| Revisión pendiente | rojo | alerta sobre el bien devuelto con daño |

- Los estados vacíos explican y proponen: "Aun no hay equipos en esta
  coordinacion. Registre el primero con el boton Nuevo equipo."
- Las confirmaciones describen la consecuencia: "Se dara de baja el equipo
  Laptop Dell E5470. Dejara de aparecer en el inventario operativo, pero se
  conservara en el historial del equipo."

### 8.2 Vista del ADMINISTRADOR

**Menú:** Direcciones | Personas | Inventario | Préstamos | Panel

El menú tiene cinco entradas y no siete. Las categorías salieron de él porque
se administran desde el Inventario (RF-31), y Responsables y Operadores se
fundieron en Personas (RF-28): eran dos entradas para la misma pregunta, "quién
trabaja aquí", que además obligaba a responderla antes de poder buscar.

**Página principal:** Direcciones (ruta `/direcciones`)

Cada Dirección es una sección, y sus Coordinaciones se disponen dentro como
**cuadros en rejilla**. La versión 2.0 las listaba en franjas horizontales que
cruzaban toda la pantalla: el ojo tenía que recorrer la línea entera para saber
si esa unidad podía trabajar, y el usuario inexperto se perdía. En cuadros,
cada Coordinación se lee completa de un vistazo y varias se comparan de golpe,
que es exactamente lo que el Administrador viene a hacer.

```
INICTEL-UNI
+------------------------------------------------------------------+
| Direccion de Investigacion    [Editar]   [+ Nueva coordinacion]   |
|                                                                  |
|  +--------------------------+  +--------------------------+      |
|  | Coordinacion de Redes    |  | Coordinacion de Senales   |      |
|  | ------------------------ |  | ------------------------- |      |
|  | Responsable              |  | (!) Sin responsable       |      |
|  | Ana Diaz                 |  | Su inventario esta parado |      |
|  | ------------------------ |  | ------------------------- |      |
|  |  128       3        5    |  |   0       1        0      |      |
|  | equipos  labs   operad.  |  | equipos labs  operad.     |      |
|  | ------------------------ |  | ------------------------- |      |
|  | [120 operativos][8 prest]|  |                           |      |
|  | ------------------------ |  | ------------------------- |      |
|  |   Ver detalle y opciones |  |   Ver detalle y opciones  |      |
|  +--------------------------+  +--------------------------+      |
+------------------------------------------------------------------+
```

- El cuadro entero **se abre** (RF-15b): lleva solo lo que se compara de un
  vistazo, y sus acciones están dentro. Es un control alcanzable con el
  teclado, no un bloque con un clic encima (RNF-32).
- El borde superior del cuadro es verde institucional cuando la Coordinación
  puede operar y **rojo cuando no tiene Responsable**: la única condición que
  la bloquea, visible desde el otro extremo de la pantalla.
- Las tres cifras aparecen siempre en el mismo orden y en la misma posición, de
  modo que se comparan entre cuadros sin releer las etiquetas.
- La Coordinación no tiene sigla, así que el cuadro no la muestra: su nombre es
  como se la nombra (RF-11).
- Las Direcciones no se crean ni se desactivan: solo se corrigen sus datos con
  **Editar** (RN-30).

**Ficha de una coordinación (RF-15b)**

```
+--------------------------------------------------------------+
|  Coordinacion de Redes                                   [X]  |
|  Direccion de Investigacion y Desarrollo Tecnologico          |
|                                                              |
|  Responsable  Ana Diaz Vega      [ Dar baja al responsable ] |
|                                                              |
|  Equipos        128  [120 operativos] [8 prestados]          |
|  Operadores     5 operadores                                 |
|  Laboratorios   3 laboratorios (3 activos)                   |
|                                                              |
|  LABORATORIOS                        [ + Nuevo laboratorio ] |
|  Lab. de Redes - Pabellon A     12 equipos [edit][Desactivar]|
|  Lab. de Antenas                 4 equipos [edit][Desactivar]|
|  Lab. de Pruebas (Desactivado)   0 equipos [edit][Activar]   |
|                                                              |
|  [Editar datos] [Ver inventario] [Desactivar coordinacion]   |
|                                              [ Cerrar ]      |
+--------------------------------------------------------------+
```

- La acción del puesto de Responsable es la que corresponde a su estado: si lo
  tiene, **Dar baja al responsable**; si no, **Asignar responsable**, en rojo,
  que lleva a Personas con la tarea planteada. Nunca las dos: sustituir es
  hacer una y después la otra (RF-26).
- La baja pide confirmación y nombra lo que deja parado; hecha, el cuadro pasa
  a rojo en la misma pantalla y el saliente queda sin coordinación (RNF-26,
  RNF-31).
- Los laboratorios se administran aquí, y no en una ventana aparte: son de esta
  Coordinación y se miran cuando se mira esta Coordinación.
- **Una ventana a la vez**: al abrir un formulario o una confirmación, la ficha
  se aparta y vuelve al cerrarlos, ya con los datos recargados (RNF-26).

**Nueva coordinación (RF-11b, RF-11c)**

El formulario pide, en una sola pantalla, el **nombre** de la Coordinación, su
descripción opcional y el nombre de **su primer laboratorio**, con una
explicación de por qué hace falta. No pide sigla: la Coordinación no tiene
(RF-11). Al guardar, aparece en esta misma pantalla como un cuadro más, marcado
en rojo: le falta su Responsable, y así lo dice (RF-11c).

**Personas (`/personas`)**

Una sola tabla con todas las cuentas del sistema y un único botón de alta,
"Nueva persona". Cinco filtros la recorren: texto libre (nombre, DNI, usuario
o correo), **rol**, **puesto** (sin asignar / con puesto), Coordinación y
estado de la cuenta. Cada fila muestra el rol como distintivo de color y texto
—incluido **Sin asignar**—, de modo que la lista se lee entera sin filtrar
nada.

```
+--------------------------------------------------------------------+
| Personas                                          [+ Nueva persona] |
| Buscar [....] Rol [v] Puesto [v] Coord [v] Cuenta [v]               |
+--------------------------------------------------------------------+
| Persona        Rol             Usuario   Cuenta   Acciones          |
| Ana Diaz      [Responsable]    adiaz     Activa   [Ver detalle]     |
|  Coordinadora                                     [ Asignar ]       |
| Luis Rojas    [Operador]       lrojas    Activa   [Ver detalle]     |
|  Tecnico                                          [ Asignar ]       |
| Marta Paz     [Administrador]  mpaz      Activa   [Ver detalle]     |
|  Patrimonio                                       [ Asignar ]       |
| Sofia Campos  [Sin asignar]    scampos   Activa   [Ver detalle]     |
|  Practicante                                      [ Asignar ]       |
+--------------------------------------------------------------------+
```

La tabla lleva lo que sirve para **encontrar** a alguien. El DNI, el correo y
la coordinación salieron de ella y viven en la ficha, a un clic de **Ver
detalle** (RF-28f): son datos que se consultan de una persona concreta, no
recorriendo una lista, y ocupaban tres columnas que había que cruzar para leer
cada fila.

```
+--------------------------------------------------------------+
|  Ana Diaz Vega                                           [X]  |
|  [Responsable] [Cuenta activa]                                |
|                                                              |
|  DNI                     45871236                            |
|  Cargo                   Coordinadora de laboratorio         |
|  Correo institucional    ana.diaz@inictel-uni.edu.pe         |
|  Nombre de usuario       adiaz                               |
|  Coordinacion            Coordinacion de Redes               |
|  Ultimo acceso           12/08/2026 10:32                    |
|  Registrada el           03/03/2026                          |
|                                                              |
|  [Editar datos] [Restablecer contrasena] [Desactivar cuenta] |
|                                        [ Asignar puesto ]    |
+--------------------------------------------------------------+
```

En la fila quedan dos acciones: **Ver detalle** y **Asignar**, que abre la
ventana de puestos (RF-28d). **Editar**, **Restablecer contraseña**,
**Desbloquear** y **Activar o desactivar la cuenta** viven dentro de la ficha,
con los datos delante: son cosas que se hacen sobre una persona concreta, y
repetirlas en la fila era ofrecer dos caminos al mismo sitio. En la fila de
quien todavía no tiene puesto, **Asignar** es la acción primaria, porque es lo
único que le falta a esa persona para poder trabajar. Ninguna fila ofrece
eliminar, porque los usuarios no se eliminan (RN-09).

La pantalla no encabeza su contenido con ningún panel de tareas: las
Coordinaciones paradas se ven en Direcciones, en su propio cuadro rojo, que es
donde se administran (RF-28b). Al llegar desde una de esas tarjetas, la
pantalla entra en **modo encargo**: una barra recuerda qué puesto se está
cubriendo y las filas de quien no tiene ninguno pasan a ofrecer **Asignar
aquí**.

El formulario de alta pide solo quién es la persona (RF-16b). No pregunta rol
ni Coordinación: eso es el puesto, y se decide después. El **nombre de usuario
y el correo institucional se escriben enteros, a mano**: el sistema no los
compone a partir del nombre, porque son los que la institución dio a esa
persona y un dato adivinado parece correcto sin serlo (RF-16).

Al enviarlo, **el sistema no guarda todavía**: el formulario se aparta y en su
lugar aparece una ventana con los datos tal como van a quedar y con la frase
que dice qué le faltará (RF-16d). Una ventana a la vez, nunca una encima de
otra. Sus dos salidas son literales: **Aceptar** registra, **Cancelar** cancela
el registro y cierra el formulario.

```
+--------------------------------------------------------------+
|  Confirmar el registro                                        |
|  Se registrara a Sofia Campos Ruiz con estos datos:           |
|                                                              |
|  Nombre completo         Sofia Campos Ruiz                    |
|  DNI                     41200011                             |
|  Cargo                   Practicante de laboratorio           |
|  Correo institucional    sofia.campos@inictel-uni.edu.pe      |
|  Nombre de usuario       scampos                              |
|                                                              |
|  (i) Quedara registrada como Sin asignar y todavia no podra  |
|      entrar: asignele una coordinacion y su rol desde su     |
|      fila. Su contrasena nace en ese momento.                |
|                                                              |
|                            [ Cancelar ]     [ Aceptar ]      |
+--------------------------------------------------------------+
```

**El alta termina en el alta**: aceptada, la pantalla confirma que la persona
quedó registrada y repite en una frase qué le falta, pero no abre nada más. Su
fila ya está en la lista, marcada **Sin asignar** y con su botón **Asignar**
como acción primaria: ahí sigue hoy y seguirá mañana, que es justo lo que el
alta partida en dos vino a permitir (RF-16c).

**Primer ingreso del Administrador (RF-06b)**

Dos pasos cortos, y solo la primera vez: *sus datos* (nombres, primer apellido,
segundo apellido y DNI, todos obligatorios) y *su contraseña*. La cuenta
inicial llega con datos de relleno, así que el sistema no puede empezar a
operar con ellos.

El cargo aparece en la misma pantalla, pero como dato resuelto y no como campo:
"Administrador del sistema". Preguntarle su cargo a la primera persona de la
plataforma es ofrecerle una elección que no existe.

**Asignar un puesto (RF-28d)**

Una sola ventana, abierta desde el botón **Asignar** de la fila de la persona.
No hay asistente ni ruta propia: lo que hay que decidir son dos cosas, y caben
juntas.

```
+--------------------------------------------------------------+
|  Asignar puesto                                          [X]  |
|  Sofia Campos Ruiz - DNI 41200011 - scampos                   |
|                                                              |
|  (i) Sofia Campos Ruiz esta registrada pero todavia no tiene |
|      puesto. Su contrasena nace al asignarla.                |
|                                                              |
|  ROL EN EL SISTEMA *                                         |
|  (o) Responsable   A cargo del inventario de una coordina-   |
|                    cion. Responde por una sola.              |
|  ( ) Operador      Registra el dia a dia. Una sola.          |
|  ( ) Administrador Organiza la institucion. Ninguna.         |
|                                                              |
|  COORDINACION A SU CARGO *                                   |
|  [ Coordinacion de Senales - DIDT                         v ]|
|    Coordinacion de Redes - DIDT (a cargo de Ana Diaz)  <- en |
|                                            gris, no elegible |
|                                                              |
|  Sera la unica coordinacion de la que responda. Las que ya   |
|  tienen responsable no pueden elegirse: para cambiarlo,      |
|  primero se le da de baja desde Direcciones.                 |
|                                                              |
|                        [ Cancelar ]  [ Asignar puesto ]      |
+--------------------------------------------------------------+
```

Si la persona **ya tiene puesto**, la ventana no ofrece asignarle otro: muestra
el que tiene con su botón **Dar de baja** y explica que cada persona pertenece
a una sola Coordinación (RN-05).

El texto del botón dice exactamente lo que va a ocurrir: **Asignar puesto** o
**Nuevo administrador**. Pulsarlo abre una confirmación flotante que nombra
quién quedará como qué y dónde, y si va a generarse una contraseña. Aceptada,
la ventana muestra qué pasó y —solo si la persona estrena puesto— su contraseña
recién generada, para anotarla y entregarla en mano.

**Inventario (`/inventario`)**

Se exige primero elegir la Coordinación. Tabla en modo solo lectura: sin botón
de nuevo equipo, sin acciones de fila que modifiquen. Los filtros de condición
operan dentro de la Coordinación elegida.

### 8.3 Vista del RESPONSABLE

**Menú:** Inicio | Inventario | Préstamos | Mi equipo humano

**Página principal:** Panel de su Coordinación (`/panel`)

Tarjetas grandes con números y acceso directo: Disponibles | Prestados | En
mantenimiento | Dados de baja. Alertas destacadas: préstamos vencidos y equipos
con revisión pendiente tras una devolución con daño.

**Inventario (`/inventario`)**

```
+--------------------------------------------------------------+
| Inventario - Coordinacion de Redes            [+ Nuevo equipo]|
| [Todos] [Operativos] [Mantenimiento] [Dados de baja][Prestados]|
| Buscar: [.................]  Categoria [v]  Laboratorio [v]   |
+--------------------------------------------------------------+
```

La pestaña **"Todos" viene activa al entrar** (V39-03): la pantalla empieza
enseñando el inventario entero y las demás pestañas acotan desde ahí. Cada fila
lleva a la ficha del bien, que es donde viven Editar, Mantenimiento y Dar de
baja según corresponda a la condición actual.

**Ficha del bien (`/inventario/:id`)**

```
+--------------------------------------------------------------+
| Espectrografo RESONON                                        |
| Resonon PIKA IR+ - Coordinacion de Redes    [Operativo]      |
|  [< Volver] [Registrar salida] [Mantenimiento] [Baja] [Editar]|
+--------------------------------------------------------------+
| Datos del equipo          | Historial del equipo             |
|   Responsable del equipo: |  (alta, mantenimiento, baja,     |
|     Ana Torres  [Cambiar] |   cambio de responsable...)      |
|   Fecha de adquisicion    |                                  |
| Fotografia                |                                  |
| Prestamos de este equipo  |                                  |
|   ... fila ... [Ver detalle]                                 |
+--------------------------------------------------------------+
```

**Registrar salida** abre el formato de registro de uso del laboratorio,
lo enseña en PDF y lo descarga (RF-78). Es exclusivo del Responsable y **no
registra el préstamo**: la propia ventana lo recuerda (RN-36, RF-79). En la
tabla de préstamos, **Ver detalle** abre las observaciones de entrega y de
devolución y dice quién prestó a quién (RF-66b). La línea de tiempo de la
derecha ya no repite esos préstamos ni muestra las ediciones del bien
(RF-56).

La **fotografía** se ve y se amplía en la ficha, pero **no se cambia aquí**:
eso se hace en *Editar*, con el resto de los datos y con su confirmación
(RF-51f). Ahí, la sección de fotografía ofrece la cámara del dispositivo o un
archivo, y no elegir nada conserva la que el equipo ya tiene.

**Responsable del equipo** encabeza los datos del bien, con el nombre de quien
lo tiene hoy y el botón **Cambiar** al lado (RF-83). La ventana ofrece, en este
orden, *a mi cargo como responsable de la coordinación* —que es donde nace todo
bien— y después los Operadores activos de la Coordinación, uno por línea. Al
pie recuerda la consecuencia que nadie espera hasta encontrarse con ella: que
mientras un operador tenga equipos a su nombre no se le podrá dar de baja ni
cambiar de coordinación (RN-38).

**Mi equipo humano (`/mi-equipo`)**

La tabla de los Operadores de la Coordinación, con su alta, su edición y su
desactivación, y nada más. Ni tarjeta con los datos del propio Responsable
—quien mira la pantalla es esa misma persona, y sus datos viven en *Mi cuenta*
(RF-28f)— ni encabezado "Operadores" sobre una tabla de operadores: el título
de la pantalla ya lo dice, y repetirlo dos renglones más abajo es ocupar sitio
para no decir nada (RNF-22). Queda el recuento, que sí informa.

### 8.4 Vista del OPERADOR

**Menú:** Inicio | Inventario | Préstamos

**Página principal:** Inicio (`/panel`), deliberadamente mínimo

```
+--------------------------------------------------------------+
|  Coordinacion de Redes                                       |
|                                                              |
|   +------------------------+  +------------------------+     |
|   |   REGISTRAR EQUIPO     |  |   REGISTRAR PRESTAMO   |     |
|   |   Agregar un bien      |  |   Entregar un equipo   |     |
|   +------------------------+  +------------------------+     |
|                                                              |
|   42 disponibles     7 prestados     3 registrados por usted |
+--------------------------------------------------------------+
```

Dos botones grandes, sin ambigüedad, que cubren el 100% de lo que el Operador
puede hacer. Sin menús secundarios ni opciones que terminen en un error de
permisos.

**Cuando su Coordinación no tiene Responsable (RF-77b)**

```
+--------------------------------------------------------------+
|  Coordinacion de Redes                                       |
|                                                              |
|   +------------------------+  +------------------------+     |
|   |   REGISTRAR EQUIPO     |  |   REGISTRAR PRESTAMO   |     |
|   |       (apagado)        |  |       (apagado)        |     |
|   +------------------------+  +------------------------+     |
+--------------------------------------------------------------+
```

Los dos botones quedan apagados: un control que responde con un error es peor
que uno que no responde (RNF-23). Hasta la v3.7 los precedía un aviso que
explicaba el motivo; se retiró con los demás recuadros de prosa (V38-02), de
modo que el panel deja el hecho a la vista sin nombrar la causa. El servidor
rechaza igualmente la operación si llega por otra vía (RF-04).

**Registro de equipo: tres pantallas (V38-03)**

- **Paso 1** — *Identificación y códigos*: nombre, marca, modelo y categoría,
  y debajo serie, código de inventario y código patrimonial, con la ayuda en
  línea sobre dónde encontrarlos y el comodín S/N. Los dos grupos conservan su
  cabecera y su línea divisoria dentro del mismo paso.
- **Paso 2** — *Adquisición, ubicación y fotografía*: año y costo; laboratorio
  (opcional) y observaciones; y la fotografía, opcional, que en la edición no
  aparece porque allí se cambia desde la ficha del bien (RF-51b).
- **Paso 3** — *Resumen*, solo: los once campos tal como van a guardarse.

Hasta la v3.7 eran seis pantallas para once datos, la mitad de ellas con dos
campos: avanzar cuatro veces sin decidir nada no es un paso corto, es un paso
vacío. Lo que RNF-28 pide es que ninguna pantalla abrume, y ninguno de los dos
pasos actuales llena la vista de una tablet.

Cada paso se valida al salir de él, no al final (RNF-25): el primero exige sus
siete campos obligatorios y el segundo solo el año y el costo, porque
laboratorio, observaciones y fotografía son opcionales. Cuando el servidor
rechaza un dato, el formulario vuelve al paso donde vive ese campo.

Confirmación clara al terminar: "Equipo registrado. Quedó en condición
Operativo." La condición no se pregunta: se informa.

**Paso de fotografía (RF-51b)**

El Operador registra el equipo con una tablet, de pie frente al equipo. Por eso
el paso ofrece las dos vías con el mismo peso visual y ninguna letra pequeña:

```
+--------------------------------------------------------------+
|  Fotografia del equipo            (seccion del paso 2 de 3)   |
|                                                              |
|              +--------------------------+                    |
|              |                          |                    |
|              |   visor de la camara     |                    |
|              |                          |                    |
|              +--------------------------+                    |
|                                                              |
|      [ Tomar foto ahora ]  [ Elegir una imagen guardada ]    |
|                                                              |
|   Imagen JPG, PNG o WEBP de hasta 5 MB. Puede anadirla o     |
|   cambiarla mas adelante desde la ficha del equipo.          |
|                                                              |
|                      [ Atras ]  [ Continuar ]                |
+--------------------------------------------------------------+
```

- "Tomar foto ahora" abre el visor en vivo y lo sustituye por la imagen
  congelada al pulsar "Capturar"; desde ahí se puede quitar o repetir.
- "Continuar" está siempre activo: el paso se salta sin escribir nada.
- Si la cámara no está disponible o el permiso se deniega, el botón principal
  conserva su sitio y su nombre, y pasa a abrir la aplicación de cámara del
  dispositivo: el usuario nunca se queda mirando un botón que no responde.

**Préstamos (`/prestamos`)**

Lista solo los equipos etiquetados "Disponible". Formulario corto: equipo,
persona, DNI, destino, fecha estimada y observaciones. La devolución se
registra con un checklist de dos opciones claras: "Conforme, completo y en buen
estado" o "Con observaciones", que despliega obligatoriamente el detalle.

### 8.5 Página 404

Ruta no reconocida → página propia con el título "Pagina no encontrada", una
explicación breve en español y un único botón primario "Volver al inicio" que
dirige a la página principal correspondiente al rol del usuario autenticado, o
al acceso si no hay sesión.

---

## 9. Mapa de rutas

### 9.1 Rutas del frontend

| Ruta | Descripción |
| --- | --- |
| `/acceso` | Inicio de sesión |
| `/cambiar-password` | Cambio obligatorio en el primer ingreso |
| `/panel` | Panel según rol |
| `/direcciones` | Direcciones, coordinaciones y laboratorios (ADMIN). `/estructura` redirige aquí |
| `/direcciones/:id` | Detalle de una coordinación (ADMIN) |
| `/personas` | Todas las personas del sistema, con filtro por rol y por puesto (ADMIN). Aquí se registra, se consulta la ficha de cada una y se asignan los puestos. La baja del Responsable no está aquí: está en la tarjeta de su coordinación, en `/direcciones` (RF-26). `/responsables`, `/operadores` y `/responsables/:id/relevo` redirigen aquí |
| `/personas?asignarA=:coordinacion&rol=RESPONSABLE` | La misma pantalla, en modo encargo: se llega desde una tarjeta de Direcciones (RF-28b) |
| `/mi-equipo` | Integrantes de la coordinación (RESPONSABLE) |
| `/inventario` | Listado de bienes |
| `/inventario/nuevo` | Registro de un bien |
| `/inventario/:id` | Ficha e historial del bien |
| `/inventario/:id/editar` | Edición del bien (RESPONSABLE) |
| `/prestamos` | Préstamos activos e historial |
| `/prestamos/nuevo` | Registro de una salida |
| `/categorias` | Catálogo de categorías (ADMIN). Se llega desde el Inventario |
| `/cuenta` | Datos de la propia cuenta |
| `/404` | Página no encontrada |

### 9.2 Rutas de la API

| Ruta | Descripción |
| --- | --- |
| `/api/auth/**` | Login, logout, sesión, primer ingreso y cambio de password |
| `/api/organizacion/**` | Direcciones (lectura y edición; sin alta ni desactivación), coordinaciones y laboratorios |
| `/api/usuarios/**` | Personas, sus puestos y sus credenciales. `POST /api/usuarios` registra; `POST /api/usuarios/con-puesto` registra y asigna en una sola transacción, que es el alta del Responsable a un Operador suyo (RF-16e); `POST /api/usuarios/{id}/asignaciones` asigna el puesto, y lo rechaza si el de Responsable ya está ocupado o si la persona ya tiene otro; `DELETE /api/usuarios/{id}/asignaciones/{coordinacionId}` da de baja ese puesto; `PATCH /api/usuarios/{id}/estado` lleva la cuenta a ACTIVA, SUSPENDIDA o BAJA (RF-22b); `GET /api/usuarios/coordinacion/{id}/candidatos-responsable` lista a quienes pueden hacerse cargo y `POST /api/usuarios/coordinacion/{id}/responsable` ejecuta el relevo entero en una transacción (RF-26b) |
| `/api/categorias/**` | Catálogo de categorías |
| `/api/equipos/**` | Bienes, condición, baja e historial. `PUT /api/equipos/{id}/responsable-equipo` asigna o retira el operador que tiene el bien a su cargo, y es exclusivo del Responsable (RF-83) |
| `/api/prestamos/**` | Salidas y devoluciones |
| `/api/reportes/**` | Panel, exportación y `POST /api/reportes/equipos/{id}/formato-uso`, que devuelve el formato de registro de uso en PDF y no guarda nada (RF-78, RN-36) |
| `/api/archivos/**` | Fotografías de los bienes |
| `/actuator/health` | Sonda de estado del contenedor. Pública, y la única del actuator que se expone; responde si la aplicación está en pie, sin detalle de la base ni del pool (RNF-44) |

---

## 10. Criterios de aceptación

### Autorización y aislamiento

- Ningún endpoint responde datos sin token JWT válido.
- Un usuario que solicita un bien, préstamo o historial de otra Coordinación
  recibe 403 o 404, nunca los datos.
- Un ADMIN que intenta crear, editar, dar de baja o prestar un bien recibe 403.
- Un OPERADOR que intenta editar o dar de baja un bien recibe 403.
- Un usuario desactivado que intenta autenticarse recibe el mensaje de cuenta
  inactiva y ningún token.

### Estructura y roles

- Un sistema recién instalado tiene exactamente dos Direcciones, DIDT y DCTT,
  con su sigla y su descripción, sin que nadie las escriba.
- No existe ningún endpoint que cree una Dirección ni que la desactive; la
  pantalla no ofrece "Nueva dirección", "Desactivar" ni "Mostrar desactivadas".
- Una Coordinación desactivada sigue viéndose en la pantalla de Direcciones,
  con su distintivo y su botón de activar.
- Las personas de los tres roles se ven en una sola pantalla, junto a las que
  aún no tienen ninguno; el filtro de rol y el de puesto las acotan y cada fila
  dice qué rol tiene quién.
- El catálogo de categorías se alcanza desde el Inventario y no desde el menú.
- Una persona recién registrada queda con rol vacío, sin ninguna Coordinación y
  sin contraseña: aparece en la lista como "Sin asignar" y no puede entrar.
- El formulario de alta no guarda al enviarse: abre una ventana con los datos
  escritos y dos salidas. Cancelar no registra nada; aceptar registra.
- Terminado el alta no se abre ninguna otra ventana: el sistema solo confirma
  el registro y enuncia que hay que asignarle una Coordinación y su rol.
- El registro de una persona no exige que exista ninguna Coordinación; su
  asignación, sí.
- La fila de la lista no muestra el DNI ni la coordinación; "Ver detalle" abre
  la ficha, y allí están, junto al correo, las fechas y el botón "Editar".
- Asignar un puesto a quien lo estrena devuelve una contraseña temporal,
  distinta en cada asignación; asignar un puesto a quien ya tenía credenciales
  no devuelve ninguna.
- No es posible asignar a nadie —responsable u operador— una segunda
  Coordinación: la operación falla y explica que antes hay que darle de baja la
  que tiene. La base lo garantiza además con un índice único.
- No es posible nombrar administrador a quien pertenece a alguna Coordinación.
- No es posible asignar ningún puesto a quien ya tiene uno: la operación se
  rechaza nombrando el que tiene y diciendo dónde se le da de baja. Vale también
  cuando quien lo tiene es un Administrador, que no ocupa Coordinación.
- Un Administrador no puede asignarse un puesto a sí mismo: el servidor lo
  rechaza y su propia fila no ofrece la acción.
- Dada la baja del puesto, la misma persona vuelve a poder recibir uno.
- Una cuenta **suspendida** no puede entrar y conserva su rol y su Coordinación;
  al reactivarla, vuelve a su puesto tal como lo dejó.
- Una persona **dada de baja** no puede entrar y queda sin rol y sin
  Coordinación en el mismo acto; su historial en equipos y préstamos se
  conserva. Al reincorporarla vuelve con su cuenta y **sin** puesto.
- El Administrador puede suspender y dar de baja a responsables, operadores y
  otros administradores, pero no a sí mismo, y el sistema rechaza cualquiera de
  las dos sobre el **último Administrador activo**, igual que rechaza
  nombrarlo responsable de una coordinación.
- El Responsable puede suspender y dar de baja únicamente a los **Operadores**
  de su Coordinación; sobre cualquier otro rol recibe 403.
- **Cambiar responsable** ofrece exactamente tres perfiles: personas sin puesto,
  operadores de esa misma Coordinación y administradores. Quien ya es
  Responsable de otra no aparece, y el servidor lo rechaza si llega por otra
  vía.
- Ejecutado el cambio, la Coordinación tiene al nuevo Responsable y el saliente
  queda sin rol y sin Coordinación, con su cuenta activa, sin que la
  Coordinación haya quedado parada en ningún momento.
- Si quien toma el puesto no tenía ninguno, el sistema devuelve su contraseña
  temporal una sola vez; si ya tenía credenciales, no devuelve ninguna.
- La ventana de asignación ofrece los tres roles y, para los operativos, el
  selector con **todas las Coordinaciones activas** que existen en ese momento,
  incluida una creada mientras la pantalla estaba abierta.
- Una consulta de coordinaciones rechazada no deja el selector vacío el resto
  de la sesión: la siguiente vez que se pide, se vuelve a preguntar al
  servidor.
- La pantalla de personas no muestra ningún panel de "Coordinaciones sin
  responsable"; esa alerta vive en el cuadro de la Coordinación, en Direcciones.
- Ni el nombre de usuario ni el correo institucional se autocompletan a partir
  del nombre: los dos campos quedan vacíos hasta que se escriben enteros.
- La ventana de asignación de una persona recién registrada muestra los tres
  roles y, al elegir uno operativo, su selector de Coordinaciones: no la
  confunde con alguien que ya tiene puesto, aunque su rol llegue ausente del
  JSON en lugar de nulo.
- No es posible registrar un segundo RESPONSABLE activo en una Coordinación: la
  asignación se rechaza nombrando a quien ocupa el puesto, y el selector no
  deja elegir esa Coordinación.
- El cuadro de una Coordinación se abre con un clic y con el teclado, y su
  ficha trae los laboratorios, las cifras y las acciones; no existe una ventana
  suelta de laboratorios.
- La ficha de una Coordinación con responsable ofrece "Dar baja al
  responsable" y no "Asignar"; la que no lo tiene, al revés.
- Crear una Coordinación no pide sigla, y no existe columna `sigla` en la tabla
  `coordinacion`.
- No es posible registrar ni editar una persona sin su segundo apellido: el
  formulario no deja continuar y el servidor lo rechaza con el mensaje junto al
  campo.
- En la lista de personas, la fila ofrece "Ver detalle" y "Asignar", y ninguna
  otra acción.
- Dar de baja al responsable pide confirmación, funciona aunque la Coordinación
  tenga equipos, deja al saliente sin coordinación y con su cuenta activa, y
  deja la tarjeta marcada en rojo.
- Con la Coordinación sin responsable, registrar un equipo, una salida o una
  devolución se rechaza con un mensaje que dice por qué, y los dos accesos
  directos del panel del Operador aparecen apagados. La consulta del inventario
  sigue funcionando.
- Nombrado el sucesor, la Coordinación vuelve a tener exactamente un
  RESPONSABLE y a poder registrar y prestar.
- No existe ningún endpoint de relevo ni de búsqueda por DNI; la ruta
  `/responsables/:id/relevo` redirige a `/personas`.
- Dar de baja el puesto de alguien lo devuelve al estado "Sin asignar" sin
  desactivar su cuenta.
- Suspender a un Responsable no le quita el puesto: su Coordinación sigue
  teniendo responsable y sigue operando. Darlo de baja sí se lo quita, y
  entonces la Coordinación queda parada hasta que se nombre a otro.
- No existe ningún endpoint que elimine físicamente un usuario.
- No es posible crear una Coordinación sin indicar su primer Laboratorio.
- Tras crear una Coordinación, el sistema permanece en la pantalla de
  Direcciones y la nueva tarjeta aparece marcada como sin responsable.
- No es posible desactivar el único Laboratorio activo de una Coordinación.
- El Administrador de la cuenta inicial no puede operar el sistema hasta
  registrar sus nombres, sus dos apellidos y su DNI reales junto con su
  contraseña; su cargo queda fijado en "Administrador del sistema" sin que se
  le pregunte.

### Inventario

- Un bien recién registrado queda en condición OPERATIVO y con la fecha y hora
  del servidor.
- El responsable del bien es el RESPONSABLE vigente de la Coordinación, aunque
  lo haya registrado un OPERADOR.
- No es posible registrar dos bienes con el mismo número de serie, código de
  inventario o código patrimonial en toda la institución.
- No es posible asignar a un bien un Laboratorio de otra Coordinación.
- El listado de inventario muestra por defecto solo los operativos y los
  filtros de condición funcionan sobre la Coordinación del usuario.
- Un equipo registrado por el Responsable de una Coordinación aparece en el
  listado del Administrador en cuanto este elige esa Coordinación, sin recargar
  el navegador, y con las acciones de escritura ausentes (RF-46, RF-49, RN-22).
  Lo mismo vale para los préstamos de esa Coordinación (RF-68).
- Una fecha de adquisición futura o un costo negativo son rechazados con mensaje
  en línea.
- El registro de un bien puede completarse sin fotografía: el paso de
  fotografía se salta y el alta termina igual.
- Con una fotografía elegida, el bien queda registrado y con su imagen
  visible en la ficha sin ninguna acción adicional.
- Si el envío de la fotografía falla, el bien sigue registrado y el sistema
  avisa que la fotografía quedó pendiente.
- Denegar el permiso de cámara no bloquea el paso: el sistema lo explica y
  ofrece la alternativa de elegir o capturar un archivo.

### Préstamos

- La ventana de préstamos solo ofrece bienes en condición OPERATIVO,
  etiquetados "Disponible".
- Un bien PRESTADO no puede prestarse nuevamente.
- Un bien PRESTADO no puede darse de baja ni pasar a mantenimiento.
- La devolución exige declarar conformidad; si no es conforme, la observación
  es obligatoria.
- Una devolución con daño deja el bien marcado para revisión y solo el
  RESPONSABLE puede confirmarlo en MANTENIMIENTO.

### Historial e inmutabilidad

- Cada cambio de condición genera un movimiento con su fecha y hora.
- No existe ningún endpoint que actualice o elimine un movimiento del historial
  de un bien; la base de datos lo impide además por disparador.
- Ninguna petición puede fijar una fecha de alta, préstamo, devolución o baja:
  el servidor ignora cualquier fecha enviada por el cliente.




### Fotografía, reportes en blanco y negro y campos de importe (v3.11)

- Un Operador que llame a `POST /api/equipos/{id}/foto` recibe **403**, tenga el
  equipo fotografía o no y sea o no de su coordinación.
- La ficha del bien **no ofrece subir ni cambiar** la fotografía a nadie: la
  muestra, la amplía a tamaño completo y, al Responsable, le dice que se cambia
  desde *Editar*.
- El formulario de **Editar** de un equipo, abierto por el Responsable, ofrece
  la fotografía con las dos vías: cámara del dispositivo y archivo. La elegida
  se ve en el resumen y **no se envía hasta confirmar** el guardado.
- Guardar la edición **sin tocar la fotografía** conserva la que el equipo tenía;
  el resumen lo dice con esas palabras.
- Si la edición se guarda bien pero la fotografía falla, el aviso lo distingue:
  el equipo quedó actualizado y la imagen no.
- En el **alta**, el paso de fotografía y su rótulo aparecen solo si quien
  registra es el Responsable.
- Los reportes de inventario en **Excel, CSV y PDF** no contienen ningún color:
  ni azul de marca, ni cabecera con fondo oscuro, ni filas alternas sombreadas.
- El **formato de registro de salida** tampoco: sus bandas de sección son texto
  negro con línea inferior, y ninguna celda lleva relleno.
- Los tres reportes de inventario encabezan con **Sede Central INICTEL-UNI**, el
  nombre de la **Dirección** y el de la **Coordinación**, seguidos de qué es el
  documento y de cuándo se generó. En el CSV, cada línea del encabezado ocupa su
  propia fila.
- El Administrador que exporta sin elegir coordinación obtiene *Todas las
  direcciones y coordinaciones*, no un hueco en blanco.
- El formato de registro de salida nombra la **Dirección**, la Coordinación y el
  laboratorio del equipo.
- En **Costo en soles**, el símbolo `S/` queda a la izquierda del campo y el
  importe empieza después de él, sin montarse. Un campo con error conserva su
  borde rojo y el campo enfocado, su anillo.

### Fecha de adquisición y responsable del equipo (v3.10)

- El alta de un bien pide la **fecha** de adquisición en un calendario acotado
  entre el 01/01/1980 y hoy. Una fecha futura se rechaza en el formulario y
  también en el servidor, aunque llegue por otra vía.
- La ficha, el listado, el resumen del alta, la exportación y el formato de
  registro de uso muestran la fecha completa en `dd/MM/yyyy`, no el año.
- El formato de registro de uso imprime en el punto 2 la fecha de la orden de
  compra tal como el papel la pide.
- Un bien recién registrado —también por un Operador— queda **a cargo del
  Responsable** de la coordinación, y la ficha lo dice con esas palabras.
- El botón **Cambiar** del responsable del equipo aparece solo para el
  Responsable, y no aparece en un bien dado de baja.
- La ventana ofrece únicamente **operadores activos de esa coordinación**. Un
  operador suspendido o dado de baja no figura, y una petición hecha a mano con
  su identificador —o con el de alguien de otra coordinación— se rechaza.
- Un Operador que llame al endpoint recibe 403; el Administrador, también.
- Cada cambio deja un movimiento `RESPONSABLE` en el historial del bien, y la
  línea de tiempo de la ficha lo muestra con la frase de quién a quién.
- **Un operador con equipos a su cargo no se puede dar de baja ni retirar de su
  coordinación**: las dos operaciones se rechazan diciendo cuántos equipos son
  y qué hay que hacer antes. Tras reasignarlos, las dos funcionan.
- **Suspender** a ese mismo operador sí se permite: conserva su puesto y sus
  equipos.
- Dar de baja un bien lo suelta: su operador deja de tenerlo a su cargo y, si
  era el único, ya puede dejar el puesto.
- Ascender a Responsable a un Operador que tenía equipos a su nombre funciona
  en un solo acto: sus equipos quedan a cargo del Responsable —que es él
  mismo— y la operación no falla por la llave foránea.
- Los campos **Teléfono de contacto**, **Celular** y **Hora** del formato de
  registro de uso tienen la misma altura, el mismo borde, la misma tipografía y
  el mismo ancho que los demás campos de su fila.

### Alta atómica, listados, ficha del bien y formato de uso (v3.9)

- El Responsable registra un operador y, si el servidor rechaza cualquier parte
  de la operación, **no queda nada**: ni la persona registrada sin puesto, ni el
  DNI, el correo o el nombre de usuario ocupados. Repetir el alta con los
  mismos datos funciona.
- Un DNI, un correo o un nombre de usuario ya registrados se señalan **en su
  campo** dentro del formulario, que sigue abierto con lo escrito, y no solo en
  un aviso flotante.
- La ventana de confirmación del alta dice, antes de aceptar, que la persona
  quedará como operador de esa coordinación y que se generará su contraseña.
- El inventario abre por la pestaña **Todos** y los préstamos también; en las
  dos pantallas "Todos" es la primera de la fila y el resto conserva su orden.
- La línea de tiempo de la ficha de un equipo prestado y devuelto **no muestra**
  esos dos movimientos, ni las ediciones del bien; el recuento del encabezado
  cuenta lo que se ve. Los movimientos siguen registrados: el endpoint del
  historial los devuelve todos.
- Cada fila de "Préstamos de este equipo" ofrece **Ver detalle**, y allí se leen
  quién entregó, a quién, las observaciones de entrega, quién recibió la
  devolución, si fue conforme y las observaciones de retorno. Un préstamo aún
  abierto lo dice en lugar de mostrar una devolución vacía.
- El botón **Registrar salida** aparece en la ficha del equipo solo para el
  **Responsable**. Un Operador o un Administrador que llamen al endpoint a mano
  reciben 403, y también un Responsable que pida el formato de un bien de otra
  Coordinación (RN-23).
- El formato generado trae impresos, sin haberlos escrito nadie: descripción,
  código patrimonial, código de inventario, marca, modelo, número de serie,
  valor de compra, fecha de adquisición, área y **la condición actual del bien**,
  además de la fecha y la hora de emisión como inicio de uso.
- El PDF **se muestra antes de descargarse**, y desde la vista previa se puede
  volver al formulario sin perder lo escrito.
- Tras emitir el formato: el equipo **conserva su condición**, su historial no
  tiene ningún movimiento nuevo, no aparece ningún préstamo y ninguna tabla ha
  crecido (RN-36). La pantalla lo advierte al mostrar el documento y al
  descargarlo, y el propio PDF lo lleva impreso al pie.
- La cabecera Content-Security-Policy y la etiqueta `<meta>` declaran
  `frame-src 'self' blob:` y siguen sin `'unsafe-inline'` y sin `'unsafe-eval'`
  en ninguna directiva; la prueba de regresión lo comprueba.

### Idioma, presentación y despliegue (v3.8)

- Ninguna pantalla escribe "Contrasena", "Gestion", "Coordinacion", "Prestamos"
  ni "Categoria": el texto visible lleva sus tildes y sus eñes, y también los
  datos que el sistema trae puestos —las dos Direcciones y las ocho categorías
  salen de `V2` acentuadas—.
- El nombre de un campo del código no cambia por acentuar su rótulo: la
  cabecera dice `CONDICIÓN` y la propiedad sigue siendo `equipo.condicion`.
- No existe ningún `<div class="aviso">` en las plantillas salvo los dos que
  muestran el error devuelto por el servidor. Los estados vacíos, la validación
  junto al campo y las ventanas de confirmación siguen en su sitio.
- El registro de un equipo se completa en tres pantallas: identificación y
  códigos, adquisición con ubicación y fotografía, y resumen. En la edición son
  también tres, y la de fotografía no aparece.
- Las barras de los paneles miden lo que representan: la serie mayor llena la
  pista y las demás guardan su proporción.
- La acción principal está arriba a la derecha en todas las pantallas, también
  cuando la descripción ocupa dos renglones.
- En el listado de inventario ningún código se parte en dos líneas, y la
  columna de acciones queda visible aunque la tabla sea más ancha que la
  pantalla.
- `docker compose up -d` levanta los tres servicios y los tres quedan sanos;
  solo el frontend publica puerto.
- La respuesta del HTML y la de cualquier recurso estático llevan
  Content-Security-Policy sin `'unsafe-inline'` ni `'unsafe-eval'`, más
  X-Frame-Options, Referrer-Policy, Permissions-Policy y HSTS.
- Un `.env` sin la clave de la base, la del JWT o la de la cuenta inicial
  detiene el arranque nombrando la que falta.
- Si `APP_CORS_ORIGENES_PERMITIDOS` no coincide con el origen del navegador,
  puerto incluido, el acceso responde 403 y nadie entra: el despliegue no se da
  por bueno sin comprobar el ingreso **desde un navegador**, porque una prueba
  con curl no lleva cabecera `Origin` y pasa igualmente.

### Interfaz y navegación

- La política CSP servida no contiene `'unsafe-inline'` ni `'unsafe-eval'`.
- No existe ningún atributo style ni manejador de evento en línea en el HTML
  del frontend; hay una prueba automática que lo verifica.
- Una ruta inexistente muestra la página 404 con el botón de regreso a la
  página principal del rol.
- Cada rol ve únicamente las opciones de menú que puede ejercer.
- El encabezado nombra la coordinación del usuario y no ofrece elegir otra.
- Un ingreso satisfactorio abre el saludo con el rol, la Coordinación y la
  Dirección de quien entra, y se cierra con un solo botón. Recargar la página
  no lo repite.
- Ninguna acción de las enumeradas en RNF-26 se ejecuta con un solo clic:
  todas abren antes una ventana que describe la consecuencia y ofrece
  **Cancelar**.
- En ningún momento hay dos ventanas superpuestas: al abrirse una confirmación,
  la ventana que la pidió deja de mostrarse.
- Ninguna petición del cliente lleva cabecera `X-Coordinacion`, y el servidor
  no la lee: el ámbito lo resuelve él con la coordinación asignada.
- Un cambio hecho desde otra sesión —un equipo prestado, un responsable dado de
  baja, un puesto asignado— aparece en la pantalla abierta sin que el usuario
  recargue el navegador, y al volver a la pestaña aparece de inmediato.
- La recarga automática no enciende el indicador de carga, no muestra avisos de
  error, no cierra ninguna ventana abierta y no se produce con la pestaña
  oculta.

---

## 11. Fases de implementación

| Fase | Contenido |
| --- | --- |
| Fase 1 | Esquema nuevo (Flyway) y contexto de organización: Direcciones, Coordinaciones y Laboratorios. |
| Fase 2 | Identidad: tres roles, adscripción, reglas de responsable único, cambio de responsable con relevo, activación y desactivación. |
| Fase 3 | Inventario por Coordinación con el nuevo modelo de bien y el historial inmutable de movimientos. |
| Fase 4 | Préstamos y devoluciones con aislamiento por Coordinación. |
| Fase 5 | Panel por rol y exportación. |
| Fase 6 | Frontend por segmento objetivo, rutas concisas, 404 y CSP estricta verificada. |
| Fase 7 | Endurecimiento de concurrencia (300 usuarios), índices y pool. |
| Fase 8 | Despliegue institucional con Docker, HTTPS y respaldos. |
| Fase 9 (v3.0) | Supresión de la bitácora de acciones, primer ingreso ampliado del Administrador, laboratorio obligatorio por Coordinación, flujo guiado de creación y asignación, y estructura en tarjetas. |
| Fase 10 (v3.1) | Fotografía opcional en el registro del bien, con captura desde la cámara del dispositivo. |
| Fase 11 (v3.2) | Direcciones precargadas y sin alta ni desactivación, catálogo de categorías dentro del Inventario, y gestión de personas unificada en una sola pantalla con filtro por rol. |
| Fase 12 (v3.3) | Alta partida en registro previo y asignación de puesto, contraseña que nace con el puesto, Responsable a cargo de varias Coordinaciones, y relevo absorbido por la asignación. |
| Fase 13 (v3.4) | Una sola Coordinación por persona, baja explícita del Responsable en la tarjeta de su Coordinación, rechazo de la asignación sobre un puesto ocupado, ficha de la persona con su edición dentro, y confirmación flotante de toda acción importante. |
| Fase 14 (v3.5) | Ficha de la Coordinación tras abrir su tarjeta, Coordinación sin sigla, segundo apellido obligatorio, Coordinación sin Responsable parada del todo con aviso en el panel del Operador, una ventana a la vez, saludo de bienvenida y esquema consolidado en dos migraciones. |
| Fase 15 (v3.6) | El puesto se asigna solo a la persona libre y nunca a uno mismo; refresco automático de las pantallas y del puesto del usuario, sin recarga manual del navegador; la alerta de Coordinación sin Responsable queda solo en Direcciones; el nombre de usuario y el correo se escriben a mano; el selector de Coordinaciones deja de quedarse vacío por un fallo memorizado; la persona sin puesto se normaliza al entrar, para que "sin rol" no se confunda con "con rol"; "Mi equipo humano" queda con sus operadores y nada más; el Administrador vuelve a ver el inventario y los préstamos de la Coordinación que elige; y la pantalla de acceso estrena la fotografía de la institución como fondo. |
| Fase 16 (v3.7) | Tres estados de cuenta —activa, suspendida y de baja—, con la suspensión que conserva el puesto y la baja que lo libera; suspensión y baja al alcance del Administrador sobre cualquiera y del Responsable sobre sus Operadores, con el último Administrador activo protegido; y el cambio de Responsable como un solo acto dentro de la Coordinación, que libera al saliente sin pararla. |
| Fase 17 (v3.8) | La interfaz escrita en español con sus tildes; los recuadros de prosa retirados de todas las pantallas; el registro de un equipo en tres pasos en lugar de seis; las barras de los paneles, que llevaban vacías, mostrando por fin su proporción; la acción principal fija arriba a la derecha y la tabla del inventario legible sin cortar los códigos; y el despliegue con Docker Compose —base de datos, backend y frontend—, con nginx sirviendo la aplicación, reenviando la API y emitiendo las cabeceras de endurecimiento. |

---

## 12. Estado de la implementación

La versión 3.11 está **implementada en su totalidad** sobre la 3.10. No toca el
esquema, no añade ni retira endpoints —uno cambia de rol permitido— y no
incorpora ninguna dependencia.

| Cambio | Dónde vive |
| --- | --- |
| V311-01 La fotografía, del Responsable | Backend: `EquipoController` pasa el endpoint de la foto de `hasAnyRole('RESPONSABLE','OPERADOR')` a `hasRole('RESPONSABLE')`, y `GestionInventarioServicio.asignarFoto` cambia `exigirOperativoDe` por `exigirResponsableDe`, que es el mismo guardián de editar, mantenimiento y baja. La anotación no es la única defensa: el servicio comprueba además la Coordinación del bien (RN-23) |
| V311-02 La fotografía, en Editar | Frontend: `formulario-bien` muestra la sección de fotografía también en la edición y `guardar()` adjunta la imagen siempre que haya una elegida, no solo en el alta; `nombreFoto` distingue *Se conserva la actual* de *Sin fotografía*, y el aviso de fallo distingue actualizar de registrar. `foto-bien` pierde su bloque de subida, su `@Output actualizado` y sus dependencias de notificación: queda como visor, y `detalle-bien` deja de escucharle |
| V311-03 El paso, según el rol | `formulario-bien`: `puedeFotografiar` se lee de la sesión y decide a la vez la sección y el rótulo del paso, de modo que el asistente no anuncia lo que no va a mostrar |
| V311-04 Reportes en blanco y negro | `GeneradorReporteInventarioArchivos`: en Excel desaparecen `IndexedColors.DARK_BLUE`, la fuente blanca y el `FillPatternType`, y la cabecera queda en negrita con bordes; en el PDF desaparecen el azul del título, el fondo de la cabecera y el sombreado alterno, y cada celda gana su recuadro negro. Los dos `import` de color de POI se retiran porque ya no los usa nadie |
| V311-05 El formato, también | `GeneradorFormatoUsoPdf`: las constantes `MARCA`, `GRIS_CABECERA` y `GRIS_LINEA` se reducen a una sola, `LINEA` en negro; las bandas de sección pasan de celda con fondo azul y texto blanco a texto negro con `Rectangle.BOTTOM` y línea de 1,2 pt; las etiquetas pierden su fondo gris |
| V311-06 El encabezado institucional | `Institucion.SEDE` —"Sede Central " + `NOMBRE`, junto a la constante que ya existía—; el puerto `GeneradorReporteInventario` gana el registro `Cabecera(sede, direccion, coordinacion)` con su método `ambito()`, y sus tres métodos lo reciben; `ExportacionInventarioServicio` lo compone con `ServicioPublicoOrganizacion.ubicacionDeCoordinacion`, que ya devolvía la Dirección de cada Coordinación. `FormatoUsoDto` gana `sede` y `direccion`, y `FormatoUsoServicio` los resuelve por la misma vía |
| V311-07 El símbolo S/ | `styles.css`: la exclusión de tipos de la regla base se envuelve en `:where()`. La v3.10 la había escrito como `:not()` encadenados, que suman especificidad: la regla pasó de (0,1,1) a (0,4,1) y se puso por delante de `.campo-moneda input`, `.campo.invalido input` e `input:focus`, anulándolas. Con `:where()` la regla pesa (0,0,1) y las tres vuelven a ganarle |
| V311-08 Comprobación | Las 82 pruebas del backend y las 45 del frontend siguen en verde, y las dos compilaciones salen limpias. El PDF del formato se genera sin ningún operador de color |

---

La versión 3.10 está **implementada en su totalidad** sobre la 3.9. Es la
primera desde la 3.7 que **cambia el esquema**: `V1__esquema_inicial.sql` se
rehace, como permite RNF-43 mientras la base esté vacía. Añade un endpoint y no
retira ninguno, y no incorpora ninguna dependencia.

| Cambio | Dónde vive |
| --- | --- |
| V310-01 Fecha de adquisición | Base: `equipo.anio_adquisicion INTEGER` → `equipo.fecha_adquisicion DATE`, con su CHECK del límite inferior. Backend: `Equipo.fechaAdquisicion` (`LocalDate`) y `exigirFecha`, que sustituye a `exigirAnio` y comprueba el límite superior al día; `GuardarEquipoComando`, `EquipoRequest` —con `@PastOrPresent` y el formato `yyyy-MM-dd`—, `EquipoDto`, `EquipoResumenDto`, `EquipoJpaEntity`, `InventarioMapper`, `FormatoUsoDto`, el generador del PDF y las columnas de la exportación. Frontend: `fechaAdquisicion: string` en los tres modelos, `FECHA_MINIMA_ADQUISICION` y `fechaMaximaAdquisicion()` en lugar de las de año, el campo `type="date"` acotado con `min`/`max`, y el formato `dd/MM/yyyy` en listado, ficha, resumen del alta y vista previa del formato. La ordenación del listado pasa de `anioAdquisicion` a `fechaAdquisicion` |
| V310-02 Responsable del equipo | Base: `equipo.responsable_equipo_id`, su índice parcial y la llave foránea compuesta contra `usuario_coordinacion`. Backend: `Equipo.responsableEquipoId` con `ponerACargoDe`, `devolverAlResponsableDeCoordinacion` y `estaACargoDeUnOperador`; `EquipoDto` gana el identificador y el **nombre ya resuelto** de quien lo tiene, que el servicio calcula al leer —el operador, o el Responsable vigente si no hay ninguno— de modo que un relevo no obliga a tocar ni un bien |
| V310-03 Solo el Responsable reparte | `GestionInventarioServicio.asignarResponsableDeEquipo`, que reutiliza `exigirResponsableDe` —el mismo guardián que editar y dar de baja— y comprueba el destinatario con `DirectorioUsuarios.esOperadorActivoDe`, un método nuevo del puerto hacia `iam` resuelto por `ServicioPublicoIam`. Endpoint `PUT /api/equipos/{id}/responsable-equipo` con `@PreAuthorize("hasRole('RESPONSABLE')")`. Frontend: el puerto, el adaptador, la fachada y el componente `ResponsableEquipo`, abierto desde la ficha |
| V310-04 La baja bloqueada | `GestionUsuariosServicio` gana el puerto `BienesACargo` —declarado en el dominio de `iam` y resuelto por `BienesACargoInventario` contra el servicio de host abierto del inventario, que es como `iam` habla con `organizacion` desde la v3.3— y lo consulta en `retirarDe` y en `cambiarEstado(BAJA)`. La suspensión no lo consulta, a propósito |
| V310-05 La regla en la base | La llave foránea apunta a la **asignación** y no al usuario, de modo que la fila referenciada es justo la que habría que borrar para quitarle el puesto. `DEFERRABLE INITIALLY DEFERRED` porque `usuario_coordinacion` se mapea como `@ElementCollection` y Hibernate reescribe la fila —borrado más alta— al cambiar el rol de una persona dentro de la misma transacción |
| V310-06 Las dos sueltas | `Equipo.darDeBaja` pone el responsable del equipo a `null`, con lo que el conteo de bienes a cargo puede limitarse a los activos sin que la llave foránea contradiga al servicio. `ServicioPublicoInventario.devolverAlResponsableLosBienesDe` la ejecuta sobre todos los bienes de una persona, y `cambiarResponsable` la invoca cuando el entrante es Operador de esa misma Coordinación —antes de reescribir su asignación, que es lo que la haría fallar— |
| V310-07 El movimiento nuevo | `TipoMovimiento.RESPONSABLE` con su etiqueta, el CHECK de `movimiento_equipo` ampliado, y en el frontend la unión de tipos, su icono y su color. No entra en `MOVIMIENTOS_OCULTOS`: la ficha sí lo muestra |
| V310-08 Los campos descolocados | `styles.css`: la regla base pasa de enumerar tipos a `input:not([type=checkbox]):not([type=radio]):not([type=file]):not([type=range])`, más una regla propia para `date`, `time`, `datetime-local` y `month` —`appearance: none` y `line-height: normal`, que es lo que WebKit necesita para no dejarlos más bajos— y cifras de anchura fija en los campos numéricos y de hora |
| V310-09 Comprobación | Las 82 pruebas del backend —ocho nuevas sobre el responsable del equipo y cuatro reescritas sobre la fecha de adquisición— y las 45 del frontend siguen en verde, y las dos compilaciones salen limpias |

---

La versión 3.9 está **implementada en su totalidad** sobre la 3.8. **No ha
exigido ninguna migración**: el esquema queda exactamente como estaba. Añade
dos endpoints —el alta con puesto y el formato de uso— y no retira ninguno. No
hay dependencias nuevas: el PDF se dibuja con OpenPDF, que ya estaba en el
proyecto para la exportación del inventario.

| Cambio | Dónde vive |
| --- | --- |
| V39-01 Alta atómica del operador | Backend: `GestionUsuariosServicio` se parte en `registrarPersona` y `asignarPuesto`, que ahora comparten `crear`, `asignar` y el nuevo `registrarConPuesto`, todo bajo un solo `@Transactional`; `AltaConPuestoRequest` reúne las dos peticiones sin cambiar sus validaciones y `UsuarioController` expone `POST /api/usuarios/con-puesto`. La coordinación se valida **antes** de dar de alta a nadie, para que el error llegue con el formulario todavía delante. Frontend: `UsuariosPuerto.crearConPuesto`, su adaptador y `UsuariosFacade.registrarYAsignar`, que deja de encadenar dos llamadas con `switchMap` |
| V39-02 El error en su campo | `FormularioUsuario` gana la entrada `erroresServidor`, que siembra su señal de errores desde fuera —hacía falta porque en modo `soloDatos` el formulario entrega los datos y es la pantalla de fuera quien los envía, así que la respuesta del servidor llegaba a otro sitio—; `MiEquipo` guarda `erroresAlta` y la alimenta con `erroresDeCampo(error)`. La ventana de confirmación pasa a mostrar la `nota` que la pantalla de fuera ya le pasaba y que no se dibujaba en ninguna parte |
| V39-03 "Todos" primero | `PESTANAS_INVENTARIO` en `equipo.model.ts` y `PESTANAS` en `prestamos.ts`: la entrada `todos` se mueve al primer puesto. Los dos componentes ya arrancaban con `PESTANAS[0]`, de modo que no hubo que tocar ninguna lógica |
| V39-04 Línea de tiempo sin repeticiones | `detalle-bien.ts`: la constante `MOVIMIENTOS_OCULTOS` —`PRESTAMO`, `DEVOLUCION`, `EDICION`— y la señal calculada `hitos`, que es lo que la plantilla recorre y cuenta. El filtro vive en la presentación a propósito: el endpoint del historial sigue devolviendo la línea de tiempo completa, que es lo que exige RN-21 |
| V39-05 Detalle del préstamo | `detalle-bien`: señal `prestamoDetalle`, columna de acciones con **Ver detalle** en la tabla de préstamos y una ventana con las dos mitades de la operación —entrega y devolución— separadas por sus títulos. Los datos ya llegaban en `PrestamoDto`: `usuarioPrestaNombre`, `usuarioRecibeNombre`, `observacionesSalida` y `observacionesRetorno` no se mostraban en ninguna pantalla de la ficha |
| V39-06 Formato de registro de uso | Backend, contexto `reportes`: `GenerarFormatoUsoComando` (lo escrito a mano), `FormatoUsoDto` (el documento entero, listo para dibujar), el puerto `GeneradorFormatoUso` y su adaptador `GeneradorFormatoUsoPdf` con OpenPDF, `FormatoUsoServicio` y `FormatoUsoController`. En `inventario`, `ServicioPublicoInventario.fichaDe` abre la ficha de un bien al contexto de reportes por el mismo servicio de host abierto que ya usaba la exportación (RNF-39). Frontend: contexto `reportes` con `formato-uso.model.ts`, el puerto `FormatoUsoPuerto`, su adaptador, `FormatoUsoFacade` y el componente `FormatoUso`, más el botón **Registrar salida** en la ficha del bien |
| V39-07 No se guarda nada | El servicio es `@Transactional(readOnly = true)` y no llama a ningún repositorio de escritura; el controlador responde 200 y no 201, y no existe ningún GET de formatos emitidos. La regla queda enunciada en RN-36 y repetida en la pantalla, en el pie del PDF y en el javadoc de las cuatro clases del módulo |
| V39-08 CSP | `frame-src 'self' blob:` en las tres declaraciones que tienen que decir lo mismo: `SeguridadConfig.CSP_ESTRICTA`, `frontend/seguridad-cabeceras.conf` y la etiqueta `<meta>` de `index.html`. `csp.spec.ts` la añade a las directivas exigidas |
| V39-09 Comprobación | Las 71 pruebas del backend —tres nuevas sobre el generador del formato, que comprueban que un documento con todos sus campos manuales en blanco sale igual de bien que uno lleno— y las 45 del frontend —la de regresión de CSP incluida— siguen en verde, y las dos compilaciones salen limpias |

---

La versión 3.8 está **implementada en su totalidad** sobre la 3.7. No ha
exigido ninguna migración, ningún endpoint nuevo ni ninguno retirado, y no toca
una sola regla de negocio: su huella está en la capa de presentación y en los
archivos de despliegue, que hasta ahora no existían. La única dependencia nueva
del backend es el *starter* de actuator, que `SeguridadConfig` ya daba por
supuesto al declarar `/actuator/health` como ruta pública.

| Cambio | Dónde vive |
| --- | --- |
| V38-01 Español con tildes | Las 18 plantillas y las cadenas de 20 componentes del frontend; `environment.ts` y `environment.prod.ts` (`nombreSistema`, y la versión que decía 2.0); `condicion.ts` ("Revisión pendiente"); y los datos base de `V2__datos_base.sql` —las dos Direcciones y las ocho categorías—. La corrección se aplicó solo al texto visible: en las plantillas, a los nodos de texto y a una lista blanca de atributos (`placeholder`, `title`, `aria-label`, `data-etiqueta`, `titulo`, `detalle`, `mensaje`), nunca dentro de `{{ }}` ni de los bloques `@if`/`@for`; en TypeScript, solo a cadenas entre comillas simples que son frases, porque las dobles envuelven expresiones de Angular dentro de las plantillas en línea |
| V38-02 Sin recuadros de prosa | 34 de los 36 bloques `<div class="aviso">` retirados de `acceso`, `asignar-puesto`, `cambiar-password`, `detalle-persona`, `formulario-usuario`, `mi-equipo`, `personas`, `detalle-bien`, `formulario-bien`, `estructura`, `formulario-prestamo`, `panel` y `principal`, junto con los 16 bloques `@if` que quedaron vacíos y sin `@else` detrás. Sobreviven los dos que interpolan `error()`. Las ramas de una cadena `@if`/`@else` se vaciaron pero no se borraron: eliminarlas habría hecho aparecer el formulario del `@else` en casos donde hoy está oculto |
| V38-03 Alta en tres pasos | `formulario-bien.ts`: `titulos` pasa de seis entradas a tres, `indiceFoto` desaparece —la fotografía deja de ser un paso—, `pasoValido` funde los antiguos 0 y 1 en el nuevo 0, `todoValido` recorre `[0, 1]` y `pasoDeCampo` remapea los once campos a los dos pasos que los piden. En `formulario-bien.html`, los grupos conservan su cabecera —`h2` el primero de cada paso, `h3` los siguientes— y la fotografía queda bajo `@if (!esEdicion())` |
| V38-04 Las barras de los paneles | `styles.css`: `.barra-dato .relleno` gana `display: block`. Era un `<span>` dentro de `.pista`, que no es contenedor flex, así que quedaba en flujo inline y el navegador ignoraba su anchura: Angular fijaba `width: 33%` y el elemento medía 0 px |
| V38-05 La acción principal fija | `styles.css`: `.acciones-cabecera` y las de `.tarjeta-cabecera` con `margin-left: auto`; las descripciones de encabezado y de tarjeta limitadas a `72ch` |
| V38-06 Tabla legible | `styles.css`: `.celda-codigos` con `white-space: nowrap`, `.celda-principal` con `min-width`, relleno de celda reducido de `1.15rem` a `.9rem` y `.celda-acciones` en `position: sticky` sobre el borde derecho, con su fondo repuesto para las filas de baja, vencidas y en foco. En `inventario.html`, las dos clases nuevas sobre sus celdas |
| V38-07 Despliegue | `backend/Dockerfile` y `frontend/Dockerfile` en dos etapas; `frontend/nginx.conf` con `frontend/seguridad-cabeceras.conf` incluido en cada `location`; `docker-compose.yml` con los tres servicios, sus sondas y su orden de arranque; `.env.example` como plantilla y dos `.dockerignore`. En el backend, `spring-boot-starter-actuator` y el bloque `management` de `application.yml`, que expone solo `health` y sin detalle |
| V38-08 Comprobación | Las 40 reglas documentadas se ejecutaron contra la API de la imagen de producción —primer ingreso, estructura, registro y asignación, RN-06, RN-33, RF-25, RF-22b, RN-34, RN-35, RN-13, RN-14, RN-16, RN-17, RN-22, RN-23, RN-25, RF-59, RF-64 y el aislamiento por Coordinación— y todas se cumplen. Las 45 pruebas del frontend, la de regresión de CSP incluida, y las del backend siguen en verde |

---

La versión 3.7 estaba **implementada en su totalidad** sobre la 3.6. Exigió
rehacer la línea base del esquema —la base no tenía datos, y RNF-43 lo permite
mientras el sistema no esté instalado— y añadió tres endpoints: el de estado
cambió de forma y los dos del relevo eran nuevos.

| Cambio | Dónde vive |
| --- | --- |
| V37-01 Tres estados de cuenta | `EstadoCuenta` (`iam/domain/model`), con `permiteEntrar()` y `sigueEnLaInstitucion()`. En `Usuario`, el campo `estado` sustituye a `activo` y las transiciones son métodos con nombre —`suspender`, `reactivar`, `darDeBaja`, `reincorporar`—, cada uno con su regla: la baja llama a `liberarPuesto()` y la reincorporación no lo devuelve. `estaActiva()` y `estaDeBaja()` sustituyen a `isActivo()` en la autenticación (`AutenticacionServicio`, `UsuarioDetalle`, `JwtAuthenticationFilter`) |
| V37-02 Quién puede ejercerlas | `GestionUsuariosServicio.cambiarEstado(id, destino)` con `exigirPermisoDeGestion` —que ya acotaba al Responsable a sus Operadores—, `exigirQueNoSeaElPropioUsuario` y `exigirQueQuedeUnAdministrador`, esta última invocada también desde el relevo. Desaparece la prohibición de desactivar a un Responsable vigente: ahora se puede, y la baja libera su puesto |
| V37-03 Cambiar responsable | `Usuario.asumirResponsabilidadDe` y `Usuario.liberarPuesto` en el dominio; `GestionUsuariosServicio.cambiarResponsable` y `candidatosAResponsable` con `exigirCandidaturaValida` en la aplicación; `UsuarioJpaRepository.candidatosAResponsable` en la persistencia. Endpoints `GET /api/usuarios/coordinacion/{id}/candidatos-responsable` y `POST /api/usuarios/coordinacion/{id}/responsable` con `CambioResponsableRequest`. En el frontend, `estructura.ts` con `abrirCambioDeResponsable`, `candidatos`, `pedirCambioDeResponsable` y `cambiarResponsable`, y su ventana en `estructura.html` |
| V37-04 La misma ventana nombra al primero | El botón **Asignar responsable** del cuadro rojo abre la misma ventana en lugar de navegar a `/personas`; `EstructuraOrganizacional.asignarResponsable` desaparece. La ruta con `asignarA` sigue soportada en `Personas`, pero ninguna pantalla la genera |
| V37-05 Esquema con estado | `V1__esquema_inicial.sql`: `usuario.estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA'` con `ck_usuario_estado`, e índice `ix_usuario_rol_estado`. `UsuarioJpaEntity` con `@Enumerated(STRING)`; las consultas por coordinación pasan de `activo = TRUE` a `estado <> BAJA` —la suspendida sigue siendo del equipo— y `FiltroUsuarios.activo` se convierte en `estado`. En el frontend, `EstadoCuenta`, `ESTADOS_CUENTA`, `claseEstadoCuenta` y el filtro de la pantalla de personas |
| V37-06 Pruebas | `UsuarioTest` sube a 30 casos: la baja que libera el puesto, la reincorporación que no lo devuelve, las transiciones imposibles, la cuenta sin activar que no recibe puesto, el ascenso del operador, el administrador que baja al terreno, el responsable que no toma una segunda coordinación y el saliente que queda libre |

---

La versión 3.6 estaba **implementada en su totalidad** sobre la 3.5. No exigió
migración, ni endpoint nuevo, ni ninguno retirado: una invariante que faltaba y
un latido que ya no obliga a nadie a pulsar F5.

| Cambio | Dónde vive |
| --- | --- |
| V36-01 El puesto se da a quien está libre | `Usuario.exigirPuestoLibre()`, invocado por `asignarA` y por `asignarComoAdministrador`, que absorbe las dos comprobaciones parciales que había —el cambio de rol con asignaciones vivas y la segunda coordinación—. En la aplicación, `GestionUsuariosServicio.exigirPersonaLibre`, que enuncia la regla con los nombres delante, y `exigirQueNoSeaElPropioUsuario(usuario, mensaje)`, que generaliza la comprobación que ya existía para la desactivación (RF-25) y ahora sirve también a la asignación. En el frontend, `AsignarPuesto.yaTienePuesto` mira el rol y no solo la coordinación, con `puestoInstitucional` y `esUnoMismo`; `personas.html` y `detalle-persona.html` no ofrecen la acción sobre uno mismo. Pruebas en `UsuarioTest`: `elOperadorNoRecibeOtroPuesto`, `elAdminNoCambiaDePuesto` y `trasLaBajaVuelveAEstarLibre` |
| V36-02 Refresco automático | `RefrescoAutomatico` (`compartido/aplicacion/refresco-automatico.ts`): un único latido —intervalo de 30 s, `visibilitychange` y `focus`, filtrado a la pestaña visible— que cada pantalla consume con `alRefrescar()` en su constructor y suelta sola al destruirse. Cada componente recibe un parámetro `silencioso` en su carga (`panel`, `inventario`, `prestamos`, `personas`, `estructura`, `mi-equipo`, `categorias`, `detalle-bien`), que suprime el indicador de carga y los avisos de error; `detalle-bien` además no expulsa de la ficha si la recarga de fondo falla. `Principal` refresca el perfil, de modo que el menú y el ámbito siguen al puesto. `OrganizacionFacade.recargarCoordinaciones()` descarta la memoria de `coordinacionesDisponibles`, que si no dejaría congelado quién tiene responsable |
| V36-03 Personas sin panel de pendientes | `personas.html` sin la sección `panel-pendientes`; `Personas.sinResponsable` y `asignarResponsable` desaparecen con ella —el encargo sigue llegando por la consulta `asignarA` desde `estructura.ts`—. En `styles.css` se retiran las dos reglas de `.panel-pendientes`; `.lista-pendientes` se queda, que la usa la ventana de asignación |
| V36-04 Usuario y correo escritos a mano | `formulario-usuario.ts` sin `sugerirCorreo()`, sin `sugerirUsuario()` y sin el ayudante `normalizar` que las servía; en su plantilla desaparecen los dos `(blur)` y el correo gana su marcador de posición y su ayuda |
| V36-05 Coordinaciones al abrir la ventana | `OrganizacionFacade.coordinacionesDisponibles()` con `catchError` que olvida la memoria antes de propagar el fallo: `shareReplay({refCount:false})` guardaba también el error y lo repetía toda la sesión. `Personas.asignarPuesto` recarga la lista al abrir, y `coordinacionesCargando` viaja al componente como `[cargandoCoordinaciones]`, que distingue "todavía no han llegado" de "no hay ninguna". Pruebas en `organizacion.facade.spec.ts` |
| V36-06 La persona llega completa | `normalizarUsuario` en `usuario.model.ts`, aplicado por `usuarios-http.adapter` a todo lo que devuelve una persona —lista, ficha, alta, edición, asignación, baja, estado y desbloqueo— y por `autenticacion-http.adapter` a la de la sesión. `AsignarPuesto` guarda `persona.rol ?? null` y compara con `!= null`, para que un dato sin normalizar no vuelva a decir que hay puesto donde no lo hay. Pruebas en `usuario.model.spec.ts`, con la respuesta tal como la manda el servidor |
| V36-07 Mi equipo humano, solo el equipo | `mi-equipo.html` sin el bloque `@if (responsable(); as jefe)` y sin el `<h2>Operadores</h2>` de la cabecera de la tarjeta; `mi-equipo.ts` sin el `computed` `responsable`, que solo servía a esa tarjeta. En `styles.css` se retiran `.tarjeta-responsable`, `.identidad-responsable` y `.avatar-grande`, que no las usaba nadie más |
| V36-08 El Administrador ve el inventario que elige | `Inventario.faltaElegirCoordinacion` y `Prestamos.faltaElegirCoordinacion` pasan de `computed()` a método: `coordinacionElegida` es un campo enlazado con `ngModel`, no una señal, y un `computed` que no depende de ninguna señal cambiante nunca se recalcula. El backend no se toca: `UsuarioAutenticado.ambitoDeConsulta` ya devolvía al ADMIN la Coordinación solicitada, y `exigirOperativo` sigue negándole toda escritura (RF-46, RF-49, RF-68, RN-22) |
| V36-09 Fotografía de fondo en el acceso | `.pantalla-acceso` en `styles.css`: `background-image` con el velo del degradado de marca (`.34`/`.26`) sobre `url('assets/inictel.jpg')`, `cover` centrado, y `background-color` de respaldo —que va detrás de la imagen y solo se ve mientras carga o si no llega—. `.pie-acceso` gana `text-shadow`, que es lo que sostiene su contraste ahora que el velo no oscurece. El empaquetador emite la imagen en `media/` con su huella, servida desde el propio origen: `img-src 'self'` la admite sin tocar la CSP |

---

La versión 3.5 estaba **implementada en su totalidad** sobre la 3.4. No exigió
ningún endpoint nuevo ni ninguno retirado: lo que cambió es el esquema
—declarado de nuevo desde cero, porque la base estaba vacía— y la interfaz.

| Cambio | Dónde vive |
| --- | --- |
| V35-01 Ficha de la coordinación | `estructura.ts`: `detalle` sustituye a `laboratoriosDe`, con `abrirDetalle`, `refrescarDetalle` —que mantiene la ficha al día tras cada acción— y `ventanaLibre`. En `estructura.html`, la tarjeta pasa de `<article>` a `<button>` con su pie "Ver detalle y opciones", y la ventana de laboratorios se convierte en la ficha completa. En `styles.css`, `button.tarjeta-coordinacion` y `.coord-pie`; se retiran `.coord-acciones*` y `.coord-sigla` |
| V35-02 Coordinación sin sigla | Migración `V1` sin la columna; `Coordinacion.crear` / `actualizar`, `CoordinacionDto`, `CoordinacionRequest`, `NuevaCoordinacionRequest`, `CoordinacionJpaEntity`, `OrganizacionMapper` y `GestionOrganizacionServicio` sin el parámetro. En el frontend, `estructura.model.ts` y el campo del formulario, que ahora solo aparece para direcciones |
| V35-03 Desactivar en la ficha | `personas.html`: la fila conserva **Ver detalle** y **Asignar**; el cambio de estado se ejerce desde `detalle-persona` |
| V35-04 Segundo apellido obligatorio | `NombrePersona` lo exige —y `Usuario.exigirNombreCompleto` desaparece por redundante—; `UsuarioRequest` con `@NotBlank`; `UsuarioJpaEntity` y la migración `V1` con `NOT NULL`. En el frontend, `formulario-usuario` (marca de obligatorio, `valido` y envío sin `null`) y `usuario.model.ts` |
| V35-05 Coordinación parada del todo | `GestionPrestamosServicio.exigirResponsableVigente`, invocado también desde `registrarDevolucion`; `GestionInventarioServicio.crear` ya lo exigía. `PanelControlServicio` rellena `sinResponsable` con 1 en el panel de coordinación, y `panel.html` lo convierte en aviso y en dos accesos directos apagados |
| V35-06 Una ventana a la vez | `formulario-usuario.html` envuelve el formulario en `@if (!confirmando())`; `estructura.html` condiciona la ficha a `ventanaLibre()`; `personas.ts` ya cerraba la ficha al abrir cualquier otra ventana |
| V35-07 Saludo de bienvenida | `SesionStore.recienIngresado` / `saludado()`, encendido al autenticarse; `principal.ts` y el modal al final de `principal.html`. El nombre de la Dirección llega en `UsuarioDto.CoordinacionAsignada.direccionNombre`, resuelto por `ServicioPublicoOrganizacion.ubicacionDeCoordinacion` a través del puerto `EstructuraOrganizacional.Ubicacion` |
| V35-08 Esquema consolidado | `V1__esquema_inicial.sql` y `V2__datos_base.sql`. Se eliminan `V3`, `V4`, `V5` y `V6`, cuyo contenido queda incorporado |

---

La versión 3.4 estaba **implementada en su totalidad** sobre la 3.3. Exigió
entonces la migración `V6`, que estrechaba las asignaciones a una por persona;
desde la v3.5 esa regla nace ya en `V1` y aquel archivo no existe (V35-08).
Ningún endpoint nuevo: la baja del Responsable es el `DELETE` de asignaciones
que ya existía, ahora con su botón propio y su confirmación. Se retiró la
cabecera `X-Coordinacion` con todo lo que la sostenía.

| Cambio | Dónde vive |
| --- | --- |
| V34-01 Una sola coordinación | Indice `uk_persona_en_una_coordinacion`, único sobre `usuario_id` (hoy en `V1`, entonces en `V6`). `Usuario.asignarA` rechaza la segunda; `Usuario.getCoordinacion` sustituye a `getCoordinacionPrincipal`. `UsuarioAutenticado` pasa de `Set<Long> coordinaciones` + `coordinacionActiva` a un único `coordinacion`; `ContextoUsuarioSpring` deja de leer la cabecera y `ServicioJwt` emite un solo claim `coord`. En el frontend, `SesionStore` sin `cambiarCoordinacion` ni almacenamiento de la elegida, `jwt.interceptor` sin `X-Coordinacion`, `principal.html` sin selector, y `coordinacionDe` en `usuario.model.ts` |
| V34-02 El puesto ocupado se rechaza | `GestionUsuariosServicio.exigirPuestoDeResponsableLibre`, que sustituye a `liberarPuestoDeResponsable`; `AsignacionRealizadaDto` sin el campo `relevado`. En el frontend, `asignar-puesto` marca como no elegibles las coordinaciones con responsable (`ocupada`) y avisa cuando todas lo tienen |
| V34-03 Baja del responsable | `GestionUsuariosServicio.retirarDe` con `exigirPermisoDeBaja`: solo el Administrador da de baja a un Responsable. En el frontend, `estructura.ts` inyecta `UsuariosFacade` y expone `pedirBajaDeResponsable` / `darDeBajaResponsable`; la tarjeta ofrece **Dar baja al responsable** o **Asignar responsable**, nunca las dos |
| V34-04 Sin responsable no se registra ni se presta | `GestionInventarioServicio.crear` exige el responsable vigente en lugar de aceptar `null`; puerto `DirectorioResponsables` y adaptador `DirectorioResponsablesIam` en `prestamos`, consultados por `GestionPrestamosServicio.registrar`. Se retiró de `iam` el puerto `CensoDeBienes` y su adaptador, que solo servían para prohibir la baja |
| V34-05 Ficha de la persona | Componente `detalle-persona` (`.ts` + `.html`), declarado en `app-module.ts`; `personas.html` sin las columnas de DNI y coordinación, con **Ver detalle** en cada fila, y `Personas.verDetalle` / `editar` encadenando ficha y formulario |
| V34-06 Confirmaciones | `formulario-usuario` con `confirmando`, `revisar()` y `cancelarConfirmacion()`; `asignar-puesto` con `confirmandoAsignacion` y `confirmandoBaja`; `personas` y `mi-equipo` con `confirmandoPassword` y `confirmandoDesbloqueo`; `estructura` con `bajaDeResponsable`, `cambioDeCoordinacion` y `cambioDeLaboratorio`. Todas usan el componente `app-confirmacion` que ya existía |

---

La versión 3.3 estaba **implementada en su totalidad** sobre la 3.2. Exigió
una migración —`V5`, que crea la tabla de asignaciones y retira la adscripción
del usuario—, dos endpoints nuevos y tres retirados.

| Cambio | Dónde vive |
| --- | --- |
| V33-01 Registro previo | `Usuario.registrar` sin rol ni coordinación; `RegistrarUsuarioComando` y `UsuarioRequest` reducidos a los datos personales; `POST /api/usuarios` devuelve el `UsuarioDto` y ya no una contraseña. En el frontend, `formulario-usuario` sin selector de rol ni de coordinación, y `Personas.alGuardar` limitado a confirmar y recargar la lista: no abre la ventana de asignación (RF-16c) |
| V33-02 Asignación con contraseña | `Usuario.asignarA` / `asignarComoAdministrador`; `GestionUsuariosServicio.asignar` con `guardarConPassword`; `POST /api/usuarios/{id}/asignaciones` y `DELETE /api/usuarios/{id}/asignaciones/{coordinacionId}`. En el frontend, el componente `asignar-puesto` |
| V33-03 Varias coordinaciones | La tabla `usuario_coordinacion`, entonces creada por `V5` y hoy declarada en `V1`; `AsignacionEmbebida` y la `@ElementCollection` de `UsuarioJpaEntity`, que siguen en pie. Lo demás lo revirtió la v3.4: la tabla conserva un índice único por persona (V34-01) y ya no existen `coordinacionActiva`, la cabecera `X-Coordinacion` ni el selector de ámbito |
| V33-04 Sin asistente de relevo | Eliminados `Relevo` (componente, plantilla y ruta), `CambioResponsableRequest`, `PersonaEncontradaDto` y los endpoints de relevo y de búsqueda por DNI: siguen sin existir. El relevo automático de `liberarPuestoDeResponsable` sí desapareció en la v3.4 (V34-02) |
| V33-05 La coordinación se queda a la vista | `estructura.ts`: el alta recarga el árbol en lugar de navegar; `asignarResponsable` lleva a `/personas` con `asignarA` y `rol` en la consulta, que `Personas` interpreta como encargo |

---

La versión 3.2 estaba **implementada en su totalidad** sobre la 3.1. Exigió
una migración —`V4`, que precarga las dos Direcciones— y retiró dos
endpoints; ninguno nuevo. El resto es dónde vive cada opción.

| Cambio | Dónde vive |
| --- | --- |
| V32-01 Direcciones precargadas | La inserción de DIDT y DCTT, entonces en `V4` y hoy en `V2__datos_base.sql` (V35-08) |
| V32-02 Sin alta de Direcciones | `OrganizacionController`: sin `POST /direcciones`; `GestionOrganizacionServicio.crearDireccion` y `Direccion.crear` eliminados. En el frontend, sin `crearDireccion` en puerto, adaptador ni fachada, y sin botón "Nueva dirección" |
| V32-03 Sin desactivación de Direcciones | Sin `PATCH /direcciones/{id}/estado`; `Direccion.activar`, `Direccion.desactivar` y `CoordinacionRepositorio.contarActivasPorDireccion` eliminados. `estructura.ts` pide el árbol entero (`estructura(false)`) para que las coordinaciones desactivadas no desaparezcan |
| V32-04 Direcciones y categorías en su sitio | `principal.ts`: menú con `/direcciones` y sin `/categorias`; `app-routing-module.ts` con la redirección de `/estructura`; botón "Gestionar categorías" en `inventario.html` y regreso al inventario en `categorias.html` |
| V32-05 Personas en una sola pantalla | Ruta `/personas` con `Personas` sin `data.rol`; redirecciones desde `/responsables` y `/operadores`; filtro `rolFiltro` y columna de rol en `personas.html`; selector de rol en `formulario-usuario` (`permiteElegirRol`, `rolElegido`) |

---

### Estado de la versión 3.1

La versión 3.1 está implementada en su totalidad sobre la 3.0, que también
lo está. La 3.1 no ha exigido migración de base de datos ni endpoint nuevo: el
bien ya tenía su campo `foto_url` y la API ya exponía
`POST /api/equipos/{id}/foto`, abierto a RESPONSABLE y OPERADOR. Lo que cambia
es cuándo y desde dónde se llama.

| Cambio | Dónde vive |
| --- | --- |
| V31-01 Paso de fotografía en el alta | `formulario-bien.ts` y `formulario-bien.html`: los pasos se arman con el de fotografía solo cuando no hay `id` de edición; `indiceFoto` e `indiceResumen` gobiernan el asistente |
| V31-02 Captura desde la cámara | Componente `captura-foto` (`captura-foto.ts` + `.html`), con `getUserMedia`, visor en vivo y congelado en un lienzo; icono `camara` en `icono.ts`; estilos `.captura-foto`, `.visor-camara` y `.acciones-foto` en `styles.css` |
| V31-03 Cámara solo para el propio origen | `SeguridadConfig.cadenaDeSeguridad`: `camera=(self)` en la `Permissions-Policy`. La CSP no se toca y `csp.spec.ts` sigue en verde |
| V31-04 La foto no tumba el alta | `FormularioBien.adjuntarFoto`: el alta se confirma antes de enviar la imagen y un fallo del envío deriva en aviso, no en pérdida del registro |

---

### Estado de la versión 3.0

Los cambios V3-01 a V3-08 están en el código. La migración `V3` que entonces
retiraba la tabla `auditoria` ya no existe: desde la v3.5 el esquema se declara
sin ella (V35-08).

| Cambio | Dónde vive |
| --- | --- |
| V3-01 Sin bitácora | Eliminados el contexto `auditoria` y el paquete de eventos del backend, y el contexto `auditoria`, la ruta `/historial` y su entrada de menú del frontend |
| V3-02 Primer ingreso ampliado | `POST /api/auth/primer-ingreso`, `Usuario.completarPrimerIngreso`, pantalla `/cambiar-password` en dos pasos |
| V3-03 Candado | Icono `candado` en las listas de personas y en la pantalla de cuenta |
| V3-04 Laboratorio obligatorio | `POST /api/organizacion/coordinaciones` con el primer laboratorio; `Laboratorio.desactivar` rechaza el último activo |
| V3-05 Sin laboratorios no hay bienes | `GestionInventarioServicio.crear` y el aviso previo del formulario de alta |
| V3-06 Asignación guiada | La creación de una Coordinación conducía al asistente `/responsables/:id/relevo`; desde la v3.3 deja la tarjeta a la vista con su botón de asignación (V33-05) |
| V3-07 Tarjetas | `estructura.html` con la rejilla `.rejilla-coordinaciones` |
| V3-08 Una sola Coordinación | Selector "Asignar a una coordinación" con su advertencia y filtrado por RN-06 |

---

**FIN DEL DOCUMENTO**
