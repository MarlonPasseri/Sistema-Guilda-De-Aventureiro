const AUTH_DEFAULTS = {
    operator: { username: "operador", password: "GuildaOperador@123" },
    admin: { username: "estrategista", password: "GuildaAdmin@123" }
};

const fieldIds = [
    "organizacaoId",
    "recursoId",
    "missaoId",
    "aventureiroId",
    "usuarioCadastroId",
    "roleIdsCsv",
    "nome",
    "email",
    "tituloMissao",
    "termo",
    "tipoBusca",
    "classe",
    "especie",
    "ativo",
    "nivel",
    "nivelMinimo",
    "statusMissao",
    "statusUsuario",
    "nivelPerigo",
    "papelNaMissao",
    "categoria",
    "raridade",
    "precoMin",
    "precoMax",
    "recompensaOuro",
    "lealdade",
    "inicio",
    "fim",
    "page",
    "size",
    "sortBy",
    "direction",
    "ttlRankingSegundos",
    "historicoMongoHabilitado"
];

const elements = {
    activePresetLabel: document.getElementById("activePresetLabel"),
    accessHintLabel: document.getElementById("accessHintLabel"),
    lastStatusLabel: document.getElementById("lastStatusLabel"),
    clockLabel: document.getElementById("clockLabel"),
    coverageCountLabel: document.getElementById("coverageCountLabel"),
    groupCountLabel: document.getElementById("groupCountLabel"),
    historyCountLabel: document.getElementById("historyCountLabel"),
    activeModuleLabel: document.getElementById("activeModuleLabel"),
    activeMethodLabel: document.getElementById("activeMethodLabel"),
    responseModeLabel: document.getElementById("responseModeLabel"),
    presetHint: document.getElementById("presetHint"),
    authUsername: document.getElementById("authUsername"),
    authPassword: document.getElementById("authPassword"),
    authEnabled: document.getElementById("authEnabled"),
    methodInput: document.getElementById("methodInput"),
    pathInput: document.getElementById("pathInput"),
    bodyInput: document.getElementById("bodyInput"),
    requestPreview: document.getElementById("requestPreview"),
    curlPreview: document.getElementById("curlPreview"),
    output: document.getElementById("output"),
    metaStatus: document.getElementById("metaStatus"),
    metaTime: document.getElementById("metaTime"),
    metaType: document.getElementById("metaType"),
    metaTotal: document.getElementById("metaTotal"),
    metaAuth: document.getElementById("metaAuth"),
    catalogGroups: document.getElementById("catalogGroups"),
    actionSearch: document.getElementById("actionSearch"),
    historyList: document.getElementById("historyList")
};

const fields = Object.fromEntries(fieldIds.map((id) => [id, document.getElementById(id)]));
const historyState = [];
let currentPresetKey = "status";

const presets = {
    freeMode: {
        module: "Livre",
        label: "Modo livre",
        access: "public",
        method: "GET",
        path: "/status",
        hint: "Request carregado do historico. Edite rota, metodo e corpo livremente.",
        usedFields: [],
        query: () => ({}),
        body: null
    },
    status: {
        module: "Plataforma",
        label: "Status da API",
        access: "public",
        method: "GET",
        path: "/status",
        hint: "Valida se a API esta de pe e devolve as rotas-base da guilda.",
        usedFields: [],
        query: () => ({}),
        body: null
    },
    health: {
        module: "Plataforma",
        label: "Health check",
        access: "public",
        method: "GET",
        path: "/actuator/health",
        hint: "Leitura direta do Actuator para deploy e monitoramento.",
        usedFields: [],
        query: () => ({}),
        body: null
    },
    info: {
        module: "Plataforma",
        label: "Actuator info",
        access: "public",
        method: "GET",
        path: "/actuator/info",
        hint: "Consulta publica do endpoint info do Spring Boot.",
        usedFields: [],
        query: () => ({}),
        body: null
    },
    auditUsers: {
        module: "Audit",
        label: "Listar usuarios",
        access: "admin",
        method: "GET",
        path: "/audit/usuarios",
        hint: "Auditoria do schema legado por organizacao.",
        usedFields: ["organizacaoId"],
        query: (ctx) => ({ organizacaoId: ctx.organizacaoId }),
        body: null
    },
    auditUserDetail: {
        module: "Audit",
        label: "Consultar usuario por id",
        access: "admin",
        method: "GET",
        path: "/audit/usuarios/:recursoId",
        hint: "Consulta um usuario especifico da camada de audit.",
        usedFields: ["recursoId"],
        query: () => ({}),
        body: null
    },
    auditCreateUser: {
        module: "Audit",
        label: "Criar usuario",
        access: "admin",
        method: "POST",
        path: "/audit/usuarios",
        hint: "Cria um usuario e associa os papeis informados em roleIds CSV.",
        usedFields: ["organizacaoId", "nome", "email", "statusUsuario", "roleIdsCsv"],
        query: () => ({}),
        body: (ctx) => ({
            organizacaoId: asNumber(ctx.organizacaoId, 1),
            nome: ctx.nome || "Lyra Navegante",
            email: ctx.email || "lyra.navegante@guilda.local",
            senhaHash: "hash-demo-2026",
            status: ctx.statusUsuario || "ATIVO",
            roleIds: parseCsvNumbers(ctx.roleIdsCsv, [1])
        })
    },
    auditRoles: {
        module: "Audit",
        label: "Listar roles",
        access: "admin",
        method: "GET",
        path: "/audit/roles",
        hint: "Mostra os papeis e permissions associados a uma organizacao.",
        usedFields: ["organizacaoId"],
        query: (ctx) => ({ organizacaoId: ctx.organizacaoId }),
        body: null
    },
    auditRoleDetail: {
        module: "Audit",
        label: "Consultar role por id",
        access: "admin",
        method: "GET",
        path: "/audit/roles/:recursoId",
        hint: "Leitura individual de role para auditoria fina.",
        usedFields: ["recursoId"],
        query: () => ({}),
        body: null
    }
};

Object.assign(presets, {
    listAventureiros: {
        module: "Aventureiros",
        label: "Listar aventureiros",
        access: "operator",
        method: "GET",
        path: "/aventureiros",
        hint: "Consulta com filtros de classe, atividade, nivel e paginacao.",
        usedFields: ["organizacaoId", "classe", "ativo", "nivelMinimo", "page", "size", "sortBy", "direction"],
        query: (ctx) => ({
            organizacaoId: ctx.organizacaoId,
            classe: ctx.classe,
            ativo: ctx.ativo,
            nivelMinimo: ctx.nivelMinimo,
            page: ctx.page,
            size: ctx.size,
            sortBy: ctx.sortBy || "nome",
            direction: ctx.direction || "asc"
        }),
        body: null
    },
    searchAventureiros: {
        module: "Aventureiros",
        label: "Buscar aventureiros por nome",
        access: "operator",
        method: "GET",
        path: "/aventureiros/busca",
        hint: "Busca textual parcial com paginacao e ordenacao.",
        usedFields: ["organizacaoId", "nome", "page", "size", "sortBy", "direction"],
        query: (ctx) => ({
            organizacaoId: ctx.organizacaoId,
            nome: ctx.nome,
            page: ctx.page,
            size: ctx.size,
            sortBy: ctx.sortBy || "nome",
            direction: ctx.direction || "asc"
        }),
        body: null
    },
    detailAventureiro: {
        module: "Aventureiros",
        label: "Detalhar aventureiro",
        access: "operator",
        method: "GET",
        path: "/aventureiros/:recursoId",
        hint: "Consulta detalhada de um aventureiro especifico.",
        usedFields: ["organizacaoId", "recursoId"],
        query: (ctx) => ({ organizacaoId: ctx.organizacaoId }),
        body: null
    },
    createAventureiro: {
        module: "Aventureiros",
        label: "Registrar aventureiro",
        access: "operator",
        method: "POST",
        path: "/aventureiros",
        hint: "Cria um aventureiro novo ligado ao usuario de cadastro.",
        usedFields: ["organizacaoId", "usuarioCadastroId", "nome", "classe", "nivel"],
        query: () => ({}),
        body: (ctx) => ({
            organizacaoId: asNumber(ctx.organizacaoId, 1),
            usuarioCadastroId: asNumber(ctx.usuarioCadastroId, 1),
            nome: ctx.nome || "Kael do Norte",
            classe: ctx.classe || "GUERREIRO",
            nivel: asNumber(ctx.nivel, 5)
        })
    },
    updateAventureiro: {
        module: "Aventureiros",
        label: "Atualizar aventureiro",
        access: "operator",
        method: "PUT",
        path: "/aventureiros/:recursoId",
        hint: "Atualiza nome, classe e nivel mantendo a integridade do dominio.",
        usedFields: ["organizacaoId", "recursoId", "nome", "classe", "nivel"],
        query: (ctx) => ({ organizacaoId: ctx.organizacaoId }),
        body: (ctx) => ({
            nome: ctx.nome || "Kael do Norte",
            classe: ctx.classe || "GUERREIRO",
            nivel: asNumber(ctx.nivel, 5)
        })
    },
    encerrarAventureiro: {
        module: "Aventureiros",
        label: "Encerrar vinculo",
        access: "operator",
        method: "PATCH",
        path: "/aventureiros/:recursoId/encerrar-vinculo",
        hint: "Encerra o vinculo do aventureiro com a organizacao.",
        usedFields: ["organizacaoId", "recursoId"],
        query: (ctx) => ({ organizacaoId: ctx.organizacaoId }),
        body: null
    },
    recrutarAventureiro: {
        module: "Aventureiros",
        label: "Recrutar novamente",
        access: "operator",
        method: "PATCH",
        path: "/aventureiros/:recursoId/recrutar-novamente",
        hint: "Reativa um aventureiro previamente desligado.",
        usedFields: ["organizacaoId", "recursoId"],
        query: (ctx) => ({ organizacaoId: ctx.organizacaoId }),
        body: null
    },
    upsertCompanheiro: {
        module: "Aventureiros",
        label: "Definir ou substituir companheiro",
        access: "operator",
        method: "PUT",
        path: "/aventureiros/:recursoId/companheiro",
        hint: "Acopla um companheiro especial ao aventureiro informado.",
        usedFields: ["organizacaoId", "recursoId", "especie", "lealdade"],
        query: (ctx) => ({ organizacaoId: ctx.organizacaoId }),
        body: (ctx) => ({
            nome: "Nyx",
            especie: ctx.especie || "CORUJA",
            lealdade: asNumber(ctx.lealdade, 88)
        })
    },
    removeCompanheiro: {
        module: "Aventureiros",
        label: "Remover companheiro",
        access: "operator",
        method: "DELETE",
        path: "/aventureiros/:recursoId/companheiro",
        hint: "Remove o companheiro atual do aventureiro informado.",
        usedFields: ["organizacaoId", "recursoId"],
        query: (ctx) => ({ organizacaoId: ctx.organizacaoId }),
        body: null
    }
});

const presetGroups = [
    {
        title: "Plataforma e status",
        description: "Saude do backend, Actuator e rotas publicas de verificacao.",
        items: ["status", "health", "info"]
    },
    {
        title: "Audit e seguranca",
        description: "Usuarios, roles e o cadastro administrativo do legado.",
        items: ["auditUsers", "auditUserDetail", "auditCreateUser", "auditRoles", "auditRoleDetail"]
    },
    {
        title: "Aventureiros e companheiros",
        description: "Cadastro, consulta, atualizacao, desligamento, recontratacao e companheiros.",
        items: [
            "listAventureiros",
            "searchAventureiros",
            "detailAventureiro",
            "createAventureiro",
            "updateAventureiro",
            "encerrarAventureiro",
            "recrutarAventureiro",
            "upsertCompanheiro",
            "removeCompanheiro"
        ]
    },
    {
        title: "Missoes e painel tatico",
        description: "Fluxo operacional de missao e o ranking tatico dos ultimos 15 dias.",
        items: ["listMissoes", "missaoDetail", "createMissao", "addParticipacao", "top15dias"]
    },
    {
        title: "Relatorios da guilda",
        description: "Consolidacoes por aventureiro e por missao para leitura estrategica.",
        items: ["ranking", "missionMetrics"]
    },
    {
        title: "Marketplace e busca",
        description: "Consultas textuais, filtros e agregacoes sobre o indice guilda_loja.",
        items: [
            "productByName",
            "productByDescription",
            "productByPhrase",
            "productFuzzy",
            "productMulti",
            "productFilter",
            "productPriceRange",
            "productAdvanced",
            "aggCategory",
            "aggRarity",
            "aggAveragePrice",
            "aggPriceBands"
        ]
    },
    {
        title: "Historico e diagnosticos",
        description: "Historico Mongo, leitura da autoconfiguracao e ajuste dinamico da aplicacao.",
        items: ["searchHistory", "persistenceConditions", "runtimeConfig", "updateRuntimeConfig"]
    }
];

function asNumber(value, fallback) {
    const parsed = Number(value);
    return Number.isFinite(parsed) && value !== "" ? parsed : fallback;
}

function asDecimal(value, fallback) {
    if (value === "" || value === null || value === undefined) {
        return fallback;
    }
    return Number(value);
}

function parseCsvNumbers(value, fallback) {
    const parsed = String(value || "")
        .split(",")
        .map((item) => item.trim())
        .filter(Boolean)
        .map((item) => Number(item))
        .filter(Number.isFinite);

    return parsed.length ? parsed : fallback;
}

function collectContext() {
    return Object.fromEntries(Object.entries(fields).map(([key, input]) => [key, input.value.trim()]));
}

function cleanParams(params) {
    return Object.fromEntries(
        Object.entries(params).filter(([, value]) => value !== "" && value !== null && value !== undefined)
    );
}

function formatJson(data) {
    return JSON.stringify(data, null, 2);
}

function getPreset(key = currentPresetKey) {
    return presets[key] || presets.freeMode;
}

function accessLabel(access) {
    if (access === "admin") {
        return "ADMIN";
    }
    if (access === "operator") {
        return "OPERADOR ou ADMIN";
    }
    return "PUBLICO";
}

function pathWithParams(path, ctx) {
    return path.replace(/:([a-zA-Z0-9_]+)/g, (_, token) => encodeURIComponent(ctx[token] || "1"));
}

function buildUrl(pathValue, presetKey) {
    const preset = getPreset(presetKey);
    const ctx = collectContext();
    const resolvedPath = pathWithParams(pathValue || preset.path, ctx);
    const url = new URL(resolvedPath, window.location.origin);

    Object.entries(cleanParams(preset.query(ctx))).forEach(([key, value]) => {
        url.searchParams.set(key, value);
    });

    return url.pathname + url.search;
}

function generatedBodyText(presetKey) {
    const preset = getPreset(presetKey);
    if (!preset.body) {
        return "";
    }
    return formatJson(preset.body(collectContext()));
}

function decorateMeta(element, text, state) {
    element.textContent = text;
    element.className = state ? `meta-chip ${state}` : "meta-chip";
}

function currentAuthModeText() {
    if (!elements.authEnabled.checked) {
        return "sem auth";
    }
    if (!elements.authUsername.value || !elements.authPassword.value) {
        return "credencial incompleta";
    }
    return `basic ${elements.authUsername.value}`;
}

function buildCurlCommand(method, url, bodyText) {
    const parts = ["curl.exe"];

    if (elements.authEnabled.checked && elements.authUsername.value && elements.authPassword.value) {
        parts.push(`-u "${elements.authUsername.value}:${elements.authPassword.value}"`);
    }

    parts.push(`-X ${method}`);
    parts.push(`"${window.location.origin}${url}"`);

    if (bodyText && !["GET", "DELETE"].includes(method)) {
        const escaped = bodyText.replaceAll("\"", "\\\"");
        parts.push("-H \"Content-Type: application/json\"");
        parts.push(`--data "${escaped}"`);
    }

    return parts.join(" ");
}

function syncPreview() {
    const preset = getPreset();
    if (elements.bodyInput.dataset.generated === "true" && preset.body) {
        elements.bodyInput.value = generatedBodyText(currentPresetKey);
    }

    const method = elements.methodInput.value;
    const url = buildUrl(elements.pathInput.value.trim() || preset.path, currentPresetKey);
    const bodyText = elements.bodyInput.value.trim();

    elements.requestPreview.textContent = `${method} ${url}`;
    elements.curlPreview.textContent = buildCurlCommand(method, url, bodyText);
    elements.activeMethodLabel.textContent = method;
}

function fillAuth(role) {
    const credentials = role === "admin" ? AUTH_DEFAULTS.admin : AUTH_DEFAULTS.operator;
    elements.authUsername.value = credentials.username;
    elements.authPassword.value = credentials.password;
    elements.authEnabled.checked = true;
    syncPreview();
}

function resetContext() {
    Object.values(fields).forEach((input) => {
        input.value = input.dataset.default || "";
    });
    syncPreview();
}

function highlightFields(usedFields) {
    document.querySelectorAll("[data-field]").forEach((node) => {
        node.classList.toggle("active-field", usedFields.includes(node.dataset.field));
    });
}

function escapeHtml(text) {
    return String(text)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;");
}

function renderCatalog(filterText = "") {
    const normalizedFilter = filterText.trim().toLowerCase();

    const markup = presetGroups.map((group) => {
        const items = group.items
            .map((key) => ({ key, preset: presets[key] }))
            .filter(({ preset }) => {
                if (!normalizedFilter) {
                    return true;
                }
                const haystack = [
                    preset.label,
                    preset.hint,
                    preset.path,
                    preset.module,
                    accessLabel(preset.access)
                ].join(" ").toLowerCase();
                return haystack.includes(normalizedFilter);
            });

        if (!items.length) {
            return "";
        }

        const actions = items.map(({ key, preset }) => `
            <button type="button" class="module-action ${key === currentPresetKey ? "is-active" : ""}" data-preset="${key}">
                <div class="module-action-header">
                    <strong>${escapeHtml(preset.label)}</strong>
                    <div class="badge-row">
                        <span class="badge method-${preset.method.toLowerCase()}">${escapeHtml(preset.method)}</span>
                        <span class="badge access-${escapeHtml(preset.access)}">${escapeHtml(accessLabel(preset.access))}</span>
                    </div>
                </div>
                <small>${escapeHtml(preset.hint)}</small>
                <code>${escapeHtml(preset.path)}</code>
            </button>
        `).join("");

        return `
            <section class="module-card">
                <h3>${escapeHtml(group.title)}</h3>
                <p>${escapeHtml(group.description)}</p>
                <div class="module-actions">${actions}</div>
            </section>
        `;
    }).join("");

    elements.catalogGroups.innerHTML = markup || '<p class="history-empty">Nenhuma acao encontrada para esse filtro.</p>';
}

function updateCounters() {
    elements.coverageCountLabel.textContent = `${Object.keys(presets).length - 1} acoes`;
    elements.groupCountLabel.textContent = `${presetGroups.length} modulos`;
    elements.historyCountLabel.textContent = String(historyState.length);
}

function applyPreset(presetKey) {
    currentPresetKey = presetKey;
    const preset = getPreset(presetKey);

    elements.activePresetLabel.textContent = preset.label;
    elements.accessHintLabel.textContent = accessLabel(preset.access);
    elements.activeModuleLabel.textContent = preset.module;
    elements.presetHint.textContent = preset.hint;
    elements.methodInput.value = preset.method;
    elements.pathInput.value = preset.path;
    elements.bodyInput.value = preset.body ? generatedBodyText(presetKey) : "";
    elements.bodyInput.dataset.generated = preset.body ? "true" : "false";
    elements.authEnabled.checked = preset.access !== "public";

    highlightFields(preset.usedFields);
    renderCatalog(elements.actionSearch.value);
    syncPreview();
}

function inferTotalLabel(response, body) {
    const totalCount = response.headers.get("x-total-count");
    if (totalCount) {
        return totalCount;
    }
    if (Array.isArray(body)) {
        return String(body.length);
    }
    if (body && typeof body === "object") {
        return `${Object.keys(body).length} campos`;
    }
    return "--";
}

function pushHistory(item) {
    historyState.unshift(item);
    historyState.splice(8);
    renderHistory();
    updateCounters();
}

function renderHistory() {
    if (!historyState.length) {
        elements.historyList.innerHTML = '<p class="history-empty">Nenhuma chamada executada ainda.</p>';
        return;
    }

    elements.historyList.innerHTML = historyState.map((item, index) => `
        <div class="history-item">
            <strong>${escapeHtml(item.method)} ${escapeHtml(item.url)}</strong>
            <span>${escapeHtml(item.label)} | status ${escapeHtml(String(item.status))} | ${escapeHtml(item.authMode)}</span>
            <div class="history-actions">
                <button type="button" class="action-button" data-history="${index}" data-mode="load">Carregar</button>
                <button type="button" class="action-button primary" data-history="${index}" data-mode="run">Rodar de novo</button>
            </div>
        </div>
    `).join("");
}

function loadFromHistory(item) {
    currentPresetKey = "freeMode";
    elements.activePresetLabel.textContent = item.label;
    elements.accessHintLabel.textContent = "PERSONALIZADO";
    elements.activeModuleLabel.textContent = "Livre";
    elements.presetHint.textContent = presets.freeMode.hint;
    elements.methodInput.value = item.method;
    elements.pathInput.value = item.url;
    elements.bodyInput.value = item.bodyText;
    elements.bodyInput.dataset.generated = "false";
    highlightFields([]);
    renderCatalog(elements.actionSearch.value);
    syncPreview();
}

function safeJsonParse(text) {
    if (!text) {
        return null;
    }

    try {
        return JSON.parse(text);
    } catch (error) {
        return text;
    }
}

async function executeRequest(request) {
    const liveRequest = request || {
        label: getPreset().label,
        method: elements.methodInput.value,
        url: buildUrl(elements.pathInput.value.trim() || getPreset().path, currentPresetKey),
        bodyText: elements.bodyInput.value.trim()
    };

    const options = {
        method: liveRequest.method,
        headers: {
            Accept: "application/json"
        }
    };

    if (elements.authEnabled.checked && elements.authUsername.value && elements.authPassword.value) {
        options.headers.Authorization = `Basic ${btoa(`${elements.authUsername.value}:${elements.authPassword.value}`)}`;
    }

    if (liveRequest.bodyText && !["GET", "DELETE"].includes(liveRequest.method)) {
        options.headers["Content-Type"] = "application/json";
        options.body = liveRequest.bodyText;
    }

    elements.output.textContent = `Consultando ${liveRequest.url}...`;
    decorateMeta(elements.metaStatus, "status ...", "");
    decorateMeta(elements.metaTime, "tempo ...", "");
    decorateMeta(elements.metaType, "tipo ...", "");
    decorateMeta(elements.metaTotal, "total ...", "");
    decorateMeta(elements.metaAuth, currentAuthModeText(), "");

    const startedAt = performance.now();

    try {
        const response = await fetch(liveRequest.url, options);
        const elapsed = Math.round(performance.now() - startedAt);
        const contentType = (response.headers.get("content-type") || "sem tipo").split(";")[0];
        const text = await response.text();

        let parsedBody = text;
        try {
            parsedBody = text ? JSON.parse(text) : null;
        } catch (error) {
            parsedBody = text;
        }

        decorateMeta(elements.metaStatus, `status ${response.status}`, response.ok ? "ok" : "error");
        decorateMeta(elements.metaTime, `tempo ${elapsed} ms`, "");
        decorateMeta(elements.metaType, `tipo ${contentType}`, "");
        decorateMeta(elements.metaTotal, `total ${inferTotalLabel(response, parsedBody)}`, "");
        decorateMeta(elements.metaAuth, currentAuthModeText(), elements.authEnabled.checked ? "ok" : "");

        elements.responseModeLabel.textContent = contentType.includes("json") ? "JSON" : "RAW";
        elements.lastStatusLabel.textContent = `${response.status} ${response.ok ? "ok" : "erro"}`;
        elements.output.textContent = formatJson({
            request: {
                method: liveRequest.method,
                url: liveRequest.url,
                auth: currentAuthModeText(),
                body: safeJsonParse(liveRequest.bodyText)
            },
            response: {
                status: response.status,
                ok: response.ok,
                headers: {
                    contentType,
                    location: response.headers.get("location"),
                    totalCount: response.headers.get("x-total-count"),
                    totalPages: response.headers.get("x-total-pages")
                },
                body: parsedBody
            }
        });

        pushHistory({
            label: liveRequest.label,
            method: liveRequest.method,
            url: liveRequest.url,
            bodyText: liveRequest.bodyText,
            status: response.status,
            authMode: currentAuthModeText()
        });
    } catch (error) {
        decorateMeta(elements.metaStatus, "status falhou", "error");
        decorateMeta(elements.metaTime, "tempo --", "");
        decorateMeta(elements.metaType, "tipo rede", "error");
        decorateMeta(elements.metaTotal, "total --", "");
        decorateMeta(elements.metaAuth, currentAuthModeText(), elements.authEnabled.checked ? "error" : "");
        elements.lastStatusLabel.textContent = "Erro de rede";
        elements.output.textContent = formatJson({
            request: liveRequest,
            auth: currentAuthModeText(),
            error: String(error)
        });
    }
}

function copyCurlToClipboard() {
    navigator.clipboard.writeText(elements.curlPreview.textContent)
        .then(() => {
            elements.lastStatusLabel.textContent = "curl copiado";
        })
        .catch(() => {
            elements.lastStatusLabel.textContent = "Falha ao copiar curl";
        });
}

function updateClock() {
    elements.clockLabel.textContent = new Date().toLocaleTimeString("pt-BR");
}

document.getElementById("authOperatorButton").addEventListener("click", () => fillAuth("operator"));
document.getElementById("authAdminButton").addEventListener("click", () => fillAuth("admin"));
document.getElementById("authClearButton").addEventListener("click", () => {
    elements.authUsername.value = "";
    elements.authPassword.value = "";
    elements.authEnabled.checked = false;
    syncPreview();
});

document.getElementById("previewButton").addEventListener("click", syncPreview);
document.getElementById("executeButton").addEventListener("click", () => executeRequest());
document.getElementById("regenerateButton").addEventListener("click", () => {
    if (getPreset().body) {
        elements.bodyInput.value = generatedBodyText(currentPresetKey);
        elements.bodyInput.dataset.generated = "true";
    }
    syncPreview();
});
document.getElementById("copyCurlButton").addEventListener("click", copyCurlToClipboard);
document.getElementById("clearButton").addEventListener("click", () => {
    elements.bodyInput.value = "";
    elements.bodyInput.dataset.generated = "false";
    syncPreview();
});
document.getElementById("resetContextButton").addEventListener("click", resetContext);

Object.values(fields).forEach((input) => input.addEventListener("input", syncPreview));
[elements.methodInput, elements.pathInput, elements.authUsername, elements.authPassword, elements.authEnabled]
    .forEach((input) => input.addEventListener("input", syncPreview));

elements.bodyInput.addEventListener("input", () => {
    elements.bodyInput.dataset.generated = "false";
    syncPreview();
});

elements.actionSearch.addEventListener("input", () => renderCatalog(elements.actionSearch.value));

elements.catalogGroups.addEventListener("click", (event) => {
    const button = event.target.closest("button[data-preset]");
    if (!button) {
        return;
    }
    applyPreset(button.dataset.preset);
});

elements.historyList.addEventListener("click", (event) => {
    const button = event.target.closest("button[data-history]");
    if (!button) {
        return;
    }
    const item = historyState[Number(button.dataset.history)];
    if (!item) {
        return;
    }
    loadFromHistory(item);
    if (button.dataset.mode === "run") {
        executeRequest(item);
    }
});

updateCounters();
updateClock();
setInterval(updateClock, 1000);
renderCatalog();
applyPreset("status");
executeRequest();

Object.assign(presets, {
    listMissoes: {
        module: "Missoes",
        label: "Listar missoes",
        access: "operator",
        method: "GET",
        path: "/missoes",
        hint: "Leitura operacional com filtros por status, perigo e periodo.",
        usedFields: ["organizacaoId", "statusMissao", "nivelPerigo", "inicio", "fim", "page", "size", "sortBy", "direction"],
        query: (ctx) => ({
            organizacaoId: ctx.organizacaoId,
            status: ctx.statusMissao,
            nivelPerigo: ctx.nivelPerigo,
            dataDe: ctx.inicio,
            dataAte: ctx.fim,
            page: ctx.page,
            size: ctx.size,
            sortBy: ctx.sortBy || "createdAt",
            direction: ctx.direction || "desc"
        }),
        body: null
    },
    missaoDetail: {
        module: "Missoes",
        label: "Detalhar missao",
        access: "operator",
        method: "GET",
        path: "/missoes/:missaoId",
        hint: "Consulta completa de uma missao com participacoes.",
        usedFields: ["organizacaoId", "missaoId"],
        query: (ctx) => ({ organizacaoId: ctx.organizacaoId }),
        body: null
    },
    createMissao: {
        module: "Missoes",
        label: "Criar missao",
        access: "operator",
        method: "POST",
        path: "/missoes",
        hint: "Abre uma missao nova com titulo, status e nivel de perigo.",
        usedFields: ["organizacaoId", "tituloMissao", "nivelPerigo", "statusMissao", "inicio", "fim"],
        query: () => ({}),
        body: (ctx) => ({
            organizacaoId: asNumber(ctx.organizacaoId, 1),
            titulo: ctx.tituloMissao || "Patrulha do Vale Nebuloso",
            nivelPerigo: ctx.nivelPerigo || "MEDIO",
            status: ctx.statusMissao || "PLANEJADA",
            dataInicio: ctx.inicio || null,
            dataTermino: ctx.fim || null
        })
    },
    addParticipacao: {
        module: "Missoes",
        label: "Adicionar participacao",
        access: "operator",
        method: "POST",
        path: "/missoes/:missaoId/participacoes",
        hint: "Vincula um aventureiro a uma missao com papel e recompensa.",
        usedFields: ["organizacaoId", "missaoId", "aventureiroId", "papelNaMissao", "recompensaOuro"],
        query: (ctx) => ({ organizacaoId: ctx.organizacaoId }),
        body: (ctx) => ({
            aventureiroId: asNumber(ctx.aventureiroId, 1),
            papelNaMissao: ctx.papelNaMissao || "LIDER",
            recompensaOuro: asDecimal(ctx.recompensaOuro, 150),
            destaque: true
        })
    },
    top15dias: {
        module: "Missoes",
        label: "Top tatico dos ultimos 15 dias",
        access: "operator",
        method: "GET",
        path: "/missoes/top15dias",
        hint: "Consulta o ranking tatico consolidado da view materializada de leitura.",
        usedFields: [],
        query: () => ({}),
        body: null
    },
    ranking: {
        module: "Relatorios",
        label: "Ranking de participacao",
        access: "operator",
        method: "GET",
        path: "/relatorios/aventureiros/ranking",
        hint: "Agregado por periodo com filtro opcional por status da missao.",
        usedFields: ["organizacaoId", "inicio", "fim", "statusMissao"],
        query: (ctx) => ({
            organizacaoId: ctx.organizacaoId,
            inicio: ctx.inicio,
            fim: ctx.fim,
            statusMissao: ctx.statusMissao
        }),
        body: null
    },
    missionMetrics: {
        module: "Relatorios",
        label: "Metricas de missoes",
        access: "operator",
        method: "GET",
        path: "/relatorios/missoes",
        hint: "Resumo consolidado de participantes e recompensas por missao.",
        usedFields: ["organizacaoId", "inicio", "fim"],
        query: (ctx) => ({
            organizacaoId: ctx.organizacaoId,
            inicio: ctx.inicio,
            fim: ctx.fim
        }),
        body: null
    }
});

Object.assign(presets, {
    productByName: {
        module: "Marketplace",
        label: "Buscar produto por nome",
        access: "operator",
        method: "GET",
        path: "/produtos/busca/nome",
        hint: "Busca textual simples no campo nome do indice guilda_loja.",
        usedFields: ["termo"],
        query: (ctx) => ({ termo: ctx.termo }),
        body: null
    },
    productByDescription: {
        module: "Marketplace",
        label: "Buscar por descricao",
        access: "operator",
        method: "GET",
        path: "/produtos/busca/descricao",
        hint: "Explora match textual no campo descricao.",
        usedFields: ["termo"],
        query: (ctx) => ({ termo: ctx.termo }),
        body: null
    },
    productByPhrase: {
        module: "Marketplace",
        label: "Buscar frase exata",
        access: "operator",
        method: "GET",
        path: "/produtos/busca/frase",
        hint: "Retorna produtos cuja descricao contem a frase exata informada.",
        usedFields: ["termo"],
        query: (ctx) => ({ termo: ctx.termo }),
        body: null
    },
    productFuzzy: {
        module: "Marketplace",
        label: "Buscar fuzzy por nome",
        access: "operator",
        method: "GET",
        path: "/produtos/busca/fuzzy",
        hint: "Tolerancia a erro de digitacao para o nome do produto.",
        usedFields: ["termo"],
        query: (ctx) => ({ termo: ctx.termo }),
        body: null
    },
    productMulti: {
        module: "Marketplace",
        label: "Buscar em nome e descricao",
        access: "operator",
        method: "GET",
        path: "/produtos/busca/multicampos",
        hint: "Busca multicampos para encontrar itens por contexto.",
        usedFields: ["termo"],
        query: (ctx) => ({ termo: ctx.termo }),
        body: null
    },
    productFilter: {
        module: "Marketplace",
        label: "Busca textual com filtro",
        access: "operator",
        method: "GET",
        path: "/produtos/busca/com-filtro",
        hint: "Cruza descricao textual com categoria estruturada.",
        usedFields: ["termo", "categoria"],
        query: (ctx) => ({ termo: ctx.termo, categoria: ctx.categoria }),
        body: null
    },
    productPriceRange: {
        module: "Marketplace",
        label: "Buscar por faixa de preco",
        access: "operator",
        method: "GET",
        path: "/produtos/busca/faixa-preco",
        hint: "Recupera produtos dentro de uma faixa monetaria.",
        usedFields: ["precoMin", "precoMax"],
        query: (ctx) => ({ min: ctx.precoMin, max: ctx.precoMax }),
        body: null
    },
    productAdvanced: {
        module: "Marketplace",
        label: "Busca avancada",
        access: "operator",
        method: "GET",
        path: "/produtos/busca/avancada",
        hint: "Cruza categoria, raridade e faixa de preco.",
        usedFields: ["categoria", "raridade", "precoMin", "precoMax"],
        query: (ctx) => ({
            categoria: ctx.categoria,
            raridade: ctx.raridade,
            min: ctx.precoMin,
            max: ctx.precoMax
        }),
        body: null
    },
    aggCategory: {
        module: "Marketplace",
        label: "Agregacao por categoria",
        access: "operator",
        method: "GET",
        path: "/produtos/agregacoes/por-categoria",
        hint: "Conta quantos itens existem em cada categoria.",
        usedFields: [],
        query: () => ({}),
        body: null
    },
    aggRarity: {
        module: "Marketplace",
        label: "Agregacao por raridade",
        access: "operator",
        method: "GET",
        path: "/produtos/agregacoes/por-raridade",
        hint: "Agrupa os produtos por raridade do indice.",
        usedFields: [],
        query: () => ({}),
        body: null
    },
    aggAveragePrice: {
        module: "Marketplace",
        label: "Preco medio",
        access: "operator",
        method: "GET",
        path: "/produtos/agregacoes/preco-medio",
        hint: "Media de preco de todos os produtos indexados.",
        usedFields: [],
        query: () => ({}),
        body: null
    },
    aggPriceBands: {
        module: "Marketplace",
        label: "Faixas de preco",
        access: "operator",
        method: "GET",
        path: "/produtos/agregacoes/faixas-preco",
        hint: "Distribui o estoque em buckets de preco estrategicos.",
        usedFields: [],
        query: () => ({}),
        body: null
    },
    searchHistory: {
        module: "Historico e diagnosticos",
        label: "Historico de buscas",
        access: "operator",
        method: "GET",
        path: "/produtos/buscas/historico",
        hint: "Consulta as ultimas buscas persistidas no MongoDB.",
        usedFields: ["tipoBusca"],
        query: (ctx) => ({ tipoBusca: ctx.tipoBusca }),
        body: null
    },
    persistenceConditions: {
        module: "Historico e diagnosticos",
        label: "Diagnostico de persistencia",
        access: "admin",
        method: "GET",
        path: "/diagnosticos/autoconfiguracao/persistencia",
        hint: "Le as auto-configuracoes ativas e bloqueadas para a camada de dados.",
        usedFields: [],
        query: () => ({}),
        body: null
    },
    runtimeConfig: {
        module: "Historico e diagnosticos",
        label: "Consultar configuracoes dinamicas",
        access: "admin",
        method: "GET",
        path: "/diagnosticos/configuracoes",
        hint: "Mostra o TTL do ranking e o estado do historico Mongo.",
        usedFields: [],
        query: () => ({}),
        body: null
    },
    updateRuntimeConfig: {
        module: "Historico e diagnosticos",
        label: "Atualizar configuracoes dinamicas",
        access: "admin",
        method: "PATCH",
        path: "/diagnosticos/configuracoes",
        hint: "Ajusta TTL do ranking e habilita ou desabilita o historico Mongo em runtime.",
        usedFields: ["ttlRankingSegundos", "historicoMongoHabilitado"],
        query: (ctx) => ({
            ttlRankingSegundos: ctx.ttlRankingSegundos,
            historicoMongoHabilitado: ctx.historicoMongoHabilitado
        }),
        body: null
    }
});
