document.addEventListener('DOMContentLoaded', () => {
    // --- Elementos da UI ---
    const campeonatoSelect = document.getElementById('campeonato-select');
    const time1Select = document.getElementById('time1-select');
    const time2Select = document.getElementById('time2-select');
    const calculateBtn = document.getElementById('calculateBtn');

    const btnText = document.getElementById('btn-text');
    const btnLoader = document.getElementById('btn-loader');
    const resultsPanel = document.getElementById('resultsPanel');
    const loader = document.getElementById('loader');
    const errorPanel = document.getElementById('errorPanel');
    const resultsContent = document.getElementById('resultsContent');
    const team1Container = document.getElementById('team1-container');
    const team2Container = document.getElementById('team2-container');

    // Variável para armazenar a ordem das tabelas
    let ordemDasTabelas = [];

    // --- Funções de Carregamento ---

    // Carrega a ordem das tabelas do JSON
    async function carregarOrdemTabelas() {
        try {
            const response = await fetch('json/ordem_tabelas.json');
            ordemDasTabelas = await response.json();
        } catch (error) {
            console.error("Erro ao carregar a ordem das tabelas, a usar ordem padrão:", error);
            // Ordem de fallback caso o ficheiro não seja encontrado
            ordemDasTabelas = ["Scores & Fixtures", "Standard Stats", "Shooting"];
        }
    }

    // Carrega a lista de campeonatos do JSON
    async function carregarCampeonatos() {
        try {
            const response = await fetch('json/campeonatos.json');
            const campeonatos = await response.json();

            campeonatos.forEach((nomeCampeonato, index) => {
                const option = document.createElement('option');
                option.value = nomeCampeonato;
                option.textContent = nomeCampeonato;
                if (index === 0) {
                    option.disabled = true;
                    option.selected = true;
                }
                campeonatoSelect.appendChild(option);
            });
        } catch (error) {
            console.error("Erro ao carregar campeonatos:", error);
            alert("Não foi possível carregar a lista de campeonatos.");
        }
    }

    // Carrega a lista de times com base no campeonato selecionado
    async function carregarTimes(nomeCampeonato) {
        // Limpa e desativa os dropdowns de times
        time1Select.innerHTML = '<option value="">Selecione o Time 1</option>';
        time2Select.innerHTML = '<option value="">Selecione o Time 2</option>';
        time1Select.disabled = true;
        time2Select.disabled = true;
        calculateBtn.disabled = true;

        if (!nomeCampeonato || nomeCampeonato === "Selecione um Campeonato") return;

        const arquivoTimes = `json/${nomeCampeonato}.json`;

        try {
            const response = await fetch(arquivoTimes);
            const times = await response.json();
            times.sort(); // Ordena os times alfabeticamente

            times.forEach(time => {
                const option1 = document.createElement('option');
                option1.value = time;
                option1.textContent = time;
                time1Select.appendChild(option1);

                const option2 = document.createElement('option');
                option2.value = time;
                option2.textContent = time;
                time2Select.appendChild(option2);
            });

            // Ativa os dropdowns
            time1Select.disabled = false;
            time2Select.disabled = false;

        } catch (error) {
            console.error(`Erro ao carregar times de ${arquivoTimes}:`, error);
            alert(`Não foi possível carregar a lista de times para o campeonato selecionado.`);
        }
    }

    // --- Lógica de Eventos ---

    // Quando o utilizador muda o campeonato
    campeonatoSelect.addEventListener('change', (event) => {
        const nomeCampeonatoSelecionado = event.target.value;
        carregarTimes(nomeCampeonatoSelecionado);
    });

    // Ativa o botão de análise apenas quando dois times diferentes são selecionados
    [time1Select, time2Select].forEach(select => {
        select.addEventListener('change', () => {
            const time1 = time1Select.value;
            const time2 = time2Select.value;
            if (time1 && time2 && time1 !== time2) {
                calculateBtn.disabled = false;
            } else {
                calculateBtn.disabled = true;
            }
        });
    });

    // Quando o botão de análise é clicado
    calculateBtn.addEventListener('click', async () => {
        const time1Query = time1Select.value;
        const time2Query = time2Select.value;

        // Preparar a UI para o carregamento
        resultsPanel.classList.remove('hidden');
        loader.classList.remove('hidden');
        resultsContent.classList.add('hidden');
        errorPanel.classList.add('hidden');

        team1Container.style.minHeight = 'auto';
        team2Container.style.minHeight = 'auto';

        team1Container.innerHTML = '';
        team2Container.innerHTML = '';
        calculateBtn.disabled = true;
        btnText.classList.add('hidden');
        btnLoader.classList.remove('hidden');

        try {
            const response = await fetch(`/api/stats?team1=${encodeURIComponent(time1Query)}&team2=${encodeURIComponent(time2Query)}`);
            const data = await response.json();

            if (data.error) throw new Error(data.details || data.error);

            displayResults(data);
            loader.classList.add('hidden');
            resultsContent.classList.remove('hidden');

        } catch (error) {
            loader.classList.add('hidden');
            errorPanel.textContent = `Erro: ${error.message}`;
            errorPanel.classList.remove('hidden');
        } finally {
            calculateBtn.disabled = false; // Reativa o botão mesmo se houver erro
            btnText.classList.remove('hidden');
            btnLoader.classList.add('hidden');
        }
    });

    // --- Funções de Exibição ---

    function displayResults(data) {
        if (data.team1Data) displayTeamData(team1Container, data.team1Data);
        if (data.team2Data) displayTeamData(team2Container, data.team2Data);

        // Lógica para igualar a altura das tabelas correspondentes
        setTimeout(() => {
            equalizeTableHeights();
        }, 0);
    }

    function displayTeamData(container, teamData) {
        const teamTitle = document.createElement('h2');
        teamTitle.className = 'text-3xl font-bold text-gray-800 mb-6 text-center';
        teamTitle.textContent = teamData.teamName;
        container.appendChild(teamTitle);

        // Itera pela ordem definida que foi carregada do JSON
        ordemDasTabelas.forEach(tableName => {
            if (teamData.tables && teamData.tables[tableName]) {
                createAndDisplayTable(container, tableName, teamData.tables[tableName]);
            }
        });
    }

    function createAndDisplayTable(container, tableName, tableData) {
        if (!tableData || tableData.length === 0) return;

        const tableWrapper = document.createElement('div');
        tableWrapper.className = 'bg-white rounded-xl shadow-lg p-4 mb-6';
        tableWrapper.dataset.tableName = tableName.replace(/\s+/g, '-');

        const tableTitle = document.createElement('h3');
        tableTitle.className = 'text-xl font-semibold text-gray-700 mb-4';
        tableTitle.textContent = tableName;
        tableWrapper.appendChild(tableTitle);

        const tableContainer = document.createElement('div');
        tableContainer.className = 'overflow-x-auto';
        const table = document.createElement('table');
        table.className = 'min-w-full text-sm';

        const thead = document.createElement('thead');
        thead.className = 'bg-gray-100';
        const headerRow = document.createElement('tr');
        const headers = Object.keys(tableData[0]);
        headers.forEach(text => {
            const th = document.createElement('th');
            th.className = 'px-4 py-2 text-left font-medium text-gray-600';
            th.textContent = text;
            headerRow.appendChild(th);
        });
        thead.appendChild(headerRow);
        table.appendChild(thead);

        const tbody = document.createElement('tbody');
        tableData.forEach(rowData => {
            const tr = document.createElement('tr');
            tr.className = 'border-b';
            headers.forEach(header => {
                const td = document.createElement('td');
                td.className = 'px-4 py-2 text-gray-700';
                td.textContent = rowData[header];
                tr.appendChild(td);
            });
            tbody.appendChild(tr);
        });
        table.appendChild(tbody);

        tableContainer.appendChild(table);
        tableWrapper.appendChild(tableContainer);
        container.appendChild(tableWrapper);
    }

    function equalizeTableHeights() {
        console.log("A igualar alturas das tabelas...");
        ordemDasTabelas.forEach(tableName => {
            const safeTableName = tableName.replace(/\s+/g, '-');
            const table1 = document.querySelector(`#team1-container [data-table-name="${safeTableName}"]`);
            const table2 = document.querySelector(`#team2-container [data-table-name="${safeTableName}"]`);

            if (table1 && table2) {
                table1.style.minHeight = 'auto';
                table2.style.minHeight = 'auto';

                const height1 = table1.offsetHeight;
                const height2 = table2.offsetHeight;
                const maxHeight = Math.max(height1, height2);

                table1.style.minHeight = `${maxHeight}px`;
                table2.style.minHeight = `${maxHeight}px`;
                console.log(`Altura da tabela "${tableName}" definida para ${maxHeight}px`);
            }
        });
    }

    // --- INICIALIZAÇÃO ---
    async function init() {
        // Garante que os dropdowns e o botão começam desativados
        time1Select.disabled = true;
        time2Select.disabled = true;
        calculateBtn.disabled = true;

        // Carrega primeiro a ordem das tabelas e depois os campeonatos
        await carregarOrdemTabelas();
        await carregarCampeonatos();
    }

    init(); // Chama a função de inicialização
});
