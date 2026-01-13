# Utilise une image Alpine legere avec JRE 21 (pas besoin de JDK en production) sinon eclipse-temurin:21-jdk-jammy (assez lourd mais avec un JDK)
FROM eclipse-temurin:21-jre-alpine

# Definit le repertoire de travail dans le conteneur
WORKDIR /app

# Variables d'environnement pour la configuration
ENV EUREKA_SERVER_HOST=eureka-server
ENV EUREKA_SERVER_PORT=9102
ENV HOSTNAME=mgateway

# Copier le jar de l'application
COPY target/mgateway-*.jar app.jar

# Exposer le port sur lequel Eureka ecoute
EXPOSE 9010

# Commande pour lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]