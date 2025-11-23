# 1. Берем официальный JDK для сборки
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Копируем pom.xml и загружаем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем всё приложение
COPY src ./src

# Сборка JAR-файла
RUN mvn package -DskipTests

# 2. Рантайм — легкий JDK/JRE
FROM eclipse-temurin:17-jre

WORKDIR /app

# Копируем JAR из первой стадии
COPY --from=build /app/target/*.jar app.jar

# Порт
EXPOSE 8080

# Запуск
ENTRYPOINT ["java", "-jar", "app.jar"]
