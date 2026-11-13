/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hr.algebra.dal;

import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author daniel.bele
 */
public final class RepositoryFactory {

    private static final String PATH = "/config/repository.properties";
    private static final String CLASS_NAME = "CLASS_NAME";

    private static Repository repository;

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream is = RepositoryFactory.class.getResourceAsStream(PATH)) {

            PROPERTIES.load(is);
            repository = (Repository) Class
                    .forName(PROPERTIES.getProperty(CLASS_NAME))
                    .getDeclaredConstructor()
                    .newInstance();

        } catch (Exception ex) {
            System.getLogger(RepositoryFactory.class.getName())
                    .log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    public static Repository getRepository() { // null
        return repository;
    }

    private RepositoryFactory() {
    }

}
