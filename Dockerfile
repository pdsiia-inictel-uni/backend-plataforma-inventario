# =============================================================================
# Backend - Sistema de Gestion de Inventarios INICTEL-UNI
#
# Imagen en dos etapas: la primera compila con el JDK y Maven, la segunda solo
# lleva el JRE y el jar. Asi la imagen que se publica no arrastra el compilador,
# el repositorio de dependencias ni el codigo fuente (RNF-44).
# =============================================================================

FROM eclipse-temurin:21-jdk-alpine AS construccion

WORKDIR /origen

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src

RUN ./mvnw -B -q clean package -DskipTests \
    && mv target/*.jar aplicacion.jar

# -------------------------------------------------------------------- Ejecucion
FROM eclipse-temurin:21-jre-alpine AS ejecucion

RUN addgroup -S inventario && adduser -S -G inventario inventario

WORKDIR /aplicacion

RUN mkdir -p /datos/archivos && chown -R inventario:inventario /datos
VOLUME ["/datos/archivos"]

COPY --from=construccion --chown=inventario:inventario /origen/aplicacion.jar aplicacion.jar

USER inventario
EXPOSE 8080

ENV APP_ARCHIVOS_DIRECTORIO=/datos/archivos \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseContainerSupport -Duser.timezone=America/Lima -Dfile.encoding=UTF-8"

HEALTHCHECK --interval=15s --timeout=5s --start-period=60s --retries=5 \
    CMD wget -qO- http://127.0.0.1:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "-jar", "aplicacion.jar"]
