# Usa una imagen base de OpenJDK 11
FROM openjdk:11-jdk-slim

# Establece el directorio de trabajo en el contenedor
WORKDIR /app

# Copia el contenido del proyecto en el contenedor
COPY . /app/

# Asegúrate de que el archivo gradlew tenga permisos de ejecución
RUN chmod +x gradlew

# Ejecuta Gradle para limpiar y construir el proyecto
RUN ./gradlew clean build -x test -x check

# Expone el puerto en el que la aplicación se ejecutará (por defecto Ktor usa 8080)
EXPOSE 8080

# Ejecuta la aplicación cuando se inicie el contenedor
CMD ["java", "-jar", "build/libs/datingApp-1.0-SNAPSHOT.jar"]
