FROM ubuntu:22.04

WORKDIR /app

COPY target/myspace .

RUN chmod +x myspace

ENV PORT=8080

EXPOSE 8080

CMD ["./myspace"]