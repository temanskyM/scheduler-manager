# Stage 1: Build the application
FROM registry.access.redhat.com/ubi9/openjdk-21 AS builder
ARG PROJECT_DIR=.
ARG WORKDIR=/home/app
USER root
WORKDIR ${WORKDIR}

# Cache Maven dependencies
COPY ${PROJECT_DIR}/pom.xml .

RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

# Copy source code
COPY ${PROJECT_DIR}/src/ ./src

# Build the application
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests -B

# Stage 2: Extract layers using layertools
FROM registry.access.redhat.com/ubi9/openjdk-21 AS layer_extractor
ARG WORKDIR=/home/app
WORKDIR ${WORKDIR}

COPY --from=builder ${WORKDIR}/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 3: Build the runtime image
FROM registry.access.redhat.com/ubi9/openjdk-21-runtime
ARG WORKDIR=/home/app
ARG PROJECT_DIR=report-server
USER default
WORKDIR ${WORKDIR}

# Install necessary packages
USER root

# Install necessary packages
USER root
RUN microdnf -y update && \
    microdnf -y install fontconfig && \
    microdnf clean all

RUN useradd -ms /bin/bash spring-user
USER spring-user
# Copy application layers
COPY --from=layer_extractor ${WORKDIR}/dependencies/ ./
COPY --from=layer_extractor ${WORKDIR}/snapshot-dependencies/ ./
COPY --from=layer_extractor ${WORKDIR}/spring-boot-loader/ ./
COPY --from=layer_extractor ${WORKDIR}/application/ ./

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]