package snippets;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.FactoryConfigurationError;
import java.io.File;
import java.io.IOException;

import org.openspice.jspice.alert.Alert;

public class ReadDocumentElement {
		public static final Element readDocumentRoot( final File inventory_conf ) {
		try {
			try {
				final DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
				final DocumentBuilder b = f.newDocumentBuilder();
				final Document d = b.parse( inventory_conf );
				return d.getDocumentElement();
			} catch ( final ParserConfigurationException e ) {
				throw new RuntimeException( e );
			} catch ( final FactoryConfigurationError factoryConfigurationError ) {
				throw new RuntimeException( factoryConfigurationError );
			} catch ( final SAXException e ) {
				throw new RuntimeException( e );
			} catch ( final IOException e ) {
				throw new RuntimeException( e );
			}
		} catch ( RuntimeException e ) {
			throw new Alert( e.getCause(), "Cannot read inventory configuration file" ).culprit( "file", inventory_conf ).mishap();
		}
	}

}
