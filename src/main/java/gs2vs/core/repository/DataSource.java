//package gs2vs.core.repository;
//
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//
//import java.sql.Connection;
//import java.sql.SQLException;
//
//public class DataSource {
//
//    private static HikariConfig config = new HikariConfig();
//    private static HikariDataSource ds;
//
//    static {
//        config.setJdbcUrl( "jdbc:postgresql://localhost/d2emo" );
//        config.setUsername( "lorance" );
////        config.setPassword( "" );
//        config.setConnectionTimeout(3000);
//        config.setConnectionInitSql("select 1");
//        config.setInitializationFailTimeout( 3000 );
//        config.addDataSourceProperty( "cachePrepStmts" , "true" );
//        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
//        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
//        ds = new HikariDataSource( config );
//    }
//
//    private DataSource() {}
//
//    public static Connection getConnection() throws SQLException {
//        return ds.getConnection();
//    }
//}
