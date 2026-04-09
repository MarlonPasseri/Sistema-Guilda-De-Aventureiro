package br.com.guilda.registro.config;

public final class RuntimePropertyKeys {

    public static final String DYNAMIC_PROPERTY_SOURCE = "guildaDynamicProperties";
    public static final String RANKING_CACHE_TTL_SEGUNDOS = "guilda.cache.ranking.ttl-segundos";
    public static final String HISTORICO_MONGO_HABILITADO = "guilda.marketplace.historico.mongo.habilitado";
    public static final String RANKING_CACHE_REDIS_KEY = "missoes:top15dias";

    private RuntimePropertyKeys() {
    }
}
