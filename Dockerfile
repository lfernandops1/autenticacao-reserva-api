# Usar imagem oficial Java 17 para build e execução
FROM eclipse-temurin:17-jdk-alpine AS build

# Diretório de trabalho
WORKDIR /app

# Copiar arquivos do build e dependências
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# Executar build para gerar o jar
RUN ./gradlew clean bootJar --no-daemon

# Imagem runtime mínima
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copiar o jar gerado do estágio build
COPY --from=build /app/build/libs/*.jar app.jar

# Expõe a porta padrão do Spring Boot
EXPOSE 8080

# Rodar a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
