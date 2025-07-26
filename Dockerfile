# Usa uma imagem base que já tem o Java 11
FROM openjdk:11-jdk-slim

# Instala o Google Chrome e outras dependências necessárias para o Selenium
RUN apt-get update && apt-get install -y \
    wget \
    gnupg \
    unzip \
    --no-install-recommends \
    && wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update && apt-get install -y \
    google-chrome-stable \
    --no-install-recommends \
    # CORREÇÃO: Instala o chromedriver correspondente
    && CHROME_VERSION=$(google-chrome --version | cut -f 3 -d ' ' | cut -d '.' -f 1) \
    && DRIVER_VERSION=$(wget -qO- https://googlechromelabs.github.io/chrome-for-testing/LATEST_RELEASE_${CHROME_VERSION}) \
    && wget -O /tmp/chromedriver.zip https://storage.googleapis.com/chrome-for-testing-public/${DRIVER_VERSION}/linux64/chromedriver-linux64.zip \
    && unzip /tmp/chromedriver.zip -d /usr/local/bin \
    && rm /tmp/chromedriver.zip \
    && mv /usr/local/bin/chromedriver-linux64/chromedriver /usr/local/bin/chromedriver \
    && chmod +x /usr/local/bin/chromedriver \
    # Limpa o cache
    && rm -rf /var/lib/apt/lists/*

# Copia o código da aplicação para dentro do contentor
WORKDIR /app
COPY . .

# Adiciona permissão de execução ao Maven Wrapper
RUN chmod +x ./mvnw

# Executa o Maven para construir o projeto
RUN ./mvnw package

# Expõe a porta que a nossa aplicação usa
EXPOSE 3000

# O comando para iniciar a aplicação (será sobrescrito pelo Procfile)
CMD ["java", "-jar", "target/analise-futebol-1.0-SNAPSHOT.jar"]
