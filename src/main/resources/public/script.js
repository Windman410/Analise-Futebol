// --- Elementos da UI ---
const calculateBtn = document.getElementById('calculateBtn');
const btnText = document.getElementById('btn-text');
const btnLoader = document.getElementById('btn-loader');

const resultsPanel = document.getElementById('resultsPanel');
const loader = document.getElementById('loader');
const errorPanel = document.getElementById('errorPanel');
const resultsContent = document.getElementById('resultsContent');

// --- Funções de Cálculo (Poisson) ---
function factorial(n) {
    if (n < 0) return 0;
    if (n === 0 || n === 1) return 1;
    let result = 1;
    for (let i = 2; i <= n; i++) {
        result *= i;
    }
    return result;
}

function poissonProbability(k, lambda) {
    return (Math.pow(lambda, k) * Math.exp(-lambda)) / factorial(k);
}

// --- Lógica Principal ---
calculateBtn.addEventListener('click', async () => {
    const team1Query = document.getElementById('team1Name').value.trim();
    const team2Query = document.getElementById('team2Name').value.trim();

    if (!team1Query || !team2Query) {
        alert('Por favor, insira o nome dos dois times.');
        return;
    }

    // 1. Preparar a UI para o carregamento
    resultsPanel.classList.remove('hidden');
    loader.classList.remove('hidden');
    resultsContent.classList.add('hidden');
    errorPanel.classList.add('hidden');
    errorPanel.textContent = ''; // Limpa erros anteriores
    calculateBtn.disabled = true;
    btnText.classList.add('hidden');
    btnLoader.classList.remove('hidden');

    try {
        // MUDANÇA: Buscar um time de cada vez para maior estabilidade.
        
        // --- BUSCA TIME 1 ---
        console.log(`Buscando dados para: ${team1Query}`);
        const response1 = await fetch(`/api/stats?team=${encodeURIComponent(team1Query)}`);
        const stats1 = await response1.json();

        if (stats1.code) { // Verifica se a resposta é um erro
            throw new Error(`Erro ao buscar ${team1Query}: [Código ${stats1.code}] ${stats1.message} - ${stats1.details}`);
        }
        console.log(`Dados de ${team1Query} recebidos.`);
        
        // --- BUSCA TIME 2 ---
        console.log(`Buscando dados para: ${team2Query}`);
        const response2 = await fetch(`/api/stats?team=${encodeURIComponent(team2Query)}`);
        const stats2 = await response2.json();

        if (stats2.code) { // Verifica se a resposta é um erro
            throw new Error(`Erro ao buscar ${team2Query}: [Código ${stats2.code}] ${stats2.message} - ${stats2.details}`);
        }
        console.log(`Dados de ${team2Query} recebidos.`);

        // 3. Realizar os cálculos com os dados recebidos
        const team1Attack = stats1.goalsFor / stats1.gamesPlayed;
        const team1Defense = stats1.goalsAgainst / stats1.gamesPlayed;
        const team2Attack = stats2.goalsFor / stats2.gamesPlayed;
        const team2Defense = stats2.goalsAgainst / stats2.gamesPlayed;

        const lambda1 = team1Attack * team2Defense; // Gols esperados do time 1
        const lambda2 = team2Attack * team1Defense; // Gols esperados do time 2

        let probTeam1Wins = 0, probTeam2Wins = 0, probDraw = 0;
        const scoreProbs = [];

        for (let i = 0; i <= 6; i++) { // Gols Time 1
            for (let j = 0; j <= 6; j++) { // Gols Time 2
                const prob = poissonProbability(i, lambda1) * poissonProbability(j, lambda2);
                scoreProbs.push({ score: `${i} - ${j}`, prob: prob });
                if (i > j) probTeam1Wins += prob;
                else if (j > i) probTeam2Wins += prob;
                else probDraw += prob;
            }
        }
        
        const totalProb = probTeam1Wins + probTeam2Wins + probDraw;
        scoreProbs.sort((a, b) => b.prob - a.prob);

        // 4. Exibir os resultados na tela
        displayResults(stats1, stats2, probTeam1Wins / totalProb, probDraw / totalProb, probTeam2Wins / totalProb, scoreProbs.slice(0, 5), totalProb);
        loader.classList.add('hidden');
        resultsContent.classList.remove('hidden');

    } catch (error) {
        // 5. Tratar erros
        loader.classList.add('hidden');
        errorPanel.textContent = `${error.message}`;
        errorPanel.classList.remove('hidden');
    } finally {
        // 6. Restaurar o botão
        calculateBtn.disabled = false;
        btnText.classList.remove('hidden');
        btnLoader.classList.add('hidden');
    }
});

function displayResults(stats1, stats2, prob1, probD, prob2, scores, totalProb) {
    // Garante que a propriedade players seja um array antes de usar o .map
    const team1Players = Array.isArray(stats1.players) ? stats1.players : [];
    const team2Players = Array.isArray(stats2.players) ? stats2.players : [];

    resultsContent.innerHTML = `
        <div class="text-center mb-4">
            <h2 class="text-3xl font-bold text-gray-800">${stats1.teamName} vs ${stats2.teamName}</h2>
        </div>
        
        <!-- Probabilidades -->
        <div>
            <h3 class="text-xl font-semibold text-gray-800 mb-3">Probabilidades do Jogo</h3>
            <div class="grid grid-cols-3 text-center bg-gray-100 p-4 rounded-lg">
                <div>
                    <div class="text-lg font-bold text-blue-600">${(prob1 * 100).toFixed(1)}%</div>
                    <div class="text-sm text-gray-600">${stats1.teamName}</div>
                </div>
                <div>
                    <div class="text-lg font-bold text-gray-700">${(probD * 100).toFixed(1)}%</div>
                    <div class="text-sm text-gray-600">Empate</div>
                </div>
                <div>
                    <div class="text-lg font-bold text-blue-600">${(prob2 * 100).toFixed(1)}%</div>
                    <div class="text-sm text-gray-600">${stats2.teamName}</div>
                </div>
            </div>
        </div>

        <!-- Placares -->
        <div>
            <h3 class="text-xl font-semibold text-gray-800 mb-3">Placares Mais Prováveis</h3>
            <table class="min-w-full bg-white rounded-lg border">
                <thead class="bg-gray-200">
                    <tr>
                        <th class="p-3 text-left text-sm font-semibold text-gray-600">Placar</th>
                        <th class="p-3 text-right text-sm font-semibold text-gray-600">Probabilidade</th>
                    </tr>
                </thead>
                <tbody>
                    ${scores.map(s => `
                        <tr class="border-t">
                            <td class="p-3 text-left font-medium">${s.score}</td>
                            <td class="p-3 text-right text-gray-600">${(s.prob / totalProb * 100).toFixed(2)}%</td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        </div>

        <!-- Jogadores -->
        <div>
            <h3 class="text-xl font-semibold text-gray-800 mb-3">Jogadores para Observar</h3>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div class="bg-gray-50 p-3 rounded-lg">
                    <h4 class="font-semibold text-gray-700 mb-2">${stats1.teamName}</h4>
                    <ul class="list-disc list-inside">${team1Players.length > 0 ? team1Players.map(p => `<li class="text-gray-600">${p}</li>`).join('') : '<li class="text-gray-400">Nenhum jogador encontrado.</li>'}</ul>
                </div>
                <div class="bg-gray-50 p-3 rounded-lg">
                    <h4 class="font-semibold text-gray-700 mb-2">${stats2.teamName}</h4>
                    <ul class="list-disc list-inside">${team2Players.length > 0 ? team2Players.map(p => `<li class="text-gray-600">${p}</li>`).join('') : '<li class="text-gray-400">Nenhum jogador encontrado.</li>'}</ul>
                </div>
            </div>
        </div>
    `;
}
