# ── STAGE 1: Build NetCal-DNC ─────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS dnc-build
WORKDIR /dnc

COPY DNC-EthernetTSN/DNC/src ./src
COPY DNC-EthernetTSN/DNC/lib ./lib
COPY DNC-EthernetTSN/DNC/pom.xml .

# Create dummy rtc.jar to satisfy surefire plugin resolution (not needed at runtime)
RUN mkdir -p lib/RTCToolbox/rtc && touch lib/RTCToolbox/rtc/rtc.jar

RUN mvn install -Dmaven.test.skip=true -q

# ── STAGE 2: Build DNC-EthernetTSN ───────────────────
FROM maven:3.9-eclipse-temurin-17 AS lib-build
WORKDIR /lib

COPY DNC-EthernetTSN/src ./src
COPY DNC-EthernetTSN/pom.xml .

# Copy the .m2 with NetCal-DNC already installed
COPY --from=dnc-build /root/.m2 /root/.m2

RUN mvn install -Dmaven.test.skip=true -q

# ── STAGE 3: Build tsn-api ────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS app-build
WORKDIR /app

COPY pom.xml .

# Copy .m2 with NetCal-DNC + DNC-EthernetTSN installed
COPY --from=lib-build /root/.m2 /root/.m2

RUN mvn dependency:go-offline -q

COPY src ./src

ARG PROFILE=worker
RUN mvn package -Dmaven.test.skip=true -Dquarkus.profile=${PROFILE} -q

# ── STAGE 4: Runtime ──────────────────────────────────
FROM eclipse-temurin:17-jre

WORKDIR /deployments

COPY --from=app-build /app/target/quarkus-app/lib/ lib/
COPY --from=app-build /app/target/quarkus-app/*.jar ./
COPY --from=app-build /app/target/quarkus-app/app/ app/
COPY --from=app-build /app/target/quarkus-app/quarkus/ quarkus/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]
