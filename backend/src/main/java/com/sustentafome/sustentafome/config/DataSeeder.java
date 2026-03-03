package com.sustentafome.sustentafome.config;

import com.sustentafome.sustentafome.auth.AppUser;
import com.sustentafome.sustentafome.auth.UserRepository;
import com.sustentafome.sustentafome.auth.UserRole;
import com.sustentafome.sustentafome.inventory.Armazem;
import com.sustentafome.sustentafome.inventory.ArmazemRepository;
import com.sustentafome.sustentafome.production.Product;
import com.sustentafome.sustentafome.production.ProductRepository;
import com.sustentafome.sustentafome.production.UnitType;
import com.sustentafome.sustentafome.production.UnidadeProdutiva;
import com.sustentafome.sustentafome.production.UnidadeProdutivaRepository;
import com.sustentafome.sustentafome.donation.EntidadeBeneficiaria;
import com.sustentafome.sustentafome.donation.EntidadeBeneficiariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository,
                                ProductRepository productRepository,
                                UnidadeProdutivaRepository unidadeProdutivaRepository,
                                EntidadeBeneficiariaRepository beneficiariaRepository,
                                ArmazemRepository armazemRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.saveAll(List.of(
                        AppUser.builder().firstName("Admin").lastName("User").username("admin").email("admin@example.com").phone("111111111").emailVerified(true).password(passwordEncoder.encode("admin123")).role(UserRole.ADMIN).build(),
                        AppUser.builder().firstName("Maria").lastName("Operadora").username("operador").email("operador@example.com").phone("222222222").emailVerified(true).password(passwordEncoder.encode("operador123")).role(UserRole.OPERADOR).build(),
                        AppUser.builder().firstName("Lucas").lastName("Logistica").username("logistica").email("logistica@example.com").phone("333333333").emailVerified(true).password(passwordEncoder.encode("log123")).role(UserRole.LOGISTICA).build(),
                        AppUser.builder().firstName("Olga").lastName("Ong").username("ong").email("ong@example.com").phone("444444444").emailVerified(true).password(passwordEncoder.encode("ong123")).role(UserRole.ONG).build()
                ));
            }

            if (productRepository.count() == 0) {
                productRepository.saveAll(List.of(
                        new Product(null, "Batata Inglesa", "ALIMENTO"),
                        new Product(null, "Batata Doce", "ALIMENTO"),
                        new Product(null, "Shiitake", "ALIMENTO"),
                        new Product(null, "Cogumelo Ostra", "ALIMENTO/RACAO"),
                        new Product(null, "Peixe", "ALIMENTO"),
                        new Product(null, "Alcool", "ENERGIA"),
                        new Product(null, "Biomassa", "INSUMO"),
                        new Product(null, "Biofertilizante", "INSUMO"),
                        new Product(null, "Biogas", "ENERGIA")
                ));
            }

            if (unidadeProdutivaRepository.count() == 0) {
                unidadeProdutivaRepository.saveAll(List.of(
                        new UnidadeProdutiva(null, "Estufa Batata", UnitType.ESTUFA_BATATA, "Setor Norte", true),
                        new UnidadeProdutiva(null, "Estufa Cana", UnitType.ESTUFA_CANA, "Setor Leste", true),
                        new UnidadeProdutiva(null, "Aquicultura Peixes", UnitType.AQUICULTURA, "Tanque 1", true),
                        new UnidadeProdutiva(null, "Cogumelo Shiitake", UnitType.COGUMELO_SHIITAKE, "Sala 2", true),
                        new UnidadeProdutiva(null, "Cogumelo Ostra", UnitType.COGUMELO_OSTRA, "Sala 3", true)
                ));
            }

            if (armazemRepository.count() == 0) {
                armazemRepository.save(new Armazem(null, "Armazem Central", "Patio 1"));
            }

            if (beneficiariaRepository.count() == 0) {
                beneficiariaRepository.saveAll(List.of(
                        new EntidadeBeneficiaria(null, "Cozinha Solidaria Centro", "COZINHA", "Centro"),
                        new EntidadeBeneficiaria(null, "ONG Maos Dadas", "ONG", "Zona Norte"),
                        new EntidadeBeneficiaria(null, "Comunidade Esperanca", "COMUNIDADE", "Zona Leste")
                ));
            }
        };
    }
}
