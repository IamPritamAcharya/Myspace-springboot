
FROM ghcr.io/graalvm/native-image-community:21 AS builder

WORKDIR /app
COPY . .

RUN ./mvnw -Pnative native:compile

FROM ubuntu:22.04

WORKDIR /app
COPY --from=builder /app/target/myspace .

RUN chmod +x myspace

ENV PORT=8080
EXPOSE 8080

CMD ["./myspace"]