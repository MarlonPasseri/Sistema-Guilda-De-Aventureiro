package br.com.guilda.registro;

import br.com.guilda.registro.service.AuditService;
import br.com.guilda.registro.service.AventureiroService;
import br.com.guilda.registro.service.ConfiguracaoDinamicaService;
import br.com.guilda.registro.service.HistoricoBuscaProdutoService;
import br.com.guilda.registro.service.MissaoService;
import br.com.guilda.registro.service.PainelTaticoMissaoService;
import br.com.guilda.registro.service.ProdutoMarketplaceService;
import br.com.guilda.registro.service.RelatorioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:securitytest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.properties.hibernate.default_schema=PUBLIC",
    "spring.sql.init.mode=never",
    "spring.data.mongodb.uri=mongodb://localhost:27017/securitytest",
    "spring.elasticsearch.uris=http://localhost:9200",
    "guilda.marketplace.historico.mongo.habilitado=false"
})
@AutoConfigureMockMvc
class ApplicationSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuditService auditService;

    @MockBean
    private AventureiroService aventureiroService;

    @MockBean
    private MissaoService missaoService;

    @MockBean
    private RelatorioService relatorioService;

    @MockBean
    private PainelTaticoMissaoService painelTaticoMissaoService;

    @MockBean
    private ProdutoMarketplaceService produtoMarketplaceService;

    @MockBean
    private HistoricoBuscaProdutoService historicoBuscaProdutoService;

    @MockBean
    private ConfiguracaoDinamicaService configuracaoDinamicaService;

    @Test
    void devePermitirStatusSemAutenticacao() throws Exception {
        mockMvc.perform(get("/status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mensagem").value("API da Guilda em execucao"));
    }

    @Test
    void devePermitirPaginaInicialSemAutenticacao() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(forwardedUrl("index.html"));
    }

    @Test
    void deveBloquearEndpointProtegidoSemAutenticacao() throws Exception {
        mockMvc.perform(get("/audit/usuarios").param("organizacaoId", "1"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void devePermitirAcessoAdminAoEndpointDeDiagnostico() throws Exception {
        mockMvc.perform(get("/diagnosticos/configuracoes")
                .with(httpBasic("estrategista", "GuildaAdmin@123")))
            .andExpect(status().isOk());
    }

    @Test
    void deveNegarAcessoDoOperadorAoEndpointAdministrativo() throws Exception {
        mockMvc.perform(get("/diagnosticos/configuracoes")
                .with(httpBasic("operador", "GuildaOperador@123")))
            .andExpect(status().isForbidden());
    }
}
