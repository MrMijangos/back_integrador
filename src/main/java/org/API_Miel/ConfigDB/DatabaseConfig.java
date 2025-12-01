package org.API_Miel.ConfigDB;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;
    private static boolean isInitialized = false; 

    public static void initialize() {
        if (isInitialized) return;

        // 1. Cargar .env
        // QUITAMOS .filename() para evitar el error. Por defecto busca ".env"
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        // 2. Obtener variables
        String host = dotenv.get("DB_HOST");
        String port = dotenv.get("DB_PORT");
        String name = dotenv.get("DB_NAME");
        String user = dotenv.get("DB_USER");
        // CORREGIDO: Usamos DB_PASS que es como se llama en tu archivo
        String pass = dotenv.get("DB_PASS"); 

        // 3. LOGICA DE RESPALDO (PLAN B)
        // Si host es null, significa que no leyó el archivo .env, así que usamos los datos directos.
        if (host == null) {
            System.out.println("⚠️ ADVERTENCIA: No se leyó el .env. Usando credenciales de respaldo.");
            host = "54.88.20.156";  
            port = "3306";
            name = "ecommerce_miel";
            user = "fernando";      
            pass = "fer123";        
        } else {
            System.out.println("✅ Archivo .env detectado correctamente.");
        }

        DB_USER = user;
        DB_PASSWORD = pass;

        // 4. Construir URL
        DB_URL = "jdbc:mysql://" + host + ":" + port + "/" + name
                + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

        System.out.println("🔌 URL Final: " + DB_URL); // Debug

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            isInitialized = true;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error driver MySQL", e);
        }
    }
    
    public static Connection getConnection() throws SQLException {
        if (!isInitialized) initialize();
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}