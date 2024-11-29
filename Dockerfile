# Usar una imagen base con Maven y OpenJDK
FROM maven:3.8.5-openjdk-17 AS builder

# Definir el directorio de trabajo en el contenedor
WORKDIR /app

# Copiar el archivo pom.xml y las dependencias de Maven
COPY pom.xml /app/
RUN mvn dependency:go-offline

# Copiar todo el código fuente al contenedor
COPY src /app/src

# Construir el archivo .jar de la aplicación con Maven
RUN mvn clean package -DskipTests

# Usar una imagen base más ligera para ejecutar la aplicación
FROM openjdk:17-jdk-slim

# Definir el directorio de trabajo para la aplicación en el contenedor
WORKDIR /app

# Copiar el archivo .jar generado en la etapa de construcción al contenedor
COPY --from=builder /app/target/mi-aplicacion-2-0.0.1-SNAPSHOT.jar /app/mi-aplicacion-2-0.0.1-SNAPSHOT.jar

# Exponer el puerto en el que la aplicación Spring Boot estará corriendo
EXPOSE 8081

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "mi-aplicacion-2-0.0.1-SNAPSHOT.jar"]