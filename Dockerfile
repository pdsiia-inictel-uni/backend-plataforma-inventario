# =============================================================================
# Backend - Sistema de Gestion de Inventarios INICTEL-UNI
#
# Dos etapas (RNF-44): la primera compila con el JDK y Maven; la segunda solo
# lleva el JRE y la aplicacion. La imagen que se publica no arrastra el
# compilador, el repositorio de dependencias ni el codigo fuente.
#
# La aplicacion se copia por CAPAS de Spring Boot: primero las dependencias,
# que casi nunca cambian, y al final el codigo propio. Un cambio en el codigo
# solo invalida la ultima capa, de modo que reconstruir y subir una version
# nueva mueve unos KB y no las decenas de MB de librerias.
# =============================================================================

# ----------------------------------------------------------------- Compilacion
FROM eclipse-temurin:21-jdk-alpine AS construccion

WORKDIR /origen

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw

# La cache de Maven vive fuera de la imagen (BuildKit): las dependencias se
# descargan una vez por maquina y no en cada construccion.
COPY src src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -q clean package -DskipTests \
    && cp target/*.jar aplicacion.jar \
    && java -Djarmode=tools -jar aplicacion.jar extract --layers --launcher --destination capas

# -------------------------------------------------------------------- Ejecucion
FROM eclipse-temurin:21-jre-alpine AS ejecucion

# Usuario sin privilegios: el proceso no corre como root (RNF-44).
RUN addgroup -S inventario && adduser -S -G inventario inventario \
    && mkdir -p /datos/archivos && chown -R inventario:inventario /datos

WORKDIR /aplicacion

# De la que menos cambia a la que mas: cada capa se reutiliza mientras no cambie.
COPY --from=construccion --chown=inventario:inventario /origen/capas/dependencies/ ./
COPY --from=construccion --chown=inventario:inventario /origen/capas/spring-boot-loader/ ./
COPY --from=construccion --chown=inventario:inventario /origen/capas/snapshot-dependencies/ ./
COPY --from=construccion --chown=inventario:inventario /origen/capas/application/ ./

# Fotografias y PDF de baja: en un volumen, para que un redespliegue no se los
# lleve por delante (RF-42, RF-51).
VOLUME ["/datos/archivos"]

USER inventario
EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod \
    APP_ARCHIVOS_DIRECTORIO=/datos/archivos \
    TZ=America/Lima \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport -Duser.timezone=America/Lima -Dfile.encoding=UTF-8"

# Sonda de estado: solo /actuator/health, sin detalle (RNF-44). El margen de
# arranque cubre la primera ejecucion de Flyway sobre una base vacia.
HEALTHCHECK --interval=15s --timeout=5s --start-period=90s --retries=5 \
    CMD wget -qO- http://127.0.0.1:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
