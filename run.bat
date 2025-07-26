@echo off
REM Define o titulo da janela do terminal
TITLE Servidor de Analise de Futebol (Java)

echo.
echo ===================================================
echo  Iniciando Servidor de Analise de Futebol (Java)
echo ===================================================
echo.

REM Verifica se o Maven esta instalado, procurando pelo ficheiro pom.xml
if not exist "pom.xml" (
    echo ERRO: O ficheiro 'pom.xml' nao foi encontrado.
    echo Certifique-se de que esta a executar este script na pasta raiz do seu projeto Maven.
    pause
    exit /b
)

echo --- A compilar o projeto e a iniciar o servidor... ---
echo --- (Isto pode demorar um pouco na primeira vez, pois o Maven ira descarregar as dependencias) ---
echo.

REM Executa o comando do Maven para compilar e correr a aplicacao
mvn compile exec:java

echo.
echo --- O servidor foi encerrado. ---
pause
