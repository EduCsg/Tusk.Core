# 1 - Build
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /application

# Cache de dependências
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

# Build da aplicação
COPY src ./src
RUN ./mvnw clean package -DskipTests

RUN java -Djarmode=layertools -jar target/*.jar extract

# 2 - Runtime
FROM eclipse-temurin:25-jre-alpine
WORKDIR /application

# Cria usuário
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copia os arquivos extraídos do build para o runtime
COPY --from=build /application/dependencies/ ./
COPY --from=build /application/spring-boot-loader/ ./
COPY --from=build /application/snapshot-dependencies/ ./
COPY --from=build /application/application/ ./

# Entrypoint
ENTRYPOINT ["java", "-XX:+UseG1GC", "org.springframework.boot.loader.launch.JarLauncher"]