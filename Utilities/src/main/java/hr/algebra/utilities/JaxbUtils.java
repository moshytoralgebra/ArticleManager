package hr.algebra.utilities;

import hr.algebra.model.ArticleList;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import java.io.File;

public final class JaxbUtils {

    private JaxbUtils() {}

    public static void exportToXml(ArticleList articleList, File outputFile) throws Exception {
        JAXBContext context = JAXBContext.newInstance(ArticleList.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(articleList, outputFile);
    }
}
