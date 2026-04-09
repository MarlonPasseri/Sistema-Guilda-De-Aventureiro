package br.com.guilda.registro.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "guilda.security")
public class GuildaSecurityProperties {

    private final User admin = new User();
    private final User operador = new User();

    public User getAdmin() {
        return admin;
    }

    public User getOperador() {
        return operador;
    }

    public static class User {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
