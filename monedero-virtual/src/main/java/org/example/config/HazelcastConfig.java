package org.example.config;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Collections;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.hazelcast.enabled", havingValue = "true", matchIfMissing = true)
public class HazelcastConfig {

    @Bean
    @Primary
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();
        config.setInstanceName("monedero-virtual-instance");
        
        // Configuración de red (para desarrollo, solo local)
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPort(5701);
        networkConfig.setPortAutoIncrement(true);
        networkConfig.getJoin().getMulticastConfig().setEnabled(false);
        networkConfig.getJoin().getTcpIpConfig()
                .setEnabled(true)
                .setMembers(Collections.singletonList("127.0.0.1"));
        
        // Configuración de mapas para caché
        // Mapa para puntos de usuario
        MapConfig userPointsMapConfig = new MapConfig("userPoints");
        userPointsMapConfig.setBackupCount(1);
        userPointsMapConfig.setEvictionConfig(
                new EvictionConfig()
                        .setEvictionPolicy(EvictionPolicy.LRU)
                        .setMaxSizePolicy(MaxSizePolicy.PER_NODE)
                        .setSize(10000));
        userPointsMapConfig.setTimeToLiveSeconds(3600); // 1 hora
        config.addMapConfig(userPointsMapConfig);
        
        // Mapa para rangos de usuario
        MapConfig userRanksMapConfig = new MapConfig("userRanks");
        userRanksMapConfig.setBackupCount(1);
        userRanksMapConfig.setEvictionConfig(
                new EvictionConfig()
                        .setEvictionPolicy(EvictionPolicy.LRU)
                        .setMaxSizePolicy(MaxSizePolicy.PER_NODE)
                        .setSize(10000));
        userRanksMapConfig.setTimeToLiveSeconds(1800); // 30 minutos
        config.addMapConfig(userRanksMapConfig);
        
        // Mapa para saldos de monederos
        MapConfig walletBalancesMapConfig = new MapConfig("walletBalances");
        walletBalancesMapConfig.setBackupCount(1);
        walletBalancesMapConfig.setEvictionConfig(
                new EvictionConfig()
                        .setEvictionPolicy(EvictionPolicy.LRU)
                        .setMaxSizePolicy(MaxSizePolicy.PER_NODE)
                        .setSize(10000));
        walletBalancesMapConfig.setTimeToLiveSeconds(30); // 30 segundos
        config.addMapConfig(walletBalancesMapConfig);
        
        // Configuración para notificaciones
        config.addTopicConfig(new TopicConfig("notifications"));
        
        return Hazelcast.newHazelcastInstance(config);
    }
}
